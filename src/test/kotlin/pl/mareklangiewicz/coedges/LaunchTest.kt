package pl.mareklangiewicz.coedges

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import pl.mareklangiewicz.uspek.USpekRunner
import pl.mareklangiewicz.uspek.eq
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
                "started" o { done has "started" }
                "only started" o { done hasNot "ended" }
                delay(480)
                "only started after 490ms" o { done hasNot "ended" }
                delay(10)
                "ended after 500ms" o { done has "ended" }
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
                "not started yet" o { done.size eq 0 }
                delay(1)
                "started first iteration" o { done has "iteration 1" }
                "did not start second iteration" o { done hasNot "iteration 2" }
                delay(190)
                "started 4 iterations" o {
                    done has "iteration 4"
                    done hasNot "iteration 5"
                }
                job.cancel()
                delay(300)
                "did not start fifth iteration" o { done hasNot "iteration 5" }
            }
        }

        "On withTimeout" o {
            runBlocking {
                val done = mutableSetOf<String>()
                try {
                    withTimeout(250) {
                        repeat(10) {
                            done += "iteration ${it + 1}"
                            delay(50)
                        }
                    }
                    fail("Not cancelled")
                }
                catch (e: CancellationException) { done.size eq 5 }
            }
        }
    }
}
