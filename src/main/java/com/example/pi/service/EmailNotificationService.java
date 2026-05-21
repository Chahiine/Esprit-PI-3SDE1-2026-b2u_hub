package com.example.pi.service;

import com.example.pi.entity.Evaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final Optional<JavaMailSender> mailSender;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${app.mail.mode:console}")
    private String mailMode;

    @Value("${app.mail.from:sassichahine68@gmail.com}")
    private String mailFrom;

    public EmailNotificationService(Optional<JavaMailSender> mailSender) {
        this.mailSender = mailSender;
    }

    public String sendTestEmail(String to) {
        if (!mailEnabled) {
            return "E-mail desactive (app.mail.enabled=false).";
        }
        String subject = "B2U-HUB — Test notification e-mail";
        String body = """
                Bonjour,

                Ceci est un e-mail de TEST depuis l'API B2U-HUB (Spring Boot).
                Si vous recevez ce message, la configuration SMTP Gmail fonctionne.

                — B2U-HUB PI
                """;
        return dispatch(to, subject, body);
    }

    public String sendEvaluationToStudent(Evaluation evaluation) {
        if (!StringUtils.hasText(evaluation.getStudentEmail())) {
            return "E-mail etudiant manquant — notification non envoyee.";
        }
        if (!mailEnabled) {
            return "E-mail desactive. Evaluation enregistree sans notification.";
        }

        String subject = "B2U-HUB — Nouvelle evaluation de " + evaluation.getEnterpriseName();
        String body = buildBody(evaluation);
        return dispatch(evaluation.getStudentEmail(), subject, body);
    }

    private String dispatch(String to, String subject, String body) {
        if ("console".equalsIgnoreCase(mailMode)) {
            logConsoleMail(to, subject, body);
            return "Notification simulee (mode console) — contenu affiche dans la console IntelliJ. "
                    + "Destinataire : " + to
                    + ". Pour un vrai Gmail : app.mail.mode=smtp + mot de passe application.";
        }

        if (mailSender.isEmpty()) {
            return "SMTP non configure. Mettez app.mail.mode=smtp et le mot de passe Gmail dans "
                    + "application-local.properties puis redemarrez.";
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.get().send(message);
            log.info("E-mail envoye a {}", to);
            return "E-mail envoye a " + to + " — verifiez la boite de reception et les spams.";
        } catch (Exception e) {
            log.error("Echec envoi e-mail a {}", to, e);
            return "Echec envoi e-mail : " + rootCauseMessage(e)
                    + ". Utilisez GET /api/mail/status pour diagnostiquer.";
        }
    }

    private void logConsoleMail(String to, String subject, String body) {
        log.info("""
                
                ========== B2U-HUB — E-MAIL (MODE CONSOLE) ==========
                De      : {}
                Vers    : {}
                Objet   : {}
                ----------------------------------------
                {}
                =====================================================
                """, mailFrom, to, subject, body);
    }

    private static String rootCauseMessage(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t.getMessage() != null ? t.getMessage() : e.toString();
    }

    private String buildBody(Evaluation e) {
        String comment = StringUtils.hasText(e.getComment()) ? e.getComment() : "(aucun commentaire)";
        return """
                Bonjour %s,

                L'entreprise %s a publie une evaluation pour votre projet « %s » sur B2U-HUB.

                Note globale : %d/5
                Date du projet : %s

                Feedback :
                %s

                Consultez votre espace etudiant :
                http://localhost:4200/evaluation/etudiant

                — Equipe B2U-HUB
                """.formatted(
                e.getStudentName(),
                e.getEnterpriseName(),
                e.getProjectTitle(),
                e.getRating(),
                e.getProjectDate(),
                comment);
    }
}
