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
    private var options = WebClientOptions(defaultHost = "cz-rostock.church.tools", defaultPort = 443, ssl = true, logActivity = true)
    private val client = create(Vertx.vertx(), options)
    private val session = WebClientSession.create(client)
    private val login = System.getProperty("ct.login")
    private val passwd = System.getProperty("ct.passwd")
    private val campGroupId = 233

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

    private suspend fun handleGetGroups(pToken: String): JsonObject {

        val response = client.get("/api/groups")
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

    private suspend fun login(username: String, password: String): String {
        val responseLogin = session.post("/api/login")
                .putHeader("content-type", "application/json")
                .addQueryParam("username", username)
                .addQueryParam("password", password)
                .sendAwait()
        val res = responseLogin.body().toJsonObject()
        println(responseLogin.body().toJson())
        val personId = res.getJsonObject("data").getInteger("personId")
        val uri = "/api/persons/" + personId + "/logintoken"

        val responseToken = session.get(uri)
                .putHeader("content-type", "application/json")
                .sendAwait()
        println(responseToken)
        return responseToken.body().toJsonObject().getString("data")
    }

    private suspend fun cdbGetMasterData(): JsonObject {
        val response = session.post("/index.php?q=churchdb/ajax")
                .putHeader("content-type", "application/x-www-form-urlencoded")
                .putHeader("Accept", "application/json")
                .addQueryParam("func", "getMasterData")
                .sendAwait()
        println(response.body().toString())
        return response.body().toJsonObject()
    }

    private suspend fun getUsersOfGroup(pToken: String, pGroupId: Int): JsonObject {
        val response = session.get("/api/groups/" + pGroupId + "/members")
                .putHeader("content-type", "application/json")
                .addQueryParam("login_token", pToken)
                .addQueryParam("limit", "100")
                .sendAwait()
        println(response.body().toString())
        return response.body().toJsonObject()
    }

    private suspend fun getAdditionalGroupValues(pGroupId: Int): JsonObject {
        val response = session.post("/index.php?q=churchdb/ajax")
                .putHeader("content-type", "application/x-www-form-urlencoded")
                .putHeader("Accept", "application/json")
                .addQueryParam("func", "getAdditionalGroupFields")
                .addQueryParam("g_id", pGroupId.toString())
                .sendAwait()
        println(response.body().toString())
        return response.body().toJsonObject()
    }

    suspend fun getAllPersondata(): JsonObject {
        val response = session.post("/index.php?q=churchdb/ajax")
                .putHeader("content-type", "application/x-www-form-urlencoded")
                .putHeader("Accept", "application/json")
                .addQueryParam("func", "getAllPersonData")
                .sendAwait()
        println(response.body().toString())
        return response.body().toJsonObject()
    }

    private suspend fun collectDataPersonList(pGroupId: Int): JsonObject {
        val masterData = cdbGetMasterData()
        val groups = masterData.getJsonObject("data").getJsonObject("groups")
        val campGroup = groups.getJsonObject(pGroupId.toString())
        val childGroupIds = campGroup.getJsonObject("childs")


        val childGroups = JsonArray()
        childGroupIds.forEach {
            val group = enrichGroup(it.key.toInt(), groups)
            childGroups.add(group)
        }
        campGroup.put("childGroups", childGroups)
        return campGroup
    }


    private suspend fun enrichGroup(pGroupId: Int, pGroups: JsonObject): JsonObject {
        val group = pGroups.getJsonObject(pGroupId.toString())
        val allUsers = getAllPersondata().getJsonObject("data")
        val usersJsonObj = JsonObject()
        allUsers.forEach {
            it.value as JsonObject
        }
        val filtered = allUsers.filter { (key, value) ->
            (value as JsonObject).getJsonObject("groupmembers", JsonObject()).containsKey(group.getString("id"))
        }
        filtered.forEach {
            (it.value as JsonObject).put("groupmember", (it.value as JsonObject).getJsonObject("groupmembers").getJsonObject(group.getString("id")))
            usersJsonObj.put(it.key, it.value as JsonObject)
        }

        group.put("users", filtered)
        var groupValues = getAdditionalGroupValues(pGroupId).getValue("data")
        if (groupValues !is JsonObject) {
            groupValues = JsonObject()
        }
        group.put("additionalGroupValues", groupValues)
        processAdditionalGroupValues(usersJsonObj, groupValues)
        return group
    }

    private fun processAdditionalGroupValues(pUsers: JsonObject, pGroupValues: JsonObject) {
        pGroupValues.forEach {
            processSingleAdditionalGroupValue(pUsers, it.value as JsonObject)
        }
    }

    private fun processSingleAdditionalGroupValue(pUsers: JsonObject, pGroupValue: JsonObject) {
        var userValues = pGroupValue.getValue("data")
        if (userValues !is JsonObject) {
            userValues = JsonObject()
        }
        userValues.forEach {
            val grpValue = pGroupValue.copy()
            grpValue.put("value", (it.value as JsonObject).getString("value"))
            var userGrpValues = pUsers.getJsonObject(it.key).getJsonObject("groupValues")
            if (userGrpValues !is JsonObject) {
                userGrpValues = JsonObject()
                pUsers.getJsonObject(it.key).put("groupValues", userGrpValues)
            }
            userGrpValues.put(grpValue.getString("fieldname"), grpValue)
        }
    }
}
