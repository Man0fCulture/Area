package com.epitech.area.integrations.gmail

import com.epitech.area.domain.entities.UserService
import com.epitech.area.sdk.*
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import org.bson.Document
import java.io.ByteArrayOutputStream
import java.util.*
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * Service Gmail - Envoi et réception d'emails
 *
 * Triggers:
 * - new_email: Nouvel email reçu
 * - email_with_subject: Email avec un sujet spécifique
 *
 * Actions:
 * - send_email: Envoyer un email
 * - reply_email: Répondre à un email
 */
class GmailService : ServiceDefinition() {
    override val id = "gmail"
    override val name = "Gmail"
    override val description = "Send and receive emails via Gmail"
    override val category = "Email"
    override val requiresAuth = true

    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val httpTransport = GoogleNetHttpTransport.newTrustedTransport()

    override suspend fun executeTrigger(
        triggerId: String,
        config: Map<String, String>,
        userService: UserService?
    ): TriggerResult {
        if (userService == null) {
            return TriggerResult.error("Gmail requires authentication")
        }

        return when (triggerId) {
            "new_email" -> handleNewEmail(userService)
            "email_with_subject" -> handleEmailWithSubject(config, userService)
            else -> TriggerResult.error("Unknown trigger: $triggerId")
        }
    }

    private suspend fun handleNewEmail(userService: UserService): TriggerResult {
        return try {
            val gmail = buildGmailService(userService)
            val response = gmail.users().messages().list("me")
                .setMaxResults(1)
                .setQ("is:unread")
                .execute()

            val messages = response.messages
            if (messages.isNullOrEmpty()) {
                return TriggerResult.notTriggered()
            }

            val messageId = messages[0].id
            val message = gmail.users().messages().get("me", messageId).execute()

            val headers = message.payload.headers
            val from = headers.find { it.name.equals("From", ignoreCase = true) }?.value ?: "unknown"
            val subject = headers.find { it.name.equals("Subject", ignoreCase = true) }?.value ?: "no subject"

            runtime.log("New email from $from: $subject")

            TriggerResult.success(
                "message_id" to messageId,
                "from" to from,
                "subject" to subject,
                "snippet" to (message.snippet ?: ""),
                "triggered_at" to System.currentTimeMillis()
            )
        } catch (e: Exception) {
            runtime.log("Failed to check new email: ${e.message}", LogLevel.ERROR)
            TriggerResult.error("Failed to check emails: ${e.message}")
        }
    }

    private suspend fun handleEmailWithSubject(
        config: Map<String, String>,
        userService: UserService
    ): TriggerResult {
        val targetSubject = config["subject"]
            ?: return TriggerResult.error("Missing 'subject' parameter")

        return try {
            val gmail = buildGmailService(userService)
            val query = "is:unread subject:$targetSubject"
            val response = gmail.users().messages().list("me")
                .setMaxResults(1)
                .setQ(query)
                .execute()

            val messages = response.messages
            if (messages.isNullOrEmpty()) {
                return TriggerResult.notTriggered()
            }

            val messageId = messages[0].id
            val message = gmail.users().messages().get("me", messageId).execute()

            val headers = message.payload.headers
            val from = headers.find { it.name.equals("From", ignoreCase = true) }?.value ?: "unknown"
            val subject = headers.find { it.name.equals("Subject", ignoreCase = true) }?.value ?: "no subject"

            runtime.log("Email with subject '$targetSubject' from $from")

            TriggerResult.success(
                "message_id" to messageId,
                "from" to from,
                "subject" to subject,
                "snippet" to (message.snippet ?: ""),
                "triggered_at" to System.currentTimeMillis()
            )
        } catch (e: Exception) {
            runtime.log("Failed to check email with subject: ${e.message}", LogLevel.ERROR)
            TriggerResult.error("Failed to check emails: ${e.message}")
        }
    }

    override suspend fun executeAction(
        actionId: String,
        config: Map<String, String>,
        triggerData: Document,
        userService: UserService?
    ): ActionResult {
        if (userService == null) {
            return ActionResult.error("Gmail requires authentication")
        }

        return when (actionId) {
            "send_email" -> handleSendEmail(config, userService)
            "reply_email" -> handleReplyEmail(config, triggerData, userService)
            else -> ActionResult.error("Unknown action: $actionId")
        }
    }

