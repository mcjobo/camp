package org.bolay.camp.authentication

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.User
import io.vertx.ext.auth.ldap.LdapAuthentication
import io.vertx.ext.auth.ldap.LdapAuthenticationOptions
import io.vertx.ext.auth.ldap.impl.LdapAuthenticationImpl

class ChurchToolsLdapAuthentication(vertx : Vertx, authenticationOptions : LdapAuthenticationOptions) : LdapAuthenticationImpl(vertx,authenticationOptions) {



}