package com.jello.jello_app.report.exception;

import com.jello.jello_app.common.exception.BusinessException;

public class ReportAlreadyUnapprovedException extends BusinessException {
    public ReportAlreadyUnapprovedException(Long reportId) {
        super("Report de ID: " + reportId + " já está desaprovado");
    }
}
