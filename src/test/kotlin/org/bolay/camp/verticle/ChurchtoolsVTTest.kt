package org.bolay.camp.verticle

import io.vertx.core.Vertx
import io.vertx.ext.unit.TestSuite
import io.vertx.ext.unit.junit.VertxUnitRunner
import io.vertx.junit5.VertxExtension
import org.jetbrains.annotations.TestOnly
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.extension.ExtendWith

@DisplayName("Vertx Tests")
@ExtendWith(VertxExtension::class)
class ChurchtoolsVTTest  {

    @BeforeEach
    fun  beforeAll(){
        var vertx = Vertx.vertx()
        vertx.deployVerticle(ChurchtoolsVT())

    }

    @Test
    fun test1(){
        val eventBus1 = Vertx.vertx().eventBus()
        val res = eventBus1.publish("getGroups", "tolle Message")

        assert(value = false)
    }



}