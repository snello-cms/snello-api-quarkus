package io.snello.model.events;

import io.snello.model.enums.TriggerEventType;

public class TriggerEvent {
    public TriggerEventType type;
    public String table;

    public TriggerEvent(TriggerEventType type, String table) {
        this.type = type;
        this.table = table;
    }
}
