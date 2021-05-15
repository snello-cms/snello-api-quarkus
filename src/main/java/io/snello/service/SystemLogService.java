package io.snello.service;

import io.snello.model.SystemEventLog;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class SystemLogService {

    @Inject
    ApiService apiService;

    Logger logger = Logger.getLogger(getClass());

    public SystemEventLog persistSystemLog(String newPath, String method, String jsonObject, String principal) {
        SystemEventLog log = new SystemEventLog(newPath, method, jsonObject, principal);
        try {
            logger.info("PERSIST: " + log);
//            apiService.create("systemeventlogs", log.toMap(), "uuid");
            return log;
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }

}
