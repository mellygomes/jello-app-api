package com.jello.jello_app.email.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static com.jello.jello_app.utils.EmailUtils.getEmailMessage;
import static com.jello.jello_app.utils.EmailUtils.getResetPasswordMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private static final String NEW_USER_ACCOUNT_VERIFICATION = "New User Account Verification";
    private static final String PASSWORD_RESET_REQUEST = "Reset Password Request";
    private final JavaMailSender sender;
    @Value("${spring.mail.verify.host}")
    private String host;
    @Value("${spring.mail.username}")
    private String fromMail;

    @Async
    public void sendNewAccountEmail(String name, String emailTo, String token) {
        try {
            var message = new SimpleMailMessage();
            message.setSubject(NEW_USER_ACCOUNT_VERIFICATION);
            message.setFrom(fromMail);
            message.setTo(emailTo);
            message.setText(getEmailMessage(name, host, token));
            sender.send(message);
        } catch (MailException ex) {
            log.error("Falha ao enviar e-mail de confirmação", ex);
        }
    }

    @Async
    public void sendPasswordResetEmail(String name, String emailTo, String token) {
        try {
            var message = new SimpleMailMessage();
            message.setSubject(PASSWORD_RESET_REQUEST);
            message.setFrom(fromMail);
            message.setTo(emailTo);
            message.setText(getResetPasswordMessage(name, host, token));
            sender.send(message);
        } catch (MailException ex) {
            log.error("Falha ao enviar e-mail de recuperação de senha", ex);
        }
    }
}