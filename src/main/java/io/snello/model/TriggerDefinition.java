package io.snello.model;

import io.snello.model.enums.TriggerDefinitionType;
import io.snello.model.enums.TriggerEventType;

public class TriggerDefinition {

    public String uuid;
    public String name;

    public String tablename;
    public TriggerEventType trigger_eventtype;
    public TriggerDefinitionType triggerDefinitionType;

    public String webhook_url;
    public String webhook_method;
    public String webhook_username;
    public String webhook_password;

    public String email_recipient;
    public String email_title;
    public String email_body;
}
