package ru.lord.firebase_chat

data class DatabaseMessage(
    val author: String? = null,
    val text: String? = null
) {
    fun toMessage(key: String) = Message(author = author, text = text, key = key)
}
