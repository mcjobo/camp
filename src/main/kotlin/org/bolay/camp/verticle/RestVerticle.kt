package org.bolay.camp.verticle

import io.vertx.core.Future
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.User
import io.vertx.ext.auth.ldap.LdapAuthentication
import io.vertx.ext.auth.ldap.LdapAuthenticationOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.sstore.LocalSessionStore
import io.vertx.kotlin.core.eventbus.requestAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class RestVerticle : CoroutineVerticle() {


    override suspend fun start() {
        println("RestVerticle verticle startet!")

        var server = vertx.createHttpServer()
        val router = Router.router(vertx)
        val staticHandler = StaticHandler.create("webroot/dist")
        val bodyHandler = BodyHandler.create()
        val sessionStore = LocalSessionStore.create(vertx)
        val sessionHandler = SessionHandler.create(sessionStore)
        val ldapOptions = LdapAuthenticationOptions()
                .setUrl("ldap://files.cz-rostock.de:1389")
                .setAuthenticationQuery("cn={0},ou=users,o=churchtools")
        val ldapAuthenticator = LdapAuthentication.create(vertx, ldapOptions)


        router.route().handler(sessionHandler)
        router.route("/").handler { routingContext ->

            val session = routingContext.session()
            val user = session.get<User>("user")
            val path = routingContext.normalizedPath()
            if (session.get<User>("user") == null) {
                routingContext.response().setStatusCode(303).putHeader("Location", "/login?redirect=" + path).end("/login?redirect=" + path)
            }
        }








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
                routingContext.redirect(redirectUrl)
            }
        }
        router.route("/api/v1/createPersonTable").handler { routingContext ->
            GlobalScope.launch(vertx.dispatcher()) {
                val personTable = createPersonTable()
                routingContext.response().putHeader("content-type", "application/json").end(personTable)
            }
        }
        router.route(HttpMethod.POST, "/api/v1/postSigninToGroup").handler(bodyHandler)
        router.route(HttpMethod.POST, "/api/v1/postSigninToGroup").handler { routingContext ->
            GlobalScope.launch(vertx.dispatcher()) {
                val signinToGroup = handlePostSigninToGroup(routingContext.getBodyAsString())
                routingContext.response().putHeader("content-type", "application/json").end(signinToGroup)
            }
        }
        router.route(HttpMethod.POST, "/api/v1/postLogin").handler(bodyHandler)
        router.route(HttpMethod.POST, "/api/v1/postLogin").handler { routingContext ->
            val message = routingContext.getBodyAsString()
            val userJson = JsonObject(message)
//            userJson.put("username", "cn="+userJson.getString("username")+",ou=users,o=churchtools")
            val user = User.fromName(userJson.getString("username"))
            routingContext.session().put("user", user)
            println(message)

            ldapAuthenticator.authenticate(userJson).onComplete { ar ->
                if (ar.succeeded()) {
                    routingContext.response().setStatusCode(200).end()
                } else {
                    routingContext.response().setStatusCode(403).end()
                }
            }


        }

        router.route("/*").handler(staticHandler)

        router.route(HttpMethod.GET, "/*").handler(staticHandler).failureHandler { event: RoutingContext ->  // This serves up the React app
            event.response().sendFile("webroot/dist/index.html")
        }

        router.get().handler { ctx ->
            ctx.response().sendFile("webroot/dist/index.html")
            ctx.next()
        }

        server.requestHandler(router).listen(8080)

//        handlePostSigninToGroup("""{
//
//                "firstName": "Deleteme2",
//                "lastName": "User",
//                "team": "Fischadler",
//                "comment": "das ist die allertollste von allen",
//                "tShirt": "kein T-Shirt"
//
//              }
//        """.trimIndent())
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

    suspend fun createPersonTable(): String {
        var reply = vertx.eventBus().requestAwait<String>("org.bolay.camp.createPersonTable", "")
        return reply.body()

    }

    suspend fun handlePostSigninToGroup(pData: String): String {
        var reply = vertx.eventBus().requestAwait<String>("org.bolay.camp.postSigninToGroup", pData)
        return reply.body()
    }

}