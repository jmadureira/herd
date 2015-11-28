package io.herd.scheduler;

import io.herd.base.Preconditions;

public class CronSchedulerBuilder {

    private String expression;

    public CronSchedulerBuilder(String expression) {
        this.expression = expression;
    }

    public void build() {
        parseExpression();
    }

    private void parseExpression() {
        Preconditions.checkNotEmpty(expression, "A valid cron expression must be provided.");
        String[] fields = expression.split(" ");
        Preconditions.checkArraySize(fields, 6, "Cron expression '" + expression + "' must contain 6 fields");
    }
}
