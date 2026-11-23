package com.jello.jello_app.auth.service;

import com.jello.jello_app.auth.exception.TooManyAttemptsException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptServiceTest {

    private final LoginAttemptService service = new LoginAttemptService();

    @Test
    void shouldBlockAfterFiveFailures() {
        String key = service.keyFor("Fulano", "127.0.0.1");

        for (int i = 0; i < 5; i++) {
            assertDoesNotThrow(() -> service.checkAllowed(key));
            service.recordFailure(key);
        }

        assertThrows(TooManyAttemptsException.class, () -> service.checkAllowed(key));
    }

    @Test
    void shouldAllowAgainAfterReset() {
        String key = service.keyFor("fulano", "127.0.0.1");

        for (int i = 0; i < 5; i++) service.recordFailure(key);
        service.reset(key);

        assertDoesNotThrow(() -> service.checkAllowed(key));
    }

    @Test
    void shouldKeepKeysSeparatePerIp() {
        String blocked = service.keyFor("fulano", "10.0.0.1");
        String other = service.keyFor("fulano", "10.0.0.2");

        for (int i = 0; i < 5; i++) service.recordFailure(blocked);

        assertThrows(TooManyAttemptsException.class, () -> service.checkAllowed(blocked));
        assertDoesNotThrow(() -> service.checkAllowed(other));
    }
}
