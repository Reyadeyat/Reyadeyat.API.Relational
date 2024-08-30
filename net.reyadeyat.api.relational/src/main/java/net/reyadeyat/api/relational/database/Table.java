/*
 * Copyright (C) 2023 Reyadeyat
 *
 * Reyadeyat/RELATIONAL.API is licensed under the
 * BSD 3-Clause "New" or "Revised" License
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://reyadeyat.net/LICENSE/RELATIONAL.API.LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.reyadeyat.api.relational.database;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.reyadeyat.api.relational.data.ModelDefinition;
import net.reyadeyat.api.relational.data.DataClass;
import net.reyadeyat.api.relational.data.DataLookup;
import net.reyadeyat.api.relational.data.DataProcessor;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.reyadeyat.api.library.jdbc.JDBCSource;
import net.reyadeyat.api.library.json.JsonResultset;
import net.reyadeyat.api.library.json.JsonUtil;
import net.reyadeyat.api.library.util.Returns;
import net.reyadeyat.api.relational.model.Enterprise;
import net.reyadeyat.api.relational.model.EnterpriseModel;
import net.reyadeyat.api.relational.model.ForeignKeyField;
import net.reyadeyat.api.relational.request.Request;
import net.reyadeyat.api.relational.request.RequestField;
import net.reyadeyat.api.relational.request.RequestTable;

/**
 * 
 * Description
 * 
 *
 * @author Mohammad Nabil Mostafa
 * <a href="mailto:code@reyadeyat.net">code@reyadeyat.net</a>
 * 
 * @since 2023.01.01
 */
public class Table {
    private String data_datasource_name;
    private String data_database_name;
    private RequestTable request_table;
    private HashMap<String, Field> field_name_map;
    private HashMap<String, Field> field_alias_map;
    private HashMap<String, String> field_name_alias;
    private HashMap<String, String> field_alias_name;
    private ArrayList<Field> field_list;
    private String select_where_condition;
    private ArrayList<Field> select_where_condition_field_list;
    private String updateWhereCondition;
    private ArrayList<Field> updateWhereConditionFields;
    private HashMap<String, ForeignKey> foreignKeys;//Foreign Table, Keys
    private HashMap<String, DependentKey> dependentKeys;//Dependent Table, Keys
    private HashMap<String, JoinKey> joinKeys;//Join Table, Keys
    private ArrayList<String> join_sql_list;
    
    private Boolean hasPrimaryKeyAI;
    private Boolean hasPrimaryKeyMI;
    
    private Integer countPrimaryKeyAI;
    private Integer countPrimaryKeyMI;
        
    private ArrayList<ForeignKey> foreign_keys_list;
    private ArrayList<DependentKey> dependent_keys_list;
    private String inner_join_statement;
    
    //PrimaryKeys
    protected ArrayList<Field> primary_keys;
    protected ArrayList<Field> primary_keys_manual_increment_fields;
    protected ArrayList<Field> primary_keys_auto_increment_fields;
    protected ArrayList<Field> uniqueness_fields_all;
    protected ArrayList<Field> uniqueness_fields_any;
    //protected HashMap<String, Table> foreign_tables;
    //Insert
    protected Boolean hasVariable;
    protected ArrayList<Field> insert_fields;
    protected String valid_insert_fields;
    protected String uniqueness_insert_statement;
    protected String insert_statement;
    protected String insert_set_statement;
    protected ArrayList<String> insert_set_statement_fields_name;
    protected ArrayList<String> insert_set_statement_fields_alias;
    protected String insert_statement_select;
    //Select
    protected ArrayList<Field> select_fields;
    protected String valid_select_fields;
    protected String select_statement;
    protected String select_statement_from;
    protected ArrayList<Field> select_statement_groupby;
    protected ArrayList<Field> select_statement_orderby;
    //Update
    protected Boolean safe_update;
    protected ArrayList<Field> update_fields;
    protected String valid_update_fields;
    protected String uniqueness_update_statement;
    protected String update_statement;
    //Delete
    protected String delete_statement;
    
    private transient Table parent_table;
    private ArrayList<Table> child_table_list;
    private HashMap<String, Table> child_table_map;
    
    private static HashMap<Integer, EnterpriseModel<Enterprise>> data_model_map = new HashMap<Integer, EnterpriseModel<Enterprise>>();
    
    public Table(String data_database_name, String data_datasource_name, Integer model_id, Table parent_table, RequestTable request_table, JsonArray table_error_list) throws Exception {
        this.parent_table = parent_table;
        this.data_database_name = data_database_name;
        this.data_datasource_name = data_datasource_name;
        this.request_table = request_table;
        field_name_map = new HashMap<String, Field>();
        field_alias_map = new HashMap<String, Field>();
        field_name_alias = new HashMap<>();
        field_alias_name = new HashMap<>();
        hasPrimaryKeyAI = null;
        hasPrimaryKeyMI = null;
        field_list = new ArrayList<Field>();
        join_sql_list = new ArrayList<String>();
        joinKeys = new HashMap<String, JoinKey>();
        
        safe_update = true;
        
        initializeTable(model_id, table_error_list);
        
        init(table_error_list);
        
        if (table_error_list.size() > 0) {
            return;
        }
        
        child_table_map = new HashMap<>();
        child_table_list = new ArrayList<Table>();
        if (request_table.child_request_table_list != null && request_table.child_request_table_list.size() > 0) {
            for (int i = 0; i < request_table.child_request_table_list.size(); i++) {
                RequestTable child_request_table = request_table.child_request_table_list.get(i);
                Table child_table = new Table(data_database_name, data_datasource_name, model_id, this, child_request_table, table_error_list);
                child_table_list.add(child_table);
                child_table_map.put(child_table.request_table.table_alias, child_table);
            }
        }
    }
    
    public Boolean init(JsonArray error_list) throws Exception {
        StringBuilder sb = new StringBuilder();
        if (data_database_name == null) {
            error_list.add("Database name is not defined");
        }

        if (request_table.table_name == null) {
            error_list.add("Table name is not defined");
        }

        postInit(error_list);

        primary_keys = new ArrayList<>();
        primary_keys_manual_increment_fields = new ArrayList<>();
        primary_keys_auto_increment_fields = new ArrayList<>();
        uniqueness_fields_all = new ArrayList<>();
        uniqueness_fields_any = new ArrayList<>();

        insert_fields = new ArrayList<>();
        select_fields = new ArrayList<>();
        update_fields = new ArrayList<>();
        select_statement_groupby = new ArrayList<>();

        ArrayList<Field> tabel_field_list = getFields();
        
        //Build Table Field List
        
        HashMap<Integer, Field> order_by_list = new HashMap<Integer, Field>();
        for (Field f : tabel_field_list) {
            if (f.isPrimaryKey() == true) {
                primary_keys.add(f);
            }
            if (f.isPrimaryKeyMI() == true) {
                primary_keys_manual_increment_fields.add(f);
            }
            if (f.isPrimaryKeyAI() == true) {
                primary_keys_auto_increment_fields.add(f);
            }
            if (f.isOrderBy() == true) {
                order_by_list.put(f.getOrderByOrder(), f);
            }
            if (f.isAllowedTo(Field.INSERT) == true) {
                insert_fields.add(f);
            }
            if (f.isAllowedTo(Field.SELECT) == true) {
                select_fields.add(f);
            }
            if (f.isAllowedTo(Field.UPDATE) == true) {
                update_fields.add(f);
            }
            if (f.isGroup() == true) {
                select_statement_groupby.add(f);
            }
        }

        valid_insert_fields = (insert_fields.size() == 0 ? null : fieldAliasToCsv(insert_fields));
        valid_select_fields = (select_fields.size() == 0 ? null : fieldAliasToCsv(select_fields));
        valid_update_fields = (update_fields.size() == 0 ? null : fieldAliasToCsv(update_fields));

        if (order_by_list != null && order_by_list.size() > 0) {
            select_statement_orderby = new ArrayList<Field>(order_by_list.size());
            Set<Integer> keys = order_by_list.keySet();
            ArrayList<Integer> tkeys = new ArrayList<Integer>(keys);
            Collections.sort(tkeys);
            for (Integer i : tkeys) {
                select_statement_orderby.add(order_by_list.get(i));
            }
        }

        if (primary_keys == null) {
            error_list.add("Primary Keys are not defined field_list tableFields");
        }

        if (request_table.transaction_type_list.contains("insert") == true) {
            for (Field f : insert_fields) {
                if (f.isUniqueAll()) {
                    uniqueness_fields_all.add(f);
                }
                if (f.isUniqueAny()) {
                    uniqueness_fields_any.add(f);
                }
                if (f.isVariable()) {
                    hasVariable = true;
                }
            }
            sb.setLength(0);
            if (request_table.transaction_type_list.contains("insert") == true && insert_fields.size() == 0) {
                error_list.add("no valid insert field_list defined");
            } else if (/*insert_fields != null && */request_table.transaction_type_list.contains("insert") == false) {
                //ignore
            } else {
                sb.setLength(0);
                sb.append("INSERT INTO `").append(data_database_name).append("`.`").append(request_table.table_name).append("` (");
                for (int i = 0; i < insert_fields.size(); i++) {
                    Field field = insert_fields.get(i);
                    if (field.isPrimaryKeyAI() == false) {
                        sb.append(field.getSQLInsertName()).append(",");
                    }
                }
                sb.delete(sb.length() - 1, sb.length());
                if (hasPrimaryKeyMI() == false) {
                    sb.append(") VALUES (");
                } else {
                    sb.append(") SELECT ");
                }
                for (int i = 0; i < insert_fields.size(); i++) {
                    Field field = insert_fields.get(i);
                    if (field.isPrimaryKeyMI() == true) {
                        sb.append("IFNULL(MAX(" + field.getSQLName() + "), 0) + 1,");
                    } else if (field.isInsertFormulaDefined() == true) {
                        sb.append(field.getInsertFormulaDefined()).append(",");
                    } else {
                        sb.append("?,");
                    }
                }
                sb.delete(sb.length() - 1, sb.length());
                if (hasPrimaryKeyMI() == false) {
                    sb.append(")");
                }
                if (hasPrimaryKeyMI() == true) {
                    sb.append(" FROM `").append(data_database_name).append("`.`").append(request_table.table_name).append("`");
                    Integer counter = primary_keys.size() - countPrimaryKeyMI();
                    if (counter > 0) {
                        sb.append(" WHERE");
                    }
                    for (int i = 0; i < primary_keys.size(); i++) {
                        Field field = primary_keys.get(i);
                        if (field.isPrimaryKeyMI() == false) {
                            sb.append(" ").append(field.getSQLName()).append("=?").append(" AND");
                        }
                    }
                    if (counter > 0) {
                        sb.delete(sb.length() - " AND".length(), sb.length());
                    }
                    sb.append(" FOR UPDATE");
                }
                insert_statement = sb.toString();
                if (insert_set_statement != null) {
                    sb.setLength(0);
                    sb.append("INSERT INTO `").append(data_database_name).append("`.`").append(request_table.table_name).append("` (");
                    for (int i = 0; i < insert_fields.size(); i++) {
                        Field f = insert_fields.get(i);
                        if (f.isPrimaryKeyAI() == false) {
                            sb.append(f.getSQLName()).append(",");
                        }
                    }
                    sb.delete(sb.length() - 1, sb.length());
                    sb.append(") ");
                    sb.append(insert_set_statement);
                    insert_set_statement = sb.toString();
                }
                sb.setLength(0);
                if (primary_keys.size() > 0 && insert_fields.size() > 0) {
                    if (hasVariable == true) {
                        //Generate at runtime
                        insert_statement_select = null;
                    } else {
                        sb.append("SELECT ");
                        for (int i = 0; i < tabel_field_list.size(); i++) {
                            Field f = tabel_field_list.get(i);
                            if (f.getTable().equalsIgnoreCase(request_table.table_name) == true) {
                                sb.append(" ").append(f.getSelect(null)).append(",");
                            }
                        }
                        sb.replace(sb.length() - 1, sb.length(), " ");
                        sb.append("FROM `").append(data_database_name).append("`.`").append(request_table.table_name).append("` WHERE");
                        for (int i = 0; i < insert_fields.size(); i++) {
                            Field f = insert_fields.get(i);
                            if (f.isPrimaryKeyAI() == false && f.isPrimaryKeyMI() == false) {
                                sb.append(" ").append(f.getSQLName()).append(" ").append(f.isNullable() ? "<=>?" : "=?").append(i + 1 == insert_fields.size() ? "" : " AND");
                            }
                        }
                        insert_statement_select = sb.toString();
                    }
                }
                if (primary_keys.size() > 0 || uniqueness_fields_all.size() > 0 || uniqueness_fields_any.size() > 0) {
                    if (hasVariable == true) {
                        error_list.add("Uniqueness Statement can't contain variable field");
                    }
                    Boolean hasUniqueness = false;
                    sb.setLength(0);
                    sb.append("SELECT ").append(getFieldsFor(Field.INSERT, true));
                    sb.append(" FROM `").append(data_database_name).append("`.`").append(request_table.table_name).append("` WHERE ");
                    Integer primaryKeyIncrement = 0;
                    if (primary_keys.size() > 0) {
                        for (int i = 0; i < primary_keys.size(); i++) {
                            Field field = primary_keys.get(i);
                            if (field.isPrimaryKeyAI() == true || field.isPrimaryKeyMI() == true) {
                                primaryKeyIncrement++;
                            } else {
                                hasUniqueness = true;
                                sb.append(" ").append(primary_keys.get(i).getSQLName()).append("=?").append(i + 1 == primary_keys.size() ? "" : " AND");
                            }
                        }
                    }
                    if (uniqueness_fields_all.size() > 0) {
                        hasUniqueness = true;
                        if ((primary_keys.size() - primaryKeyIncrement) > 0) {
                            sb.append(" AND ");
                        }
                        for (int i = 0; i < uniqueness_fields_all.size(); i++) {
                            //sb.append(" ").append(uniqueness_fields_all.get(i).getSQLName()).append("<=>?").append(i + 1 == uniqueness_fields_all.size() ? "" : " AND");
                            sb.append(" ").append(uniqueness_fields_all.get(i).getSQLName()).append("=?").append(i + 1 == uniqueness_fields_all.size() ? "" : " AND");
                        }
                    }
                    if (uniqueness_fields_any.size() > 0) {
                        hasUniqueness = true;
                        if ((primary_keys.size() - primaryKeyIncrement) > 0 || uniqueness_fields_all.size() > 0) {
                            sb.append(" AND ");
                        }
                        sb.append("(");
                        for (int i = 0; i < uniqueness_fields_any.size(); i++) {
                            //sb.append(" ").append(uniqueness_fields_any.get(i).getSQLName()).append("<=>?").append(i + 1 == uniqueness_fields_any.size() ? "" : " OR");
                            sb.append(" ").append(uniqueness_fields_any.get(i).getSQLName()).append("=?").append(i + 1 == uniqueness_fields_any.size() ? "" : " OR");
                        }
                        sb.append(")");
                    }
                    uniqueness_insert_statement = hasUniqueness == false ? null : sb.toString();//(sb.length() == 0 ? null : sb.toString());
                }
            }
        }
        if (request_table.transaction_type_list.contains("select") == true) {
            sb.setLength(0);
            if (request_table.transaction_type_list.contains("select") == true && select_fields.size() == 0) {
                error_list.add("no valid select field_list defined");
            } else if (/*select_fields != null && */request_table.transaction_type_list.contains("select") == false) {
                //ignore
            } else {
                sb.setLength(0);
                if (select_statement_from == null) {
                    sb.append("`").append(data_database_name).append("`.`").append(request_table.table_name).append("`").append(" AS `").append(request_table.table_alias).append("`");
                    sb.append(getInnerJoin());
                    select_statement_from = sb.length() == 0 ? null : sb.toString();
                }
                sb.setLength(0);
                if (select_statement_from != null) {
                    sb.append("$SELECT$ FROM ").append(select_statement_from).append(" $WHERE$ $GROUPBY$ $HAVING$ $ORDERBY$");
                } else {
                    sb.append("$SELECT$ FROM `").append(data_database_name).append("`.`").append(request_table.table_name).append("` $WHERE$ $GROUPBY$ $HAVING$ $ORDERBY$");
                }
                select_statement = sb.toString();
            }
        }
        if (request_table.transaction_type_list.contains("update") == true) {
            sb.setLength(0);
            if (update_fields == null && request_table.transaction_type_list.contains("update") == true) {
                error_list.add("no valid update field_list defined");
            } else if (/*update_fields != null && */request_table.transaction_type_list.contains("update") == false) {
                //ignore
            } else {
                /*valid_update_fields = String.join(",", update_fields);
                for (String fieldName : update_fields) {
                    if (tableFields.get(fieldName) == null) {
                        initerror = true;
                        error_list.add("Update Field '" + fieldName + "' is not defined in field_list tableFields");
                    }
                }*/
                //StringBuilder sb = new StringBuilder();
                sb.setLength(0);
                sb.append("UPDATE `").append(data_database_name).append("`.`").append(request_table.table_name).append("` SET $UPDATE$ $WHERE$");
                update_statement = sb.toString();
                if (uniqueness_fields_all.size() > 0 || uniqueness_fields_any.size() > 0) {
                    Boolean hasUniqueness = false;
                    sb.setLength(0);
                    sb.append("SELECT ").append(getFieldsFor(Field.UPDATE, true));
                    /*Make Select shows count as a flag of uniquness
                            Group by field_list to get these uniqueness*/
                    sb.append(" FROM `").append(data_database_name).append("`.`").append(request_table.table_name).append("` WHERE ");
                    if (primary_keys != null) {
                        hasUniqueness = true;
                        for (int i = 0; i < primary_keys.size(); i++) {
                            Field f = primary_keys.get(i);
                            sb.append(" ");
                            if (f.isPrimaryKeyAI() || f.isPrimaryKeyMI()) {
                                sb.append(f.getSQLName()).append("<>?");
                            } else {
                                sb.append(f.getSQLName()).append("=?");
                            }
                            sb.append(i + 1 == primary_keys.size() ? "" : " AND");
                        }
                    }
                    if (uniqueness_fields_all.size() > 0) {
                        hasUniqueness = true;
                        if (primary_keys.size() > 0) {
                            sb.append(" AND ");
                        }
                        for (int i = 0; i < uniqueness_fields_all.size(); i++) {
                            //sb.append(" ").append(uniqueness_fields_all.get(i).getSQLName()).append("<=>?").append(i + 1 == uniqueness_fields_all.size() ? "" : " AND");
                            sb.append(" ").append(uniqueness_fields_all.get(i).getSQLName()).append("=?").append(i + 1 == uniqueness_fields_all.size() ? "" : " AND");
                        }
                    }
                    if (uniqueness_fields_any.size() > 0) {
                        hasUniqueness = true;
                        if (primary_keys.size() > 0 || uniqueness_fields_all.size() > 0) {
                            sb.append(" AND ");
                        }
                        sb.append("(");
                        for (int i = 0; i < uniqueness_fields_any.size(); i++) {
                            //sb.append(" ").append(uniqueness_fields_any.get(i).getSQLName()).append("<=>?").append(i + 1 == uniqueness_fields_any.size() ? "" : " OR");
                            sb.append(" ").append(uniqueness_fields_any.get(i).getSQLName()).append("=?").append(i + 1 == uniqueness_fields_any.size() ? "" : " OR");
                        }
                        sb.append(")");
                    }
                    uniqueness_update_statement = hasUniqueness == false ? null : sb.toString();//(sb.length() == 0 ? null : sb.toString());
                }
            }
        }
        if (request_table.transaction_type_list.contains("delete") == true) {
            sb.setLength(0);
            sb.append("DELETE FROM `").append(data_database_name).append("`.`").append(request_table.table_name).append("` $WHERE$");
            delete_statement = sb.toString();
        }
        
        return error_list.size() > 0;//error_list
    }
    
