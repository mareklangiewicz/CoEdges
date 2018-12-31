package pl.mareklangiewicz.coedges

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import pl.mareklangiewicz.uspek.USpekRunner
import pl.mareklangiewicz.uspek.o
import pl.mareklangiewicz.uspek.uspek

@RunWith(USpekRunner::class)
class PlaygroundTest {

    @Test
    fun sample1() = uspek {

        "On different dispatchers" o {
            runBlocking {
                launch { // context of the parent, main runBlocking coroutine
                    println("main runBlocking      : I'm working in thread ${Thread.currentThread().name}")
                }
                launch(Dispatchers.Unconfined) { // not confined -- will work with main thread
                    println("Unconfined            : I'm working in thread ${Thread.currentThread().name}")
                }
                launch(Dispatchers.Default) { // will get dispatched to DefaultDispatcher
                    println("Default               : I'm working in thread ${Thread.currentThread().name}")
                }
                launch(newSingleThreadContext("MyOwnThread")) { // will get its own new thread
                    println("newSingleThreadContext: I'm working in thread ${Thread.currentThread().name}")
                }
            }
        }
    }
}
