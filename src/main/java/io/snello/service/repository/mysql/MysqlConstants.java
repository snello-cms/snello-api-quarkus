package io.snello.service.repository.mysql;

public class MysqlConstants {

    public static final String COUNT_QUERY = " SELECT COUNT(*) AS SIZE_OF FROM ";
    public static final String DELETE_FROM = "DELETE FROM ";
    public static final String SHOW_TABLES_INIT = "SHOW TABLES ";
    public static final String SHOW_TABLES_END = " LIKE ?";
    public static final String SELECT_FROM = " SELECT * FROM ";
    public static final String INSERT_INTO = "INSERT INTO ";
    public static final String TRUNCATE_TABLE = "TRUNCATE TABLE ";
    public static final String DROP_TABLE = "DROP TABLE ";


    public static final String ESCAPE = "`";


    public static String creationQueryConditions = "CREATE TABLE IF NOT EXISTS `conditions` (\n" +
            "  `uuid` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `metadata_uuid` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `metadata_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `separator` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `condition` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `query_params` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `sub_query` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `metadata_multijoin_uuid` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  PRIMARY KEY (uuid)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    public static String creationQueryDocuments = "CREATE TABLE IF NOT EXISTS `documents` (\n" +
            "  `uuid` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `original_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `path` varchar(255) COLLATE utf8mb4_unicode_ci ,\n" +
            "  `mimetype` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `size` int(12),\n" +
            "  `table_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `table_key` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  PRIMARY KEY (uuid)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    public static String creationQueryDraggables = "CREATE TABLE IF NOT EXISTS draggables (\n" +
            "  `uuid` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `template` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `style` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `image` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `static_vars` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `dynamic_vars` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  PRIMARY KEY (uuid)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    public static String creationQueryDroppables = "CREATE TABLE IF NOT EXISTS droppables (\n" +
            "  `uuid` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `html` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `draggables` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `dynamic_values` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `dynamics` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  PRIMARY KEY (uuid)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    public static String creationQueryExtensions = "CREATE TABLE IF NOT EXISTS extensions (\n" +
            "  `uuid` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `icon` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `description` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `tag_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `library_path` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  PRIMARY KEY (uuid)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    public static String creationQueryFieldDefinitions = "CREATE TABLE IF NOT EXISTS `fielddefinitions` (\n" +
            "  `uuid` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `metadata_uuid` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `metadata_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `label` varchar(255) COLLATE utf8mb4_unicode_ci  NOT NULL,\n" +
            "  `type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `input_type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `options` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `group_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `tab_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `validations` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `table_key` BOOLEAN NOT NULL DEFAULT FALSE,\n" +
            "  `input_disabled` BOOLEAN NOT NULL DEFAULT FALSE,\n" +
            "  `function_def` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL ,\n" +
            "  `sql_type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `sql_definition` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `default_value` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `pattern` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `join_table_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `join_table_key` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `join_table_select_fields` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `searchable` BOOLEAN NOT NULL DEFAULT FALSE,\n" +
            "  `search_condition` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `search_field_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `show_in_list` BOOLEAN NOT NULL DEFAULT FALSE,\n" +
            "  PRIMARY KEY (uuid)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    public static String creationQueryMetadatas = "CREATE TABLE IF NOT EXISTS `metadatas` (\n" +
            "  `uuid` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `table_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `icon` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `select_fields` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `search_fields` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `alias_table` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `alias_condition` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `table_key` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `table_key_type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `table_key_addition` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `creation_query` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `order_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `tab_groups` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `already_exist` BOOLEAN NOT NULL DEFAULT FALSE,\n" +
            "  `created` BOOLEAN NOT NULL DEFAULT FALSE,\n" +
            "  PRIMARY KEY (uuid)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    public static String creationQuerySelectQueries = "CREATE TABLE IF NOT EXISTS `selectqueries` (\n" +
            "  `uuid` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `query_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `with_params` boolean DEFAULT false,\n" +
            "  `select_query` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `select_query_count` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  PRIMARY KEY (uuid)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";


    public static String creationLinksQueries = "CREATE TABLE IF NOT EXISTS `links` (\n" +
            "  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `labels` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `metadata_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `metadata_key` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `metadata_searchable_field` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `metadata_lock_field` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `metadata_generated_uuid` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  `created` BOOLEAN NOT NULL DEFAULT FALSE,\n" +
            "  PRIMARY KEY (name)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    public static String joinTableQuery = "CREATE TABLE IF NOT EXISTS `%1$s` ( " +
            "`%2$s` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL, " +
            "%3$s varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL, " +
            "id int NOT NULL AUTO_INCREMENT, PRIMARY KEY (id) ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";


    public static String creationQuerySystemEventLogs = "CREATE TABLE IF NOT EXISTS `systemeventlogs` (\n" +
            "  `uuid` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `query_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,\n" +
            "  `with_params` boolean DEFAULT false,\n" +
            "  `select_query` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,\n" +
            "  PRIMARY KEY (uuid)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";


}
