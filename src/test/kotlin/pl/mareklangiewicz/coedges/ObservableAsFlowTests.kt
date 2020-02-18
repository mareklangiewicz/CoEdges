package pl.mareklangiewicz.coedges

import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.DisposableHelper
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import org.junit.jupiter.api.TestFactory
import pl.mareklangiewicz.smokkx.smokkx
import pl.mareklangiewicz.uspek.eq
import pl.mareklangiewicz.uspek.o
import pl.mareklangiewicz.uspek.uspekTestFactory
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


// My Observable.asFlow() experimental implementation copy (PR to kotlinx.coroutines)
@ExperimentalCoroutinesApi
public fun <T: Any> ObservableSource<T>.asFlow(): Flow<T> = callbackFlow {

    val disposableRef = AtomicReference<Disposable>()

    val observer = object : Observer<T> {
        override fun onComplete() { close() }
        override fun onSubscribe(d: Disposable) { DisposableHelper.setOnce(disposableRef, d) }
        override fun onNext(t: T) { sendBlocking(t) }
        override fun onError(e: Throwable) { close(e) }
    }

    subscribe(observer)
    awaitClose { DisposableHelper.dispose(disposableRef) }
}

@Suppress("EXPERIMENTAL_API_USAGE")
class ObservableAsFlowTests {

    @TestFactory
    fun factory() = uspekTestFactory {

        "On Subject source" o {
            val source = PublishSubject.create<String>()

            "On source asFlow" o {
                val flow = source.asFlow()

                "On collect flow" o {

                    val emit = smokkx<String, Unit>(autoCancel = true)
                    val job = GlobalScope.async(Dispatchers.Unconfined) {
                        flow.collect(emit::invoke)
                    }

                    "source has observer" o { source.hasObservers() eq true }
                    "no emitting yet" o { emit.invocations.size eq 0 }

                    "On first source item" o {
                        source.onNext("item 1")

                        "first item is being emitted" o { emit.invocations has "item 1" }

                        "On second source item during first emission" o {
                            source.onNext("item 2")

                            "no new emit invocations yet" o { emit.invocations.size eq 1 }

                            "On first emit resume" o {
                                emit.resume(Unit)

                                "emit buffered item" o { emit.invocations eq listOf("item 1", "item 2") }

                                "On second emit resume" o {
                                    emit.resume(Unit)

                                    "no more emissions" o { emit.invocations.size eq 2 }
                                }
                            }
                        }

                        "On first emit resume" o {
                            emit.resume(Unit)

                            "no more emissions" o { emit.invocations.size eq 1 }

                            "On cancel flow after first emit" o {
                                job.cancel()

                                "no emit is cancelled" o { emit.cancellations eq 0 }
                                "source is unsubscribed" o { source.hasObservers() eq false }
                            }
                        }

                        "On first emit exception" o {
                            val ex = RuntimeException("first emit failed")
                            emit.resumeWithException(ex)

                            "collection completes with exception" o { job.getCompletionExceptionOrNull()?.cause eq ex }
                            "source is unsubscribed" o { source.hasObservers() eq false }
                        }

                        "On cancel flow during first emit" o {
                            job.cancel()

                            "emit is cancelled" o { emit.cancellations eq 1 }
                            "source is unsubscribed" o { source.hasObservers() eq false }
                        }
                    }

                    "On source onError before any onNext" o {
                        val ex = RuntimeException("source error")
                        source.onError(ex)

                        "collection completes with exception" o { job.getCompletionExceptionOrNull()?.cause eq ex }
                    }


                    "On source onComplete before any onNext" o {
                        source.onComplete()

                        "collection is completed" o { job.isCompleted eq true }
                        "collection is not cancelled" o { job.isCancelled eq false }
                    }

                    "On cancel flow collection" o {
                        job.cancel()

                        "source is unsubscribed" o { source.hasObservers() eq false }
                    }
                }
            }
        }
    }
}
