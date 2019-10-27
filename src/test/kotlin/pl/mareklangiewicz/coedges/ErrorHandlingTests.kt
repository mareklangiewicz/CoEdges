package pl.mareklangiewicz.coedges

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.GlobalScope
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

        "On GlobalScope.async with exception handler" o { runBlocking {
            val job = GlobalScope.async(Unconfined + handler) {
                done += "async started".tee
                throw RuntimeException("some error".tee)
            }
            job.join() // not necessary because we use Unconfined (but notice it does not rethrow RuntimeException)
            // also notice that this "job" is actually a Deferred<Nothing>

            "async was started" o { done has "async started" }
            "async job is cancelled" o { job.isCancelled eq true }
            "exception was NOT handled" o { done hasNot { "handled" in it } }
        } }

        "On GlobalScope.launch with exception handler" o { runBlocking {
            val job = GlobalScope.launch(Unconfined + handler) {
                done += "launch started".tee
                throw RuntimeException("some error".tee)
            }
            job.join() // not necessary because we use Unconfined (but notice it does not rethrow RuntimeException)

            "launch was started" o { done has "launch started" }
            "launch job is cancelled" o { job.isCancelled eq true }
            "exception was handled" o { done has { Regex("handled.*RuntimeException.*some error") in it } }
        } }

    }
}
