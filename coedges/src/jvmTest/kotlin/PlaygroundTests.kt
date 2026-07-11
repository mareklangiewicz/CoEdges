@file:Suppress("EXPERIMENTAL_API_USAGE")

package pl.mareklangiewicz.coedges

import kotlinx.coroutines.*
import org.junit.jupiter.api.*
import pl.mareklangiewicz.upue.*
import pl.mareklangiewicz.uspek.*

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
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
