package com.devtools.utils.tools

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.nio.charset.StandardCharsets

class JwtTool {
    fun decodeJwt(token: String): String {
        val parts = token.split('.')
        require(parts.size == 3 && parts.all { it.isNotBlank() }) {
            "يجب أن يتكون JWT من ثلاثة أجزاء مفصولة بنقاط."
        }
        val payloadBytes = try {
            Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("جزء Payload ليس Base64 URL صالحاً.", e)
        }
        val payload = String(payloadBytes, StandardCharsets.UTF_8)
        return try {
            when (val value = JSONTokener(payload).nextValue()) {
                is JSONObject -> value.toString(4)
                is JSONArray -> value.toString(4)
                else -> throw IllegalArgumentException("Payload يجب أن يكون JSON object أو array.")
            }
        } catch (e: Exception) {
            if (e is IllegalArgumentException) throw e
            throw IllegalArgumentException("Payload ليس JSON صالحاً.", e)
        }
    }
}
