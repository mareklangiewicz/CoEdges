package pl.mareklangiewicz.coedges

import io.reactivex.processors.AsyncProcessor
import io.reactivex.processors.PublishProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import pl.mareklangiewicz.uspek.USpekRunner
import pl.mareklangiewicz.uspek.eq
import pl.mareklangiewicz.uspek.o
import pl.mareklangiewicz.uspek.uspek

@RunWith(USpekRunner::class)
class AsFlowTests {

    @Test
    fun uspek() = uspek {

        val done = mutableListOf<String>()

        "On Publisher source" o {
            val source = PublishProcessor.create<Int>()

            "On source asFlow" o {
                val flow = source.asFlow()

                "On collect flow" o {

                    val job = GlobalScope.launch(Dispatchers.Unconfined) {
                        flow.collect {

                        }
                    }

                    "source has subscriber" o { source.hasSubscribers() eq true }

                    "On cancel flow collection" o {
                        job.cancel()

                        "source is unsubscribed" o { source.hasSubscribers() eq false }
                    }
                }
            }
        }
    }
}
