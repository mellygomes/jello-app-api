package com.jello.jello_app.confirmation.service;

import com.jello.jello_app.confirmation.exception.ConfirmationNotFoundException;
import com.jello.jello_app.confirmation.model.Confirmation;
import com.jello.jello_app.confirmation.repository.ConfirmationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfirmationService {

    private final ConfirmationRepository confirmationRepository;

    public Confirmation getConfirmationByKey(String token) {
        return confirmationRepository.findByConfirmationKey(token)
                .orElseThrow(() -> new ConfirmationNotFoundException(token));
    }

    public void deleteConfirmation(Confirmation confirmation) {
        confirmationRepository.delete(confirmation);
    }

}
