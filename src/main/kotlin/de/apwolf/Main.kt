package de.apwolf

import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.ext.cluster.infinispan.InfinispanClusterManager
import org.slf4j.LoggerFactory

const val useClusteredSessionStore = true

fun main() {

    val logger = LoggerFactory.getLogger("de.apwolf.Main.kt")

    val clusterManager = InfinispanClusterManager()

    Vertx.clusteredVertx(VertxOptions().setClusterManager(clusterManager)).onSuccess { vertx ->
        vertx.exceptionHandler { e ->
            logger.error(e.message, e)
        }
        vertx.deployVerticle(HttpVerticle(useClusteredSessionStore))
        logger.info("Started HTTP Verticle")
    }.onFailure {
        logger.error("Failed to start HTTP Verticle", it)
    }
}