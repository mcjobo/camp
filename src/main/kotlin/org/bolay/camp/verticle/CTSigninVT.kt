package org.bolay.camp.verticle

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.web.client.sendAwait
import io.vertx.kotlin.ext.web.client.sendJsonObjectAwait
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CTSigninVT : ChurchtoolsVT() {

    override suspend fun start() {
        super.start()
        println("CTSigninVT verticle startet!")

        vertx.eventBus().consumer<String>("org.bolay.camp.postSigninToGroup") { message ->
            println("received postSigninToGroup Message")
            GlobalScope.launch(vertx.dispatcher()) {
                val pData = message.body()
                val postSigninToGroup = handlePostSigninToGroup(login(), pData)


                message.reply(postSigninToGroup.encodePrettily())
            }
        }
    }

    private suspend fun handlePostSigninToGroup(pToken: String, pData: String): JsonObject {
        val signupJson = JsonObject(pData)
        val response = session.post("/index.php?q=churchdb/ajax")
                .putHeader("content-type", "application/x-www-form-urlencoded")
                .putHeader("Accept", "application/json")
                .putHeader("csrf-token", csrfToken)
                .addQueryParam("func", "getPersonByName")
                .addQueryParam("searchpattern", signupJson.getString("firstName") + " " + signupJson.getString("lastName"))
                .sendAwait()
        var personJson = response.bodyAsJsonObject().getJsonObject("data").getJsonObject("data")
        var person = JsonObject()
        if(personJson != null && personJson.map.size > 0) {
            val temp = personJson.map.iterator().next()
            person = personJson.getJsonObject(temp.key)
        } else {
            var newPerson = JsonObject()
            newPerson.put("campusId", 0)
            newPerson.put("statusId", 0)
            newPerson.put("departmentIds", JsonArray().add(4))
            newPerson.put("firstName", signupJson.getString("firstName"))
            newPerson.put("lastName", signupJson.getString("lastName"))
            if(signupJson.containsKey("street")) newPerson.put("street", signupJson.getString("street", ""))
            if(signupJson.containsKey("zip")) newPerson.put("zip", signupJson.getString("zip", ""))
            if(signupJson.containsKey("city")) newPerson.put("city", signupJson.getString("city", ""))
            if(signupJson.containsKey("birthday")) newPerson.put("birthday", signupJson.getString("birthday", ""))
            if(signupJson.containsKey("email")) newPerson.put("email", signupJson.getString("email", ""))
            if(signupJson.containsKey("mobil")) newPerson.put("mobil", signupJson.getString("mobil", ""))

            val response = session.post("/api/persons")
                    .putHeader("content-type", "application/json")
                    .addQueryParam("login_token", pToken)
                    .sendJsonObjectAwait(newPerson)
            person = response.bodyAsJsonObject()
        }


        val response2 = session.post("/index.php?q=churchdb/ajax")
                .putHeader("content-type", "application/x-www-form-urlencoded")
                .putHeader("Accept", "application/json")
                .putHeader("csrf-token", csrfToken)
                .addQueryParam("func", "addPersonGroupRelation")
                .addQueryParam("groupmemberstatus_id", "8")
                .addQueryParam("date", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .addQueryParam("comment", signupJson.getString("comment"))
                .addQueryParam("custom42", signupJson.getString("team"))
                .addQueryParam("custom48", "")
                .addQueryParam("custom51", "")
                .addQueryParam("custom54", "")
                .addQueryParam("custom57", "")
                .addQueryParam("custom60", "")
                .addQueryParam("custom63", "")
                .addQueryParam("custom42", signupJson.getString("tShirt"))
                .addQueryParam("custom42", "0")
                .addQueryParam("custom42", "0")
                .addQueryParam("ignoreGroupSize", "true")
                .addQueryParam("id", person.getString("id"))
                .addQueryParam("g_id", "264")
                .sendAwait()


        return response2.bodyAsJsonObject()
    }

}