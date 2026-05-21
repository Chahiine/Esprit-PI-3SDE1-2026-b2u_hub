package com.example.pi.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/mail")
@CrossOrigin("*")
public class MailStatusController {

    private final Optional<JavaMailSender> mailSender;
    private final Environment env;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.mode:console}")
    private String mailMode;

    public MailStatusController(Optional<JavaMailSender> mailSender, Environment env) {
        this.mailSender = mailSender;
        this.env = env;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        String password = env.getProperty("spring.mail.password", "");
        boolean passwordOk = StringUtils.hasText(password) && !password.contains("REPLACE");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mailEnabled", mailEnabled);
        body.put("mailMode", mailMode);
        body.put("smtpConfigured", mailSender.isPresent());
        body.put("gmailPasswordSet", passwordOk);
        body.put("from", env.getProperty("app.mail.from", "—"));

        if (!mailEnabled) {
            body.put("message", "E-mail desactive. Mettez app.mail.enabled=true");
        } else if ("console".equalsIgnoreCase(mailMode)) {
            body.put("message", "Mode CONSOLE : le feedback est affiche dans les logs IntelliJ (pas d'e-mail reel).");
        } else if (!passwordOk) {
            body.put("message", "Mode SMTP mais mot de passe Gmail manquant (REPLACE_WITH_APP_PASSWORD).");
        } else if (mailSender.isEmpty()) {
            body.put("message", "SMTP non demarre — redemarrez Spring Boot apres configuration.");
        } else {
            body.put("message", "Pret pour envoi Gmail reel. Testez POST /api/mail/test");
        }

        return ResponseEntity.ok(body);
    }
}
