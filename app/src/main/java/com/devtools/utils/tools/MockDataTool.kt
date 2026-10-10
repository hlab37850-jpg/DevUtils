package com.devtools.utils.tools

import org.json.JSONObject
import java.util.UUID
import kotlin.random.Random

class MockDataTool {
    private val firstNames = listOf("سارة", "علي", "نور", "محمد", "ليان", "عمر", "ريم", "خالد")
    private val lastNames = listOf("أحمد", "سالم", "حسن", "إبراهيم", "ناصر", "يوسف")

    fun generateMockData(): String {
        val first = firstNames[Random.nextInt(firstNames.size)]
        val last = lastNames[Random.nextInt(lastNames.size)]
        val emailId = UUID.randomUUID().toString().take(8)
        return JSONObject()
            .put("id", UUID.randomUUID().toString())
            .put("name", "$first $last")
            .put("email", "user-$emailId@example.com")
            .toString(4)
    }
}
