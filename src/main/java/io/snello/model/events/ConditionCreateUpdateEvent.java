package io.snello.model.events;

import io.snello.model.Condition;

import java.util.Map;

public class ConditionCreateUpdateEvent {
    public Condition condition;

    public ConditionCreateUpdateEvent(Condition condition) {
        this.condition = condition;
    }

    public ConditionCreateUpdateEvent(Map<String, Object> map) {
        this.condition = new Condition(map);
    }

    @Override
    public String toString() {
        return "ConditionCreateUpdateEvent{" +
                "condition=" + condition +
                '}';
    }
}
