package com.sd.rest.client

import akka.NotUsed
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.{Flow, Source}

trait ApiClient {
  def test(request: String)
          (flow: Flow[HttpRequest, HttpResponse, NotUsed]): Source[String, NotUsed]
}
