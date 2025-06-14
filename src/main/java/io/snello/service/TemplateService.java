package io.snello.service;

import io.quarkus.logging.Log;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateExtension;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.MultivaluedMap;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Singleton
public class TemplateService {

    @Inject
    Engine engine;

    public String parse(Map<String, Object> map, MultivaluedMap<String, String> queryParameters) {
        Log.info("creating content for template " + map);
        Template myTemplate = engine.parse((String) map.get("body"));
        return myTemplate
                .data("values", map)
                .data("queryParameters", queryParameters)
                .render();
    }

    @TemplateExtension
    public static String dateFormat(LocalDateTime date, String pattern) {
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
            return date.format(dtf);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date.toString();
    }
}
