package com.alerts.decorators;

import com.alerts.Alert;

/**
 * Decorator that prepends a priority level to the alert condition message.
 */
public class PriorityAlertDecorator extends AlertDecorator {
    private final String priority;

    public PriorityAlertDecorator(Alert alert, String priority) {
        super(alert);
        this.priority = priority;
    }

    @Override
    public String getCondition() {
        return "[" + priority + "] " + decoratedAlert.getCondition();
    }
}
