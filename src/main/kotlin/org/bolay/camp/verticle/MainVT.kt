package org.bolay.camp.verticle

import io.vertx.core.Vertx

fun main() {
    var vertx = Vertx.vertx()
    vertx.deployVerticle(ChurchtoolsVT())
    vertx.deployVerticle(RestVerticle())
}