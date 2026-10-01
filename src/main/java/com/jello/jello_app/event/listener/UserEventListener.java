package com.jello.jello_app.event.listener;

import com.jello.jello_app.email.service.EmailService;
import com.jello.jello_app.event.UserEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final EmailService emailService;

    @EventListener
    public void onUserEvent(UserEvent event) {
        switch (event.getType()) {
            case REGISTRATION ->
                    emailService.sendNewAccountEmail(event.getUser().getUsername(), event.getUser().getEmail(), (String) event.getData().get("key"));
            case RESET_PASSWORD ->
                    emailService.sendPasswordResetEmail(event.getUser().getUsername(), event.getUser().getEmail(), (String) event.getData().get("key"));
            default -> {
            }
        }
    }
}
