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
                val done = mutableSetOf<String>()
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

        "On cancellation" o {
            runBlocking {
                val done = mutableSetOf<String>()
                val job = launch {
                    repeat(10) {
                        done += "iteration ${it+1}"
                        delay(50)
                    }
                }
                "not started yet" o { assert(done.isEmpty()) }
                delay(1)
                "started first iteration" o { assert("iteration 1" in done) }
                "did not start second iteration" o { assert("iteration 2" !in done) }
                delay(190)
                "started 4 iterations" o {
                    assert("iteration 4" in done)
                    assert("iteration 5" !in done)
                }
                job.cancel()
                delay(300)
                "did not start fifth iteration" o {
                    assert("iteration 5" !in done)
                }
            }
        }
    }
}
