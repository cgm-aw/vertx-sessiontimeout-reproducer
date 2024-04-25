package de.apwolf

import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.ext.cluster.infinispan.InfinispanClusterManager
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("de.apwolf#main")

    val clusterManager = InfinispanClusterManager()

    Vertx.clusteredVertx(VertxOptions().setClusterManager(clusterManager)).onSuccess { vertx ->
        vertx.exceptionHandler {
           e -> logger.error(e.message, e)
        }
        vertx.deployVerticle(HttpVerticle())
        logger.info("Started HTTP Verticle")
    }.onFailure {
        logger.error("Failed to start HTTP Verticle", it)
    }
}