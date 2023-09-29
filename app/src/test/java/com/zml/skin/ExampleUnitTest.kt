package com.zml.skin

import kotlinx.coroutines.*
import org.junit.Test

import org.junit.Assert.*
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.math.E

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)


        CoroutineScope(EmptyCoroutineContext).launch {

            println("Hello======")
        }

        runBlocking {
            launch {
                delay(1000L)
                println("World!")
            }
            println("Hello")
        }


//        val result = testFunc(object : Function2<Int,Int,String> {
//            override fun invoke(p1: Int, p2: Int): String {
//                return "a+b=${p1+p2}\n"
//            }
//        })

        //print(result)

    }

    fun testFunc(func:Function2<Int,Int,String>):String{

        return func.invoke(1,1)

    }
}