package io.snello.service.ai.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.Tool;
import io.quarkus.logging.Log;
import io.quarkus.security.identity.SecurityIdentity;
import io.snello.model.FieldDefinition;
import io.snello.model.Metadata;
import io.snello.model.SearchResult;
import io.snello.service.ApiService;
import io.snello.service.MetadataService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;

import java.util.HashMap;
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

    @Tool("Lists records from a CMS entity with filters and pagination. " +
          "Arguments: entityName, params, limit, start. " +
          "params is a simple HashMap<String, String> and is converted internally to a multivalued map. " +
          "Use params for filters with the same syntax as REST query params: " +
          "field=value (exact), field_ne, field_lt, field_lte, field_gt, field_gte, " +
          "field_contains/field_like, field_ilike, field_containss, field_rlike, field_llike, field_ncontains, " +
          "field_in=a,b,c, field_nn, field_inn, field_nie, field_ie. " +
          "Examples: params={\"status\":\"published\",\"price_gte\":\"50\",\"title_ilike\":\"java\"}. " +
          "Returns a SearchResult JSON with load-more metadata (hasMore, remaining, nextStart). " +
          "Maximum limit is 10.")
    public String listRecords(String entityName, HashMap<String, String> params, int limit, int start) {
        try {
            Metadata metadata = apiService.metadata(entityName);
            if (metadata == null) {
                return "Entity '" + entityName + "' not found. Use listEntities() to see available entities.";
            }

            if (limit <= 0) {
                limit = 10;
            }
            if (limit > 10) {
                limit = 10;
            }
            if (start < 0) {
                start = 0;
            }

            MultivaluedHashMap<String, String> effectiveParams = new MultivaluedHashMap<>();
            if (params != null) {
                params.forEach((key, value) -> effectiveParams.putSingle(key, value == null ? "" : value));
            }

            if (metadata.api_protected) {
                if (!isAdminOrManager()) {
                    checkAuthenticated();
                    effectiveParams.put(metadata.username_field, List.of(currentUsername()));
                } else {
                    Log.info("admin or manager");
                }
            }

            long count = apiService.count(entityName, effectiveParams);
            List<Map<String, Object>> records = apiService.list(entityName, effectiveParams, null, limit, start);
            int currentSize = records == null ? 0 : records.size();
            long remaining = Math.max(0L, count - (start + currentSize));
            boolean hasMore = remaining > 0;
            int nextStart = start + currentSize;
            SearchResult result = new SearchResult(count, count, start, limit, hasMore, remaining, nextStart, records);
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            Log.error("listRecords error for: " + entityName, e);
            return "Error listing records for '" + entityName + "': " + e.getMessage();
        }
    }

    @Tool("Explains how to build filters for listRecords params using REST query-param syntax.")
    public String explainFilters() {
        StringBuilder sb = new StringBuilder();
        sb.append("Filter guide for listRecords(entityName, params, limit, start):\n\n");
        sb.append("1) Pagination and sorting\n");
        sb.append("- limit and start are supported\n");
        sb.append("- maximum limit is 10 (if limit > 10, limit is forced to 10)\n");
        sb.append("- Use hasMore/remaining/nextStart in response to ask for additional data\n");
        sb.append("- sort is supported via params['_sort'] with format field:asc|desc\n\n");

        sb.append("2) params format\n");
        sb.append("- params is a simple map: field -> single value\n");
        sb.append("- Example: {status:published, price_gte:50, title_ilike:java}\n\n");

        sb.append("3) Supported filter suffixes\n");
        sb.append("- Equality: field=value\n");
        sb.append("- Comparison: _ne, _lt, _lte, _gt, _gte\n");
        sb.append("- Text: _contains, _like, _ilike, _containss, _rlike, _llike, _ncontains\n");
        sb.append("- Set: _in (comma-separated values)\n");
        sb.append("- Null checks: _nn (IS NOT NULL), _inn (IS NULL)\n");
        sb.append("- Empty checks: _nie (not empty), _ie (empty)\n\n");

        sb.append("4) Examples\n");
        sb.append("- status exact match: {status:published}\n");
        sb.append("- numeric range: {price_gte:50, price_lte:200}\n");
        sb.append("- contains case-insensitive: {title_ilike:spring}\n");
        sb.append("- IN list: {status_in:[draft,published,review]}\n");
        sb.append("- pagination call: limit=10, start=20\n");
        sb.append("- if hasMore=true, client can show: 'Carica altri dati'\n\n");

        sb.append("All filters are combined with logical AND.");
        return sb.toString();
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

    private boolean isAdminOrManager() {
        return securityIdentity.hasRole("admin") || securityIdentity.hasRole("Admin")
                || securityIdentity.hasRole("manager") || securityIdentity.hasRole("Manager");
    }

    private String currentUsername() {
        if (securityIdentity.getPrincipal() == null) {
            throw new SecurityException("Authentication required to access CMS data.");
        }
        return securityIdentity.getPrincipal().getName();
    }
}
