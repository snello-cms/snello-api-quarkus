package io.snello.service;

import io.snello.model.SystemEventLog;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SystemLogService {
    Logger logger = Logger.getLogger(getClass());

    public SystemEventLog persistSystemLog(String newPath, String method, String jsonObject, String principal) {
        SystemEventLog log = new SystemEventLog(newPath, method, jsonObject, principal);
        try {
//         log.persist();
            return log;
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }

}
