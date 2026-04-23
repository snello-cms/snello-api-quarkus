package io.snello.service.ai.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.Tool;
import io.quarkus.logging.Log;
import io.quarkus.security.identity.SecurityIdentity;
import io.snello.model.FieldDefinition;
import io.snello.model.Metadata;
import io.snello.service.ApiService;
import io.snello.service.MetadataService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;

import java.util.List;
import java.util.Map;

import static io.snello.management.AppConstants.*;

/**
 * LangChain4j Tools that expose snello.io CMS APIs to the AI assistant.
 * <p>
 * Tools are grouped into two areas:
 * <ul>
 *   <li><b>Schema / Metadata</b>: read entity schemas and list available entities.</li>
 *   <li><b>Data</b>: list, fetch, create and update records.</li>
 * </ul>
 */
@ApplicationScoped
public class SnelloCmsTools {

    @Inject
    ApiService apiService;

    @Inject
    MetadataService metadataService;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    ObjectMapper objectMapper;

    // ── Schema / Metadata tools ───────────────────────────────────────────────

    @Tool("Returns the list of all available entity names (table names) registered in the CMS")
    public String listEntities() {
        try {
            List<String> names = metadataService.metadataMap().keySet().stream().sorted().toList();
            return "Available entities: " + String.join(", ", names);
        } catch (Exception e) {
            Log.error("listEntities error", e);
            return "Error retrieving entity list: " + e.getMessage();
        }
    }

    @Tool("Returns the schema (fields, types and constraints) of a CMS entity. " +
          "Call this before any data entry operation to know which fields are required and their types.")
    public String getEntitySchema(String entityName) {
        try {
            Metadata metadata = apiService.metadataWithFields(entityName);
            if (metadata == null) {
                return "Entity '" + entityName + "' not found. Use listEntities() to see available entities.";
            }
            List<FieldDefinition> fields = metadata.fields;
            if (fields == null || fields.isEmpty()) {
                return "Entity '" + entityName + "' has no fields defined.";
            }
            StringBuilder sb = new StringBuilder("Schema for entity '").append(entityName).append("':\n");
            sb.append("- Primary key: ").append(metadata.table_key).append(" (").append(metadata.table_key_type).append(")\n");
            for (FieldDefinition fd : fields) {
                sb.append("- ").append(fd.name)
                  .append(" | type: ").append(fd.type)
                  .append(" | input_type: ").append(fd.input_type);
                if (fd.options != null && !fd.options.isBlank()) {
                    sb.append(" | options: ").append(fd.options);
                }
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            Log.error("getEntitySchema error for: " + entityName, e);
            return "Error retrieving schema for '" + entityName + "': " + e.getMessage();
        }
    }

    // ── Data tools ────────────────────────────────────────────────────────────

    @Tool("Lists records from a CMS entity. Provide the entity name and the maximum number of records to return (default 10).")
    public String listRecords(String entityName, int limit) {
        checkAuthenticated();
        try {
            if (limit <= 0 || limit > 100) limit = 10;
            MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();
            List<Map<String, Object>> records = apiService.list(entityName, params, null, limit, 0);
            if (records == null || records.isEmpty()) {
                return "No records found in entity '" + entityName + "'.";
            }
            return objectMapper.writeValueAsString(records);
        } catch (Exception e) {
            Log.error("listRecords error for: " + entityName, e);
            return "Error listing records for '" + entityName + "': " + e.getMessage();
        }
    }

    @Tool("Fetches a single record from a CMS entity by its UUID.")
    public String fetchRecord(String entityName, String uuid) {
        checkAuthenticated();
        try {
            String tableKey = apiService.table_key(entityName);
            Map<String, Object> record = apiService.fetch(null, entityName, uuid, tableKey);
            if (record == null) {
                return "Record with uuid '" + uuid + "' not found in entity '" + entityName + "'.";
            }
            return objectMapper.writeValueAsString(record);
        } catch (Exception e) {
            Log.error("fetchRecord error for: " + entityName + " uuid: " + uuid, e);
            return "Error fetching record: " + e.getMessage();
        }
    }

    @Tool("Creates a new record in a CMS entity. " +
          "Provide the entity name and the record data as a JSON string. " +
          "Call getEntitySchema first to know the required fields.")
    public String createRecord(String entityName, String jsonData) {
        checkAuthenticated();
        checkWritePermission(entityName);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(jsonData, Map.class);
            String tableKey = apiService.table_key(entityName);
            Map<String, Object> created = apiService.create(entityName, data, tableKey);
            return "Record created successfully: " + objectMapper.writeValueAsString(created);
        } catch (Exception e) {
            Log.error("createRecord error for: " + entityName, e);
            return "Error creating record in '" + entityName + "': " + e.getMessage();
        }
    }

    @Tool("Updates an existing record in a CMS entity. " +
          "Provide the entity name, the record UUID and the fields to update as a JSON string.")
    public String updateRecord(String entityName, String uuid, String jsonData) {
        checkAuthenticated();
        checkWritePermission(entityName);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(jsonData, Map.class);
            String tableKey = apiService.table_key(entityName);
            Map<String, Object> updated = apiService.merge(entityName, data, uuid, tableKey);
            return "Record updated successfully: " + objectMapper.writeValueAsString(updated);
        } catch (Exception e) {
            Log.error("updateRecord error for: " + entityName + " uuid: " + uuid, e);
            return "Error updating record: " + e.getMessage();
        }
    }

    // ── Security helpers ──────────────────────────────────────────────────────

    private void checkAuthenticated() {
        if (securityIdentity.isAnonymous()) {
            throw new SecurityException("Authentication required to access CMS data.");
        }
    }

    private void checkWritePermission(String entityName) {
        boolean isAdmin = securityIdentity.hasRole("Admin") || securityIdentity.hasRole("Manager");
        if (!isAdmin) {
            throw new SecurityException(
                "You do not have permission to write data to entity '" + entityName + "'.");
        }
    }
}