    public void postInit(JsonArray error_list) {
        this.field_list = new ArrayList<Field>(field_name_map.values());
        Collections.sort(this.field_list);
        if (joinKeys != null) {
            Set<String> joinKeysSet = joinKeys.keySet();
            for (String key : joinKeysSet) {
                JoinKey joinKey = joinKeys.get(key);
                joinKey.prepareJoinStatement();
            }
        }
        if (foreignKeys != null) {
            Set<String> foreignKeysSet = foreignKeys.keySet();
            for (String key : foreignKeysSet) {
                ForeignKey foreignKey = foreignKeys.get(key);
                foreignKey.prepareForeinessValidationStatement();
            }
        }
        if (dependentKeys != null) {
            Set<String> dependentKeysSet = dependentKeys.keySet();
            for (String key : dependentKeysSet) {
                DependentKey dependentKey = dependentKeys.get(key);
                dependentKey.prepareDependencyValidationStatement();
            }
        }
    }
    
    public String getTableName() {
        return request_table.table_name;
    }
    
    public Table getTableTree() {
        return this;
    }
    
    public Field getNamedField(String name) {
        return field_name_map.get(name);
    }
    public Field getAliasedField(String alias) {
        return field_alias_map.get(alias);
    }
    
    public ArrayList<Field> getFields() {
        return field_list;
    }
    
    public HashMap<String, Field> getNamedFieldMap() {
        return field_name_map;
    }
    
    public HashMap<String, Field> getAliasedFieldMap() {
        return field_alias_map;
    }
    
    private void checkDuplicity(Field field, JsonArray table_error_list) {
        for (Field f : field_list) {
            if (request_table.table_name.equalsIgnoreCase(f.getTable()) == true
                    && f.isVariable() == false && f.hasFormulaDefined() == false && f.getName().equalsIgnoreCase(field.getName())) {
                table_error_list.add("Field name '" + f.getTable() + "'.'" + f.getName() + "' is duplicated");
            }
            if (request_table.table_name.equalsIgnoreCase(f.getTable()) == true
                    && f.isVariable() == false && f.hasFormulaDefined() == false && f.getAlias().equalsIgnoreCase(field.getAlias())) {
                table_error_list.add("Field alias '" + f.getTable() + "'.'" + f.getAlias() + "' is duplicated");
            }
        }
    }
        
    public Field addField(String table_name, String table_alias, String field_name, String field_alias, Boolean nullable, Boolean group_by, FieldType field_type, JsonArray table_error_list) {
        Field field = new Field(table_name, table_alias, field_list.size(), field_name, field_alias, nullable, group_by, field_type, table_error_list);
        if (table_error_list.size() > 0) {
            return field;
        }
        checkDuplicity(field, table_error_list);
        field_list.add(field);
        field_name_map.put(field_name, field);
        field_alias_map.put(field_alias, field);
        field_name_alias.put(field_name, field_alias);
        field_alias_name.put(field_alias, field_name);
        return field;
    }
    
    public Boolean hasSelectWhereCondition() {
        return this.select_where_condition != null;
    }
    
    public void addSelectWhereCondition(String[] field_list, String select_where_condition) {
        this.select_where_condition = select_where_condition;
        this.select_where_condition_field_list = new ArrayList<Field>();
        for (String fieldName : field_list) {
            Field field = field_name_map.get(fieldName);
            this.select_where_condition_field_list.add(field);
        }
    }
    
    public String getSelectWhereCondition() {
        return this.select_where_condition;
    }
    
    public ArrayList<Field> getSelectWhereConditionFields() {
        return this.select_where_condition_field_list;
    }
    
    public Boolean hasUpdateWhereCondition() {
        return this.updateWhereCondition != null;
    }
    
    public void addUpdateWhereCondition(String[] field_list, String updateWhereCondition) {
        this.updateWhereCondition = updateWhereCondition;
        this.updateWhereConditionFields = new ArrayList<Field>();
        for (String fieldName : field_list) {
            Field field = field_name_map.get(fieldName);
            this.updateWhereConditionFields.add(field);
        }
    }
    
    public String getUpdateWhereCondition() {
        return this.updateWhereCondition;
    }
    
    public ArrayList<Field> getUpdateWhereConditionFields() {
        return this.updateWhereConditionFields;
    }
    
    private void addForeignKey(String key, String foreign_table_name, String foreign_table_alias) {
        foreignKeys = (foreignKeys != null ? foreignKeys : new HashMap<String, ForeignKey>());
        if (foreignKeys.get(key) == null) {
            foreignKeys.put(key, new ForeignKey(key, data_database_name, foreign_table_name, foreign_table_alias));
        }
    }
    
    public void addForeignField(String key, String table_field_alias, String foreign_table_name, String foreign_table_alias, String foreign_field_alias, JsonArray table_error_list) {
        if (field_alias_map.get(table_field_alias) == null) {
            table_error_list.add("Field alias '" + table_field_alias + "' is not exist in table '" + request_table.table_alias + "'");
            return;
        }
        Field field = field_alias_map.get(table_field_alias);
        addForeignKey(key, foreign_table_name, foreign_table_alias);
        ForeignKey foreignKey = foreignKeys.get(key);
        foreignKey.addForeignField(field, foreign_field_alias);
    }
    
    private void addDependentKey(String key, String dependent_table) {
        dependentKeys = (dependentKeys != null ? dependentKeys : new HashMap<String, DependentKey>());
        if (dependentKeys.get(key) == null) {
            dependentKeys.put(key, new DependentKey(key, data_database_name, dependent_table));
        }
    }
    
    public void addDependentField(String key, String dependent_table, String field_alias, String dependent_field_alias, JsonArray table_error_list) {
        if (field_alias_map.get(field_alias) == null) {
            table_error_list.add("Field alias '" + field_alias + "' is not exist in table '" + request_table.table_name + "'");
            return;
        }
        Field field = field_alias_map.get(field_alias);
        addDependentKey(key, dependent_table);
        DependentKey dependentKey = dependentKeys.get(key);
        dependentKey.addDependentField(field, dependent_field_alias);
    }
    
    public void addJoinSQL(String join_sql) {
        join_sql_list.add(join_sql);
    }
    
    private void addJoinKey(String key, String join_table, String join_table_alias, JoinKey.JoinType join_type) {
        if (joinKeys.get(key) == null) {
            joinKeys.put(key, new JoinKey(key, data_database_name, request_table.table_name, request_table.table_alias, join_table, join_table_alias, join_type));
        }
    }
    
    public void addJoinField(String key, String join_table_name, String join_table_alias, String field_alias, String join_field_name, JoinKey.JoinType join_type, JsonArray table_error_list) {
        if (field_name_map.get(field_alias) == null) {
            table_error_list.add("Field alias '" + field_alias + "' is not exist in table '" + request_table.table_name + "'");
            return;
        }
        Field field = field_name_map.get(field_alias);
        addJoinKey(key, join_table_name, join_table_alias, join_type);
        JoinKey joinKey = joinKeys.get(key);
        joinKey.addJoinField(field, join_field_name);
    }
    
    private void prepareJoin() {
        if (joinKeys == null) {
            inner_join_statement = "";
            return;
        }
        StringBuilder fks = new StringBuilder();
        ArrayList<String> joinKeySet = new ArrayList<String>(joinKeys.keySet());
        for (int i = 0; i < joinKeySet.size(); i++) {
            String join_field = joinKeySet.get(i);
            JoinKey joinKey = joinKeys.get(join_field);
            fks.append(joinKey.getJoinStatement());
        }
        for (String free_join_sql : join_sql_list) {
            fks.append(" ").append(free_join_sql);
        }
        inner_join_statement = fks.toString();
    }
    
    public String getInnerJoin() {
        if (inner_join_statement == null) {
            prepareJoin();
        }
        return inner_join_statement;
    }
    
    public ArrayList<ForeignKey> getForeignKeys() {
        if (foreign_keys_list == null) {
            foreign_keys_list = foreignKeys == null ? new ArrayList<ForeignKey>() : new ArrayList<ForeignKey>(foreignKeys.values());
        }
        return foreign_keys_list;
    }
    
    public ArrayList<DependentKey> getDepenencyKeys() {
        if (dependent_keys_list == null) {
            dependent_keys_list = dependentKeys == null ? new ArrayList<DependentKey>() : new ArrayList<DependentKey>(dependentKeys.values());
        }
        return dependent_keys_list;
    }
    
    public Boolean hasPrimaryKeyAI() {
        if (hasPrimaryKeyAI == null) {
            hasPrimaryKeyAI = false;
            for (Field field : field_list) {
                if (field.isPrimaryKeyAI() == true) {
                    hasPrimaryKeyAI = true;
                    break;
                }
            }
        }
        return hasPrimaryKeyAI;
    }
    
    public Integer countPrimaryKeyAI() {
        if (countPrimaryKeyAI == null) {
            countPrimaryKeyAI = 0;
            for (Field field : field_list) {
                if (field.isPrimaryKeyMI() == true) {
                    countPrimaryKeyAI++;
                }
            }
        }
        return countPrimaryKeyAI;
    }
    
    public Boolean hasPrimaryKeyMI() {
        if (hasPrimaryKeyMI == null) {
            hasPrimaryKeyMI = false;
            for (Field field : field_list) {
                if (field.isPrimaryKeyMI() == true) {
                    hasPrimaryKeyMI = true;
                    break;
                }
            }
        }
        return hasPrimaryKeyMI;
    }
    
    public Integer countPrimaryKeyMI() {
        if (countPrimaryKeyMI == null) {
            countPrimaryKeyMI = 0;
            for (Field field : field_list) {
                if (field.isPrimaryKeyMI() == true) {
                    countPrimaryKeyMI++;
                }
            }
        }
        return countPrimaryKeyMI;
    }
    
    private String createRuntimeInsertStatementSelect(Map<String, String> variables) throws Exception {
        StringBuilder sb = new StringBuilder();
        ArrayList<Field> tabel_fields = getFields();
        sb.append("SELECT ");
        for (int i = 0; i < tabel_fields.size(); i++) {
            Field f = tabel_fields.get(i);
            if (f.getTable().equalsIgnoreCase(request_table.table_name) == true) {
                if (f.isVariable() == false) {
                    sb.append(" ").append(f.getSelect()).append(",");
                } else {
                    sb.append(" ").append(f.getSelect(variables)).append(",");
                }
            }
        }
        sb.replace(sb.length() - 1, sb.length(), " ");
        sb.append("FROM `").append(data_database_name).append("`.`").append(request_table.table_name).append("` WHERE");
        for (int i = 0; i < insert_fields.size(); i++) {
            Field f = insert_fields.get(i);
            if (f.isPrimaryKeyAI() == false && f.isPrimaryKeyMI() == false) {
                sb.append(" ").append(f.getSQLName()).append(" ").append(f.isNullable() ? "<=>?" : "=?").append(i + 1 == insert_fields.size() ? "" : " AND");
            }
        }
        //insert_statement_select = sb.toString();
        insert_statement_select = null;
        return sb.toString();
    }
    
    public void validateInserFields(JsonArray error_list) {
        if (insert_fields == null) {
            error_list.add("Internal System Error, Contact Adminstrator, uninitialized 'insert_fields' is null");
        } else if (valid_insert_fields == null) {
            error_list.add("Internal System Error, Contact Adminstrator, uninitialized 'valid_insert_fields' is null");
        } else if (insert_statement == null) {
            error_list.add("Internal System Error, Contact Adminstrator, uninitialized 'insert_statement' is null");
        }
    }
    
    public void validateSelectFields(JsonArray error_list) {
        if (select_fields == null) {
            error_list.add("Internal System Error, Contact Adminstrator, uninitialized 'select_fields' is null");
        } else if (valid_select_fields == null) {
            error_list.add("Internal System Error, Contact Adminstrator, uninitialized 'valid_select_fields' is null");
        } else if (select_statement == null) {
            error_list.add("Internal System Error, Contact Adminstrator, uninitialized 'select_statement' is null");
        }
    }
    
    public void validateUpdateFields(JsonArray error_list) {
        if (update_fields == null) {
            error_list.add("Internal System Error, Contact Adminstrator, uninitialized 'update_fields' is null");
        } else if (valid_update_fields == null) {
            error_list.add("Internal System Error, Contact Adminstrator, uninitialized 'valid_update_fields' is null");
        } else if (update_statement == null) {
            error_list.add("Internal System Error, Contact Adminstrator, uninitialized 'update_statement' is null");
        }
    }
    
