@file:Suppress("EXPERIMENTAL_API_USAGE")

package pl.mareklangiewicz.coedges

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.TestFactory
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
}
