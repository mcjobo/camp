package org.bolay.camp.verticle

import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
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

        router.route("/").handler { routingContext ->
            routingContext.response().putHeader("content-type", "application/json").end(rootHandler())
        }
        router.route("/static/*").handler(StaticHandler.create())
        // Bind "/" to our hello message - so we are still compatible.
        router.route("/listen/camp.pdf").handler { routingContext ->
            routingContext.response()
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .putHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment")
                    .sendFile("webroot/listen/camp.pdf")
        }
        router.route("/api/v1/getGroups").handler { routingContext ->
            GlobalScope.launch(vertx.dispatcher()) {
                var groups = getGroups()
                routingContext.response().putHeader("content-type", "application/json").end(groups)
            }
        }
        router.route("/api/v1/getMasterData").handler { routingContext ->
            GlobalScope.launch(vertx.dispatcher()) {
                val cdbMasterData = cdbGetMasterData()
                routingContext.response().putHeader("content-type", "application/json").end(cdbMasterData)
            }
        }
        router.route("/api/v1/createPdfTable").handler { routingContext ->
            GlobalScope.launch(vertx.dispatcher()) {
                val redirectUrl = createPdfTable()
                routingContext.response().setStatusCode(303).putHeader("Location", redirectUrl).end(redirectUrl)
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

    suspend fun cdbGetMasterData(): String {
        var reply = vertx.eventBus().requestAwait<String>("org.bolay.camp.cdbGetMasterData", "")
        return reply.body()
    }

    suspend fun createPdfTable(): String {
        var reply = vertx.eventBus().requestAwait<String>("org.bolay.camp.createPdfTable", "")
        return reply.body()
    }
}