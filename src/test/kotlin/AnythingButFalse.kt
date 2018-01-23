import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import ml.duncte123.skybot.utils.AirUtils

public typealias Anything = Any

@Deprecated("The following code may be removed!", level = DeprecationLevel.WARNING)
infix fun Any.but(value: Any): Any {
    return when {
        value == this -> AssertionError("the new value can't be equals than the current.")
        value is Boolean -> !value
        value is String -> AirUtils.generateRandomString(value.length)
        value::class.java == Any::class.java -> {
            async {
                delay(3200)
                print("memes")
            }
        }
        else -> this
    }
}

@Deprecated("The following code may be removed!", level = DeprecationLevel.WARNING)
fun main(args: Array<String>) = runBlocking {
    val res = Anything() but Any()
    if (res is Deferred<*>) {
        res.join()
    }
}