    public void validateDeleteFields(JsonArray error_list) {
        if (delete_statement == null) {
            error_list.add("Internal System Error, Contact Adminstrator, uninitialized 'delete_statement' is null");
        }
    }
    
    protected void defineInsertSet(String insert_set_statement) {
        this.insert_set_statement = insert_set_statement;
        insert_set_statement_fields_name = new ArrayList<String>();
        insert_set_statement_fields_alias = new ArrayList<String>();

        String b = insert_set_statement.substring(7, insert_set_statement.indexOf("FROM"));
        String[] ff = b.split(",");
        for (int i = 0; i < ff.length; i++) {
            String[] fff = ff[i].split("AS");
            insert_set_statement_fields_name.add(fff[0].trim());
            insert_set_statement_fields_alias.add(fff[1].replace("`", "").replace(",", "").trim());
        }
    }
    
    protected void defineSafeUpdate(Boolean safe_update) {
        this.safe_update = safe_update;
    }
    
    public Boolean mustSafeUpdate() {
        return this.safe_update;
    }

    protected void addForeignKey(String key, String table_field_alias, String foreign_table_name, String foreign_table_alias, String foreign_field_alias, JsonArray table_error_list) {
        addForeignField(key, table_field_alias, foreign_table_name, foreign_table_alias, foreign_field_alias, table_error_list);
    }

    protected void addDependentKey(String key, String field_alias, String dependendTable, String dependendField, JsonArray table_error_list) {
        addDependentField(key, dependendTable, field_alias, dependendField, table_error_list);
    }

    protected Table getTable() {
        return this;
    }

    protected Object getNamedFieldObject(String field_name, String string) throws Exception {
        return getNamedField(field_name).getFieldObject(string);
    }
    
    protected Object getAliasedFieldObject(String field_name, String string) throws Exception {
        return getAliasedField(field_name).getFieldObject(string);
    }

    public Boolean validateInsertUniqueness(Connection con, JsonObject json, JsonArray error_list) throws Exception {
        if (uniqueness_insert_statement != null) {
            JsonArray values = json.get("values").getAsJsonArray();
            for (int oo = 0; oo < values.size(); oo++) {
                JsonObject o = values.get(oo).getAsJsonObject();
                try (PreparedStatement preparedInsertedSelectStmt = con.prepareStatement(uniqueness_insert_statement)) {
                    int idx = 0;
                    if (primary_keys.size() > 0) {
                        for (int i = 0; i < primary_keys.size(); i++) {
                            Field f = primary_keys.get(i);
                            if (f.isPrimaryKeyAI() == false && f.isPrimaryKeyMI() == false) {
                                JsonElement fje = o.get(primary_keys.get(i).getAlias());
                                String fieldValue = (/*fje == null || */fje.isJsonNull() ? null : fje.getAsString());
                                preparedInsertedSelectStmt.setObject(++idx, f.getFieldObject(fieldValue));
                            }
                        }
                    }
                    if (uniqueness_fields_all.size() > 0) {
                        for (int i = 0; i < uniqueness_fields_all.size(); i++) {
                            Field f = uniqueness_fields_all.get(i);
                            JsonElement fje = o.get(uniqueness_fields_all.get(i).getAlias());
                            String fieldValue = (fje == null || fje.isJsonNull() ? null : fje.getAsString());
                            preparedInsertedSelectStmt.setObject(++idx, f.getFieldObject(fieldValue));
                        }
                    }
                    if (uniqueness_fields_any.size() > 0) {
                        for (int i = 0; i < uniqueness_fields_any.size(); i++) {
                            Field f = uniqueness_fields_any.get(i);
                            JsonElement fje = o.get(uniqueness_fields_any.get(i).getAlias());
                            String fieldValue = (fje == null || fje.isJsonNull() ? null : fje.getAsString());
                            preparedInsertedSelectStmt.setObject(++idx, f.getFieldObject(fieldValue));
                        }
                    }

                    try (ResultSet resultset = preparedInsertedSelectStmt.executeQuery()) {
                        while (resultset.next()) {
                            StringBuilder sb = new StringBuilder();
                            StringBuilder ssb = new StringBuilder();
                            //primary_keys
                            for (int i = 0; i < primary_keys.size(); i++) {
                                Field f = primary_keys.get(i);
                                ssb.append("'").append(primary_keys.get(i).getAlias()).append("'=").append(f.isText() || f.isDateTime() ? "'" : "")
                                        .append(resultset.getString(primary_keys.get(i).getAlias())).append(f.isText() || f.isDateTime() ? "'" : "").append(",");
                            }
                            if (ssb.length() > 0) {
                                ssb.delete(ssb.length() - 1, ssb.length());
                                error_list.add("Record with [Primary Keys] unique values {" + ssb.toString() + "," + sb.toString() + "} already exists");
                            }
                            ssb.setLength(0);
                            //uniqueness_fields_all
                            sb.setLength(0);
                            for (int i = 0; i < uniqueness_fields_all.size(); i++) {
                                Field f = uniqueness_fields_all.get(i);
                                sb.append("'").append(uniqueness_fields_all.get(i).getAlias()).append("'=").append(f.isText() || f.isDateTime() ? "'" : "")
                                        .append(resultset.getString(uniqueness_fields_all.get(i).getAlias())).append(f.isText() || f.isDateTime() ? "'" : "").append(",");
                            }
                            if (sb.length() > 0) {
                                sb.delete(sb.length() - 1, sb.length());
                                error_list.add("Record with [all] unique values {" + sb.toString() + "} already exists");
                            }
                            //uniqueness_fields_any
                            sb.setLength(0);
                            for (int i = 0; i < uniqueness_fields_any.size(); i++) {
                                Field f = uniqueness_fields_any.get(i);
                                sb.append("'").append(uniqueness_fields_any.get(i).getAlias()).append("'=").append(f.isText() || f.isDateTime() ? "'" : "")
                                        .append(resultset.getString(uniqueness_fields_any.get(i).getAlias())).append(f.isText() || f.isDateTime() ? "'" : "").append(",");
                            }
                            if (sb.length() > 0) {
                                sb.delete(sb.length() - 1, sb.length());
                                error_list.add("Record with [any] unique values {" + sb.toString() + "} already exists");
                            }
                        }
                        resultset.close();
                    } catch (Exception sqlx) {
                        throw sqlx;
                    }
                    preparedInsertedSelectStmt.close();
                } catch (Exception sqlx) {
                    throw sqlx;
                }
            }
        }
        return error_list.size() == 0;
    }

    public Boolean validateUpdateUniqueness(Connection con, JsonObject json, JsonArray error_list) throws Exception {
        if (uniqueness_update_statement != null) {
            JsonArray values = json.get("values").getAsJsonArray();
            for (int oo = 0; oo < values.size(); oo++) {
                JsonObject o = values.get(oo).getAsJsonObject();
                StringBuilder mpk = null; 
                //Get the where clause must include the primary key in where clause else it is not safe
                //Validate Primary key field in Where Field array list
                JsonObject where = JsonUtil.getJsonObject(json, "where", false);
                JsonArray where_value_list = JsonUtil.getJsonArray(where, "values", true);
                JsonArray where_field_list = JsonUtil.getJsonArray(where, "field_list", true);
                if (where_field_list != null) {
                    for (int i = 0; i < primary_keys.size(); i++) {
                        Field field = primary_keys.get(i);
                        if (o.get(field.getAlias()) == null) {
                            if (mpk == null) {
                                mpk = new StringBuilder();
                                mpk.append("Safe Update can't continue, missing Primary Key {");
                            }
                            mpk.append(field.getAlias()).append(",");
                        }
                    }
                }
                if (mpk != null) {
                    mpk.delete(mpk.length() - 1, mpk.length());
                    mpk.append("}, check in group index[").append(oo + 1).append("]");
                    error_list.add(mpk.toString());
                    return false;
                }
                try (PreparedStatement preparedInsertedSelectStmt = con.prepareStatement(uniqueness_update_statement)) {
                    int idx = 0;
                    if (primary_keys.size() > 0) {
                        for (int i = 0; i < primary_keys.size(); i++) {
                            Field f = primary_keys.get(i);
                            JsonElement fje = o.get(primary_keys.get(i).getAlias());
                            String fieldValue = (/*fje == null || */fje.isJsonNull() ? null : fje.getAsString());
                            preparedInsertedSelectStmt.setObject(++idx, f.getFieldObject(fieldValue));
                        }
                    }
                    if (uniqueness_fields_all.size() > 0) {
                        for (int i = 0; i < uniqueness_fields_all.size(); i++) {
                            Field f = uniqueness_fields_all.get(i);
                            JsonElement fje = o.get(uniqueness_fields_all.get(i).getAlias());
                            String fieldValue = (fje == null || fje.isJsonNull() ? null : fje.getAsString());
                            preparedInsertedSelectStmt.setObject(++idx, f.getFieldObject(fieldValue));
                        }
                    }
                    if (uniqueness_fields_any.size() > 0) {
                        for (int i = 0; i < uniqueness_fields_any.size(); i++) {
                            Field f = uniqueness_fields_any.get(i);
                            JsonElement fje = o.get(uniqueness_fields_any.get(i).getAlias());
                            String fieldValue = (fje == null || fje.isJsonNull() ? null : fje.getAsString());
                            preparedInsertedSelectStmt.setObject(++idx, f.getFieldObject(fieldValue));
                        }
                    }

                    try (ResultSet resultset = preparedInsertedSelectStmt.executeQuery()) {
                        while (resultset.next()) {
                            StringBuilder sb = new StringBuilder();
                            StringBuilder ssb = new StringBuilder();
                            //primary_keys
                            for (int i = 0; i < primary_keys.size(); i++) {
                                Field f = primary_keys.get(i);
                                ssb.append("'").append(primary_keys.get(i).getAlias()).append("'=").append(f.isText() || f.isDateTime() ? "'" : "")
                                        .append(resultset.getString(primary_keys.get(i).getAlias())).append(f.isText() || f.isDateTime() ? "'" : "").append(",");
                            }
                            ssb = (ssb.length() > 1 ? ssb.delete(ssb.length() - 1, ssb.length()) : ssb);
                            if (ssb.length() > 0) {
                                ssb.delete(ssb.length() - 1, ssb.length());
                                error_list.add("Record with [Primary Keys] unique values {" + ssb.toString() + "," + sb.toString() + "} already exists");
                            }
                            ssb.setLength(0);
                            //uniqueness_fields_all
                            sb.setLength(0);
                            for (int i = 0; i < uniqueness_fields_all.size(); i++) {
                                Field f = uniqueness_fields_all.get(i);
                                sb.append("'").append(uniqueness_fields_all.get(i).getAlias()).append("'=").append(f.isText() || f.isDateTime() ? "'" : "")
                                        .append(resultset.getString(uniqueness_fields_all.get(i).getAlias())).append(f.isText() || f.isDateTime() ? "'" : "").append(",");
                            }
                            if (sb.length() > 0) {
                                sb.delete(sb.length() - 1, sb.length());
                                error_list.add("Record with [all] unique values {" + sb.toString() + "} already exists");
                            }
                            //uniqueness_fields_any
                            sb.setLength(0);
                            for (int i = 0; i < uniqueness_fields_any.size(); i++) {
                                Field f = uniqueness_fields_any.get(i);
                                sb.append("'").append(uniqueness_fields_any.get(i).getAlias()).append("'=").append(f.isText() || f.isDateTime() ? "'" : "")
                                        .append(resultset.getString(uniqueness_fields_any.get(i).getAlias())).append(f.isText() || f.isDateTime() ? "'" : "").append(",");
                            }
                            if (sb.length() > 0) {
                                sb.delete(sb.length() - 1, sb.length());
                                error_list.add("Record with [any] unique values {" + sb.toString() + "} already exists");
                            }
                        }
                        resultset.close();
                    } catch (Exception sqlx) {
                        throw sqlx;
                    }
                    preparedInsertedSelectStmt.close();
                } catch (Exception sqlx) {
                    throw sqlx;
                }
            }
        }
        return error_list.size() == 0;
    }

