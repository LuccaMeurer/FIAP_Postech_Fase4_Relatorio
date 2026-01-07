package fiap.postech.fase4.relatorio.service;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.util.Properties;


@Service
public class EmailService {

    public void enviarRelatorio(byte[] excel, String destinatario) throws Exception {

        Properties props = new Properties();
        props.put("mail.smtp.host", System.getenv("EMAIL_HOST"));
        props.put("mail.smtp.port", System.getenv("EMAIL_PORT"));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                System.getenv("EMAIL_USER"),
                                System.getenv("EMAIL_PASS")
                        );
                    }
                });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(System.getenv("EMAIL_USER")));
        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(destinatario)
        );
        message.setSubject("Relatório Semanal de Avaliações");

        // ===== TEXTO =====
        MimeBodyPart texto = new MimeBodyPart();
        texto.setText("Segue em anexo o relatório semanal de avaliações.");

        // ===== ANEXO (FORMA CORRETA) =====
        MimeBodyPart anexo = new MimeBodyPart();

        DataSource dataSource = new DataSource() {
            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(excel);
            }

            @Override
            public OutputStream getOutputStream() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getContentType() {
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            }

            @Override
            public String getName() {
                return "relatorio.xlsx";
            }
        };

        anexo.setDataHandler(new DataHandler(dataSource));
        anexo.setFileName("relatorio.xlsx");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(texto);
        multipart.addBodyPart(anexo);

        message.setContent(multipart);

        Transport.send(message);
    }
}
