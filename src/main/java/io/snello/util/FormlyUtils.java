package io.snello.util;

import io.snello.model.FieldDefinition;
import io.snello.model.pojo.FormlyFieldConfig;

public class FormlyUtils {

    public static FormlyFieldConfig to(FieldDefinition fieldDefinition) throws Exception {
        FormlyFieldConfig formlyFieldConfig = new FormlyFieldConfig();
        formlyFieldConfig.key = fieldDefinition.name;
        formlyFieldConfig.templateOptions.label = fieldDefinition.label;
        formlyFieldConfig.templateOptions.pattern = fieldDefinition.pattern;
        switch (fieldDefinition.input_type) {
            case "radio":
            case "checkbox":
                formlyFieldConfig.type = fieldDefinition.input_type;
                break;
            case "email":
            case "text":
            case "number":
            case "tel":
            case "password":
                formlyFieldConfig.type = "input";
            case "month":
            case "color":
            case "date":
            case "datetime-local":
            case "html":
            case "range":
            case "search":
            case "time":
            case "url":
            case "week":
                break;
            default:
                throw new Exception("");
        }
        return formlyFieldConfig;
    }
}
