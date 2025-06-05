package io.snello.model.events;

import io.snello.model.Metadata;

import java.util.Map;

public class MetadataCreateUpdateEvent {
    public Metadata metadata;

    public MetadataCreateUpdateEvent(Metadata metadata) {
        this.metadata = metadata;
    }

    public MetadataCreateUpdateEvent(Map<String, Object> map) {
        this.metadata = new Metadata(map);
    }

    @Override
    public String toString() {
        return "MetadataCreateUpdateEvent{" +
                "selectQuery=" + metadata +
                '}';
    }
}
