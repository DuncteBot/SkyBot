package ml.duncte123.skybot.utils

import org.json.JSONArray
import org.json.JSONObject

class EarthUtils {
    companion object {
        @JvmStatic
        fun throwableToJSONObject(throwable: Throwable): JSONObject {

            return JSONObject().put("className", throwable::class.java.name)
                    .put("message", throwable.message)
                    .put("localiziedMessage", throwable.localizedMessage)
                    .put("cause", throwable.cause?.let { throwableToJSONObject(it) })
                    .put("supressed", throwableArrayToJSONArray(throwable.suppressed))
                    .put("stacktraces", stacktraceArrayToJSONArray(throwable.stackTrace))
        }

        @JvmStatic
        private fun throwableArrayToJSONArray(throwables: Array<Throwable>): JSONArray =
                JSONArray(throwables.map { throwableToJSONObject(it) })

        @JvmStatic
        private fun stacktraceArrayToJSONArray(stacktraces: Array<StackTraceElement>): JSONArray =
                JSONArray(stacktraces.map { stacktraceToJSONObject(it) })

        @JvmStatic
        fun stacktraceToJSONObject(stackTraceElement: StackTraceElement) =
                JSONObject().put("className", stackTraceElement.className).put("methodName", stackTraceElement.methodName)
                        .put("lineNumber", stackTraceElement.lineNumber).put("isNative", stackTraceElement.isNativeMethod)
    }
}