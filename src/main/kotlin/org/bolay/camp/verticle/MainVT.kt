package org.bolay.camp.verticle

import io.vertx.core.Vertx
import io.vertx.kotlin.core.DeploymentOptions

fun main() {
    var vertx = Vertx.vertx()
    vertx.deployVerticle(ChurchtoolsVT())
    vertx.deployVerticle(RestVerticle())
    vertx.deployVerticle(ITextWorkerVerticle(), DeploymentOptions(worker = true))
}