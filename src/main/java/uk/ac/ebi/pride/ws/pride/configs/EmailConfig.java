package uk.ac.ebi.pride.ws.pride.configs;

import com.querydsl.core.annotations.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Config
public class EmailConfig {

    @Value("${pride.support.smtp.server}")
    private String smtpHost;

    public JavaMailSenderImpl mailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

        javaMailSender.setProtocol("SMTP");
        javaMailSender.setHost(smtpHost);
        javaMailSender.setPort(25);

        return javaMailSender;
    }
}
