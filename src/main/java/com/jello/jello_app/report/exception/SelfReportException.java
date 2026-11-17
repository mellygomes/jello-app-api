package com.jello.jello_app.report.exception;

import com.jello.jello_app.common.exception.BusinessException;

public class SelfReportException extends BusinessException {
    public SelfReportException(Long userId) {
        super("Usuário com ID: " + userId + " não pode reportar a si mesmo");
    }
}
