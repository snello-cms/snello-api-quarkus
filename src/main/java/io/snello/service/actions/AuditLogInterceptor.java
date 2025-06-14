package io.snello.service.actions;

import io.quarkus.logging.Log;
import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.Arrays;

@ActionEvent
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 200) // Define a priority
public class AuditLogInterceptor {
    @AroundInvoke
    Object logInvocation(InvocationContext context) throws Exception {
        String methodName = context.getMethod().getName();
        String className = context.getTarget().getClass().getSimpleName();
        String params = Arrays.toString(context.getParameters());

        // Get the optional value from the annotation if needed
        ActionEvent auditLogAnnotation = context.getMethod().getAnnotation(ActionEvent.class);
        if (auditLogAnnotation == null) { // Could be on class level
            auditLogAnnotation = context.getTarget().getClass().getAnnotation(ActionEvent.class);
        }
        String auditDescription = auditLogAnnotation != null ? auditLogAnnotation.value() : "N/A";

        String logMessage = String.format("AUDIT [%s]: Method '%s.%s' called with params %s.",
                auditDescription, className, methodName, params);
        Log.info(logMessage);

        Object ret = context.proceed(); // Execute the original method

        Log.info(String.format("AUDIT [%s]: Method '%s.%s' completed.", auditDescription, className, methodName));
        return ret;
    }
}
