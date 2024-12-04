package com.example.hc.utils

object DailyMessages {
    private val messages = mapOf(
        0 to "Happy Sunday! Take a break and enjoy life.",
        1 to "Motivational Monday: Dream big, work hard!",
        2 to "Terrific Tuesday: You’re closer to your goals!",
        3 to "Wonderful Wednesday: Keep pushing forward.",
        4 to "Thankful Thursday: Appreciate the little things.",
        5 to "Fantastic Friday: The weekend is near!",
        6 to "Superb Saturday: Relax and recharge!"
    )

    fun getMessageForToday(): String {
        val dayOfWeek = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) - 1
        return messages[dayOfWeek] ?: "Have an amazing day!"
    }
}
