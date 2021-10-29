package com.sd.rest.client

import akka.actor.typed.{ActorSystem, Extension, ExtensionId}
import com.typesafe.config.Config

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

final class ApiConfiguration(config: Config) extends Extension {
  import ApiConfiguration._

  private[client] val scheme: String = config.getString(SchemeProperty)
  private[client] val host: String = config.getString(HostProperty)
  private[client] val port: Int = config.getInt(PortProperty)
  private[client] val retrySettings: RetrySettings =
    new RetrySettings(
      config.getInt(RetrySettingsMaxRetriesProperty),
      FiniteDuration(config.getDuration(RetrySettingsMinBackoffProperty).toMillis, TimeUnit.MILLISECONDS),
      FiniteDuration(config.getDuration(RetrySettingsMaxBackoffProperty).toMillis, TimeUnit.MILLISECONDS),
      config.getDouble(RetrySettingsRandomFactorProperty))
}

object ApiConfiguration extends ExtensionId[ApiConfiguration] {
  private[client] final val SchemeProperty = "api.scheme"
  private[client] final val HostProperty = "api.host"
  private[client] final val PortProperty = "api.port"
  private[client] final val RetrySettingsMaxRetriesProperty = "api.retry-settings.max-retries"
  private[client] final val RetrySettingsMinBackoffProperty = "api.retry-settings.min-backoff"
  private[client] final val RetrySettingsMaxBackoffProperty = "api.retry-settings.max-backoff"
  private[client] final val RetrySettingsRandomFactorProperty = "api.retry-settings.random-factor"

  final class RetrySettings(private[client] val maxRetries: Int,
                            private[client] val minBackoff: FiniteDuration,
                            private[client] val maxBackoff: FiniteDuration,
                            private[client] val randomFactor: Double)

  final override def createExtension(actorSystem: ActorSystem[_]): ApiConfiguration =
    new ApiConfiguration(actorSystem.settings.config)
}
