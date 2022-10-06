package com.flxrs.dankchat.data.twitch.message

import android.graphics.Color
import com.flxrs.dankchat.data.irc.IrcMessage
import com.flxrs.dankchat.data.twitch.badge.Badge
import com.flxrs.dankchat.data.twitch.emote.ChatMessageEmote
import java.util.*

data class PrivMessage(
    override val timestamp: Long = System.currentTimeMillis(),
    override val id: String = UUID.randomUUID().toString(),
    override val highlights: List<Highlight> = emptyList(),
    val channel: String,
    val userId: String? = null,
    val name: String = "",
    val displayName: String = "",
    val color: Int = Color.parseColor(DEFAULT_COLOR),
    val message: String,
    val originalMessage: String = message,
    val emotes: List<ChatMessageEmote> = emptyList(),
    val isAction: Boolean = false,
    val badges: List<Badge> = emptyList(),
    val timedOut: Boolean = false,
    val tags: Map<String, String>,
) : Message() {

    companion object {
        fun parsePrivMessage(ircMessage: IrcMessage): PrivMessage = with(ircMessage) {
            val name = when (ircMessage.command) {
                "USERNOTICE" -> tags.getValue("login")
                else         -> prefix.substringBefore('!')
            }

            val displayName = tags["display-name"] ?: name
            val colorTag = tags["color"]?.ifBlank { DEFAULT_COLOR } ?: DEFAULT_COLOR
            val color = Color.parseColor(colorTag)

            val ts = tags["tmi-sent-ts"]?.toLongOrNull() ?: System.currentTimeMillis()
            var isAction = false
            val messageParam = params.getOrElse(1) { "" }
            val message = when {
                params.size > 1 && messageParam.startsWith("\u0001ACTION") && messageParam.endsWith("\u0001") -> {
                    isAction = true
                    messageParam.substring("\u0001ACTION ".length, messageParam.length - "\u0001".length)
                }

                else                                                                                          -> messageParam
            }
            val channel = params[0].substring(1)
            val id = tags["id"] ?: UUID.randomUUID().toString()

            return PrivMessage(
                timestamp = ts,
                channel = channel,
                name = name,
                displayName = displayName,
                color = color,
                message = message,
                isAction = isAction,
                id = id,
                userId = tags["user-id"],
                timedOut = tags["rm-deleted"] == "1",
                tags = tags,
            )
        }
    }
}