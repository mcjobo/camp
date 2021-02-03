package org.bolay.camp.verticle

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject


fun main() {
    var vertx = Vertx.vertx()
    vertx.deployVerticle(ChurchtoolsVT())
    vertx.deployVerticle(CTSigninVT())
    vertx.deployVerticle(RestVerticle())
    vertx.deployVerticle(ITextWorkerVerticle(),   DeploymentOptions(JsonObject("""{"worker": true}""")   ))
}