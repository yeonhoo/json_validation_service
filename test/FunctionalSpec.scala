import java.io.File

import akka.stream.Materializer
import controllers.HomeController
import org.apache.commons.io.IOUtils
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsArray, JsString}
import play.api.test.Helpers._
import play.api.test.FakeRequest
import org.scalatest.concurrent.ScalaFutures

class FunctionalSpec extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures {


  def read(path: String) = IOUtils.toString(getClass.getResource(path), "UTF-8")

  implicit lazy val materializer: Materializer = app.injector.instanceOf[Materializer]
  val homeController: HomeController = app.injector.instanceOf(classOf[HomeController])

  "upload action" should {
    "return 201 [Created] and success message" when {
      "schema is valid" in {

        val validSchema =  read("/schema/valid-schema.json")

        val id = "valid-schema"
        val request = FakeRequest(POST, s"/schema/${id}").withBody(validSchema).withHeaders(CONTENT_TYPE -> "application/json")
        val result = route(app, request).get

        //val result = homeController.upload(id)(request)

        val jsonResponse = homeController.jsonResponse("uploadSchema", id, "success")

        status(result) mustEqual CREATED
        contentAsJson(result) mustBe jsonResponse
      }
    }
    "return 400 [Bad Request] and error message" when {
      "schema is invalid" in {

        val invalidSchema = read("/schema/invalid-schema.json")
        val id = "invalid-schema"
        val request = FakeRequest(POST, s"/schema/${id}").withBody(invalidSchema).withHeaders(CONTENT_TYPE -> "application/json")
        val result = route(app, request).get

        //val result = homeController.upload(id)(request)

        val jsonResponse = homeController.jsonResponse("uploadSchema", id, "error", JsString("Invalid JSON"))

        status(result) mustEqual BAD_REQUEST
        contentAsJson(result) mustBe jsonResponse
      }
    }
  }
  //TODO : the schema(file) should already be in /tmp directory, otherwise test will fail
  "validate action" should {
    "return 200 [Ok] and success message" when {
      "json document is successfully validated" in {


        val validJson = read("/config/valid-config.json")
        val id = "valid-schema"
        val request = FakeRequest(POST, s"/validate/${id}").withBody(validJson).withHeaders(CONTENT_TYPE -> "application/json")
        val result = route(app, request).get

        val jsonResponse = homeController.jsonResponse("validateDocument", id, "success")

        status(result) mustEqual OK
        contentAsJson(result) mustBe jsonResponse

      }
    }

    "return 400 [Bad Request] and error message" when {
      "json document is not a valid json" in {

        val validJson = read("/config/invalid-json-format.json")
        val id = "valid-schema"
        val request = FakeRequest(POST, s"/validate/${id}").withBody(validJson).withHeaders(CONTENT_TYPE -> "application/json")
        val result = route(app, request).get

        val jsonResponse = homeController.jsonResponse("validateDocument", id, "error", JsString("Invalid JSON"))

        status(result) mustEqual BAD_REQUEST
        contentAsJson(result) mustBe jsonResponse

      }

      "json document is invalid against the schema" in {

        val invalidAgainstSchema = read("/config/invalid-against-schema.json")
        val id = "valid-schema"
        val request = FakeRequest(POST, s"/validate/${id}").withBody(invalidAgainstSchema).withHeaders(CONTENT_TYPE -> "application/json")
        val result = route(app, request).get

        val jsonResponse = homeController.jsonResponse("validateDocument", id, "error", JsArray(Seq(JsString("Property destination missing."))))

        status(result) mustEqual BAD_REQUEST
        contentAsJson(result) mustBe jsonResponse

      }

      "no schema with id was found" in {

        val validJson = read("/config/valid-config.json")
        val id = "not-exist"
        val request = FakeRequest(POST, s"/validate/${id}").withBody(validJson).withHeaders(CONTENT_TYPE -> "application/json")
        val result = route(app, request).get

        val jsonResponse = homeController.jsonResponse("validateDocument", id, "error", JsString(s"Schema id : ${id} not found"))

        status(result) mustEqual NOT_FOUND
        contentAsJson(result) mustBe jsonResponse
      }
    }
  }

  "download action" should {
    "return 200 [Ok] and serve the schema" when {
      "schema is found and served its content successfully to client" in {

        val id = "valid-schema"
        val request = FakeRequest(GET, s"/schema/${id}").withHeaders(CONTENT_TYPE -> "application/json")
        val result = route(app, request).get

        status(result) mustEqual OK
        //contentAsJson(result) mustBe jsonResponse
      }
    }

    "return 400 [Bad Request] and error message" when {
      "schema is not found" in {

        val id = "not-exist"
        val request = FakeRequest(GET, s"/schema/${id}").withHeaders(CONTENT_TYPE -> "application/json")
        val result = route(app, request).get

        val jsonResponse = homeController.jsonResponse("downloadSchema", id, "error", JsString(s"Schema id : ${id} not found"))

        status(result) mustEqual NOT_FOUND
        contentAsJson(result) mustBe jsonResponse
      }
    }
  }


}
