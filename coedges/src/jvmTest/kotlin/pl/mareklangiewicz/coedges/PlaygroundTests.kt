@file:Suppress("EXPERIMENTAL_API_USAGE")

package pl.mareklangiewicz.coedges

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.TestFactory
import pl.mareklangiewicz.upue.asNPullee
import pl.mareklangiewicz.upue.iterator
import pl.mareklangiewicz.upue.vnmap
import pl.mareklangiewicz.upue.vnzip
import pl.mareklangiewicz.uspek.o
import pl.mareklangiewicz.uspek.uspekTestFactory

class PlaygroundTests {

    @TestFactory
    fun sample1() = uspekTestFactory {

        "On different dispatchers" o {
            runBlocking {
                launch { // context of the parent, main runBlocking coroutine
                    "main runBlocking".tee
                }
                launch(Dispatchers.Unconfined) { // not confined -- will work with main thread
                    "Unconfined".tee
                }
                launch(Dispatchers.Default) { // will get dispatched to DefaultDispatcher
                    "Default".tee
                }
                launch(newSingleThreadContext("MyOwnThread")) { // will get its own new thread
                    "newSingleThreadContext".tee
                }
            }
        }
    }

    @TestFactory
    fun samplePue() = uspekTestFactory {
        "On some npullee" o {

            val npullee = (1..10).asNPullee()
                .vnmap { it * 2 }
                .vnzip((1..10).asNPullee())

            for (i in npullee)
                println(i)
        }
    }
}
