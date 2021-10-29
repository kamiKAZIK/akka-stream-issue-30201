package com.sd.rest.client

import akka.http.scaladsl.model.StatusCode

final class ApiHttpStatusException(val status: StatusCode,
                                   private val message: String = "",
                                   private val cause: Throwable = None.orNull) extends Exception(s"API Client: $message", cause)
