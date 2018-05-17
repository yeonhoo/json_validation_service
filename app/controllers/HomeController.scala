package controllers

import javax.inject._
import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets

import com.eclipsesource.schema.{FailureExtensions, JsonSource, SchemaType, SchemaValidator}
import play.api.libs.json._
import play.api.mvc._

import scala.util.{Failure, Success, Try}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  private def withoutNull(json: JsValue): JsValue = json match {

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

  def jsonResponse(action: String, id: String, status: String, message: JsValue = JsNull) = {

    val json: JsValue = Json.obj(
      "action" -> action,
      "id" -> id,
      "status" -> status,
      "message" -> message
    )
    withoutNull(json)
  }


  def upload(id:String) = Action(parse.tolerantText) { request =>

    Try(Json.parse(request.body)) match {
      case Success(_) =>
        Try(Files.write(Files.createTempFile(id, ".json"), request.body.getBytes(StandardCharsets.UTF_8))) match {
          case Success(_) =>
            Created(jsonResponse("uploadSchema", id, "success"))
          case Failure(_) =>
            InternalServerError(jsonResponse("uploadSchema", id, "error", JsString("Error while writing the file")))
        }
      case Failure(_) =>
        BadRequest(jsonResponse("uploadSchema", id, "error", JsString("Invalid JSON")))
    }
  }

  def validate(id: String) = Action(parse.tolerantText) { request =>

    JsonSource.fromString(request.body) match {
      // it is valid json document
      case Success(jsValue) =>
        // schema with "id" exists
        Try(Files.readAllBytes(Paths.get("/tmp/" + id + ".json"))) match {
          // file successfully read
          case Success(file) =>
            val jsonSchema = Json.parse(new String(file, StandardCharsets.UTF_8))
            val schemaType = Json.fromJson[SchemaType](jsonSchema).get
            // clean null keys
            val jsonWithoutNull = withoutNull(jsValue)
            // validate
            val validationResult = SchemaValidator().validate(schemaType)(jsonWithoutNull)
            // result fold
            validationResult.fold(
              { error =>
                  BadRequest(jsonResponse("validateDocument", id, "error", error.toJson.\\("msgs")(0))) },

              { success =>
                  Ok(jsonResponse("validateDocument", id, "success")) }
            )
          // schema not found
          case Failure(ex) =>
            NotFound(jsonResponse("validateDocument", id, "error", JsString(s"Schema id : ${id} not found")))
        }
      // invalid json document
      case Failure(ex) =>
        BadRequest(jsonResponse("validateDocument", id, "error", JsString("Invalid JSON")))
    }
  }

  def download(id: String) = Action { request =>

    Try(Files.readAllBytes(Paths.get("/tmp/" + id + ".json"))) match {
      case Success(file) =>
        Ok(new String(file, StandardCharsets.UTF_8))
      case Failure(ex) =>
        //Logger.info("error : " + ex.getClass)
        NotFound(jsonResponse("downloadSchema", id, "error", JsString(s"Schema id : ${id} not found")))
    }
  }
}
