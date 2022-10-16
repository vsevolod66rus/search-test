package common.endpoints

import common.types.SearchTypes.SearchResponse
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe._

object EndpointOps {

  val baseApiEndpoint: PublicEndpoint[Unit, EndpointError, Unit, Any] =
    endpoint.errorOut(errorOutput)

  val searchEndpoint: PublicEndpoint[List[String], EndpointError, SearchResponse, Any] =
    baseApiEndpoint.in("search").in(query[List[String]]("tag")).out(jsonBody[SearchResponse])

  private lazy val errorOutput = oneOf[EndpointError](
    oneOfVariantValueMatcher(
      StatusCode.NotFound,
      jsonBody[EndpointError]
        .description("Not found")
        .example(EndpointError.notFound("Not found"))
    ) { case EndpointError(StatusCode.NotFound, _, _) => true },
    oneOfVariantValueMatcher(
      StatusCode.TooManyRequests,
      jsonBody[EndpointError]
        .description("Too Many Requests")
        .example(EndpointError.tooManyRequests("Too Many Requests"))
    ) { case EndpointError(StatusCode.TooManyRequests, _, _) => true },
    oneOfVariantValueMatcher(
      StatusCode.RequestTimeout,
      jsonBody[EndpointError]
        .description("Request Timeout")
        .example(EndpointError.requestTimeout("Request Timeout"))
    ) { case EndpointError(StatusCode.RequestTimeout, _, _) => true },
    oneOfVariantValueMatcher(
      StatusCode.InternalServerError,
      jsonBody[EndpointError]
        .description("Internal server error")
        .example(EndpointError.internalServerError("Internal server error"))
    ) { case _ => true }
  )
}
