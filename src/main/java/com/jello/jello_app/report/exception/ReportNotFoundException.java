package com.jello.jello_app.report.exception;

import com.jello.jello_app.common.exception.ResourceNotFoundException;

public class ReportNotFoundException extends ResourceNotFoundException {
    public ReportNotFoundException(Long reportId) {
        super("Report de ID: " + reportId + " não encontrado");
    }
}
