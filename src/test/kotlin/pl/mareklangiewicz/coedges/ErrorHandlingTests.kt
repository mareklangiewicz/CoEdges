package pl.mareklangiewicz.coedges

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import pl.mareklangiewicz.uspek.USpekRunner
import pl.mareklangiewicz.uspek.eq
import pl.mareklangiewicz.uspek.o
import pl.mareklangiewicz.uspek.uspek

@Suppress("EXPERIMENTAL_API_USAGE")
@RunWith(USpekRunner::class)
class ErrorHandlingTests {

    @Test
    fun uspek() = uspek {
        val done = mutableSetOf<String>()
        val handler = CoroutineExceptionHandler { _, ex -> done += "handled $ex".tee }


        "On GlobalScope.launch fail with exception handler" o { runBlocking {
            val job = GlobalScope.launch(Unconfined + handler) {
                done += "launch started".tee
                throw RuntimeException("some error".tee)
            }
            job.join() // not necessary because we use Unconfined (but notice it does not rethrow RuntimeException)

            "launch was started" o { done has "launch started" }
            "launch job is cancelled" o { job.isCancelled eq true }
            "exception was handled" o { done has { Regex("handled.*RuntimeException.*some error") in it } }
        } }

        "On GlobalScope.async fail with exception handler" o { runBlocking {
            val job = GlobalScope.async(Unconfined + handler) {
                done += "async started".tee
                throw RuntimeException("some error".tee)
                @Suppress("UNREACHABLE_CODE")
                666
            }
            job.join() // not necessary because we use Unconfined (but notice it does not rethrow RuntimeException)

            "async was started" o { done has "async started" }
            "async job is cancelled" o { job.isCancelled eq true }
            "exception was NOT handled" o { done hasNot { "handled" in it } }

            "On try job.await" o {
                try {
                    @Suppress("BlockingMethodInNonBlockingContext")
                    runBlocking { job.await() }
                }
                catch (e: RuntimeException) {
                    done += "catched $e".tee
                }
                "exception was thrown" o { done has { Regex("catched.*RuntimeException.*some error") in it } }
            }
        } }

        "On nested launch fail in GlobalScope" o { runBlocking {
            var jobInner: Job? = null
            val jobOuter = GlobalScope.launch(Unconfined + handler) {
                done += "outer launch started".tee
                jobInner = launch {
                    done += "inner launch started".tee
                    throw RuntimeException("some error".tee)
                }
            }
            jobOuter.join() // not necessary because we use Unconfined (but notice it does not rethrow RuntimeException)
            "outer launch was started" o { done has "outer launch started" }
            "inner launch was started" o { done has "inner launch started" }
            "outer launch job is cancelled" o { jobOuter.isCancelled eq true }
            "inner launch job is cancelled" o { jobInner!!.isCancelled eq true }
            "exception was handled" o { done has { Regex("handled.*RuntimeException.*some error") in it } }
        } }
    }
}