package main.service;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class MailService {

    private String username;
    private String password;
    private Properties properties;

    public MailService(){}

    public MailService(String username, String password){
        this.username = username;
        this.password = password;

        properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
    }

    public void sendMail(String to, String subject, String content, String attachment) {
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(content);

            try {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                FileDataSource fileDataSource = new FileDataSource(attachment) {

                    @Override
                    public String getContentType() {
                        return "application/octet-stream";
                    }
                };
                attachmentPart.setDataHandler(new DataHandler(fileDataSource));
                attachmentPart.setFileName(fileDataSource.getName());

                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(attachmentPart);
                message.setContent(multipart);

            }catch (NullPointerException e){
                Alert alert = new Alert(Alert.AlertType.ERROR, "No file attached!", ButtonType.OK);
                alert.setTitle(e.toString());
                alert.showAndWait();
            }
            Transport.send(message);

        } catch (MessagingException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error sending mail!", ButtonType.OK);
            alert.setTitle(e.toString());
            alert.showAndWait();
        }
    }

    public boolean isValidEmailAddress(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddress = new InternetAddress(email);
            emailAddress.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }
}
