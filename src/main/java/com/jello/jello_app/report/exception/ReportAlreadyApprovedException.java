package com.jello.jello_app.report.exception;

import com.jello.jello_app.common.exception.BusinessException;

public class ReportAlreadyApprovedException extends BusinessException {
    public ReportAlreadyApprovedException(Long reportId) {
        super("Report de ID: " + reportId + " já está aprovado");
    }
}
