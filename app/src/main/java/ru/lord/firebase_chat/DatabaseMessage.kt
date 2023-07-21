package ru.lord.firebase_chat

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class DatabaseMessage(
    val author: String? = null,
    val text: String? = null
) {
    @Exclude
    fun toMessage(key: String) = Message(author = author, text = text, key = key)

    @Exclude
    fun toMap() = mapOf(
        "author" to author,
        "text" to text
    )
}
