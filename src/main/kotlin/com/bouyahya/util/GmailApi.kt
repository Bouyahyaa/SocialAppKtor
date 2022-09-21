package com.bouyahya.util

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.StringUtils
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import org.apache.commons.codec.binary.Base64
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.GeneralSecurityException


class GmailApi {
    private val APPLICATION_NAME = "Gmail API Java Quickstart"
    private val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()
    private val user = "me"
    var service: Gmail? = null
    private val filePath = File(System.getProperty("user.dir") + "/credentials.json")


    @Throws(IOException::class, GeneralSecurityException::class)
    fun main(): Gmail? {
        return getGmailService()
        //getMailBody("Google")
    }

    @Throws(IOException::class)
    fun getMailBody(searchString: String?) {

        // Access Gmail inbox
        val request = service!!.users().messages().list(user).setQ(searchString)
        val messagesResponse = request.execute()
        request.pageToken = messagesResponse.nextPageToken

        // Get ID of the email you are looking for
        val messageId = messagesResponse.messages[0].id
        val message: Message = service!!.users().messages()[user, messageId].execute()

        // Print email body
        val emailBody: String = StringUtils
            .newStringUtf8(Base64.decodeBase64(message.getPayload().getParts().get(0).getBody().getData()))
        println("Email body : $emailBody")
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    fun getGmailService(): Gmail? {
        val input: InputStream = FileInputStream(filePath) // Read credentials.json
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(input))

        // Credential builder
        val authorize: Credential =
            GoogleCredential.Builder().setTransport(GoogleNetHttpTransport.newTrustedTransport())
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets(
                    clientSecrets.details.clientId.toString(),
                    clientSecrets.details.clientSecret.toString()
                )
                .build().setAccessToken(getAccessToken()).setRefreshToken(
                    "1//04OgH4zdBfC1yCgYIARAAGAQSNwF-L9IrOArd7lI3tjotmWQJwtTXkZQddK4VwEIejvZM7okLBX3HL83DO8hl_RyodLa9GK5t-hY"
                ) //Replace this

        // Create Gmail service
        val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
        service = Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, authorize)
            .setApplicationName(APPLICATION_NAME).build()
        return service
    }

    private fun getAccessToken(): String? {
        try {
            val params: MutableMap<String, Any> =
                LinkedHashMap()
            params["grant_type"] = "refresh_token"
            params["client_id"] =
                "596361886709-tnlu4imkgt0j449cda0jpldhucg8cfgf.apps.googleusercontent.com" //Replace this
            params["client_secret"] = "Tz5BSvpSx_wkQZXx763jGmcE" //Replace this
            params["refresh_token"] =
                "1//04VGrVGhYiCRHCgYIARAAGAQSNwF-L9IrGjxYh0BGJkUUvoJm808n1EVy4HGErJQYAKGratZ93k5y3mnlnU9Mph0_DIQUEXqRdMw" //Replace this
            val postData = StringBuilder()
            for ((key, value) in params) {
                if (postData.length != 0) {
                    postData.append('&')
                }
                postData.append(URLEncoder.encode(key, "UTF-8"))
                postData.append('=')
                postData.append(URLEncoder.encode(value.toString(), "UTF-8"))
            }
            val postDataBytes = postData.toString().toByteArray(charset("UTF-8"))
            val url = URL("https://accounts.google.com/o/oauth2/token")
            val con: HttpURLConnection = url.openConnection() as HttpURLConnection
            con.setDoOutput(true)
            con.setUseCaches(false)
            con.setRequestMethod("POST")
            con.getOutputStream().write(postDataBytes)
            val reader = BufferedReader(InputStreamReader(con.getInputStream()))
            val buffer = StringBuffer()
            var line = reader.readLine()
            while (line != null) {
                buffer.append(line)
                line = reader.readLine()
            }
            val json = JSONObject(buffer.toString())
            return json.getString("access_token")
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }
}