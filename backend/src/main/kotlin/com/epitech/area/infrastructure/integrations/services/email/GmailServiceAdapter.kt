package com.epitech.area.infrastructure.integrations.services.email

import com.epitech.area.domain.entities.UserService
import com.epitech.area.infrastructure.integrations.*
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.util.*
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class GmailServiceAdapter : ServiceAdapter {
    override val serviceId = "gmail"
    override val serviceName = "Gmail"
    
    private val logger = LoggerFactory.getLogger(GmailServiceAdapter::class.java)
    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val httpTransport = GoogleNetHttpTransport.newTrustedTransport()

    override suspend fun executeAction(
        actionId: String,
        config: Map<String, Any>,
        userService: UserService?
    ): ActionResult {
        if (userService == null) {
            return ActionResult(false, error = "Gmail requires authentication")
        }

        return when (actionId) {
            "new_email" -> checkNewEmail(userService)
            "email_with_subject" -> checkEmailWithSubject(config, userService)
            else -> ActionResult(false, error = "Unknown action: $actionId")
        }
    }

    private suspend fun checkNewEmail(userService: UserService): ActionResult {
        return try {
            val gmail = buildGmailService(userService)
            val response = gmail.users().messages().list("me")
                .setMaxResults(1)
                .setQ("is:unread")
                .execute()

            val messages = response.messages
            if (messages.isNullOrEmpty()) {
                return ActionResult(false, error = "No new emails")
            }

            val messageId = messages[0].id
            val message = gmail.users().messages().get("me", messageId).execute()
            
            val headers = message.payload.headers
            val from = headers.find { it.name.equals("From", ignoreCase = true) }?.value ?: "unknown"
            val subject = headers.find { it.name.equals("Subject", ignoreCase = true) }?.value ?: "no subject"

            ActionResult(
                success = true,
                data = mapOf(
                    "message_id" to messageId,
                    "from" to from,
                    "subject" to subject,
                    "snippet" to (message.snippet ?: ""),
                    "triggered_at" to System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to check new email", e)
            ActionResult(false, error = "Failed to check emails: ${e.message}")
        }
    }

    private suspend fun checkEmailWithSubject(config: Map<String, Any>, userService: UserService): ActionResult {
        val targetSubject = config["subject"] as? String ?: return ActionResult(
            false,
            error = "Missing 'subject' parameter"
        )

        return try {
            val gmail = buildGmailService(userService)
            val query = "is:unread subject:$targetSubject"
            val response = gmail.users().messages().list("me")
                .setMaxResults(1)
                .setQ(query)
                .execute()

            val messages = response.messages
            if (messages.isNullOrEmpty()) {
                return ActionResult(false, error = "No emails with subject '$targetSubject'")
            }

            val messageId = messages[0].id
            val message = gmail.users().messages().get("me", messageId).execute()
            
            val headers = message.payload.headers
            val from = headers.find { it.name.equals("From", ignoreCase = true) }?.value ?: "unknown"
            val subject = headers.find { it.name.equals("Subject", ignoreCase = true) }?.value ?: "no subject"

            ActionResult(
                success = true,
                data = mapOf(
                    "message_id" to messageId,
                    "from" to from,
                    "subject" to subject,
                    "snippet" to (message.snippet ?: ""),
                    "triggered_at" to System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to check email with subject", e)
            ActionResult(false, error = "Failed to check emails: ${e.message}")
        }
    }

    override suspend fun executeReaction(
        reactionId: String,
        config: Map<String, Any>,
        actionData: Map<String, Any>,
        userService: UserService?
    ): ReactionResult {
        if (userService == null) {
            return ReactionResult(false, error = "Gmail requires authentication")
        }

        return when (reactionId) {
            "send_email" -> sendEmail(config, userService)
            "reply_email" -> replyToEmail(config, actionData, userService)
            else -> ReactionResult(false, error = "Unknown reaction: $reactionId")
        }
    }

    private suspend fun sendEmail(config: Map<String, Any>, userService: UserService): ReactionResult {
        val to = config["to"] as? String ?: return ReactionResult(
            false,
            error = "Missing 'to' parameter"
        )
        val subject = config["subject"] as? String ?: return ReactionResult(
            false,
            error = "Missing 'subject' parameter"
        )
        val body = config["body"] as? String ?: return ReactionResult(
            false,
            error = "Missing 'body' parameter"
        )

        return try {
            val gmail = buildGmailService(userService)
            val email = createEmail(to, subject, body)
            val message = createMessageWithEmail(email)
            
            gmail.users().messages().send("me", message).execute()
            
            ReactionResult(
                success = true,
                message = "Email sent to $to"
            )
        } catch (e: Exception) {
            logger.error("Failed to send email", e)
            ReactionResult(false, error = "Failed to send email: ${e.message}")
        }
    }

    private suspend fun replyToEmail(
        config: Map<String, Any>,
        actionData: Map<String, Any>,
        userService: UserService
    ): ReactionResult {
        val body = config["body"] as? String ?: return ReactionResult(
            false,
            error = "Missing 'body' parameter"
        )
        
        val messageId = actionData["message_id"] as? String
        val originalFrom = actionData["from"] as? String
        val originalSubject = actionData["subject"] as? String

        if (messageId == null || originalFrom == null) {
            return ReactionResult(false, error = "Missing message_id or from in action data")
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
            
            ReactionResult(
                success = true,
                message = "Reply sent to $originalFrom"
            )
        } catch (e: Exception) {
            logger.error("Failed to reply to email", e)
            ReactionResult(false, error = "Failed to reply: ${e.message}")
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

    override suspend fun validateActionConfig(actionId: String, config: Map<String, Any>): ValidationResult {
        return when (actionId) {
            "new_email" -> ValidationResult(true)
            "email_with_subject" -> {
                val subject = config["subject"] as? String
                if (subject.isNullOrBlank()) {
                    ValidationResult(false, listOf("'subject' is required"))
                } else {
                    ValidationResult(true)
                }
            }
            else -> ValidationResult(false, listOf("Unknown action: $actionId"))
        }
    }

    override suspend fun validateReactionConfig(reactionId: String, config: Map<String, Any>): ValidationResult {
        return when (reactionId) {
            "send_email" -> {
                val errors = mutableListOf<String>()
                if ((config["to"] as? String).isNullOrBlank()) errors.add("'to' is required")
                if ((config["subject"] as? String).isNullOrBlank()) errors.add("'subject' is required")
                if ((config["body"] as? String).isNullOrBlank()) errors.add("'body' is required")
                
                if (errors.isEmpty()) ValidationResult(true) else ValidationResult(false, errors)
            }
            "reply_email" -> {
                val body = config["body"] as? String
                if (body.isNullOrBlank()) {
                    ValidationResult(false, listOf("'body' is required"))
                } else {
                    ValidationResult(true)
                }
            }
            else -> ValidationResult(false, listOf("Unknown reaction: $reactionId"))
        }
    }
}
