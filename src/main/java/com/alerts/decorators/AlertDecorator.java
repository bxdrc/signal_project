package com.alerts.decorators;

import com.alerts.Alert;

/**
 * Abstract base decorator for {@link Alert}.
 * Delegates all calls to the wrapped alert by default.
 */
public abstract class AlertDecorator implements Alert {
    protected Alert decoratedAlert;

    public AlertDecorator(Alert alert) {
        this.decoratedAlert = alert;
    }

    @Override
    public String getPatientId() { return decoratedAlert.getPatientId(); }

    @Override
    public String getCondition() { return decoratedAlert.getCondition(); }

    @Override
    public long getTimestamp() { return decoratedAlert.getTimestamp(); }
}
