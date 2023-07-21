package ru.lord.firebase_chat

data class Message(
    val author: String? = null,
    val text: String? = null,
    val key: String
) {
    fun toDatabaseMessage() = DatabaseMessage(author, text)
}

