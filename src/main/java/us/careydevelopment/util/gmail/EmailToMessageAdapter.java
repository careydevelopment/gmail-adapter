package us.careydevelopment.util.gmail;

import com.google.api.services.gmail.model.Message;
import us.careydevelopment.util.gmail.model.Email;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Properties;

public class EmailToMessageAdapter {

    private Message message = new Message();
    private Email email;

    public EmailToMessageAdapter(Email email) {
        this.email = email;
    }

    public Message adapt() throws MessagingException, IOException {
        Message message = createGmailMessageFromEmail();
        return message;
    }

    private Message createGmailMessageFromEmail() throws MessagingException, IOException {
       MimeMessage mimeMessage = convertHtmlEmailToMimeMessage();

       ByteArrayOutputStream buffer = new ByteArrayOutputStream();
       mimeMessage.writeTo(buffer);

       byte[] bytes = buffer.toByteArray();
       String encodedEmail = Base64.getEncoder().encodeToString(bytes);

       Message message = new Message();
       message.setRaw(encodedEmail);

       return message;
    }

    private MimeMessage convertEmailToMimeMessage() throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage mimeMessage = new MimeMessage(session);

        setMetadata(mimeMessage, email);

        mimeMessage.setText(email.getPlainText());

        return mimeMessage;
    }


    private MimeMessage convertHtmlEmailToMimeMessage() throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage mimeMessage = new MimeMessage(session);

        setMetadata(mimeMessage, email);

        MimeBodyPart htmlBodyPart = new MimeBodyPart();
        htmlBodyPart.setContent(email.getHtml(), "text/html");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(htmlBodyPart);

        mimeMessage.setContent(multipart);

        return mimeMessage;
    }


    private void setMetadata(MimeMessage mimeMessage, Email email) throws MessagingException {
        mimeMessage.setFrom(new InternetAddress(email.getFrom()));
        mimeMessage.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(email.getTo()));
        mimeMessage.setSubject(email.getSubject());
    }
}
