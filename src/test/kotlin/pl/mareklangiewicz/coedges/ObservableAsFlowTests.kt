package pl.mareklangiewicz.coedges

import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.disposables.Disposables
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.rx2.asFlow
import org.junit.jupiter.api.TestFactory
import pl.mareklangiewicz.smokkx.smokkx
import pl.mareklangiewicz.uspek.eq
import pl.mareklangiewicz.uspek.o
import pl.mareklangiewicz.uspek.uspekTestFactory
import kotlin.RuntimeException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


@Suppress("EXPERIMENTAL_API_USAGE")
class ObservableAsFlowTests {

    @TestFactory
    fun factory() = uspekTestFactory {

        "On fake ObservableSource" o {
            val observers = mutableListOf<Observer<in String>>()
            val source = ObservableSource<String> { observers += it }

            "On source asFlow" o {
                val flow = source.asFlow()

                "no observers yet" o { observers.size eq 0 }

                "On collect flow" o {
                    val emit = smokkx<String, Unit>(autoCancel = true)
                    val job = GlobalScope.async(Dispatchers.Unconfined) { flow.collect(emit::invoke) }

                    "source.subscribe was called and we have an observer" o { observers.size eq 1 }

                    "On onSubscribe" o {
                        val disposable = Disposables.empty()
                        observers[0].onSubscribe(disposable)

                        "upstream is not disposed yet" o { disposable.isDisposed eq false }
                        "no emitting yet" o { emit.invocations.size eq 0 }

                        "On onComplete" o {
                            observers[0].onComplete()

                            "upstream is disposed" o { disposable.isDisposed eq true }
                            "collection is completed" o { job.isCompleted eq true }
                            "collection is not cancelled" o { job.isCancelled eq false }
                        }

                        "On onError" o {
                            val ex = RuntimeException("upstream error")
                            observers[0].onError(ex)

                            "upstream is disposed" o { disposable.isDisposed eq true }
                            "collection completes with exception" o { job.getCompletionExceptionOrNull()?.cause eq ex }
                        }

                        "On onNext" o {
                            observers[0].onNext("some item")

                            "item is emitted" o { emit.invocations has "some item" }
                        }

                        "On cancel collection" o {
                            job.cancel()

                            "upstream is disposed" o { disposable.isDisposed eq true }
                        }
                    }
                    "On cancel collection before any onSubscribe" o {
                        job.cancel()

                        "job is cancelled" o { job.isCancelled eq true }

                        "On late onSubscribe" o {
                            val disposable = Disposables.empty()
                            observers[0].onSubscribe(disposable)

                            "upstream is disposed immediately" o { disposable.isDisposed eq true }
                        }
                    }
                }
            }
        }

        "On Subject source" o {
            val source = PublishSubject.create<String>()

            "On source asFlow" o {
                val flow = source.asFlow()

                "On collect flow" o {
                    val emit = smokkx<String, Unit>(autoCancel = true)
                    val job = GlobalScope.async(Dispatchers.Unconfined) { flow.collect(emit::invoke) }

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
