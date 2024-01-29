package my.app.imdbtop

import org.springframework.boot.fromApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.with

@TestConfiguration(proxyBeanMethods = false)
class TestImdbTopApplication

fun main(args: Array<String>) {
	fromApplication<ImdbTopApplication>().with(TestImdbTopApplication::class).run(*args)
}
