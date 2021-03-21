package io.snello.model;

import java.time.LocalDateTime;

//@Entity
//@Table(name = "system_event_logs")
public class SystemEventLog {

    public String uuid;
    public LocalDateTime received_date;
    public String obj;
    public String operation_type;
    public String data;
    public String principal;

    public SystemEventLog() {
    }

    public SystemEventLog(String obj, String operation_type, String data, String principal) {
        this.received_date = LocalDateTime.now();
        this.obj = obj;
        this.operation_type = operation_type;
        this.data = data;
        this.principal = principal;
    }

}
