package com.sd.rest.client

import akka.NotUsed
import akka.actor.testkit.typed.scaladsl.{ActorTestKit, ActorTestKitBase}
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter.TypedActorSystemOps
import akka.http.scaladsl.marshalling.{Marshal, Marshaller}
import akka.http.scaladsl.model._
import akka.stream.scaladsl.{Flow, Source}
import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.FixtureAnyWordSpec
import org.scalatest.{Outcome, ParallelTestExecution}

import java.util.UUID
import scala.concurrent.Future

final class AkkaMediaSparkApiClientSpec
  extends FixtureAnyWordSpec
    with Matchers
    with ParallelTestExecution {
  type FixtureParam = TestFixture

  class TestFixture(val actorTestKit: ActorTestKit)

  override def withFixture(test: OneArgTest): Outcome = {
    val actorTestKit = ActorTestKit(s"${ActorTestKitBase.testNameFromCallStack()}-${UUID.randomUUID().toString}")

    try {
      withFixture(test.toNoArgTest(new TestFixture(actorTestKit)))
    } finally {
      actorTestKit.shutdownTestKit()
    }
  }

  "AkkaApiClient" when {
    "calling test endpoint" must {
      "succeed" when {
        "request retries are not exhausted" in { testFixture =>
          import testFixture.actorTestKit.internalSystem.executionContext
          import testFixture.actorTestKit.system

          val configuration = system.extension(ApiConfiguration)
          val apiClient = new AkkaApiClient(configuration)

          transientErrorSuccess(
            "response",
            apiClient.test("request"),
            request => request.method == HttpMethods.POST &&
              request.uri == Uri.from(scheme = configuration.scheme, host = configuration.host, port = configuration.port)
                .withPath(Uri.Path(AkkaApiClient.TestFragment)) &&
              request.entity == Marshal("request").to[RequestEntity].map(_.withContentType(ContentTypes.`application/json`)).futureValue
          )
        }
      }
    }
  }

  private[this] def transientErrorSuccess[T](outputResponse: T, clientCallback: Flow[HttpRequest, HttpResponse, NotUsed] => Source[T, NotUsed], check: HttpRequest => Boolean)
                                            (implicit actorSystem: ActorSystem[Nothing], marshaller: Marshaller[T, ResponseEntity]): Unit = {
    import actorSystem.executionContext

    val failingResponses = AkkaApiClient.TransientErrors
      .map(code => Future.successful(HttpResponse(code)))
    val successfulResponse = Marshal(outputResponse).to[ResponseEntity]
      .map(e => HttpResponse(status = StatusCodes.OK, entity = e))
    val responseIterator = (failingResponses :+ successfulResponse).iterator

    val flow = Flow[HttpRequest]
      .filter { request =>
        check(request)
      }
      .mapAsyncUnordered(1) { _ =>
        responseIterator.next()
      }

    clientCallback(flow)
      .runWith(TestSink.probe(actorSystem.toClassic))
      .requestNext(outputResponse)
      .expectComplete()
  }
}
