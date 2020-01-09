package pl.mareklangiewicz.coedges

import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import pl.mareklangiewicz.uspek.USpekRunner
import pl.mareklangiewicz.uspek.o
import pl.mareklangiewicz.uspek.uspek

@Ignore // TODO: check why the USpekRunner fails - in command line too: ./gradlew clean build --info
@Suppress("EXPERIMENTAL_API_USAGE")
@RunWith(USpekRunner::class)
class USpekRunnerTests {

    @Test
    fun uspekNestedTestsFailingWriteXmlOutput() = uspek {
        "On blabla blalba blab al" o {
            "On source asFlow with buffer capacity 1" o {
                "On collect flow" o {
                    "On first source item" o {
                        "On second source item during first emission" o {
                            "dsfd xads f fd sjkf" /*if this test name is short: then it works*/ o {
                                "f" o {}
                            }
                        }
                    }
                }
            }
        }
    }
}
