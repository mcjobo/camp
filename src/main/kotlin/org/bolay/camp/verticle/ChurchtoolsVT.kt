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
                val getGroups = handleGetGroups(login())
                message.reply(getGroups.toString())
            }
        }

        vertx.eventBus().consumer<String>("org.bolay.camp.cdbGetMasterData") { message ->
            println("received getMasterData Message")
            GlobalScope.launch(vertx.dispatcher()) {
                login()
                val cdbMasterData = cdbGetMasterData()
                message.reply(cdbMasterData.toString())
            }
        }
        vertx.eventBus().consumer<String>("org.bolay.camp.cdbGetAdditionalGroupValues") { message ->
            println("received getUsersrOfGroup Message")
            GlobalScope.launch(vertx.dispatcher()) {
                login()
                val groupValues = getAdditionalGroupValues(129)
                message.reply(groupValues.toString())
            }
        }
        vertx.eventBus().consumer<String>("org.bolay.camp.cdbGetUsersOfGroup") { message ->
            println("received getUsersrOfGroup Message")
            GlobalScope.launch(vertx.dispatcher()) {

                val usersOfGroup = getUsersOfGroup(login(), 129)
                message.reply(usersOfGroup.toString())
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

    suspend fun handleGetGroups(pToken: String): JsonObject {

        var response = client.get("/api/groups")
                .putHeader("content-type", "application/json")
                .addQueryParam("login_token", pToken)
                .addQueryParam("limit", "100")
                .sendAwait()
        println(response.body().toString())
        return response.body().toJsonObject()
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
        var response = session.get("/api/groups/" + pGroupId + "/members")
                .putHeader("content-type", "application/json")
                .addQueryParam("login_token", pToken)
                .addQueryParam("limit", "100")
                .sendAwait()
        println(response.body().toString())
        return response.body().toJsonObject()
    }

    suspend fun getAdditionalGroupValues(pGroupId: Int): JsonObject {
        var response = session.post("/index.php?q=churchdb/ajax")
                .putHeader("content-type", "application/x-www-form-urlencoded")
                .putHeader("Accept", "application/json")
                .addQueryParam("func", "getAdditionalGroupFields")
                .addQueryParam("g_id", pGroupId.toString())
                .sendAwait()
        println(response.body().toString())
        return response.body().toJsonObject()
    }

    suspend fun collectDataPersonList(pGroupId: Int): JsonObject {
        var masterData = cdbGetMasterData()
        var groups = masterData.getJsonObject("data").getJsonObject("groups")
        var campGroup = groups.getJsonObject(pGroupId.toString())
        var childGroupIds = campGroup.getJsonObject("childs")


        var childGroups = JsonArray()
        childGroupIds.forEach {
            var group = enrichGroup(it.key.toInt(), groups)
            childGroups.add(group)
        }
        campGroup.put("childGoups", childGroups)


        var i = 0
        ++i

        return campGroup
    }

    suspend fun enrichGroup(pGroupId: Int, pGroups: JsonObject): Any {
        val group = pGroups.getJsonObject(pGroupId.toString())
        var users = getUsersOfGroup(login(), pGroupId)
        group.put("users", users.getValue("data"))
        var groupValues = getAdditionalGroupValues(pGroupId).getJsonObject("data")
        group.put("additionalGroupValues", groupValues)
        processAdditionalGroupValues(users, groupValues)
        return group
    }

    suspend fun processAdditionalGroupValues(pUsers: JsonObject, pGroupValues: JsonObject) {
        pGroupValues.forEach { processSingleAdditionalGroupValue(pUsers, it.value as JsonObject) }
    }

    suspend fun processSingleAdditionalGroupValue(pUsers: JsonObject, pGroupValue: JsonObject) {
        var userValues = pGroupValue.getJsonArray("data")
        userValues.forEach {
            var user = pUsers.getJsonObject(it.toString())
        }
        var i = 0
        ++i
    }
}
