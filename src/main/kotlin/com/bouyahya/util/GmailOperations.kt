package com.bouyahya.util

import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import org.apache.commons.codec.binary.Base64
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.Exception
import java.security.GeneralSecurityException
import java.util.*
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


class GmailOperations {

    @Throws(MessagingException::class, IOException::class)
    fun sendMessage(service: Gmail, userId: String?, email: MimeMessage?) {
        val message: Message = createMessageWithEmail(email!!)!!
        try {
            service.users().messages().send(userId, message).executeAsInputStream()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(MessagingException::class, IOException::class)
    fun createMessageWithEmail(email: MimeMessage): Message? {
        val baos = ByteArrayOutputStream()
        email.writeTo(baos)
        val encodedEmail: String = Base64.encodeBase64URLSafeString(baos.toByteArray())
        val message = Message()
        message.setRaw(encodedEmail)
        return message
    }

    @Throws(MessagingException::class, IOException::class)
    fun createEmail(to: String?, from: String?, subject: String?, bodyText: String?): MimeMessage? {
        val props = Properties()
        val session: Session = Session.getDefaultInstance(props, null)
        val email = MimeMessage(session)
        email.setFrom(InternetAddress(from)) //me
        email.addRecipient(javax.mail.Message.RecipientType.TO, InternetAddress(to)) //
        email.subject = subject
        email.setText(bodyText)
        return email
    }

    @Throws(IOException::class, GeneralSecurityException::class, MessagingException::class)
    fun sendEmail(toEmail: String, subjectEmail: String, bodyTextEmail: String) {
        val service: Gmail? = GmailApi().main()
        val Mimemessage =
            createEmail(
                toEmail,
                "Your email", // Replace This
                subjectEmail,
                bodyTextEmail
            )
        sendMessage(service!!, "me", Mimemessage)
    }
}