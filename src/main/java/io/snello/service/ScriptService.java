package io.snello.service;

import io.quarkus.logging.Log;
import io.snello.api.service.MailService;
import io.snello.model.Action;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class ScriptService {

    @Inject
    MetadataService metadataService;

    @Inject
    MailService mailService;

    ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

    public void execute(String js, Action action) throws Exception {
        Map<String, Object> values = new HashMap<>();
        Log.info("executeJs javascript_function : ");
        Bindings bindings = engine.createBindings();
        bindings.put("action", action);
        bindings.put("metadataService", metadataService);
        bindings.put("mailService", mailService);
        bindings.put("values", values);
        engine.eval(js, bindings);
    }

}
