package org.bolay.camp.verticle

import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient.create

import io.vertx.ext.web.client.WebClientSession
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.web.client.WebClientOptions
import io.vertx.kotlin.ext.web.client.sendAwait
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ChurchtoolsVT : CoroutineVerticle() {
    var options = WebClientOptions(defaultHost = "cz-rostock.church.tools", defaultPort = 443, ssl = true, logActivity = true)
    val client = create(Vertx.vertx(), options)
    val session = WebClientSession.create(client)

    override suspend fun start() {
        super.start()

        println("Churchtools verticle startet!")


        vertx.eventBus().consumer<String>("getGroups") { message ->
            println("received getGroups Message")
            GlobalScope.launch(vertx.dispatcher()) {
                val getGroups = handleGetGroups()
                val token = login("***", "***")
                message.reply(getGroups)
            }
        }

    }

    suspend fun handleGetGroups(): String {

        var response = client.get("/api/groups")
                .putHeader("content-type", "application/json")
                .addQueryParam("login_token", "***")
                .addQueryParam("limit", "100")
                .sendAwait()
        println(response.body().toString())
        return response.body().toString()
    }

    suspend fun login(username: String, password: String): String {
        var responseLogin = session.post("api/login")
                .putHeader("content-type", "application/json")
                .addQueryParam("username", username)
                .addQueryParam("password", password)
                .sendAwait()
        println(responseLogin)

//        var responseToken = session.get("api/persons/2/logintoken")
//                .putHeader("content-type", "application/json")
//                .sendAwait()
//        println(responseToken)
        return ""
    }
}