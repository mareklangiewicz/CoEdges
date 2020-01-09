@file:Suppress("EXPERIMENTAL_API_USAGE")

import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import pl.mareklangiewicz.coedges.tee
import pl.mareklangiewicz.coedges.unit

suspend fun channelUsage() = coroutineScope {
    val ch = produce {
        "sending started".tee
        send(1)
        "sending ended".tee
    }
    "after produce".tee
    delay(500)
    "after delay".tee
    ch.receive()
    "after receive".tee.unit
}

suspend fun typicalConcurrency() = coroutineScope {

    val job1 = launch {
        "background operation start".tee
        for (i in 1..5) {
            delay(500)
            "background operation progress $i/5".tee
        }
        "background operation end".tee
    }

    val job2 = launch {
        "waiting start".tee
        for (i in 1..3) { // usually we await for some user action like "Cancel" instead of this for loop
            delay(600)
            "waiting $i/3".tee
        }
        "waiting end".tee
    }

    job1.invokeOnCompletion { job2.cancel() }
    job2.invokeOnCompletion { job1.cancel() }
}.unit

"****** Experiment 1: channelUsage *****".tee.unit
runBlocking { channelUsage() }
"****** Experiment 2: typicaConcurrency *****".tee.unit
runBlocking { typicalConcurrency() }

