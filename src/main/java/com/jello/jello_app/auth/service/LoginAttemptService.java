package com.jello.jello_app.auth.service;

import com.jello.jello_app.auth.exception.TooManyAttemptsException;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Segura tentativa de login com senha errada: 5 falhas em 10 minutos por usuario + ip (guardado em memoria)
@Component
public class LoginAttemptService {

    private static final int MAX_FAILURES = 5;
    private static final long WINDOW_MILLIS = 10 * 60 * 1000L;
    private static final int MAX_TRACKED_KEYS = 10_000;

    private final Map<String, Deque<Long>> failures = new ConcurrentHashMap<>();

    public String keyFor(String username, String ip) {
        String name = username == null ? "" : username.trim().toLowerCase();
        return name + "|" + ip;
    }

    public void checkAllowed(String key) {
        Deque<Long> attempts = failures.get(key);
        if (attempts == null) return;

        synchronized (attempts) {
            purgeOld(attempts);
            if (attempts.size() >= MAX_FAILURES) {
                throw new TooManyAttemptsException();
            }
        }
    }

    public void recordFailure(String key) {
        if (failures.size() > MAX_TRACKED_KEYS) {
            failures.clear();
        }

        Deque<Long> attempts = failures.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (attempts) {
            purgeOld(attempts);
            attempts.addLast(System.currentTimeMillis());
        }
    }

    public void reset(String key) {
        failures.remove(key);
    }

    private void purgeOld(Deque<Long> attempts) {
        long limit = System.currentTimeMillis() - WINDOW_MILLIS;
        while (!attempts.isEmpty() && attempts.peekFirst() < limit) {
            attempts.removeFirst();
        }
    }
}
