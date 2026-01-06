package fiap.postech.fase4.relatorio.service;

import jakarta.activation.DataHandler;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.util.ByteArrayDataSource;

import org.springframework.stereotype.Service;

import java.util.Properties;


@Service
public class EmailService {

    public void enviarRelatorio(
            byte[] excel,
            String destinatario
    ) throws Exception {

        Properties props = new Properties();
        props.put("mail.smtp.host", System.getenv("EMAIL_HOST"));
        props.put("mail.smtp.port", System.getenv("EMAIL_PORT"));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(
                props,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                System.getenv("EMAIL_USER"),
                                System.getenv("EMAIL_PASS")
                        );
                    }
                }
        );

        Message message = new MimeMessage(session);
        message.setFrom(
                new InternetAddress(System.getenv("EMAIL_USER"))
        );
        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(destinatario)
        );
        message.setSubject("Relatório Semanal de Avaliações");

        MimeBodyPart texto = new MimeBodyPart();
        texto.setText(
                "Segue em anexo o relatório semanal de avaliações.",
                "UTF-8"
        );

        MimeBodyPart anexo = new MimeBodyPart();
        anexo.setFileName("relatorio.xlsx");

        ByteArrayDataSource dataSource =
                new ByteArrayDataSource(
                        excel,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                );

        anexo.setDataHandler(new DataHandler(dataSource));

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(texto);
        multipart.addBodyPart(anexo);

        message.setContent(multipart);

        Transport.send(message);
    }
}
