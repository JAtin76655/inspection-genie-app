package com.summonelec.inspectiongenie.Helper

import java.util.Properties
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

object SendEmailUtil {
    fun sendEmailWithAttachment(
        recipientEmail: Array<String>,
        subject: String,
        body: String,
        attachmentPath: String,
        senderEmail: String,
        senderPassword: String
    ) {
        val properties = Properties()
        properties["mail.smtp.auth"] = "true"
        properties["mail.smtp.starttls.enable"] = "true"
        properties["mail.smtp.host"] = "smtp.gmail.com"
        properties["mail.smtp.port"] = "587"

        val session = Session.getInstance(properties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(senderEmail, senderPassword)
            }
        })

        try {
            val message = MimeMessage(session)
            message.setFrom(InternetAddress(senderEmail))
            val recipientAddresses = InternetAddress.parse(recipientEmail.joinToString(", "))
            message.setRecipients(Message.RecipientType.TO, recipientAddresses)
            message.subject = subject

            //message.setText(body)

            val messageBodyPart = MimeBodyPart()
            messageBodyPart.setText(body)

            val attachmentPart = MimeBodyPart()
            val fileDataSource = FileDataSource(attachmentPath)
            attachmentPart.dataHandler = DataHandler(fileDataSource)
            attachmentPart.fileName = fileDataSource.name

            val multipart = MimeMultipart()
            multipart.addBodyPart(messageBodyPart)
            multipart.addBodyPart(attachmentPart)

            message.setContent(multipart)

            Transport.send(message)
        } catch (e: MessagingException) {
            e.printStackTrace()
        }
    }
}
