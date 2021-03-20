package io.snello.repository.h2;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.security.identity.SecurityIdentity;
import io.snello.model.Condition;
import io.snello.model.FieldDefinition;
import io.snello.model.Metadata;
import io.snello.model.UserDetails;
import io.snello.model.events.DbCreatedEvent;
import io.snello.repository.JdbcRepository;
import io.snello.util.ConditionUtils;
import io.snello.util.ParamUtils;
import io.snello.util.PasswordUtils;
import io.snello.util.SqlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import javax.ws.rs.core.MultivaluedMap;
import java.sql.*;
import java.util.*;

import static io.snello.management.AppConstants.DB_TYPE;
import static io.snello.management.DbConstants.*;
import static io.snello.repository.h2.H2Constants.*;

@Singleton
//@Requires(property = DB_TYPE, value = "h2")
public class H2JdbcRepository implements JdbcRepository {

    DataSource dataSource;
    Logger logger = LoggerFactory.getLogger(getClass());


    @Inject
    Event eventPublisher;

    public H2JdbcRepository(){}

    public H2JdbcRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void onLoad(@Observes StartupEvent event) {
        logger.info("Creation queries at startup: " + event.toString());
        try {
            batch(creationQueries());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        eventPublisher.fireAsync(new DbCreatedEvent());
    }

    @Override
    public String[] creationQueries() {
        return new String[]{
                creationQueryMetadatas,
                creationQueryFieldDefinitions,
                creationQueryConditions,
                creationQueryDocuments,
                creationQueryExtensions,
                creationQueryDraggables,
                creationQueryDroppables,
                creationQuerySelectQueries,
                creationUsersQueries,
                creationRolesQueries,
                creationUserRolesQueries,
                creationUrlMapRulesQueries,
                creationAdminUser,
                creationAdminRole,
                creationAdminUserRole,
                creationLinksQueries,
                creationConditionsViewRole,
                creationConditionsEditRole,
                creationDocumentsViewRole,
                creationDocumentsEditRole,
                creationExtensionsEditRole,
                creationExtensionsViewRole,
                creationFieldDefinitionsViewRole,
                creationFieldDefinitionsEditRole,
                creationLinksViewRole,
                creationLinksEditRole,
                creationMetadatasViewRole,
                creationMetadatasEditRole,
                creationRoleViewRole,
                creationRoleEditRole,
                creationSelectQueryViewRole,
                creationSelectQueryEditRole,
                creationUrlMapRuleViewRole,
                creationUrlMapRuleEditRole,
                creationUserViewRole,
                creationUserEditRole,
                creationDraggableEditRole,
                creationDraggableViewRole,
                creationDroppableEditRole,
                creationDroppableViewRole,
                creationContentsViewRole,
                creationContentsEditRole,
                creationPublicdataEditRole,
                creationChangePasswordTokenQueries
        };
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public long count(String table, String alias_condition, MultivaluedMap<String, String> httpParameters, List<Condition> conditions) throws Exception {
        StringBuffer where = new StringBuffer();
        StringBuffer select = new StringBuffer();
        List<Object> in = new LinkedList<>();
        select.append(COUNT_QUERY);
        if (alias_condition != null)
            where.append(alias_condition);

        boolean withCondition = false;
        try {
            withCondition = ConditionUtils.where(httpParameters, conditions, where, in);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        if (!withCondition) {
            ParamUtils.where(httpParameters, where, in);
        }

        try (
                Connection connection = dataSource.getConnection()) {

            if (where.length() > 0) {
                where = new StringBuffer(_WHERE_).append(where);
            }
            logger.info("query: " + select + H2SqlUtils.escape(table) + where);
            try (PreparedStatement preparedStatement = connection.prepareStatement(select + H2SqlUtils.escape(table) + where)) {
                SqlHelper.fillStatement(preparedStatement, in);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        long count = resultSet.getLong(SIZE_OF);
                        logger.info("count:" + count);
                        return count;
                    }
                }
            }
        }
        return 0;
    }


    public long count(String select_query, MultivaluedMap<String, String> httpParameters, List<Condition> conditions) throws Exception {
        return 0;
    }

    public long count(String select_query) throws Exception {
        return 0;
    }

    public boolean exist(String table, String table_key, Object uuid) throws Exception {
        String select = COUNT_QUERY + H2SqlUtils.escape(table) + _WHERE_ + H2SqlUtils.escape(table_key) + "= ?";
        List<Object> in = new LinkedList<>();
        in.add(uuid);
        try (Connection connection = dataSource.getConnection()) {
            logger.info("query: " + select);
            try (PreparedStatement preparedStatement = connection.prepareStatement(select)) {
                SqlHelper.fillStatement(preparedStatement, in);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        long count = resultSet.getLong(SIZE_OF);
                        logger.info("exist:" + (count > 0));
                        return count > 0;
                    }
                }
            }
        }
        return false;
    }


    public List<Map<String, Object>> list(String table, String sort) throws Exception {
        return list(table, null, null, null, null, sort, 0, 0);
    }


    public List<Map<String, Object>> list(String table, String select_fields, String alias_condition, MultivaluedMap<String, String> httpParameters, List<Condition> conditions, String sort, int limit, int start) throws Exception {
        StringBuffer where = new StringBuffer();
        StringBuffer order_limit = new StringBuffer();
        StringBuffer select = new StringBuffer();
        List<Object> in = new LinkedList<>();
        select.append(_SELECT_);
        if (select_fields != null) {
            //"_SELECT_ * _FROM_ "
            select.append(select_fields);
        } else {
            select.append(_ALL_);
        }
        select.append(_FROM_);
        if (alias_condition != null && !alias_condition.trim().isEmpty()) {
            where.append(H2SqlUtils.escape(alias_condition));
        }

        if (sort != null) {
            if (sort.contains(":")) {
                String[] sort_ = sort.split(":");
                order_limit.append(_ORDER_BY_).append(sort_[0]).append(" ").append(sort_[1]);
            } else {
                order_limit.append(_ORDER_BY_).append(sort);
            }
        }

//        ParamUtils.where(httpParameters, where, in);
//        ConditionUtils.where(httpParameters, conditions, where, in);

        boolean withCondition = false;
        try {
            withCondition = ConditionUtils.where(httpParameters, conditions, where, in);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        if (!withCondition) {
            ParamUtils.where(httpParameters, where, in);
        }


        if (start == 0 && limit == 0) {
            logger.info("no limits");
        } else {
            if (start > 0) {
                order_limit.append(_LIMIT_).append(" ? ");
                in.add(start);
            } else {
                order_limit.append(_LIMIT_).append(" ? ");
                in.add(0);
            }
            if (limit > 0) {
                order_limit.append(",").append(" ? ");
                in.add(limit);
            } else {
                order_limit.append(", ? ");
                in.add(10);
            }
        }
        try (Connection connection = dataSource.getConnection()) {
            if (where.length() > 0) {
                where = new StringBuffer(_WHERE_).append(where);
            }
            logger.info("LIST query: " + select.toString() + H2SqlUtils.escape(table) + where + order_limit);
            return H2SqlUtils.executeQueryList(connection, select.toString() + H2SqlUtils.escape(table) + where.toString() + order_limit.toString(), in);
        }

    }

    public List<Map<String, Object>> list(String query, MultivaluedMap<String, String> httpParameters, List<Condition> conditions, String sort, int limit, int start) throws Exception {
        StringBuffer where = new StringBuffer();
        StringBuffer order_limit = new StringBuffer();
        StringBuffer select = new StringBuffer(query);
        List<Object> in = new LinkedList<>();

        if (sort != null) {
            if (sort.contains(":")) {
                String[] sort_ = sort.split(":");
                order_limit.append(_ORDER_BY_).append(sort_[0]).append(" ").append(sort_[1]);
            } else {
                order_limit.append(_ORDER_BY_).append(sort);
            }
        }

        boolean withCondition = false;
        try {
            withCondition = ConditionUtils.where(httpParameters, conditions, where, in);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        if (!withCondition) {
            ParamUtils.where(httpParameters, where, in);
        }
        if (start == 0 && limit == 0) {
            logger.info("no limits");
        } else {
            if (start > 0) {
                order_limit.append(_LIMIT_).append(" ? ");
                in.add(start);
            } else {
                order_limit.append(_LIMIT_).append(" ? ");
                in.add(0);
            }
            if (limit > 0) {
                order_limit.append(",").append(" ? ");
                in.add(limit);
            } else {
                order_limit.append(", ? ");
                in.add(10);
            }
        }
        try (Connection connection = dataSource.getConnection()) {
            if (where.length() > 0 && !select.toString().contains(_WHERE_)) {
                where = new StringBuffer(_WHERE_).append(where);
            } else {
                where = new StringBuffer(where);
            }
            logger.info("LIST query: " + select.toString() + where + order_limit);
            return H2SqlUtils.executeQueryList(connection, select.toString() + where.toString() + order_limit.toString(), in);
        }

    }

    public List<Map<String, Object>> list(String query) throws Exception {
        List<Object> in = new LinkedList<>();
        try (Connection connection = dataSource.getConnection()) {
            logger.info("LIST query: " + query);
            return H2SqlUtils.executeQueryList(connection, query, in);
        }

    }

    public Map<String, Object> create(String table, String table_key, Map<String, Object> map) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String query = H2SqlUtils.create(table, map);
            logger.info("CREATE QUERY: " + query);
            H2SqlUtils.executeQueryCreate(connection, query, map, table_key);
        }
        return map;
    }

    public boolean query(String query, List<Object> values) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            logger.info("EXECUTE QUERY: " + query);
            return H2SqlUtils.executeQuery(connection, query, values);
        } catch (Exception e) {
            logger.error("error: ", e);
            return false;
        }

    }


    public Map<String, Object> update(String table, String table_key, Map<String, Object> map, String key) throws
            Exception {
        Map<String, Object> keys = new HashMap<>();
        List<Object> in = new LinkedList<>();
        keys.put(table_key, key);
        String query = H2SqlUtils.update(table, map, keys, in);
        try (Connection connection = dataSource.getConnection()) {
            logger.info("UPDATE QUERY: " + query);
            H2SqlUtils.executeQueryUpdate(connection, query, in);
        }
        return map;
    }

    public Map<String, Object> fetch(String select_fields, String table, String table_key, String uuid) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            if (select_fields == null) {
                select_fields = " * ";
            }
            logger.info("FETCH QUERY: " + "_SELECT_ * _FROM_ " + H2SqlUtils.escape(table) + " _WHERE_ " + table_key + " = ?");
            PreparedStatement preparedStatement = connection.prepareStatement(_SELECT_ + select_fields + _FROM_ + H2SqlUtils.escape(table)
                    + _WHERE_ + H2SqlUtils.escape(table_key) + " = ?");
            preparedStatement.setObject(1, uuid);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return H2SqlUtils.single(resultSet);
            }
        }
    }

    public boolean delete(String table, String table_key, String uuid) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            logger.info("DELETE QUERY: " + DELETE_FROM + table + _WHERE_ + table_key + " = ? ");
            PreparedStatement preparedStatement = connection.prepareStatement(DELETE_FROM + H2SqlUtils.escape(table) + _WHERE_
                    + H2SqlUtils.escape(table_key) + " = ?");
            preparedStatement.setObject(1, uuid);
            int result = preparedStatement.executeUpdate();
            return result > 0;
        }
    }

    public void batch(String[] queries) throws Exception {
        final int batchSize = 5;
        int count = 0;
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            for (String query : queries) {
                logger.info("BATCH QUERY: " + query);
                statement.addBatch(query);
                if (++count % batchSize == 0) {
                    statement.executeBatch();
                }
            }
            statement.executeBatch();
            statement.close();
        }
    }

    public boolean executeQuery(String sql) throws Exception {
        Statement statement = null;
        try (Connection connection = dataSource.getConnection()) {
            statement = connection.createStatement();
            int result = statement.executeUpdate(sql);
            if (result > 0) {
                return true;
            }
        } finally {
            if (statement != null)
                statement.close();
        }
        return false;
    }



    public boolean verifyTable(String tableName) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            PreparedStatement preparedStatement = connection.prepareStatement(SHOW_TABLES);
            List<Map<String, Object>> list = null;
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                list = H2SqlUtils.list(resultSet);
            }
            for (Map<String, Object> map : list) {
                if (!map.containsKey("table_name")) {
                    continue;
                }
                String table_name_found = (String) map.get("table_name");
                if (table_name_found.toLowerCase().equals(tableName.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public SecurityIdentity login(String username, String password) throws Exception {
        if (username == null) {
            throw new Exception("login must contain username in 'username' field");
        }
        if (password == null) {
            throw new Exception("login must contain password in 'password' field");
        }
        Map<String, Object> map = null;
        try (Connection connection = dataSource.getConnection()) {
            logger.info("login QUERY: " + LOGIN_QUERY);
            PreparedStatement preparedStatement = connection.prepareStatement(LOGIN_QUERY);
            preparedStatement.setObject(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                map = H2SqlUtils.single(resultSet);
            }
        }
        if (map == null) {
            logger.info("password not found for username: " + username);
            throw new Exception("invalid username/password");
        }
        String passwordOnDb = (String) map.get(PWD_LOWERCASE);
        String encrPassword = PasswordUtils.createPassword(password);
        if (encrPassword.equals(passwordOnDb)) {
            return new UserDetails(username, getRoles(username));
        }
        throw new Exception("Failure in authentication");
    }


    private List<String> getRoles(String username) throws Exception {
        List<String> roles = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            logger.info("roles QUERY: " + ROLES_QUERY);
            PreparedStatement preparedStatement = connection.prepareStatement(ROLES_QUERY);
            preparedStatement.setObject(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    roles.add(resultSet.getString(1));
                }
            }
        }
        return roles;
    }

    @Override
    public List<String> roles(String username) throws Exception {
        if (username == null) {
            throw new Exception("username is null!");
        }
        return getRoles(username);
    }

    public String getUserRoleQuery() {
        return INSERT_ROLE_QUERY;
    }

    @Override
    public String getJoinTableQuery() {
        return joinTableQuery;
    }

    @Override
    public String escape(String name) {
        return H2SqlUtils.escape(name);
    }

    @Override
    public String fieldDefinition2Sql(FieldDefinition fieldDefinition) throws Exception {
        return H2FieldDefinitionUtils.sql(fieldDefinition);
    }

    @Override
    public String createTableSql(Metadata metadata, List<FieldDefinition> fields, List<String> joiQueries, List<Condition> conditions) throws Exception {
        StringBuffer sb = new StringBuffer(" CREATE TABLE " + escape(metadata.table_name) + " (");
        if (metadata.table_key_type.equals("autoincrement")) {
            sb.append(escape(metadata.table_key) + " int NOT NULL AUTO_INCREMENT ");
        } else {
            sb.append(escape(metadata.table_key) + " VARCHAR(50) NOT NULL ");
        }
        for (FieldDefinition fieldDefinition : fields) {
            if (fieldDefinition.sql_definition != null && !fieldDefinition.sql_definition.trim().isEmpty()) {
                sb.append(",").append(fieldDefinition.sql_definition);
            } else {
                sb.append(",").append(fieldDefinition2Sql(fieldDefinition));
            }
            if ("multijoin".equals(fieldDefinition.type)) {
                String join_table_name = metadata.table_name + "_" + fieldDefinition.join_table_name;
                String table_id = metadata.table_name + "_id";
                String join_table_id = fieldDefinition.join_table_name + "_id";
                joiQueries.add(String.format(getJoinTableQuery(), metadata.table_name + "_" + fieldDefinition.join_table_name,
                        metadata.table_name + "_id", fieldDefinition.join_table_name + "_id"));
                Condition condition = new Condition();
                condition.metadata_multijoin_uuid = metadata.uuid;
                condition.uuid = java.util.UUID.randomUUID().toString();
                condition.metadata_name = fieldDefinition.join_table_name;
                condition.metadata_uuid = ""; //?? dove lo dovrei prendere??
                condition.condition = metadata.table_name + "_id_nn && join_table_nn";
                condition.query_params = metadata.table_name + "_id";
                condition.sub_query = fieldDefinition.join_table_key + " in (select " + join_table_id + " from " + join_table_name + " where " + table_id + " = ?)";
                conditions.add(condition);
            }
        }
        sb.append(", PRIMARY KEY (" + escape(metadata.table_key) + ")").append(") ;");
        logger.info("CREATION TABLE QUERY: " + sb.toString());
        return sb.toString();
    }
}