    public Boolean validateInsertForeignness(Connection con, JsonObject json, JsonArray error_list) throws Exception {
        ArrayList<ForeignKey> foreignKeys = getForeignKeys();
        if (foreignKeys.size() == 0) {//no need to check
            return true;
        }
        JsonArray values = json.get("values").getAsJsonArray();
        for (ForeignKey foreignKey : foreignKeys) {
            ArrayList<Field> field_list = foreignKey.getFields();
            ArrayList<Integer> nullableRecords = new ArrayList<Integer>();
            //validate nullability
            for (int oo = 0; oo < values.size(); oo++) {
                JsonObject o = values.get(oo).getAsJsonObject();
                Integer nullables = 0;
                for (int i = 0; i < field_list.size(); i++) {
                    Field f = field_list.get(i);
                    JsonElement fje = o.get(f.getAlias());
                    String fieldValue = (fje == null || fje.isJsonNull() ? null : fje.getAsString());
                    if (f.isNullable() == true && fieldValue == null) {
                        nullables++;
                    }
                }
                nullableRecords.add(nullables);
                if (nullables > 0 && nullables != field_list.size()) {
                    error_list.add("Foreign Key '" + foreignKey.getKey() + "' for Table '" + foreignKey.getForeignTableName() + "' has mixed keys {null} values check index [" + oo + "], either or null or all non-null}");
                }
            }
            for (int oo = 0; error_list.size() == 0 && oo < values.size(); oo++) {//error_list.size() == 0 to validate all records
                if (nullableRecords.get(oo) > 0) {//If foreign key is null no need to check;
                    continue;
                }
                //continue with valid nullability
                JsonObject o = values.get(oo).getAsJsonObject();
                String foreignness_statement = foreignKey.getForeinessValidationStatement();
                try (PreparedStatement preparedInsertedSelectStmt = con.prepareStatement(foreignness_statement)) {
                    int idx = 0;
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < field_list.size(); i++) {
                        Field f = field_list.get(i);
                        JsonElement fje = o.get(f.getAlias());
                        String fieldValue = (fje == null || fje.isJsonNull() ? null : fje.getAsString());
                        preparedInsertedSelectStmt.setObject(++idx, f.getFieldObject(fieldValue));
                        sb.append("`").append(foreignKey.getForeignTableAlias()).append("`.`").append(foreignKey.getForeignField(f)).append("`=").append(fieldValue).append(",");
                    }
                    sb.delete(sb.length() - 1, sb.length());
                    String s = sb.toString();
                    try (ResultSet resultset = preparedInsertedSelectStmt.executeQuery()) {
                        if (resultset.next() == false) {
                            error_list.add("Table '" + foreignKey.getForeignTableAlias() + "' doesn't have record with {" + s + "}");
                        }
                        resultset.close();
                    } catch (Exception sqlx) {
                        throw sqlx;
                    }
                    preparedInsertedSelectStmt.close();
                } catch (Exception sqlx) {
                    throw sqlx;
                }
            }
        }
        return error_list.size() == 0;
    }

    public Boolean validateUpdateForeignness(Connection con, JsonObject json, JsonArray error_list) throws Exception {
        ArrayList<ForeignKey> foreignKeys = getForeignKeys();
        if (foreignKeys.size() > 0) {
            JsonArray values = json.get("values").getAsJsonArray();
            for (ForeignKey foreignKey : foreignKeys) {
                ArrayList<Field> field_list = foreignKey.getFields();
                Integer checkForeignKeyUpdate = field_list.size();
                StringBuilder sb = new StringBuilder();
                String foreignness_statement = foreignKey.getForeinessValidationStatement();
                Boolean skipForeignKeyCheck = false;
                for (int oo = 0; oo < 1 /*values.size()*/; oo++) {
                    JsonObject o = values.get(oo).getAsJsonObject();
                    for (int i = 0; i < field_list.size(); i++) {
                        Field f = field_list.get(i);
                        JsonElement je = o.get(f.getAlias());
                        if (je == null) {
                            sb.append(f.getAlias()).append(",");
                        }
                        checkForeignKeyUpdate += (je == null ? 0 : -1);
                    }
                    sb = (sb.length() == 0 ? sb : sb.delete(sb.length() - 1, sb.length()));
                    if (checkForeignKeyUpdate == field_list.size()) {
                        skipForeignKeyCheck = true;
                    } else if (checkForeignKeyUpdate != 0) {//some field_list exists but some others doesn't
                        error_list.add("Table '" + foreignKey.getForeignTableAlias() + "' missing '" + (field_list.size() - checkForeignKeyUpdate) + "' foreign keys '" + foreignKey.getKey() + "' keys {" + sb.toString() + "}");
                        return false;
                    }
                }
                if (skipForeignKeyCheck == true) {
                    continue;
                }
                for (int oo = 0; oo < values.size(); oo++) {
                    JsonObject o = values.get(oo).getAsJsonObject();
                    try (PreparedStatement preparedInsertedSelectStmt = con.prepareStatement(foreignness_statement)) {
                        int idx = 0;
                        sb.setLength(0);
                        for (int i = 0; i < field_list.size(); i++) {
                            Field f = field_list.get(i);
                            JsonElement fje = o.get(f.getAlias());
                            String fieldValue = (fje == null || fje.isJsonNull() ? null : fje.getAsString());
                            preparedInsertedSelectStmt.setObject(++idx, f.getFieldObject(fieldValue));
                            sb.append("`").append(foreignKey.getForeignTableAlias()).append("`.`").append(foreignKey.getForeignField(f)).append("`=").append(fieldValue).append(",");
                        }
                        sb.delete(sb.length() - 1, sb.length());
                        String s = sb.toString();
                        try (ResultSet resultset = preparedInsertedSelectStmt.executeQuery()) {
                            if (resultset.next() == false) {
                                error_list.add("Table '" + foreignKey.getForeignTableAlias() + "' doesn't have record with {" + s + "}");
                            }
                            resultset.close();
                        } catch (Exception sqlx) {
                            throw sqlx;
                        }
                        preparedInsertedSelectStmt.close();
                    } catch (Exception sqlx) {
                        throw sqlx;
                    }
                }
            }
        }
        return error_list.size() == 0;
    }
    
    protected Boolean getBoolean(ResultSet resultset, String field) throws Exception {
        return resultset.getObject(field) == null ? null : resultset.getBoolean(field);
    }

    protected Integer getInt(ResultSet resultset, String field) throws Exception {
        return resultset.getObject(field) == null ? null : resultset.getInt(field);
    }

    protected Long getLong(ResultSet resultset, String field) throws Exception {
        return resultset.getObject(field) == null ? null : resultset.getLong(field);
    }

    protected Double getDouble(ResultSet resultset, String field) throws Exception {
        return resultset.getObject(field) == null ? null : resultset.getDouble(field);
    }

    protected Date getDate(ResultSet resultset, String field) throws Exception {
        return resultset.getObject(field) == null ? null : resultset.getDate(field);
    }

    protected Time getTime(ResultSet resultset, String field) throws Exception {
        return resultset.getObject(field) == null ? null : resultset.getTime(field);
    }

    protected Timestamp getTimestamp(ResultSet resultset, String field) throws Exception {
        return resultset.getObject(field) == null ? null : resultset.getTimestamp(field);
    }

    protected String getString(ResultSet resultset, String field) throws Exception {
        return resultset.getObject(field) == null ? null : resultset.getString(field);
    }

    private ServiceField isValidServiceField(RecordProcessor record_processor, String serviceField, Map<String, Field> record) {
        int pos1 = serviceField.indexOf(" AS ");
        if (pos1 == -1) {
            return null;
        }
        int pos2 = serviceField.indexOf(" ", pos1 + 5);
        if (pos2 == -1) {
            return null;
        }
        String formula = serviceField.substring(0, pos1);
        String type = serviceField.substring(pos1 + 4, pos2);
        String alias = serviceField.substring(pos2 + 1);
        if (record.containsKey(alias) == true) {
            record_processor.addError("Service Field '" + serviceField + "' has alias '" + alias + "' that is already defined as Table Field");
            return null;
        }
        return new ServiceField(formula, type, alias, record_processor.error_list);
    }

    protected boolean areValidSelectFields(RecordProcessor record_processor, List<ServiceField> serviceFields, Map<String, Field> record, List<Field> field_list) {
        List<String> field = record_processor.request.select_list;
        /*for (int i = 0; i < field.size(); i++) {
            Field f = record.get(field.get(i));
            if (f != null && f.isAllowedTo(Field.SELECT) == false) {
                record_processor.addError("Field '" + field.get(i) + "' is not allowed in select operation");
            } else if (f == null || field_list.contains(f) == false) {
                ServiceField sf = isValidServiceField(record_processor, field.get(i), record);
                if (sf != null) {
                    serviceFields.add(sf);
                    field.remove(i--);
                } else {
                    record_processor.addError("Field '" + field.get(i) + "' is not a valid field name");
                }
            }
        }*/
        //Check Mandatory Fields
        for (int i = 0; i < select_fields.size(); i++) {
            Field f = select_fields.get(i);
            if (f.isMandatoryFor(Field.SELECT) == true) {
                Boolean found = false;
                for (int x = 0; x < field.size(); x++) {
                    if (f.getAlias().equalsIgnoreCase(field.get(x))) {
                        found = true;
                        break;
                    }
                }
                if (found == false) {
                    if (f.hasDefaultValueFor(Field.SELECT) == false) {
                        record_processor.addError("Field '" + f.getAlias() + "' is mandatory to be exist in select list");
                    } else if (f.hasDefaultValueFor(Field.SELECT) == true) {
                        //ignore, put default value into where 
                        //this field is requested ie. where statement
                    }
                }
            }
        }
        return !record_processor.hasErrors();
    }

    protected boolean areValidUpdateFieldsValues(RecordProcessor record_processor, Map<String, Field> field_name_map, JsonArray recordset, List<String> conditional_field_list, ArrayList<Field> field_list, JsonArray error_list) throws Exception {
        StringBuilder error = new StringBuilder();
        for (int i = 0; i < recordset.size(); i++) {
            JsonObject record = recordset.get(i).getAsJsonObject();
            for (String fieldName : record.keySet()) {
                Field f = field_name_map.get(fieldName);
                if (f != null && f.isPrimaryKey() == false && f.isAllowedTo(Field.UPDATE) == false) {
                    record_processor.addError("Field '" + fieldName + "' is not allowed in update operation");
                } else if (f == null && conditional_field_list != null && conditional_field_list.contains(fieldName) == false) {
                    record_processor.addError("Field '" + fieldName + "' is not a valid condition field name, check record[" + (i + 1) + "]");
                } else if (conditional_field_list == null && f == null) {
                    record_processor.addError("Field '" + fieldName + "' is not a valid field name, check record[" + (i + 1) + "]");
                } else if (conditional_field_list == null && field_list.contains(f) == false) {
                    record_processor.addError("Field '" + fieldName + "' is not allowed field to be updated, check record[" + (i + 1) + "]");
                } else if (conditional_field_list == null && record.get(fieldName) != null && f != null && f.isNullable() == false && record.get(fieldName).isJsonNull() == true) {
                    record_processor.addError("Field '" + fieldName + "' doesn't accept null values, check record[" + (i + 1) + "]");
                } else if (conditional_field_list == null && f.isValid(Field.UPDATE, (record.get(fieldName) == null || record.get(fieldName).isJsonNull() ? null : record.get(fieldName).getAsString()), error) == false) {
                    record_processor.addError(error.toString() + ", check record[" + (i + 1) + "]");
                }
            }
            for (Field f : field_name_map.values()) {
                String field_alias = f.getAlias();
                if (record.has(field_alias) == true) {
                    continue;
                }
                if (f.hasDefaultValueFor(Field.UPDATE) == true) {
                    //defaultValue for Update
                    JsonElement fje = record.get(f.getAlias());
                    if (fje == null || fje.isJsonNull() == true) {
                        record.addProperty(f.getAlias(), f.getDefaultSQLValueFor(Field.UPDATE, error_list));
                    }
                }
            }
            for (int x = 0; conditional_field_list != null && x < conditional_field_list.size(); x++) {
                String fieldName = conditional_field_list.get(x);
                if (record.get(fieldName) == null) {
                    record_processor.addError("Field '" + fieldName + "', in {where.field_list} doesn't exist in values record, check record[" + (i + 1) + "]");
                }
            }
        }
        return !record_processor.hasErrors();
    }

    final static public Boolean isValidClause(RecordProcessor record_processor, Integer operation, String ii, Map<String, Field> r, List<Field> wf, List<String> vv, StringBuilder wc, StringBuilder hc, List<Argument> wa, List<Argument> ha) throws Exception {
        ArrayList<String> keyword = new ArrayList<>(Arrays.asList(new String[]{"asc", "desc", "between", "in", "like", "and", "or", "is", "null"}));
        //String symbol = "<=>(?,)";
        String symbol = "/*-+(?)'[]|<=>,;:\\\r\n\t ";
        String ignore = "\r\n\t ";
        Character sc = '\'';
        Character esc = '\'';//\"';
        Character escr = null;
        StringBuilder b = new StringBuilder(), e = new StringBuilder();
        ArrayList<String> mm = new ArrayList<String>();
        boolean s = false, p = false, h = false;
        char c, pc, nc;
        c = pc = nc = '\0';
        int l = ii.length();
        for (int x = 0; x < l; x++) {
            c = ii.charAt(x);
            nc = x == l - 1 ? '\0' : ii.charAt(x + 1);
            if (s == false && (symbol.indexOf(c) > -1)) {
                if (b.length() > 0) {
                    mm.add(b.toString());
                    b.delete(0, b.length());
                }
                if (ignore.indexOf(c) > -1) {
                    b.setLength(0);
                    continue;
                }
                if (symbol.indexOf(c) > -1) {
                    if (b.length() > 0) {
                        mm.add(b.toString());
                        b.setLength(0);
                    }
                    mm.add(String.valueOf(c));
                }
                if (s == false && c == sc) {
                    s = true;
                }
            } else if (s == true && c == esc && nc == sc) {
                if (escr == null) {
                    b.append(esc).append(sc);
                } else {
                    b.append(escr);
                }
                x++;
            } else if (s == true
                    && c == sc) {
                if (b.length() > 0) {
                    mm.add(b.toString());
                    b.setLength(0);
                }
                mm.add(String.valueOf(c));
                s = false;
            } else {
                b.append(c);
            }
            pc = c;
        }
        if (b.length() > 0) {
            mm.add(b.toString());
            b.delete(0, b.length());
        }
        b.setLength(0);

        int ml = mm.size(), vi = -1;
        String sm = "";
        for (int i = 0; i < ml; i++) {
            sm = mm.get(i);
            if (r.get(sm) != null) {
                /*if (vf.contains(sm) == false) {
                    record_processor.addError("Field '" + sm + "' is not allowed to be in where clause");
                    return false;
                }*/
                Field f = r.get(sm);
                wf.add(f);
                h = f.isGroup();
                (h ? ha : wa).add(new Argument(f));
                (h ? hc : wc).append(h ? f.getHaving() : f.getSQLName()).append(" ");
                sm = mm.get(++i);
                if (sm == null) {
                    record_processor.addError("Clause statement is incomplete");
                    return false;
                }
                if (sm.length() == 1 && "<=>".indexOf(sm) > -1) {
                    if (mm.get(i - 1).equals("?") == false && r.get(mm.get(i - 1)) == null) {
                        record_processor.addError("Invalid Mathematical operator");
                        return false;
                    } else if (sm.equals("<") && "?=>".indexOf(mm.get(i + 1)) > -1) {
                        (h ? hc : wc).append(sm);
                        if (mm.get(i + 1).equals("?")) {
                            ++vi;
                            if (vv != null && vi >= vv.size()) {
                                record_processor.addError("Caluse Paramerter value index is [" + (vi + 1) + "] while value list is only [" + vv.size() + "] items");
                            } else if (vv != null && f.isValid(operation, vv.get(vi), e) == false) {
                                record_processor.addError(e.toString());
                                return false;
                            }
                            if (vv != null) {//////Needs refactoring
                                (h ? ha : wa).get((h ? ha : wa).size() - 1).addValue(vv.get(vi));
                            }
                            sm = mm.get(++i);
                            (h ? hc : wc).append(sm).append(i + 1 == ml ? "" : "'(.".indexOf(sm) > -1 || "'.,()".indexOf(mm.get(i + 1)) > -1 ? "" : "<>".indexOf(sm) > -1 && ">=".indexOf(mm.get(i + 1)) > -1 ? "" : " ");
                        } else if ("=>".indexOf(mm.get(i + 1)) > -1) {
                            ++i;
                            if (mm.get(i + 1).equals("?") == true) {
                                ++i;
                                ++vi;
                                if (vv != null && vi >= vv.size()) {
                                    record_processor.addError("Caluse Paramerter value index is [" + (vi + 1) + "] while value list is only [" + vv.size() + "] items");
                                } else if (vv != null && f.isValid(operation, vv.get(vi), e) == false) {
                                    record_processor.addError(e.toString());
                                    return false;
                                }
                                if (vv != null) {//////Needs refactoring
                                    (h ? ha : wa).get((h ? ha : wa).size() - 1).addValue(vv.get(vi));
                                }
                                //(h ? hc : wc).append("= ? ");
                                (h ? hc : wc).append(mm.get(i-1)).append(" ? ");
                            } else if (r.get(mm.get(i + 1)) != null) {
                                /*if (vf.contains(mm.get(i+1)) == false) {
                                    record_processor.addError("Field '" + mm.get(i+1) + "' is not allowed to be in where clause");
                                    return false;
                                }*/
                                Field fl = r.get(mm.get(i + 1));
                                wf.add(fl);
                                h = fl.isGroup();
                                //(h ? hc : wc).append(sm).append("=").append(h ? fl.getHaving() : fl.getSQLName()).append(" ");
                                (h ? hc : wc).append(sm).append(mm.get(i)).append(h ? fl.getHaving() : fl.getSQLName()).append(" ");
                                ++i;
                            } else {
                                record_processor.addError("Invalid Mathematical operator");
                                return false;
                            }
                        }
                    } else if (sm.equals(">") && "?=".indexOf(mm.get(i + 1)) > -1) {
                        (h ? hc : wc).append(sm);
                        if (mm.get(i + 1).equals("?")) {
                            ++vi;
                            if (vv != null && vi >= vv.size()) {
                                record_processor.addError("Caluse Paramerter value index is [" + (vi + 1) + "] while value list is only [" + vv.size() + "] items");
                            } else if (vv != null && f.isValid(operation, vv.get(vi), e) == false) {
                                record_processor.addError(e.toString());
                                return false;
                            }
                            if (vv != null) {//////Needs refactoring
                                (h ? ha : wa).get((h ? ha : wa).size() - 1).addValue(vv.get(vi));
                            }
                            ++i;
                            //(h?hc:wc).append("?");
                            (h ? hc : wc).append(f.hasOverwriteWhereCondition() == true ? f.getOverwriteWhereCondition() : "?");
                        } else if ("=".indexOf(mm.get(i + 1)) > -1) {
                            ++i;
                            if (mm.get(i + 1).equals("?") == true) {
                                ++i;
                                ++vi;
                                if (vv != null && vi >= vv.size()) {
                                    record_processor.addError("Caluse Paramerter value index is [" + (vi + 1) + "] while value list is only [" + vv.size() + "] items");
                                } else if (vv != null && f.isValid(operation, vv.get(vi), e) == false) {
                                    record_processor.addError(e.toString());
                                    return false;
                                }
                                if (vv != null) {//////Needs refactoring
                                    (h ? ha : wa).get((h ? ha : wa).size() - 1).addValue(vv.get(vi));
                                }
                                (h ? hc : wc).append("= ? ");
                            } else if (r.get(mm.get(i + 1)) != null) {
                                /*if (vf.contains(mm.get(i+1)) == false) {
                                    record_processor.addError("Field '" + mm.get(i+1) + "' is not allowed to be in where clause");
                                    return false;
                                }*/
                                Field fl = r.get(mm.get(i + 1));
                                wf.add(fl);
                                h = fl.isGroup();
                                ++i;
                                (h ? hc : wc).append(sm).append("=").append(h ? fl.getHaving() : fl.getSQLName()).append(" ");
                                ++i;
                            } else {
                                record_processor.addError("Invalid Mathematical operator");
                                return false;
                            }
                        }
                    } else if (sm.equals("=") && "?".indexOf(mm.get(i + 1)) > -1) {
                        ++i;
                        ++vi;
                        if (vv != null && vi >= vv.size()) {
                            record_processor.addError("Caluse Paramerter value index is [" + (vi + 1) + "] while value list is only [" + vv.size() + "] items");
                        } else if (vv != null && f.isValid(operation, vv.get(vi), e) == false) {
                            record_processor.addError(e.toString());
                            return false;
                        }
                        if (vv != null) {//////Needs refactoring
                            (h ? ha : wa).get((h ? ha : wa).size() - 1).addValue(vv.get(vi));
                        }
                        //IFNULLABLE<=>IFNULL(DATE, -1)=IFNULL(?, -1)
                        //(h?hc:wc).append("<=>").append("? ");
                        (h ? hc : wc).append("<=>").append(f.hasOverwriteWhereCondition() == true ? f.getOverwriteWhereCondition() : "?").append(" ");
                    }
                } else if (sm.equalsIgnoreCase("in")) {
                    (h ? hc : wc).append(sm).append(i + 1 == ml ? "" : "'(.".indexOf(sm) > -1 || "'.,()".indexOf(mm.get(i + 1)) > -1 ? "" : "<>".indexOf(sm) > -1 && ">=".indexOf(mm.get(i + 1)) > -1 ? "" : " ");
                    if (mm.get(i - 1).equalsIgnoreCase("not") == false && r.get(mm.get(i - 1)) == null
                            && mm.get(i + 1).equals("(") == false) {
                        record_processor.addError("Invalid IN operator");
                        return false;
                    }
                    sm = mm.get(++i);
                    (h ? hc : wc).append(sm).append(i + 1 == ml ? "" : "'(.".indexOf(sm) > -1 || "'.,()".indexOf(mm.get(i + 1)) > -1 ? "" : "<>".indexOf(sm) > -1 && ">=".indexOf(mm.get(i + 1)) > -1 ? "" : " ");
                    while ((sm = mm.get(++i)).equals(")") == false) {
                        String token = "?";
                        if (mm.get(i).equalsIgnoreCase(token) == false) {
                            record_processor.addError("Invalid IN operator");
                            return false;
                        }
                        if (mm.get(i+1).equals(")") == false) {
                            token = token+",";
                        }
                            
                                ++vi;
                                if (vv != null && vi >= vv.size()) {
                                    record_processor.addError("Caluse Paramerter value index is [" + (vi + 1) + "] while value list is only [" + vv.size() + "] items");
                                } else if (vv != null && f.isValid(operation, vv.get(vi), e) == false) {
                                    record_processor.addError(e.toString());
                                    return false;
                                }
                                if (vv != null) {//////Needs refactoring
                                    (h ? ha : wa).get((h ? ha : wa).size() - 1).addValue(vv.get(vi));
                                }
                                //sm = mm.get(++i);
                                //(h ? hc : wc).append(sm).append(i + 1 == ml ? "" : "'(.".indexOf(sm) > -1 || "'.,()".indexOf(mm.get(i + 1)) > -1 ? "" : "<>".indexOf(sm) > -1 && ">=".indexOf(mm.get(i + 1)) > -1 ? "" : " ");
                            
                        
                        //Special Append
                        (h ? hc : wc).append(token).append(i + 1 == ml ? "" : "'(.".indexOf(sm) > -1 || "'.,()".indexOf(mm.get(i + 1)) > -1 ? "" : "<>".indexOf(sm) > -1 && ">=".indexOf(mm.get(i + 1)) > -1 ? "" : " ");
                        if (mm.get(i+1).equals(")") == false) {
                            i++;
                        }
                    }
                    (h ? hc : wc).append(sm).append(i + 1 == ml ? "" : "'(.".indexOf(sm) > -1 || "'.,()".indexOf(mm.get(i + 1)) > -1 ? "" : "<>".indexOf(sm) > -1 && ">=".indexOf(mm.get(i + 1)) > -1 ? "" : " ");
                } else if (sm.equalsIgnoreCase("like")) {
                    (h ? hc : wc).append(sm).append(" ");
                    if (mm.get(i + 1).equals("?") == true) {
                        ++i;
                        ++vi;
                        if (vv != null && vi >= vv.size()) {
                            record_processor.addError("Caluse Paramerter value index is [" + (vi + 1) + "] while value list is only [" + vv.size() + "] items");
                        }/* else if (vi > v.length || f.isValid(v[vi]) == false) {
                            record_processor.addError("Field '" + f.getAlias() + "' value '" + v[vi] + "' is not valid " + f.ft.toString() + " data type");
                            return false;
                        }*/
                        if (vv != null) {//////Needs refactoring
                            (h ? ha : wa).get((h ? ha : wa).size() - 1).addValue(vv.get(vi));
                        }
                        (h ? hc : wc).append(" ? ");
                    } else {
                        record_processor.addError("Invalid Mathematical operator");
                        return false;
                    }
                } else if (sm.equalsIgnoreCase("between")) {
                    (h ? hc : wc).append(sm).append(" ");
                    if (mm.get(i + 1).equals("?") == true) {
                        //(h?hc:wc).append("? ");
                        (h ? hc : wc).append(f.hasOverwriteWhereCondition() == true ? f.getOverwriteWhereCondition() : "?").append(" ");
                        ++i;
                        ++vi;
                        if (vv != null && vi >= vv.size()) {
                            record_processor.addError("Caluse Paramerter value index is [" + (vi + 1) + "] while value list is only [" + vv.size() + "] items");
                        } else if (vv != null && f.isValid(operation, vv.get(vi), e) == false) {
                            record_processor.addError(e.toString());
                            return false;
                        }
                        if (mm.get(i + 1).equalsIgnoreCase("and") == false || mm.get(i + 2).equals("?") == false) {
                            record_processor.addError("Invalid Between operator");
                            return false;
                        }
                        if (vv != null) {//////Needs refactoring
                            (h ? ha : wa).get((h ? ha : wa).size() - 1).addValue(vv.get(vi));
                        }
                        ++i;
                        ++vi;
                        if (vv != null && vi >= vv.size()) {
                            record_processor.addError("Caluse Paramerter value index is [" + (vi + 1) + "] while value list is only [" + vv.size() + "] items");
                        } else if (vv != null && f.isValid(operation, vv.get(vi), e) == false) {
                            record_processor.addError(e.toString());
                            return false;
                        }
                        if (vv != null) {//////Needs refactoring
                            (h ? ha : wa).get((h ? ha : wa).size() - 1).addValue(vv.get(vi));
                        }
                        ++i;
                        (h ? hc : wc).append("AND ? ");
                    } else {
                        record_processor.addError("Invalid Between operator");
                        return false;
                    }
                } else if (sm.equalsIgnoreCase("is")) {
                    if (mm.get(i + 1).equalsIgnoreCase("null")) {
                        ++i;
                        (h ? hc : wc).append("IS NULL");
                    } else if (mm.get(i + 1).equalsIgnoreCase("not")
                            && mm.get(i + 2).equalsIgnoreCase("null")) {
                        i++;
                        ++i;
                        (h ? hc : wc).append("IS NOT NULL");
                    } else {
                        record_processor.addError("Invalid IS Operator");
                        return false;
                    }
                } else {
                    record_processor.addError("Invalid Where clause");
                    return false;
                }
            } else if (sm.equalsIgnoreCase("and") || sm.equalsIgnoreCase("or") || sm.equals("(") || sm.equals(")") || sm.equalsIgnoreCase("not")) {
                (h ? hc : wc).append(" ").append(sm).append(" ");
            } else if (r.get(sm) == null) {
                record_processor.addError("Field '" + sm + "' is not a valid field name");
            } else {
                record_processor.addError("Invalid Where clause '" + sm + "'");
                return false;
            }
        }

        return !record_processor.hasErrors();
    }

    public boolean areValidInsertValueFields(RecordProcessor record_processor, HashMap<String, Field> record, JsonArray values, ArrayList<Field> field_list) throws Exception {
        StringBuilder error = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            JsonObject o = values.get(i).getAsJsonObject();
            for (String fieldName : o.keySet()) {
                if (fieldName.equalsIgnoreCase("tuid") == true) {
                    continue;
                }
                JsonElement fje = o.get(fieldName);
                String fieldValue = (fje.isJsonNull() ? null : fje.getAsString());
                Field f = record.get(fieldName);
                if (f != null && f.isAllowedTo(Field.INSERT) == false) {
                    record_processor.addError("Field '" + fieldName + "' is not allowed in insert operation");
                } else if (f == null || field_list.contains(f) == false) {
                    record_processor.addError("Field '" + fieldName + "' is not a valid field name for insert operation");
                } else if (f.isAllowedTo(Field.INSERT) == false) {
                    record_processor.addError("Field '" + fieldName + "' is not allowed to insert opertation");
                } else if (f.isValid(Field.INSERT, fieldValue, error) == false) {
                    record_processor.addError(error.toString());
                }
            }
        }
        if (record_processor.hasErrors() == true) {
            return false;
        }
        for (int i = 0; i < values.size(); i++) {
            JsonObject o = values.get(i).getAsJsonObject();
            for (int x = 0; x < field_list.size(); x++) {
                Field f = field_list.get(x);
                JsonElement fje = o.get(field_list.get(x).getAlias());
                String fieldValue = (fje == null || fje.isJsonNull() ? null : fje.getAsString());
                if (f.isPrimaryKeyAI() == false && f.isPrimaryKeyMI() == false
                        && f.isAllowedTo(Field.INSERT) == true && f.isValid(Field.INSERT, fieldValue, error) == false
                        && f.hasDefaultValueFor(Field.INSERT) == false) {
                    record_processor.addError(error.toString());
                } else if (fieldValue == null && f.hasDefaultValueFor(Field.INSERT) == true) {
                    o.addProperty(f.getAlias(), f.getDefaultSQLValueFor(Field.INSERT, record_processor.error_list));
                }
            }
        }
        return !record_processor.hasErrors();
    }

    protected boolean isValid(String[] parameters, JsonObject rcvd) throws Exception {

        for (String parameter : parameters) {
            JsonElement el = rcvd.get(parameter);
            if (el == null || el.isJsonNull()) {
                return false;
            }
        }
        return true;
    }

    protected String getFieldsFor(Integer operation, Boolean include_primary_keys) throws Exception {
        StringBuilder csv = new StringBuilder();
        ArrayList<Field> field_list = getFields();
        for (int i = 0; i < field_list.size(); i++) {
            Field f = field_list.get(i);
            if (f.isAllowedTo(operation)
                    || (include_primary_keys == true && f.isPrimaryKey())) {
                csv.append(f.getSelect()).append(",");
            }
        }
        if (csv.length() == 0) {
            return "";
        }
        csv.delete(csv.length() - 1, csv.length());
        return csv.toString();
    }

    protected String getFieldsSelect(Map<String, String> variables) throws Exception {
        StringBuilder csv = new StringBuilder();
        for (int i = 0; i < select_fields.size(); i++) {
            Field f = select_fields.get(i);
            if (f.isVariable() == false) {
                csv.append(f.getSelect()).append(",");
            } else {
                csv.append(f.getSelect(variables)).append(",");
            }
        }
        if (csv.length() == 0) {
            return "";
        }
        csv.delete(csv.length() - 1, csv.length());
        return csv.toString();
    }

    protected String getFieldsSelect(RecordProcessor record_processor, List<Field> field_list, List<ServiceField> service_field_list) throws Exception {
        StringBuilder csv = new StringBuilder();
        if (record_processor.hasGroupBy() == false || field_list == null || field_list.size() == 0) {
            for (int i = 0; i < select_fields.size(); i++) {
                Field f = select_fields.get(i);
                if (f == null) {
                    record_processor.addError("Field '" + field_list.get(i) + "' is not a valid field name");
                } else if (f.isVariable() == true) {
                    csv.append(f.getSelect(record_processor.request.variable_map)).append(",");
                } else {
                    csv.append(f.getSelect()).append(",");
                }
            }
        } else {
            for (int i = 0; i < field_list.size(); i++) {
                Field f = field_list.get(i);
                if (f == null) {
                    record_processor.addError("Field '" + field_list.get(i) + "' is not a valid field name");
                } else if (f.isVariable() == true) {
                    csv.append(f.getSelect(record_processor.request.variable_map)).append(",");
                } else {
                    csv.append(f.getSelect()).append(",");
                }
            }
        }
        for (int i = 0; service_field_list != null && i < service_field_list.size(); i++) {
            csv.append(service_field_list.get(i).getSelectStatement()).append(",");
        }
        if (csv.length() == 0) {
            return "";
        }
        csv.delete(csv.length() - 1, csv.length());
        return csv.toString();
    }

    protected String fieldAliasToCsv(ArrayList<Field> field_list) {
        if (field_list.size() == 0) {
            return "";
        }
        StringBuilder csv = new StringBuilder();
        for (Field f : field_list) {
            csv.append(f.getAlias()).append(",");
        }
        csv.delete(csv.length() - 1, csv.length());
        return csv.toString();
    }

    protected String getFieldsOrderBy(RecordProcessor record_processor) {
        StringBuilder csv = new StringBuilder();
        if (record_processor.request.order_by_list == null || record_processor.request.order_by_list.size() == 0) {
            for (int i = 0; select_statement_orderby != null && i < select_statement_orderby.size() && record_processor.request.response.equalsIgnoreCase("process"); i++) {
                Field f = select_statement_orderby.get(i);
                csv.append(f.getOrderBy()).append(",");
            }
        } else {
            for (int i = 0; i < record_processor.request.order_by_list.size(); i++) {
                String orderByFieldName = record_processor.request.order_by_list.get(i);
                char order = orderByFieldName.charAt(0);
                if (order == '+' || order == '-') {
                    orderByFieldName = orderByFieldName.substring(1);
                    //record_processor.addError("Field Orderby must start with '+' for ascending order or '-' for descending order");
                    //continue;
                } else {
                    order = '+';
                }
                Field f = getAliasedField(orderByFieldName);
                if (f == null) {
                    record_processor.addError("Field '" + record_processor.request.order_by_list.get(i) + "' is not a valid field name");
                } else {
                    csv.append(f.getOrderBy()).append(order == '+' ? " ASC," : " DESC,");
                }
            }
        }
        if (csv.length() == 0) {
            return "";
        }
        csv.delete(csv.length() - 1, csv.length());
        return csv.toString();
    }

    protected String getFieldsGroupBy(RecordProcessor record_processor) {
        StringBuilder csv = new StringBuilder();
        for (int i = 0; i < record_processor.request.group_by_list.size(); i++) {
            Field f = getAliasedField(record_processor.request.group_by_list.get(i));
            if (f == null) {
                record_processor.addError("Field '" + record_processor.request.group_by_list.get(i) + "' is not a valid field name");
            } else if (select_statement_groupby.contains(f) == false) {
                record_processor.addError("Field '" + record_processor.request.group_by_list.get(i) + "' is not a valid 'Group By' field name");
            } else {
                csv.append(f.getGroupBy()).append(",");
            }
        }
        if (csv.length() == 0) {
            return "";
        }
        csv.delete(csv.length() - 1, csv.length());
        if (record_processor.hasErrors() == true) {
            record_processor.addError("Fields valid for 'Group By' are [" + csv.toString() + "] is not a valid GROUP BY field name");
        }
        return csv.toString();
    }

    protected String getJsonCSV(JsonArray jsonArray) {
        StringBuilder csv = new StringBuilder();
        for (int i = 0; i < jsonArray.size(); i++) {
            csv.append(jsonArray.get(i).getAsString()).append(",");
        }
        if (csv.length() == 0) {
            return "";
        }
        csv.delete(csv.length() - 1, csv.length());
        return csv.toString();
    }

    protected String getJsonCSV(Map<String, String> variables, JsonArray csv_list, HashMap<String, Field> record) throws Exception {
        StringBuilder csv = new StringBuilder();
        for (int i = 0; i < csv_list.size(); i++) {
            Field f = record.get(csv_list.get(i).getAsString());
            if (f.isVariable() == false) {
                csv.append(f.getSelect()).append(",");
            } else {
                csv.append(f.getSelect(variables)).append(",");
            }
        }
        if (csv.length() == 0) {
            return "";
        }
        csv.delete(csv.length() - 1, csv.length());
        return csv.toString();
    }
    
    /**************************************************************************/
    /************ SELECT PROCESSOR ********************************************/
    /**************************************************************************/
    
    /**
     * @param record_processor
     * @throws java.lang.Exception
     */
    public void validateSelectStatement(RecordProcessor record_processor) throws Exception {
        StringBuilder where_clause = new StringBuilder();
        StringBuilder having_clause = new StringBuilder();

        StringBuilder query = new StringBuilder(select_statement);

        if (areValidSelectFields(record_processor, record_processor.query.service_field_list, getNamedFieldMap(), select_fields) == false) {
            record_processor.addError("ERROR: validateSelectStatement.areValidSelectFields");
            return;
        }

        if (record_processor.request.where.values != null && record_processor.request.where.clause.replaceAll("[^?]", "").length() != record_processor.request.where.values.size()) {
            record_processor.addError("ERROR: validateSelectStatement.escapement");
            return;
        }

        if (isValidClause(record_processor, Field.SELECT, record_processor.request.where.clause, getAliasedFieldMap(), record_processor.query.where_field_list, record_processor.request.where.values, where_clause, having_clause, record_processor.query.where_argument_list, record_processor.query.having_argument_list) == false) {
            record_processor.addError("ERROR: validateSelectStatement.isValidClause");
            return;
        }

        String order_by_list = null;
        if (record_processor.request.order_by_list != null) {
            order_by_list = getFieldsOrderBy(record_processor);
            if (record_processor.hasErrors() == true) {
                record_processor.addError("ERROR: validateSelectStatement.ORDER_BY_VALIDATION");
                return;
            }
        }

        String group_by_list = null;
        if (record_processor.request.group_by_list != null) {
            group_by_list = getFieldsGroupBy(record_processor);
            if (record_processor.hasErrors()) {
                record_processor.addError("ERROR: validateSelectStatement.GROUP_BY_VALIDATION");
                return;
            }
        }
        //

        int idx = query.indexOf("$SELECT$");
        query.replace(idx, idx + 8, "");
        query.insert(idx, "SELECT ");
        query.insert(idx + 7, getFieldsSelect(record_processor, field_list, record_processor.query.service_field_list));
        if (record_processor.hasErrors() == true) {
            record_processor.addError("ERROR: validateSelectStatement.SELECT");
            return;
        }

        idx = query.indexOf("$WHERE$");
        query.replace(idx, idx + 7, "");
        if (where_clause != null && where_clause.length() > 0) {
            if (where_clause.lastIndexOf(" AND ") == where_clause.length() - 5) {
                where_clause = where_clause.delete(where_clause.lastIndexOf(" AND "), where_clause.length());
            }
            if (where_clause.lastIndexOf(" OR ") == where_clause.length() - 4) {
                where_clause = where_clause.delete(where_clause.lastIndexOf(" OR "), where_clause.length());
            }
        }        
        if (where_clause != null && where_clause.length() > 0) {
            query.insert(idx, " WHERE ");
            if (hasSelectWhereCondition() == true) {
                where_clause.insert(0, getSelectWhereCondition() + " AND ");
            }
            query.insert(idx + 7, where_clause);
        } else if (hasSelectWhereCondition() == true) {
            query.insert(idx, " WHERE ");
            where_clause.setLength(0);
            where_clause.append(getSelectWhereCondition());
            query.insert(idx + 7, where_clause);
        }
        if (record_processor.hasErrors() == true) {
            record_processor.addError("ERROR: validateSelectStatement.WHERE");
            return;
        }

        idx = query.indexOf("$GROUPBY$");
        query.replace(idx, idx + 9, "");
        if (group_by_list != null && group_by_list.length() > 0) {
            query.insert(idx, "GROUP BY ");
            query.insert(idx + 9, group_by_list);
        }
        if (record_processor.hasErrors() == true) {
            record_processor.addError("ERROR: validateSelectStatement.GROUP_BY");
            return;
        }

        idx = query.indexOf("$HAVING$");
        query.replace(idx, idx + 8, "");
        if (having_clause != null && having_clause.length() > 0) {
            if (having_clause.lastIndexOf(" AND ") == having_clause.length() - 5) {
                having_clause.delete(having_clause.lastIndexOf(" AND "), having_clause.length());
            }
            if (having_clause.lastIndexOf(" OR ") == having_clause.length() - 4) {
                having_clause.delete(having_clause.lastIndexOf(" OR "), having_clause.length());
            }
        }
        if (having_clause != null && having_clause.length() > 0) {
            query.insert(idx, " HAVING ");
            query.insert(idx + 8, having_clause);
        }
        if (record_processor.hasErrors() == true) {
            record_processor.addError("ERROR: validateSelectStatement.HAVING");
            return;
        }

        idx = query.indexOf("$ORDERBY$");
        query.replace(idx, idx + 9, "");
        if (order_by_list != null && order_by_list.length() > 0) {
            query.insert(idx, "ORDER BY ");
            query.insert(idx + 9, order_by_list);
        }
        if (record_processor.hasErrors() == true) {
            record_processor.addError("ERROR: validateSelectStatement.ORDER_BY");
            return;
        }

        record_processor.query.sql = query.toString();
    }
    
    public void processSelectedRecord(RecordProcessor record_processor, ResultSet resultset, JsonObject record_object) throws Exception {
        for (int i = 0; i < record_processor.request.select_list.size(); i++) {
            String field_alias = record_processor.request.select_list.get(i);
            Field f = getAliasedField(field_alias);
            if (f.isIgnoredFor(Field.SELECT)) {
                continue;
            }
            //jrecord.addProperty(s[i], f.getFieldString(resultset.getObject(f.getAlias())));
            Object fieldObject = resultset.getObject(f.getAlias());
            if (fieldObject == null) {
                record_object.add(field_alias, JsonNull.INSTANCE);
            } else if (f.isNumeric() == true) {
                record_object.addProperty(field_alias, (Number) fieldObject);
            } else if (f.isBoolean() == true) {
                record_object.addProperty(field_alias, f.parseBoolean(fieldObject));
            } else {
                record_object.addProperty(field_alias, f.getFieldString(resultset.getObject(f.getAlias())));
            }
        }
    }
    
    public void processSelectedObjectRecord(RecordProcessor record_processor, ResultSet resultset, JsonObject record_object, List<ServiceField> service_field_list) throws Exception {
        for (int i = 0; i < record_processor.request.select_list.size(); i++) {
            String field_alias = record_processor.request.select_list.get(i);
            Field f = getAliasedField(field_alias);
            if (f.isIgnoredFor(Field.SELECT)) {
                continue;
            }
            //Object fieldObject = resultset.getObject(f.getAlias());
            Object fieldObject = f.getPostProcessedValue(Field.SELECT, resultset.getObject(f.getAlias()), record_processor.error_list);
            if (fieldObject == null) {
                record_object.add(field_alias, JsonNull.INSTANCE);
            } else if (f.isNumeric() == true) {
                record_object.addProperty(field_alias, (Number) fieldObject);
            } else if (f.isBoolean() == true) {
                record_object.addProperty(field_alias, f.parseBoolean(fieldObject));
            } else {
                //jrecord.addProperty(alias, f.getFieldString(resultset.getObject(f.getAlias())));
                record_object.addProperty(field_alias, f.getFieldString(fieldObject));
            }
        }
        for (int i = 0; i < service_field_list.size(); i++) {
            ServiceField sf = service_field_list.get(i);
            Object fieldObject = resultset.getObject(sf.getAlias());
            if (fieldObject == null) {
                record_object.add(sf.getAlias(), JsonNull.INSTANCE);
            } else if (sf.isNumeric() == true) {
                record_object.addProperty(sf.getAlias(), (Number) fieldObject);
            } else if (sf.isBoolean() == true) {
                record_object.addProperty(sf.getAlias(), sf.parseBoolean(fieldObject));
            } else {
                record_object.addProperty(sf.getAlias(), sf.getFieldString(resultset.getObject(sf.getAlias())));
            }
        }
    }
    
    public void processSelectedArrayRecord(RecordProcessor record_processor, ResultSet resultset, JsonArray record_list, List<ServiceField> service_field_list) throws Exception {
        for (int i = 0; i < record_processor.request.select_list.size(); i++) {
            String field_alias = record_processor.request.select_list.get(i);
            Field f = getAliasedField(field_alias);
            if (f.isIgnoredFor(Field.SELECT)) {
                continue;
            }
            //Object fieldObject = resultset.getObject(f.getAlias());
            Object fieldObject = f.getPostProcessedValue(Field.SELECT, resultset.getObject(f.getAlias()), record_processor.error_list);
            if (fieldObject == null) {
                record_list.add(JsonNull.INSTANCE);
            } else if (f.isNumeric() == true) {
                record_list.add((Number) fieldObject);
            } else if (f.isBoolean() == true) {
                record_list.add(f.parseBoolean(fieldObject));
            } else {
                //jrecord.add(f.getFieldString(resultset.getObject(f.getAlias())));
                record_list.add(f.getFieldString(fieldObject));
            }
        }
        for (int i = 0; i < service_field_list.size(); i++) {
            ServiceField service_field = service_field_list.get(i);
            Object fieldObject = resultset.getObject(service_field.getAlias());
            if (fieldObject == null) {
                record_list.add(JsonNull.INSTANCE);
            } else if (service_field.isNumeric() == true) {
                record_list.add((Number) fieldObject);
            } else if (service_field.isBoolean() == true) {
                record_list.add(service_field.parseBoolean(fieldObject));
            } else {
                record_list.add(service_field.getFieldString(resultset.getObject(service_field.getAlias())));
            }
        }
    }
    
    public void prepareSelectStatement(Gson gson, RecordProcessor record_processor, RecordHandler record_handler) throws Exception {
       
        validateCommand(record_processor);
        if (record_processor.hasErrors() == true) {
            return;
        }
        if (record_handler.selectInject(record_processor) == false) {
            return;
        }
        
        Request reuest = record_processor.getTableRequest();
        validateSelectStatement(record_processor);

        if (record_processor.hasErrors() == true) {
            record_processor.addError("ERROR: validateSelectStatement.selectGson.validateSelectStatement");
            return;
        }
        
        JsonArray record_key_list = gson.toJsonTree(record_processor.request.select_list, new TypeToken<List<String>>(){}.getType()).getAsJsonArray();
        record_processor.mergeJsonElement("fields", record_key_list);
    }
    
    public void executeSelect(Gson gson, Connection connection, RecordProcessor record_processor, RecordHandler record_handler) throws Exception {
        record_processor.query.t2 = Instant.now();
        if (record_handler.selectPreLogic(record_processor, connection) == false) {
            return;
        }
        try (PreparedStatement prepared_statement = connection.prepareStatement(record_processor.query.sql)) {
            int idx = 0;
            for (Argument argument : record_processor.query.having_argument_list) {
                for (String sv : argument.getValues()) {
                    prepared_statement.setObject(++idx, sv);
                }
            }
            for (Argument argument : record_processor.query.where_argument_list) {
                for (String sv : argument.getValues()) {
                    prepared_statement.setObject(++idx, sv);
                }
            }
            //JsonObject table_view = new JsonObject();
            JsonArray view = null;
            Boolean empty_set = true;
            //record_processor.name(request_table.table_alias);
            //record_processor.beginObject();
            record_processor.beginArray();
            try (ResultSet resultset = prepared_statement.executeQuery()) {
                empty_set = !resultset.isBeforeFirst();
                record_processor.query.t3 = Instant.now();
                JsonObject record_object = new JsonObject();
                while (resultset.next() == true) {
                    record_processor.beginArray();
                    processSelectedObjectRecord(record_processor, resultset, record_object, record_processor.query.service_field_list);
                    if (record_handler.selectPerRecordLogic(record_processor, resultset, record_object) == false) {
                        //do something!!
                    }
                    saveTablePrimaryRecord(record_processor, record_object);
                    
                    JsonArray record_value_list = record_processor.extractJsonObjectValueList(record_object);
                    //record_processor.mergeJsonElement("record", record_value_list);
                    record_processor.addJsonElement(record_value_list);
                    if (child_table_list != null && child_table_list.size() > 0) {
                        for (int i = 0; i < child_table_list.size(); i++) {
                            Table child_table = child_table_list.get(i);
                            child_table.executeSelect(gson, connection, record_processor.getChildTableRecordProcessor(child_table.request_table.table_alias), record_handler);
                        }
                    }
                    record_processor.endArray();
                }
            } catch (Exception sqlx) {
                throw sqlx;
            }
            if (record_handler.selectPostLogic(record_processor, connection, view) == false || record_handler.selectEject(record_processor) == false) {
                //error_list.add("Select completed but with post logic error_list encountered");
                return;
            }
            record_processor.query.t4 = Instant.now();
            if (record_processor.hasErrors() == true) {
                throw new Exception("SELECT PROGRAM INTERNAL SEQUENCE ERROR");
            }
            record_processor.endArray();
            //record_processor.endObject();
        } catch (Exception sqlx) {
            throw sqlx;
        }
    }
    
    private void saveTablePrimaryRecord(RecordProcessor record_processor, JsonObject record_object) {
        if (record_processor.request.child_list == null || record_processor.request.child_list.size() == 0) {
            return;
        }
        
        /*field_name_map.get();
        getPrimaryKey;
        getForeginKey to Parent*/
        Map<String, Object> record_stack_frame = new HashMap<>();
        record_processor.addRecordStackFrame(record_stack_frame);
        /*remove stack
                reofrm the select to inject the parent inner joined where statement*/
    }

    /**************************************************************************/
    /************ INSERT PROCESSOR ********************************************/
    /**************************************************************************/
    
    public String validateInsertSetStatement(JsonObject json) throws Exception {
        StringBuilder sql = new StringBuilder(insert_set_statement);
        JsonObject set = json.get("set").getAsJsonObject();
        Set<String> keys = set.keySet();
        for (String field_alias : keys) {
            Field f = getAliasedField(field_alias);
            String value = set.get(field_alias).getAsString();
            Integer index = sql.indexOf("?" + field_alias);
            sql.delete(index, index + field_alias.length() + 1);
            sql.insert(index, f.getQuotable(value));
        }
        return sql.toString();
        /*//insert_set_statement_fields_name
        //insert_set_statement_fields_alias
        //Validate Mandatory Paramters
        String c = w.get("clause").getAsString();
        String[] o = javaStringArray(json.get("order_by_list").getAsJsonArray());
        StringBuilder ww = new StringBuilder();
        StringBuilder hh = new StringBuilder();
        
        StringBuilder query = new StringBuilder(select_statement);
        
        JsonArray error_list = new JsonArray();
        if (areValidSelectFields(s, tableFields, select_fields, error_list) == false) {
            sendError(req, resp, "INVALID_DATABASE_REQUEST_FIELDS_VALUES", "ERROR", "validateInsertSetStatement", 400, 1400, error_list);
            return null;
        }
        
        if (c.replaceAll("[^?]", "").length() != wv.length) {
            sendError(req, resp, "INVALID_DATABASE_REQUEST_FIELDS_VALUES", "ERROR", "validateInsertSetStatement", 400, 1400, error_list);
            return null;
        }
        
        if (isValidClause(c, tableFields, wf, wv, ww, hh, error_list) == false) {
            sendError(req, resp, "INVALID_DATABASE_REQUEST_FIELDS_VALUES", "ERROR", "validateInsertSetStatement", 400, 1400, error_list);
            return null;
        }
        
        String oo = getFieldsOrderBy(vt, o, error_list);
        if (error_list.size() > 0) {
            sendError(req, resp, "INVALID_DATABASE_REQUEST_FIELDS_VALUES", "ERROR", "validateInsertSetStatement", 400, 1400, error_list);
            return null;
        }
        //
        
        int idx = query.indexOf("$SELECT$");
        query.replace(idx, idx+8, "");
        query.insert(idx, "SELECT ");
        query.insert(idx+7, getFieldsSelect(s));
        
        idx = query.indexOf("$WHERE$");
        query.replace(idx, idx+7, "");
        if (ww != null && ww.length() > 0) {
            query.insert(idx, " WHERE ");
            query.insert(idx+7, ww);
        }
        
        idx = query.indexOf("$HAVING$");
        query.replace(idx, idx+8, "");
        if (hh != null && hh.length() > 0) {
            query.insert(idx, " HAVING ");
            query.insert(idx+8, hh);
        }
        
        idx = query.indexOf("$ORDERBY$");
        query.replace(idx, idx+9, "");
        if (oo != null && oo.length() > 0) {
            query.insert(idx, " ORDER BY ");
            query.insert(idx+10, oo);
        }
        
        return query.toString();*/
    }
    
    public int insertValues(Connection con, PreparedStatement prepared_statement, JsonArray values, Map<String, String> variables, Returns returns) throws Exception {
        int affected_rows = 0;
        for (int oo = 0; oo < values.size(); oo++) {
            JsonObject o = values.get(oo).getAsJsonObject();
            int idx = 0;
            for (int i = 0; i < insert_fields.size(); i++) {
                Field field = insert_fields.get(i);
                if (field.isPrimaryKeyAI() == false && field.isPrimaryKeyMI() == false) {
                    JsonElement fje = o.get(insert_fields.get(i).getAlias());
                    String fieldValue = (fje == null || fje.isJsonNull() ? null : fje.getAsString());
                    prepared_statement.setObject(++idx, field.getFieldObject(fieldValue));
                }
            }
            if (hasPrimaryKeyMI() == true) {
                for (int i = 0; i < primary_keys.size(); i++) {
                    Field field = primary_keys.get(i);
                    if (/*field.isPrimaryKeyAI() == false && */field.isPrimaryKeyMI() == false) {
                        JsonElement fje = o.get(field.getAlias());
                        String fieldValue = (fje.isJsonNull() ? null : fje.getAsString());
                        prepared_statement.setObject(++idx, field.getFieldObject(fieldValue));
                    }
                }
            }
            prepared_statement.addBatch();
        }

        int[] affectRows = prepared_statement.executeBatch();
        for (int i = 0; i < affectRows.length; i++) {
            affected_rows += affectRows[i];
        }
        returns.Returns("t3", System.nanoTime());
        //Boolean return_insert_generated_id = getTransactionSecurityFlag(SECURITY_FLAG_RETURN_INSERT_GENERATED_ID);
        //if (return_insert_generated_id == true) {
        if (affected_rows > 0 && hasPrimaryKeyMI() == true) {
            for (int oo = 0; oo < values.size(); oo++) {
                JsonObject o = values.get(oo).getAsJsonObject();
                if (insert_statement_select == null) {
                    if (variables == null) {
                        throw new Exception("Variables are Null");
                    }
                    createRuntimeInsertStatementSelect(variables);
                }
                try (PreparedStatement preparedInsertedSelectStmt = con.prepareStatement(insert_statement_select)) {
                    int idx = 0;
                    for (int i = 0; i < insert_fields.size(); i++) {
                        Field f = insert_fields.get(i);
                        if (f.isPrimaryKeyMI() == false) {
                            JsonElement fje = o.get(insert_fields.get(i).getAlias());
                            String fieldValue = (fje == null || fje.isJsonNull() ? null : fje.getAsString());
                            preparedInsertedSelectStmt.setObject(++idx, f.getFieldObject(fieldValue));
                        }
                    }

                    try (ResultSet resultset = preparedInsertedSelectStmt.executeQuery()) {
                        while (resultset.next() == true) {
                            for (int i = 0; i < primary_keys_manual_increment_fields.size(); i++) {
                                Field f = primary_keys_manual_increment_fields.get(i);
                                o.addProperty(f.getAlias(), resultset.getBigDecimal(f.getAlias()));
                            }
                        }
                        resultset.close();
                    } catch (Exception sqlx) {
                        throw sqlx;
                    }
                    preparedInsertedSelectStmt.close();
                } catch (Exception sqlx) {
                    throw sqlx;
                }
            }
        } else if (affected_rows > 0 && hasPrimaryKeyAI() == true) {
            try (ResultSet generatedKeys = prepared_statement.getGeneratedKeys()) {
                int x = 0;
                while (generatedKeys.next()) {
                    JsonObject o = values.get(x++).getAsJsonObject();
                    int count = generatedKeys.getMetaData().getColumnCount();
                    for (int i = 0; i < count; i++) {
                        /*generatedKeys.getMetaData().getColumnLabel(i+1)*/
                        o.addProperty(primary_keys.get(i).getAlias(), generatedKeys.getInt(i + 1));
                    }
                }
                generatedKeys.close();
            } catch (Exception sqlx) {
                throw sqlx;
            }
        }
        return affected_rows;
    }
    
    public String getInsertStatement() {
        return insert_statement;
    }
    
    public ArrayList<Field> getInsertFields() {
        return insert_fields;
    }
    
    private String getInsertSetStatement() {
        return insert_set_statement;
    }
    
    /**************************************************************************/
    /************ UPDATE PROCESSOR ********************************************/
    /**************************************************************************/
    
    public String validateUpdateWhereStatement(RecordProcessor record_processor, String c, JsonArray v, JsonArray wff, JsonArray wvv, ArrayList<Field> uf, ArrayList<ArrayList<Object>> ufv, ArrayList<Argument> wa, ArrayList<Argument> ha) throws Exception {
        //Validate Mandatory Paramters
        ArrayList<String> vv = wvv == null ? null : new ArrayList<String>(Arrays.asList(JsonUtil.javaStringArray(wvv.getAsJsonArray())));
        ArrayList<String> ff = wff == null ? null : new ArrayList<String>(Arrays.asList(JsonUtil.javaStringArray(wff.getAsJsonArray())));
        ArrayList<Field> wf = new ArrayList<>();
        StringBuilder uu = new StringBuilder();
        StringBuilder ww = new StringBuilder();
        StringBuilder hh = new StringBuilder();

        StringBuilder query = new StringBuilder(update_statement);

        if (areValidUpdateFieldsValues(record_processor, getNamedFieldMap(), v, ff, update_fields, record_processor.error_list) == false) {
            record_processor.addError("ERROR: validateUpdateWhereStatement.areValidUpdateFieldsValues");
            return null;
        }

        if (c == null || c.length() == 0 || (ff == null && vv == null) || isValidClause(record_processor, Field.UPDATE, c, getNamedFieldMap(), wf, vv, ww, hh, wa, ha) == false) {
            record_processor.addError("ERROR: validateUpdateWhereStatement.isValidClause");
            return null;
        }
        //

        JsonObject job = null;
        for (int i = 0; i < v.size(); i++) {
            ArrayList<Object> r = new ArrayList<>();
            JsonObject jsonObject = v.get(i).getAsJsonObject();
            if (jsonObject == null) {
                record_processor.addError("Null Json Object on group [" + i + "]");
            } else if (jsonObject.size() == 0) {
                record_processor.addError("Empty Json Object on group [" + i + "]");
            } else if (job != null && jsonObject.size() != job.size()) {
                record_processor.addError("Json Object has different elements count on group [" + i + "]");
            }
            job = jsonObject;
            if (record_processor.hasErrors() == true) {
                record_processor.addError("ERROR: validateUpdateWhereStatement.Fields");
                return null;
            }
            if (uf.size() == 0) {
                Boolean nonPrimaryKeysExists = false;
                for (String fn : jsonObject.keySet()) {
                    Field f = getAliasedField(fn);
                    if (primary_keys != null && primary_keys.contains(fn) == true) {
                        //ignore
                        //error_list.add("Field '" + fn + "' is a primary key and is not allowed for update");
                    } else if (f == null && ff != null && ff.contains(fn) == false) {
                        record_processor.addError("Field '" + fn + "' is unknowen to where field_list");
                    } else if (f == null && ff != null && ff.contains(fn) == true) {
                        /*uf.add(f);
                        uu.append(f.getSQLName()).append("=?, ");*/
                    } else if (f == null) {
                        record_processor.addError("Field '" + fn + "' is not a valid field name");
                    } else if (f.isPrimaryKey() == false) {
                        nonPrimaryKeysExists = true;
                        uf.add(f);
                        if (f.isUpdateFormulaDefined() == true) {
                            uu.append(f.getSQLName()).append("=").append(f.getUpdateFormulaDefined()).append(",");
                        } else {
                            uu.append(f.getSQLName()).append("=?, ");
                        }
                    }
                }
                if (nonPrimaryKeysExists == false) {
                    record_processor.addError("Update abortred, no update can be performed over primary keys only record");
                } else {
                    uu.delete(uu.length() - 2, uu.length());
                }
                if (record_processor.hasErrors() == true) {
                    record_processor.addError("ERROR: validateUpdateWhereStatement.UPDATE_ABORTED");
                    return null;
                }
            }
            StringBuilder er = new StringBuilder();
            for (int x = 0; x < uf.size(); x++) {
                Field f = uf.get(x);
                String ue = JsonUtil.getJsonString(jsonObject, f.getAlias(), false);
                if (f.isValid(Field.UPDATE, ue, er) == false) {
                    record_processor.error_list.add(er.toString() + ", check in group index[" + (i + 1) + "]");
                } else {
                    String preProcessedValue = f.getPreProcessedValue(Field.UPDATE, ue, record_processor.error_list);
                    if (record_processor.hasErrors() == true) {
                        record_processor.addError("ERROR: validateUpdateWhereStatement.PRE_PROCESS");
                        return null;
                    }
                    r.add(f.getFieldObject(preProcessedValue));
                }
            }
            if (record_processor.hasErrors() == true) {
                continue;
            }
            for (int x = jsonObject.size(); x < wf.size() + jsonObject.size(); x++) {
                Field f = wf.get(x - jsonObject.size());
                String ue = null;
                if (ff == null) {
                    ue = JsonUtil.getJsonString(wvv, x - jsonObject.size(), false);
                } else {
                    ue = JsonUtil.getJsonString(jsonObject, wff.get(x - jsonObject.size()).getAsString(), false);
                }
                if (f.isValid(Field.UPDATE, ue, er) == false) {
                    record_processor.addError(er.toString());
                } else {
                    r.add(f.getFieldObject(ue));
                }
            }
            ufv.add(r);
        }

        if (record_processor.hasErrors() == true) {
            record_processor.addError("ERROR: validateUpdateWhereStatement.FIELD_VALIDATION");
            return null;
        }

        int idx = query.indexOf("$UPDATE$");
        query.replace(idx, idx + 8, "");
        if (uu != null && uu.length() > 0) {
            query.insert(idx, uu);
        }

        idx = query.indexOf("$WHERE$");
        query.replace(idx, idx + 7, "");
        if (ww != null && ww.length() > 0) {
            query.insert(idx, ww.length() == 0 ? "" : " WHERE ");
            query.insert(idx + 7, ww);
        }

        //put having in where for updates
        if (hh != null && hh.length() > 0) {
            query.insert(idx, hh.length() == 0 ? "" : " WHERE ");
            query.insert(idx + 7, hh);
        }

        return query.toString();
    }
    
    /**************************************************************************/
    /************ DELETE PROCESSOR ********************************************/
    /**************************************************************************/
    
    public String validateDeleteWhereStatement(RecordProcessor record_processor, String c, JsonArray v, JsonArray wff, JsonArray wvv, ArrayList<Field> uf, ArrayList<ArrayList<Object>> ufv, ArrayList<Argument> wa, ArrayList<Argument> ha) throws Exception {
        //Validate Mandatory Paramters
        ArrayList<String> vv = wvv == null ? null : new ArrayList<String>(Arrays.asList(JsonUtil.javaStringArray(wvv.getAsJsonArray())));
        ArrayList<String> ff = wff == null ? null : new ArrayList<String>(Arrays.asList(JsonUtil.javaStringArray(wff.getAsJsonArray())));
        ArrayList<Field> wf = new ArrayList<Field>();
        StringBuilder uu = new StringBuilder();
        StringBuilder ww = new StringBuilder();
        StringBuilder hh = new StringBuilder();

        StringBuilder query = new StringBuilder(delete_statement);

        if (c == null || c.length() == 0 || (ff == null && vv == null) || isValidClause(record_processor, Field.INSERT, c, getNamedFieldMap(), wf, vv, ww, hh, wa, ha) == false) {
            record_processor.addError("ERROR: validateDeleteWhereStatement.validateDeleteWhereStatement.isValidClause");
            return null;
        }

        JsonObject job = null;
        for (int i = 0; i < v.size(); i++) {
            ArrayList<Object> r = new ArrayList<>();
            JsonObject jsonObject = v.get(i).getAsJsonObject();
            if (jsonObject == null) {
                record_processor.addError("Null Json Object on group [" + i + "]");
            } else if (jsonObject.size() == 0) {
                record_processor.addError("Empty Json Object on group [" + i + "]");
            } else if (job != null && jsonObject.size() != job.size()) {
                record_processor.addError("Json Object has different elements count on group [" + i + "]");
            }
            job = jsonObject;
            if (record_processor.hasErrors() == true) {
                record_processor.addError("ERROR: validateDeleteWhereStatement.validateDeleteWhereStatement.Fields");
                return null;
            }
            if (uf.size() == 0) {
                for (String field_alias : jsonObject.keySet()) {
                    Field f = getAliasedField(field_alias);
                    /*if (primary_keys != null && primary_keys.contains(fn) == true) {
                        error_list.add("Field '" + fn + "' is a primary key and is not allowed for delete");
                    } else */
                    if (f == null && ff != null && ff.contains(field_alias) == false) {
                        record_processor.addError("Field '" + field_alias + "' is unknowen to where field_list");
                    } else if (f == null && ff != null && ff.contains(field_alias) == true) {
                        /*uf.add(f);
                        uu.append(f.getSQLName()).append("=?, ");*/
                    } else if (f == null) {
                        record_processor.addError("Field '" + field_alias + "' is not a valid field name");
                    } else {
                        uf.add(f);
                        //uu.append(f.getSQLName()).append("=?, ");
                    }
                }
                //uu.delete(uu.length() - 2, uu.length());
                if (record_processor.hasErrors() == true) {
                    record_processor.addError("ERROR: validateDeleteWhereStatement.validateDeleteWhereStatement.PRE_PROCESS");
                    return null;
                }
            }
            StringBuilder er = new StringBuilder();
            //// this section seems to be redundant from update method
            /*for (int x = 0; x < uf.size(); x++) {
                Field f = uf.get(x);
                String ue = getJsonString(jsonObject, f.getAlias());
                if (f.isValid(Field.DELETE, ue, er) == false) {
                    error_list.add(er.toString() + "in group index[" + i + "]");
                } else {
                    r.add(f.getFieldObject(ue));
                }
            }
            if (error_list.size() > 0) {
                continue;
            }*/
            for (int x = jsonObject.size(); x < wf.size() + jsonObject.size(); x++) {
                Field f = wf.get(x - jsonObject.size());
                String ue = null;
                if (ff == null) {
                    ue = JsonUtil.getJsonString(wvv, x - jsonObject.size(), false);
                } else {
                    ue = JsonUtil.getJsonString(jsonObject, wff.get(x - jsonObject.size()).getAsString(), false);
                }
                if (f.isValid(Field.DELETE, ue, er) == false) {
                    record_processor.addError(er.toString());
                } else {
                    r.add(f.getFieldObject(ue));
                }
            }
            ufv.add(r);
        }

        if (record_processor.hasErrors() == true) {
            record_processor.addError("ERROR: validateDeleteWhereStatement.validateDeleteWhereStatement.FIELD_VALIDATION");
            return null;
        }

        int idx = -1;
        /*int idx = query.indexOf("$UPDATE$");
        query.replace(idx, idx+8, "");
        if (uu != null) {
            query.insert(idx, uu);
        }*/

        idx = query.indexOf("$WHERE$");
        query.replace(idx, idx + 7, "");
        StringBuilder w = ww != null && ww.length() > 0 ? ww : hh;
        if (w != null) {
            query.insert(idx, w.length() == 0 ? "" : " WHERE ");
            query.insert(idx + 7, w);
        }

        return query.toString();
    }
    
    /**
     * @param model_jdbc_source
     * @param model_id
     * @param error_list
     * @throws java.lang.Exception
     */
    
    public static void loadDataModel(String secret_key, JDBCSource model_jdbc_source, JDBCSource data_jdbc_source, Integer model_id, HashMap<String, Class> interface_implementation, JsonArray error_list, Boolean foreing_key_must_link_to_primary_key) throws Exception {
        Integer model_instance_id = 1;
        DataClass.LoadMethod loadMethod = DataClass.LoadMethod.REFLECTION;
        DataLookup data_lookup = null;
        ModelDefinition data_model_definition = null;
        try (Connection data_connection = model_jdbc_source.getConnection(false)) {
            String select_model = "SELECT `model_id`, `model_instance_sequence_type_id`, `model_instance_sequence_last_value`, `model_name`, `model_version`, `model_class_path`, `model_data_lookup_category`, `modeled_database_url`, `modeled_database_url_user_name`, CAST(AES_DECRYPT(FROM_BASE64(`modeled_database_url_user_password`), ?) AS CHAR) AS `modeled_database_url_user_password`, `modeled_database_schem`, `modeled_database_name`, `modeled_database_field_open_quote`, `modeled_database_field_close_quote` FROM `"+model_jdbc_source.getDatabaseName()+"`.`model` WHERE `model_id`=?";
            try (PreparedStatement stmt = data_connection.prepareStatement(select_model)) {
                stmt.setString(1, secret_key);
                stmt.setInt(2, model_id);
                try (ResultSet resultset = stmt.executeQuery()) {
                    ArrayList<ModelDefinition> data_model_definition_list = JsonResultset.resultset(resultset, ModelDefinition.class);
                    if (data_model_definition_list == null || data_model_definition_list.size() == 0) {
                        error_list.add("Data Model ID '"+model_id+"' is not exist");
                    } else {
                        data_model_definition = data_model_definition_list.get(0);
                    }
                    resultset.close();
                } catch (Exception ex) {
                    throw ex;
                }
                stmt.close();
            } catch (Exception ex) {
                throw ex;
            }

            if (data_model_definition != null) {
                String sql = "SELECT `enum_name`, `enum_element_id`, `enum_element_code`, `enum_element_java_datatype`, `enum_element_typescript_datatype` FROM `"+model_jdbc_source.getDatabaseName()+"`.`lookup_enum` INNER JOIN `"+model_jdbc_source.getDatabaseName()+"`.`lookup_enum_element` ON `lookup_enum`.`enum_id` = `lookup_enum_element`.`enum_id` WHERE `lookup_enum`.`enum_name`=? ORDER BY `enum_name`, `enum_element_code`";
                try (PreparedStatement stmt = data_connection.prepareStatement(sql)) {
                    stmt.setString(1, data_model_definition.model_data_lookup_category);
                    try (ResultSet resultset = stmt.executeQuery()) {
                        data_lookup = new DataLookup(resultset, data_model_definition.model_data_lookup_category, "enum_name", "enum_element_id", "enum_element_code", "enum_element_java_datatype", "enum_element_typescript_datatype");
                        resultset.close();
                    } catch (Exception ex) {
                        throw ex;
                    }
                } catch (Exception ex) {
                    throw ex;
                }
            }
        } catch (Exception exception) {
            throw exception;
        }
        if (data_model_definition != null) {
            DataProcessor<Enterprise> dataProcessor = new DataProcessor<Enterprise>(EnterpriseModel.class, Enterprise.class, model_jdbc_source, data_model_definition, data_lookup, interface_implementation, foreing_key_must_link_to_primary_key);
            EnterpriseModel<Enterprise> enterprise_model = (EnterpriseModel<Enterprise>) dataProcessor.loadModelFromDatabase(model_id, model_instance_id, loadMethod);
            enterprise_model.getInstance().init();
            data_model_map.put(model_id, enterprise_model);
        }
    }
    
    private void initializeTable(Integer model_id, JsonArray table_error_list) throws Exception {
        EnterpriseModel<Enterprise> enterprise_model = data_model_map.get(model_id);
        net.reyadeyat.api.relational.model.Table database_table = enterprise_model.getInstance().getDatabase(data_database_name).getTable(request_table.table_name);
        for (RequestField request_field : request_table.request_field_list) {
            net.reyadeyat.api.relational.model.Field table_field = database_table.field_map.get(request_field.field_name);
            if (table_field == null) {
                table_error_list.add("Request Field '"+request_field.field_name+"' not found in Table '"+request_table.table_name+"'");
            }
            FieldType field_type = FieldType.getClassFieldType(table_field.getTypeJavaClass());
            Boolean nullable = table_field.nullable;
            Boolean group_by = false;
            Field field = addField(request_table.table_name, request_table.table_alias, request_field.field_name, request_field.field_alias, nullable, group_by, field_type, table_error_list);
            if (table_field.getTypeJavaClass().equals(String.class) == true) {
                field.setTexLengthRange(0, table_field.size);
            } else if (table_field.getTypeJavaClass().equals(Number.class) == true) {
                field.setNumberRange(0, table_field.size);
            }
        }
        if (request_table.parent_request_table != null) {
            ArrayList<net.reyadeyat.api.relational.model.ForeignKey> foreign_key_list = database_table.foreign_key_list;
            for (Integer foreign_key_counter = 0; foreign_key_counter < foreign_key_list.size(); foreign_key_counter++) {
                //Add Aliases to the joints
                net.reyadeyat.api.relational.model.ForeignKey foreign_key = foreign_key_list.get(foreign_key_counter);
                for (ForeignKeyField foreign_key_field : foreign_key.foreign_key_field_list) {
                    if (foreign_key.referenced_key_table_name.equals(request_table.parent_request_table.table_name) == false) {
                        throw new Exception("request_table.parent_request_table.table_name '"+request_table.parent_request_table.table_name+"' is not equal to foreign_key.table.name");
                    }
                    RequestField foreign_request_field = request_table.parent_request_table.request_field_alias_map.get(parent_table.field_name_alias.get(foreign_key_field.name));
                    String foreign_table_alias = request_table.parent_request_table.table_alias;
                    RequestField request_field = request_table.request_field_name_map.get(foreign_key_field.name);
                    addForeignKey("FK_"+foreign_key.name, request_field.field_alias, request_table.parent_request_table.table_name, request_table.parent_request_table.table_alias, foreign_request_field.field_alias, table_error_list);
                    addJoinKey("JK_"+foreign_key.name, request_table.parent_request_table.table_name, foreign_table_alias, JoinKey.JoinType.INNER_JOIN);
                    addJoinField("JK_"+foreign_key.name, foreign_key.table.name, foreign_table_alias, foreign_key_field.name, foreign_request_field.field_name, JoinKey.JoinType.INNER_JOIN, table_error_list);
                    /*for (int field_counter = 0; field_counter < foreign_key.foreign_key_field_list.size(); field_counter++) {
                        ForeignKeyField foreign_key_field = foreign_key.foreign_key_field_list.get(field_counter);
                        /////addJoinField("JK_"+foreign_key.name, foreign_key.table.name, String field_alias, String join_field_alias, table_error_list);
                        /////addForeignField("FK_"+foreign_key.name, foreign_key.table.name, String field_alias, String foreign_field_alias, table_error_list);
                    }*/
                    //addForeignKey("FK"+foreign_key_counter, request_table.table_name);
                    //addJoinKey("JK"+foreign_key_counter, request_table.table_name, JoinKey.JoinType.INNER_JOIN);
                    //net.reyadeyat.api.relational.model.Field table_field = database_table.field_name_map.get(request_field.field_name);
                    //addJoinField(String key, String join_table, String field_alias, String join_field_alias, JsonArray table_error_list);
                    //addForeignField(String key, String foreign_table, String field_alias, String foreign_field_alias, JsonArray table_error_list)
                    //addDependentKey("DK1", "project_id", "pm_task", "project_id");
                }
            }
        }
        
        
        
        //scan table
        //add field_list
        /*
        addField("customer_id", FieldType.Integer, false, false, "customer_id").allow(Field.SELECT).disallow(Field.INSERT | Field.UPDATE);
        addField("erp_customer_id", FieldType.String, true, false, "erp_customer_id").allow(Field.SELECT).disallow(Field.INSERT | Field.UPDATE);
        addField("encrypted_customer_id", FieldType.String, false, false, "encrypted_customer_id").allow(Field.SELECT).disallow(Field.INSERT | Field.UPDATE);
        addField("customer_lang", FieldType.String, true, false, "customer_lang").setTexLengthRange(1, 255);
        addField("executive_user_name", FieldType.String, true, false, "executive_user_name").setTexLengthRange(1, 255);
        addField("executive_name_ar", FieldType.String, true, false, "executive_name_ar").setTexLengthRange(1, 255);
        addField("executive_name_en", FieldType.String, true, false, "executive_name_en").setTexLengthRange(1, 255);
        addField("executive_national_id", FieldType.String, true, false, "executive_national_id").setTexLengthRange(1, 255);
        addField("executive_phone", FieldType.String, true, false, "executive_phone").setTexLengthRange(1, 255);
        addField("executive_mobile", FieldType.String, true, false, "executive_mobile").setTexLengthRange(1, 255);
        addField("executive_fax", FieldType.String, true, false, "executive_fax").setTexLengthRange(1, 255);
        addField("executive_email", FieldType.String, true, false, "executive_email").setTexLengthRange(1, 255);
        addField("executive_address", FieldType.String, true, false, "executive_address").setTexLengthRange(1, 255);
        addField("company_data_name_ar", FieldType.String, true, false, "company_data_name_ar").setTexLengthRange(1, 255);
        addField("company_data_name_en", FieldType.String, true, false, "company_data_name_en").setTexLengthRange(1, 255);
        addField("company_data_country_id", FieldType.Integer, true, false, "company_data_country_id").setTexLengthRange(1, 255);
        addField("company_data_city_id", FieldType.Integer, true, false, "company_data_city_id").setTexLengthRange(1, 255);
        addField("company_data_area_id", FieldType.Integer, true, false, "company_data_area_id").setTexLengthRange(1, 255);
        addField("company_data_building", FieldType.String, true, false, "company_data_building").setTexLengthRange(1, 255);
        addField("company_data_district", FieldType.String, true, false, "company_data_district").setTexLengthRange(1, 255);
        addField("company_data_town", FieldType.String, true, false, "company_data_town").setTexLengthRange(1, 255);
        addField("company_data_street", FieldType.String, true, false, "company_data_street").setTexLengthRange(1, 255);
        addField("company_data_postal_code", FieldType.String, true, false, "company_data_postal_code").setTexLengthRange(1, 255);
        addField("company_data_another_id", FieldType.String, true, false, "company_data_another_id").setTexLengthRange(1, 255);
        addField("company_data_address_1", FieldType.String, true, false, "company_data_address_1").setTexLengthRange(1, 255);
        //addField("company_data_address_2", FieldType.String, true, false, "company_data_address_2").setTexLengthRange(1, 255);
        addField("company_activity_branch_id", FieldType.String, true, false, "company_activity_branch_id");
        addField("company_activity_category_id", FieldType.Integer, true, false, "company_activity_category_id");
        addField("company_activity_activity_id", FieldType.Integer, true, false, "company_activity_activity_id");
        addField("company_activity_distributor_id", FieldType.Integer, true, false, "company_activity_distributor_id");
        addField("company_activity_agreement", FieldType.Boolean, true, false, "company_activity_agreement");
        addField("approved", FieldType.Boolean, true, false, "approved");
        addField("authentified", FieldType.Boolean, true, false, "authentified");
        //addField("customer_verified_by_email", FieldType.Boolean, true, false, "customer_verified_by_email");
        //addField("customer_verified_by_email_code_sent", FieldType.Boolean, true, false, "customer_verified_by_email_code_sent");
        //addField("customer_verified_by_mobile", FieldType.Boolean, true, false, "customer_verified_by_mobile");
        //addField("customer_verified_by_mobile_code_sent", FieldType.Boolean, true, false, "customer_verified_by_mobile_code_sent");
        */
    }

    private void validateCommand(RecordProcessor record_processor) throws Exception {
        Request request = record_processor.getTableRequest();
        if (record_processor.is_select() == false && (request.value_map == null || request.value_map.size() == 0) 
                && (request.insert_field_map == null && request.update_field_map == null && request.delete_field_map == null)) {
            record_processor.addError(record_processor.getCommand() + " command misses 'values' array");
        }
        if (record_processor.is_select() == true) {
            String response = record_processor.getResponseView();
            if (response.equalsIgnoreCase("tabular") == false && response.equalsIgnoreCase("tree") == false && response.equalsIgnoreCase("process") == false) {
                record_processor.addError("Unknown response type '" + response + "' define ['tabular', 'tree', 'process']");
            } else if (request.where == null) {
                record_processor.addError("Select command misses 'where' compound object");
            } else if (request.where.clause == null) {
                record_processor.addError("Select command misses 'where.clause' property");
            } else if (request.where.values == null) {
                record_processor.addError("Select command misses 'where.values' array");
            } else if (request.order_by_list == null) {
                record_processor.addError("Select command misses 'order_by_list' array");
            }
        }
    }
    
    public void process(Gson gson, RecordProcessor record_processor, RecordHandler record_handler) throws Exception {
        record_processor.beginObject();
        record_processor.name("header");
        prepareProcess(gson, record_processor, record_handler);
        if (record_processor.hasErrors() == true) {
            record_processor.endObject();
            return;
        }
        executeProcess(gson, record_processor, record_handler);
        if (record_processor.hasErrors() == true) {
            record_processor.endObject();
            return;
        }
        record_processor.name("stats");
        statProcess(gson, record_processor);
        record_processor.endObject();
        record_processor.flush();
    }
    
    private void prepareProcess(Gson gson, RecordProcessor record_processor, RecordHandler record_handler) throws Exception {
        record_processor.beginObject();
        record_processor.name("table_alias");
        record_processor.value(record_processor.request.table_alias);
        if (record_processor.is_select() == true) {
            record_processor.query.t1 = Instant.now();
            prepareSelectStatement(gson, record_processor, record_handler);
        } else if (record_processor.is_update() == true) {

        } else if (record_processor.is_insert() == true) {

        } else if (record_processor.is_delete() == true) {

        }
        if (record_processor.hasErrors() == true) {
            return;
        }
        if (child_table_map == null || child_table_map.size() == 0) {
            record_processor.endObject();
            return;
        }
        record_processor.name("children");
        record_processor.beginArray();
        for (int i = 0; i < record_processor.child_list.size(); i++) {
            RecordProcessor child_record_processor = record_processor.child_list.get(i);
            Table child_table = child_table_map.get(child_record_processor.request.table_alias);
            if (child_table == null) {
                record_processor.addError("Child Table aliased '"+child_record_processor.request.table_alias+"' is null of Parent Table aliased '"+request_table.table_alias+"'");
            }
            child_table.prepareProcess(gson, child_record_processor, record_handler);
        }
        record_processor.endArray();
        record_processor.endObject();
    }
    
    private void executeProcess(Gson gson, RecordProcessor record_processor, RecordHandler record_handler) throws Exception {
        try (Connection connection = record_handler.getDatabaseConnection(data_datasource_name)) {
            if (record_processor.is_select() == true) {
                record_processor.name("resultset");
                record_processor.beginArray();
                executeSelect(gson, connection, record_processor, record_handler);
                record_processor.endArray();
            } else if (record_processor.is_update() == true) {

            } else if (record_processor.is_insert() == true) {

            } else if (record_processor.is_delete() == true) {

            }
            if (record_processor.hasErrors() == true) {
                return;
            }
            record_processor.flush();
        } catch (Exception sqlx) {
            throw sqlx;
        }
    }
    
    public void statProcess(Gson gson, RecordProcessor record_processor) throws Exception {
        record_processor.beginObject();
        record_processor.name(record_processor.request.table_alias);
        record_processor.beginObject();
        record_processor.query_stats();
        if (child_table_map == null || child_table_map.size() == 0) {
            record_processor.endObject();
            record_processor.endObject();
            return;
        }
        record_processor.name("children");
        record_processor.beginArray();
        for (int i = 0; i < record_processor.child_list.size(); i++) {
            RecordProcessor child_record_processor = record_processor.child_list.get(i);
            Table child_table = child_table_map.get(child_record_processor.request.table_alias);
            if (child_table == null) {
                record_processor.addError("Child Table aliased '"+child_record_processor.request.table_alias+"' is null of Parent Table aliased '"+request_table.table_alias+"'");
            }
            child_table.statProcess(gson, child_record_processor);
        }
        record_processor.endArray();
        record_processor.endObject();
        record_processor.endObject();
    }
}
