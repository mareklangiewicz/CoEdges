package pl.mareklangiewicz.coedges

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.TestFactory
import pl.mareklangiewicz.uspek.eq
import pl.mareklangiewicz.uspek.o
import pl.mareklangiewicz.uspek.uspekTestFactory

@Suppress("EXPERIMENTAL_API_USAGE")
class ErrorHandlingTests {

    @TestFactory
    fun uspek() = uspekTestFactory {
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
            val job: Deferred<Unit> = GlobalScope.async(Unconfined + handler) {
                done += "async started".tee
                throw RuntimeException("some error".tee)
            }
            job.join() // not necessary because we use Unconfined (but notice it does not rethrow RuntimeException)

            "async was started" o { done has "async started" }
            "async job is cancelled" o { job.isCancelled eq true }
            "exception was NOT handled" o { done hasNot { "handled" in it } }

            try { job.await() }
            catch (e: RuntimeException) { done += "catched $e".tee }
            "await rethrows" o { done has { Regex("catched.*RuntimeException.*some error") in it } }
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

        // Tests below show that changing inner launch to async doesn't change error propagation!!
        "On nested async fail in GlobalScope" o { runBlocking {
            var jobInner: Job? = null
            val jobOuter = GlobalScope.launch(Unconfined + handler) {
                done += "outer launch started".tee
                jobInner = async {
                    done += "inner async started".tee
                    throw RuntimeException("some error".tee)
                }
            }
            jobOuter.join() // not necessary because we use Unconfined (but notice it does not rethrow RuntimeException)
            "outer launch was started" o { done has "outer launch started" }
            "inner async was started" o { done has "inner async started" }
            "outer launch job is cancelled" o { jobOuter.isCancelled eq true }
            "inner async job is cancelled" o { jobInner!!.isCancelled eq true }
            "exception was handled" o { done has { Regex("handled.*RuntimeException.*some error") in it } }
        } }
    }
}
