package com.sd.rest.client

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Flow, RetryFlow, Source}

import scala.collection.immutable
import scala.concurrent.Future
import scala.util.{Failure, Success}

final class AkkaApiClient(configuration: ApiConfiguration)
                         (implicit actorSystem: ActorSystem[Nothing]) extends ApiClient {
  import AkkaApiClient._
  import actorSystem.executionContext

  override def test(request: String)
                   (flow: Flow[HttpRequest, HttpResponse, NotUsed]): Source[String, NotUsed] =
    Source
      .future(Marshal(request).to[RequestEntity])
      .map(entity => buildRequest(
        method = HttpMethods.POST,
        path = Uri.Path(TestFragment),
        entity = entity.withContentType(ContentTypes.`application/json`)
      ))
      .via(RetryFlow.withBackoff(configuration.retrySettings.minBackoff, configuration.retrySettings.maxBackoff, configuration.retrySettings.randomFactor, configuration.retrySettings.maxRetries, flow) {
        case (request, HttpResponse(status, _, entity, _)) if TransientErrors.contains(status)  =>
          discardedEntity(entity)
          Some(request)
        case _ =>
          None
      })
      .mapAsyncUnordered(1) {
        case HttpResponse(status, _, entity, _) if status.isSuccess() =>
          Unmarshal(entity).to[String]
        case HttpResponse(status, _, entity, _) =>
          discardedEntity(entity)
          Future.failed(new ApiHttpStatusException(status, "Failed response!"))
      }
      .log("api")

  private[this] def buildRequest(method: HttpMethod = HttpMethods.GET,
                                 path: Uri.Path = Uri.Path.Empty,
                                 entity: RequestEntity = HttpEntity.Empty): HttpRequest =
    HttpRequest(
      method = method,
      uri = Uri
        .from(
          scheme = configuration.scheme,
          host = configuration.host,
          port = configuration.port)
        .withPath(path),
      entity = entity)

  private[this] def discardedEntity(entity: ResponseEntity): Unit =
    entity.discardBytes()
      .future()
      .onComplete {
        case Failure(exception) =>
          actorSystem.log.error("Failed to discard response entity!", exception)
        case Success(_) =>
          actorSystem.log.debug("Successfully discarded response entity!")
      }
}

object AkkaApiClient {
  private[client] final val TransientErrors = immutable.Seq(
    StatusCodes.BadGateway,
    StatusCodes.ServiceUnavailable,
    StatusCodes.GatewayTimeout
  )

  private[client] final val TestFragment = "/test/test"
}
