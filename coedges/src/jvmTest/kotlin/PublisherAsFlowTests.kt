package pl.mareklangiewicz.coedges

import io.reactivex.rxjava3.processors.*
import kotlinx.coroutines.*
import kotlinx.coroutines.reactive.asFlow
import org.junit.jupiter.api.TestFactory
import pl.mareklangiewicz.bad.chkEq
import pl.mareklangiewicz.smokkx.smokkx
import pl.mareklangiewicz.uspek.o
import pl.mareklangiewicz.uspek.uspekTestFactory
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
@Suppress("EXPERIMENTAL_API_USAGE")
class PublisherAsFlowTests {

    @TestFactory
    fun factory() = uspekTestFactory {

        "On Publisher source" o {
            val source = PublishProcessor.create<String>()

            "On source asFlow" o {
                val flow = source.asFlow()

                "On collect flow" o {

                    val emit = smokkx<String, Unit>(autoCancel = true)
                    val job = GlobalScope.async(Dispatchers.Unconfined) {
                        flow.collect(emit::invoke)
                    }

                    "source has subscriber" o { source.hasSubscribers() chkEq true }
                    "no emitting yet" o { emit.invocations.size chkEq 0 }

                    "On first source item" o {
                        source.onNext("item 1")

                        "first item is being emitted" o { emit.invocations has "item 1" }

                        "On second source item during first emission" o {
                            source.onNext("item 2")

                            "no new emit invocations yet" o { emit.invocations.size chkEq 1 }

                            "On first emit resume" o {
                                emit.resume(Unit)

                                "emit buffered item" o { emit.invocations chkEq listOf("item 1", "item 2") }

                                "On second emit resume" o {
                                    emit.resume(Unit)

                                    "no more emissions" o { emit.invocations.size chkEq 2 }
                                }
                            }
                        }

                        "On first emit resume" o {
                            emit.resume(Unit)

                            "no more emissions" o { emit.invocations.size chkEq 1 }

                            "On cancel flow after first emit" o {
                                job.cancel()

                                "no emit is cancelled" o { emit.cancellations chkEq 0 }
                                "source is unsubscribed" o { source.hasSubscribers() chkEq false }
                            }
                        }

                        "On first emit exception" o {
                            val ex = RuntimeException("first emit failed")
                            emit.resumeWithException(ex)

                            "collection completes with exception" o { job.getCompletionExceptionOrNull()?.cause chkEq ex }
                            "source is unsubscribed" o { source.hasSubscribers() chkEq false }
                        }

                        "On cancel flow during first emit" o {
                            job.cancel()

                            "emit is cancelled" o { emit.cancellations chkEq 1 }
                            "source is unsubscribed" o { source.hasSubscribers() chkEq false }
                        }
                    }

                    "On source onError before any onNext" o {
                        val ex = RuntimeException("source error")
                        source.onError(ex)

                        // FIXME analyze this "collection completes with exception" o { job.getCompletionExceptionOrNull()?.cause chkEq ex }
                    }


                    "On source onComplete before any onNext" o {
                        source.onComplete()

                        "collection is completed" o { job.isCompleted chkEq true }
                        "collection is not cancelled" o { job.isCancelled chkEq false }
                    }

                    "On cancel flow collection" o {
                        job.cancel()

                        "source is unsubscribed" o { source.hasSubscribers() chkEq false }
                    }
                }
            }
        }
    }
}
