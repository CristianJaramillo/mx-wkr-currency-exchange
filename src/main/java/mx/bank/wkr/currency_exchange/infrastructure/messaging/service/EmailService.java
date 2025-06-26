package mx.bank.wkr.currency_exchange;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${ai.resource.output}")
    private String outputPath;

    public void sendEmailWithTemplateAndAttachment(
            List<String> recipients,  // Ahora es una lista
            String subject,
            Map<String, Object> variables,
            String attachmentPath
    ) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Template HTML con Thymeleaf
            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process("email/welcome", context);

            helper.setTo(recipients.toArray(new String[0])); // convierte la lista a arreglo
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("gobapims@bancoppel.com");

            // Adjuntar archivo
            File file = new File(attachmentPath);
            if (!file.exists()) {
                log.error("El archivo no existe: {}", attachmentPath);
                throw new RuntimeException("Archivo no encontrado");
            }

            FileSystemResource resource = new FileSystemResource(file);
            helper.addAttachment(resource.getFilename(), resource);

            log.info("Enviando correo a: {}", recipients);
            mailSender.send(message);

        } catch (MessagingException e) {
            log.error("Error al enviar correo", e);
            throw new RuntimeException("Error al enviar correo con adjunto", e);
        }
    }
}