import com.eclipsesource.schema.{JsonSource, SchemaType, SchemaValidator}
import com.fasterxml.jackson.core.JsonParseException
import org.apache.commons.io.IOUtils
import org.scalatestplus.play.PlaySpec
import play.api.libs.json._
import scala.util.{Success, Try}

class ExamplesSpec extends PlaySpec {
  import ExamplesSpec._

  "Json Parser" should {
    "clean all null keys" in {

      val Success(json) = JsonSource.fromString(read("/config/valid-with-null3.json"))
      val result = withoutNull(json).toString()

      result must not include ("null")
    }

    "parse a valid json" in {
      val result = JsonSource.fromString(read("/config/valid-config.json"))
      result.isSuccess mustBe true
    }

    "parse a valid json with null value" in {
      val result = JsonSource.fromString(read("/config/valid-with-null.json"))
      result.isSuccess mustBe true
    }

    "fail to parse an invalid json" in {
      val result = JsonSource.fromString(read("/config/invalid-json-format.json"))
      result.isFailure mustBe true
    }

    "throw an exception with invalid Schema" in {
      val result = Try(Json.parse(invalid_schema_file))

      an [JsonParseException] should be thrownBy result.get
    }
  }

  "Validator" should {
    "validate valid config" in {
      val result = validateExample(valid_schema, "/config/valid-config.json")
      result.isSuccess mustBe true
    }
    "fail to validate valid config with null key" in {
      val result = validateExample(valid_schema, "/config/valid-with-null.json")
      result.isSuccess mustBe false // because its not clean yet
    }
    "fail to validate valid config with null key 2" in {
      val result = validateExample(valid_schema, "/config/valid-with-null3.json")
      result.isSuccess mustBe false // because its not clean yet
    }
    "fail to validate with json document against the schema" in {
      val result = validateExample(valid_schema, "/config/invalid-against-schema.json")
      result.isSuccess mustBe false
    }
    "validate the valid schema with itself" in {
      val result = validateExample(valid_schema, "/schema/valid-schema.json")
      result.isSuccess mustBe false
    }

  }

}

object ExamplesSpec extends PlaySpec {

  lazy val valid_schema_file = read("/schema/valid-schema.json")
  lazy val invalid_schema_file = read("/schema/invalid-schema.json")
  lazy val valid_schema = Json.fromJson[SchemaType](Json.parse(valid_schema_file)).get
  lazy val invalid_schema = Json.fromJson[SchemaType](Json.parse(invalid_schema_file)).get

  def read(path: String) = IOUtils.toString(getClass.getResource(path), "UTF-8")

  def validateExample(schema: SchemaType, url: String): JsResult[JsValue] = {

    val instance = JsonSource.fromUrl(getClass.getResource(url)).get
    val result = SchemaValidator().validate(schema)(instance)

    result
  }

  def withoutNull(json: JsValue): JsValue = json match {
    case JsObject(fields) =>

      JsObject(fields.flatMap {

        case (_, JsNull)  => None
        case objType @ (name, JsObject(_))  => Some(name, withoutNull(objType._2))
        case arrType @ (name, JsArray(_)) => Some(name, withoutNull(arrType._2))
        case other @ (_, _) => Some(other)
      })

    case JsArray(arr) => JsArray( arr.flatMap(el => Some(withoutNull(el))) )

    case other => other

  }

}