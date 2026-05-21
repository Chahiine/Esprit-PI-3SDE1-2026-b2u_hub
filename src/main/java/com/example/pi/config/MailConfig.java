package com.example.pi.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.StringUtils;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    @ConditionalOnProperty(name = "app.mail.mode", havingValue = "smtp")
    public JavaMailSender javaMailSender(Environment env) {
        String username = env.getProperty("spring.mail.username", "");
        String password = env.getProperty("spring.mail.password", "");

        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new IllegalStateException(
                    "app.mail.mode=smtp mais spring.mail.username/password manquant. "
                            + "Voir CONFIGURATION-EMAIL.md");
        }
        if (password.contains("REPLACE")) {
            throw new IllegalStateException(
                    "Remplacez REPLACE_WITH_APP_PASSWORD dans application-local.properties "
                            + "ou utilisez app.mail.mode=console pour la demo.");
        }

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(env.getProperty("spring.mail.host", "smtp.gmail.com"));
        sender.setPort(env.getProperty("spring.mail.port", Integer.class, 587));
        sender.setUsername(username.trim());
        sender.setPassword(password.replace(" ", "").trim());
        sender.setDefaultEncoding("UTF-8");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.connectiontimeout", "15000");
        props.put("mail.smtp.timeout", "15000");
        props.put("mail.smtp.writetimeout", "15000");

        return sender;
    }
}
