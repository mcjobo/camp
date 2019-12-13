package org.bolay.camp.verticle

import io.vertx.ext.web.Router
import io.vertx.kotlin.core.eventbus.requestAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RestVerticle : CoroutineVerticle() {


    override suspend fun start() {
        println("RestVerticle verticle startet!")

        var server = vertx.createHttpServer()
        val router = Router.router(vertx)

        // Bind "/" to our hello message - so we are still compatible.
        router.route("/").handler { routingContext ->
            routingContext.response().putHeader("content-type", "text/html").end(rootHandler())
        }
        router.route("/api/v1/getGroups").handler { routingContext ->
            GlobalScope.launch(vertx.dispatcher()) {
                var groups = getGroups()
                routingContext.response().putHeader("content-type", "text/html").end(groups)

            }
        }

        server.requestHandler(router).listen(8080)
    }


    fun rootHandler(): String {
        return "<h1>Hello from my first Vert.x 3 application</h1>"
    }

    suspend fun getGroups(): String {

        var reply = vertx.eventBus().requestAwait<String>("getGroups", "hallo")
        return reply.body()

    }
}