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
        val done = mutableSetOf<String>()
        "On launch" o {
            runBlocking {
                launch {
                    done += "started"
                    delay(500)
                    done += "ended"
                }
                delay(10)
                "started" o { assert("started" in done) }
                "only started" o { assert(done.size == 1) }
                delay(480)
                "only started after 490ms" o { assert(done.size == 1) }
                delay(10)
                "ended after 500ms" o { assert("ended" in done) }
            }
        }
    }
}
