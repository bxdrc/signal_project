package com.alerts.decorators;

import com.alerts.Alert;

/**
 * Decorator that annotates an alert as having been repeated a given number of times.
 */
public class RepeatedAlertDecorator extends AlertDecorator {
    private final int repeatCount;

    public RepeatedAlertDecorator(Alert alert, int repeatCount) {
        super(alert);
        this.repeatCount = repeatCount;
    }

    @Override
    public String getCondition() {
        return decoratedAlert.getCondition() + " [Repeated: " + repeatCount + "x]";
    }
}
