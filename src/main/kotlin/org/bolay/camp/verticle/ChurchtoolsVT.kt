package org.bolay.camp.verticle

import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
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
    val login = System.getProperty("ct.login")
    val passwd = System.getProperty("ct.passwd")
    val campGroupId = 233

    override suspend fun start() {
        super.start()


        println("Churchtools verticle startet!")


        vertx.eventBus().consumer<String>("getGroups") { message ->
            println("received getGroups Message")
            GlobalScope.launch(vertx.dispatcher()) {
                val getGroups = handleGetGroups()
                val token = login()
                message.reply(getGroups)
            }
        }

        vertx.eventBus().consumer<String>("org.bolay.camp.cdbGetMasterData") { message ->
            println("received getMasterData Message")
            GlobalScope.launch(vertx.dispatcher()) {
                login()
                val cdbMasterData = cdbGetMasterData()
                message.reply(cdbMasterData)
            }
        }
        vertx.eventBus().consumer<String>("org.bolay.camp.cdbGetAdditionalGroupValues") { message ->
            println("received getUsersrOfGroup Message")
            GlobalScope.launch(vertx.dispatcher()) {
                login()
                val groupValues = getAdditionalGroupValues()
                message.reply(groupValues)
            }
        }
        vertx.eventBus().consumer<String>("org.bolay.camp.cdbGetUsersOfGroup") { message ->
            println("received getUsersrOfGroup Message")
            GlobalScope.launch(vertx.dispatcher()) {

                val usersOfGroup = getUsersOfGroup(login(), 129)
                message.reply(usersOfGroup)
            }
        }
        vertx.eventBus().consumer<String>("org.bolay.camp.getPersonTableData") { message ->
            println("received getPersonTableData Message")
            GlobalScope.launch(vertx.dispatcher()) {
                login()
                val personTabledata = collectDataPersonList(campGroupId)
                message.reply(personTabledata.toString())
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

    suspend fun login(): String {
        return login(login, passwd)
    }

    suspend fun login(username: String, password: String): String {
        var responseLogin = session.post("/api/login")
                .putHeader("content-type", "application/json")
                .addQueryParam("username", username)
                .addQueryParam("password", password)
                .sendAwait()
        var res = responseLogin.body().toJsonObject()
        println(responseLogin.body().toJson())
        var personId = res.getJsonObject("data").getInteger("personId")
        var uri = "/api/persons/" + personId + "/logintoken"

        var responseToken = session.get(uri)
                .putHeader("content-type", "application/json")
                .sendAwait()
        println(responseToken)
        return responseToken.body().toJsonObject().getString("data")
    }

    suspend fun cdbGetMasterData(): JsonObject {
        var response = session.post("/index.php?q=churchdb/ajax")
                .putHeader("content-type", "application/x-www-form-urlencoded")
                .putHeader("Accept", "application/json")
                .addQueryParam("func", "getMasterData")
                .sendAwait()
        println(response.body().toString())
        return response.body().toJsonObject()
    }

    suspend fun getUsersOfGroup(pToken: String, pGroupId: Int): JsonObject {
        var response = client.get("/api/groups/" + pGroupId + "/members")
                .putHeader("content-type", "application/json")
                .addQueryParam("login_token", pToken)
                .addQueryParam("limit", "100")
                .sendAwait()
        println(response.body().toString())
        return response.body().toJsonObject()
    }

    suspend fun getAdditionalGroupValues(): String {
        var response = session.post("/index.php?q=churchdb/ajax")
                .putHeader("content-type", "application/x-www-form-urlencoded")
                .putHeader("Accept", "application/json")
                .addQueryParam("func", "getAdditionalGroupFields")
                .sendAwait()
        println(response.body().toString())
        return response.body().toString()
    }

    suspend fun collectDataPersonList(pGroupId: Int): JsonObject {
        var masterData = cdbGetMasterData()
        var groups = masterData.getJsonObject("data").getJsonObject("groups")
        var campGroup = groups.getJsonObject(pGroupId.toString())
        var childGroupIds = campGroup.getJsonObject("childs")

        var childGroups = JsonArray()
        childGroupIds.forEach {
            val group = groups.getJsonObject(it.key)
            var users = getUsersOfGroup(login(), it.key.toInt())
            childGroups.add(group)
        }
        campGroup.put("childGoups", childGroups)


        var i = 0
        ++i

        return campGroup
    }
}