    private suspend fun handleSendEmail(
        config: Map<String, String>,
        userService: UserService
    ): ActionResult {
        val to = config["to"] ?: return ActionResult.error("Missing 'to' parameter")
        val subject = config["subject"] ?: return ActionResult.error("Missing 'subject' parameter")
        val body = config["body"] ?: return ActionResult.error("Missing 'body' parameter")

        return try {
            val gmail = buildGmailService(userService)
            val email = createEmail(to, subject, body)
            val message = createMessageWithEmail(email)

            gmail.users().messages().send("me", message).execute()

            runtime.log("Email sent to $to")
            ActionResult.success("Email sent to $to")
        } catch (e: Exception) {
            runtime.log("Failed to send email: ${e.message}", LogLevel.ERROR)
            ActionResult.error("Failed to send email: ${e.message}")
        }
    }

    private suspend fun handleReplyEmail(
        config: Map<String, String>,
        triggerData: Document,
        userService: UserService
    ): ActionResult {
        val body = config["body"] ?: return ActionResult.error("Missing 'body' parameter")

        val messageId = triggerData["message_id"] as? String
        val originalFrom = triggerData["from"] as? String
        val originalSubject = triggerData["subject"] as? String

        if (messageId == null || originalFrom == null) {
            return ActionResult.error("Missing message_id or from in trigger data")
        }

        val replySubject = if (originalSubject?.startsWith("Re:") == true) {
            originalSubject
        } else {
            "Re: ${originalSubject ?: ""}"
        }

        return try {
            val gmail = buildGmailService(userService)
            val email = createEmail(originalFrom, replySubject, body)
            val message = createMessageWithEmail(email)
            message.threadId = messageId

            gmail.users().messages().send("me", message).execute()

            runtime.log("Reply sent to $originalFrom")
            ActionResult.success("Reply sent to $originalFrom")
        } catch (e: Exception) {
            runtime.log("Failed to reply to email: ${e.message}", LogLevel.ERROR)
            ActionResult.error("Failed to reply: ${e.message}")
        }
    }

    private fun buildGmailService(userService: UserService): Gmail {
        val accessToken = userService.credentials["accessToken"] as? String
            ?: throw IllegalStateException("Missing access token")

        val credential = GoogleCredential().setAccessToken(accessToken)

        return Gmail.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName("AREA Backend")
            .build()
    }

    private fun createEmail(to: String, subject: String, bodyText: String): MimeMessage {
        val props = Properties()
        val session = Session.getDefaultInstance(props, null)
        val email = MimeMessage(session)

        email.setFrom(InternetAddress("me"))
        email.addRecipient(javax.mail.Message.RecipientType.TO, InternetAddress(to))
        email.subject = subject
        email.setText(bodyText)

        return email
    }

    private fun createMessageWithEmail(email: MimeMessage): Message {
        val buffer = ByteArrayOutputStream()
        email.writeTo(buffer)
        val bytes = buffer.toByteArray()
        val encodedEmail = Base64.getUrlEncoder().encodeToString(bytes)

        val message = Message()
        message.raw = encodedEmail
        return message
    }

    override suspend fun validateTriggerConfig(
        triggerId: String,
        config: Map<String, String>
    ): ValidationResult {
        return when (triggerId) {
            "new_email" -> ValidationResult(true)
            "email_with_subject" -> {
                val subject = config["subject"]
                if (subject.isNullOrBlank()) {
                    ValidationResult(false, "'subject' is required")
                } else {
                    ValidationResult(true)
                }
            }
            else -> ValidationResult(false, "Unknown trigger: $triggerId")
        }
    }

    override suspend fun validateActionConfig(
        actionId: String,
        config: Map<String, String>
    ): ValidationResult {
        return when (actionId) {
            "send_email" -> {
                val errors = mutableListOf<String>()
                if (config["to"].isNullOrBlank()) errors.add("'to' is required")
                if (config["subject"].isNullOrBlank()) errors.add("'subject' is required")
                if (config["body"].isNullOrBlank()) errors.add("'body' is required")

                if (errors.isEmpty()) ValidationResult(true) else ValidationResult(false, errors)
            }
            "reply_email" -> {
                val body = config["body"]
                if (body.isNullOrBlank()) {
                    ValidationResult(false, "'body' is required")
                } else {
                    ValidationResult(true)
                }
            }
            else -> ValidationResult(false, "Unknown action: $actionId")
        }
    }
}
