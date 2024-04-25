package de.apwolf

import io.vertx.core.AbstractVerticle
import io.vertx.ext.bridge.PermittedOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import io.vertx.ext.web.sstore.ClusteredSessionStore
import io.vertx.ext.web.sstore.LocalSessionStore
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class HttpVerticle(private val useClusteredSessionStore: Boolean) : AbstractVerticle() {

    companion object {

        val LOGGER: Logger = LoggerFactory.getLogger(HttpVerticle::class.java.name)

    }

    override fun start() {
        val sessionStore = if (useClusteredSessionStore) {
            ClusteredSessionStore.create(vertx)
        } else {
            LocalSessionStore.create(vertx)
        }
        val sessionHandler = SessionHandler.create(sessionStore).setSessionTimeout(1_000 * 60 * 2)

        val router = Router.router(vertx)
        router.route().handler(sessionHandler)

        val sockJsHandler = SockJSHandler.create(vertx)
        val sockJsOptions = SockJSBridgeOptions()
            .addInboundPermitted(PermittedOptions().setAddressRegex("*"))
            .addOutboundPermitted(PermittedOptions().setAddressRegex("*"))

        router.route("/test").handler {
            LOGGER.info("Received REST request on /test with session ${it.session().id()}")
            it.response().end("ok")
        }

        router.route("/sockjs/*").subRouter(sockJsHandler.bridge(sockJsOptions) { bridgeEvent ->
            LOGGER.info(
                "Received WS request ${bridgeEvent.type()} for session ${
                    bridgeEvent.socket().webSession().id()
                }"
            )
            bridgeEvent.complete(true)
        })

        vertx.createHttpServer().requestHandler(router).listen(8086)
            .onSuccess { LOGGER.info("Listening on ${it.actualPort()}") }.onFailure {

                LOGGER.error("Failed to start web server", it)
            }

    }
}