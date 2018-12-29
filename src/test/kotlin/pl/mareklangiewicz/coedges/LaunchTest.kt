package pl.mareklangiewicz.coedges

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import pl.mareklangiewicz.uspek.USpekRunner
import pl.mareklangiewicz.uspek.o
import pl.mareklangiewicz.uspek.uspek

@RunWith(USpekRunner::class)
class LaunchTest {

    @Test
    fun uspek() = uspek {
        "On launch" o {
            runBlocking {
                launch {
                    delay(500)
                    println("after 500ms")
                }
            }
        }
    }
}
