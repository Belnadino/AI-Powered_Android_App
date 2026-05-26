package com.example.healthcare.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.activation.FileDataSource

object EmailSender {

    suspend fun sendEmail(
        smtpHost: String,
        smtpPort: String,
        senderEmail: String,
        senderPassword: String,
        recipientEmail: String,
        subject: String,
        body: String,
        attachmentFilePath: String? = null
    ) = withContext(Dispatchers.IO) {
        try {
            val props = Properties()
            props["mail.smtp.auth"] = "true"
            props["mail.smtp.starttls.enable"] = "true"
            props["mail.smtp.host"] = smtpHost
            props["mail.smtp.port"] = smtpPort

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(senderEmail, senderPassword)
                }
            })

            val message = MimeMessage(session)
            message.setFrom(InternetAddress(senderEmail))
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
            message.subject = subject

            if (attachmentFilePath != null) {
                val multipart = MimeMultipart()

                // Body
                val textPart = MimeBodyPart()
                textPart.setText(body)
                multipart.addBodyPart(textPart)

                // Attachment
                val attachmentPart = MimeBodyPart()
                val source = FileDataSource(attachmentFilePath)
                attachmentPart.dataHandler = javax.activation.DataHandler(source)
                attachmentPart.fileName = source.name
                multipart.addBodyPart(attachmentPart)

                message.setContent(multipart)
            } else {
                message.setText(body)
            }

            Transport.send(message)
            Log.d("EmailSender", "Email sent to $recipientEmail")
        } catch (e: Exception) {
            Log.e("EmailSender", "Failed to send email: ${e.message}", e)
        }
    }
}
