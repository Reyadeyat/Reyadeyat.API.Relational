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

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.reyadeyat.api.library.util.BooleanParser;

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
public class Field implements Cloneable, Comparable<Field> {

    static final public ArrayList<Integer> operation_list = new ArrayList<Integer>();
    static final public Integer SELECT = 1 << 0;
    static final public Integer UPDATE = 1 << 1;
    static final public Integer INSERT = 1 << 2;
    static final public Integer DELETE = 1 << 3;
    static final public Integer WHERE = 1 << 4;
    static final public Integer HAVING = 1 << 5;
    private Integer SQL_MASK;
    private Integer SQL_MANDATORY_MASK;
    private Integer SQL_IGNORED_MASK;
    private HashMap<Integer, FieldDefaultValue> default_value;
    private HashMap<Integer, FieldProcessor> processor;//Integer = operation

    private final FieldType field_type;
    private PrimartKeyType pt;
    private String table_name;
    private String table_alias;
    private final Boolean nullable;
    private final Boolean group;
    private final Integer index;
    private final String name;
    private final String alias;
    private final String statementName;
    private final String statementAalias;
    private String select_formula;
    private String insert_formula;
    private String update_formula;
    private Integer mins, maxs;
    private Number minn, maxn;
    private Timestamp mindts, maxdts;
    private String dtf_mindts, dtf_maxdts;
    private ArrayList<String> set;
    private String select;
    private Boolean unique_any;
    private Boolean unique_all;
    private Boolean is_variable;
    private String variable_id;
    private String variable_default_value;
    private String variable_formula;
    private Boolean orderby;
    private Integer orderbyOrder;
    private Boolean is_foreign_key, is_inner_join;
    private ArrayList<String> formula_parts;

    private String overwriteWhereCondition;

    static final private JsonPrimitive FNIND = new JsonPrimitive("Field name is not defined");

    //final static private DateTimeFormatter dtfDT = DateTimeFormatter.ISO_DATE_TIME;
    final static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    final static private SimpleDateFormat stf = new SimpleDateFormat("HH:mm:ss.SSS");
    final static private SimpleDateFormat stsf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");//YYYY-MM-DD hh:mm:ss.SSS
    //final static private SimpleDateFormat stzf = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ss.SSSZ");//YYYY-MM-DDThh:mm:ss.SSSZ
    final static private SimpleDateFormat stzf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");//YYYY-MM-DDThh:mm:ss.SSSZ

    public Field(String table_name, String table_alias, Integer index, String name, String alias, Boolean nullable, Boolean group, FieldType field_type, JsonArray table_error_list) {
        if (operation_list.size() == 0) {
            operation_list.add(SELECT);
            operation_list.add(UPDATE);
            operation_list.add(INSERT);
            operation_list.add(DELETE);
            operation_list.add(WHERE);
            operation_list.add(HAVING);
        }
        this.field_type = field_type;
        this.index = index;
        this.pt = PrimartKeyType.NotPrimaryKey;
        this.table_name = table_name;
        this.table_alias = table_alias;
        this.nullable = nullable;
        this.group = group;
        this.name = name;
        this.alias = alias;
        this.statementName = (this.name == null || this.name.isBlank() == true ? null : "`" + (this.table_alias == null ? this.table_name : this.table_alias) + "`.`" + this.name + "`");
        this.statementAalias = (this.alias == null || this.alias.isBlank() == true ? null : "`" + this.alias + "`");
        this.insert_formula = null;
        this.select_formula = null;
        this.update_formula = null;
        this.set = null;
        this.mins = this.maxs = 0;
        this.minn = this.maxn = null;
        this.mindts = this.maxdts = null;
        this.is_variable = false;
        this.orderby = this.unique_any = this.unique_all = false;
        this.is_foreign_key = is_inner_join = false;
        SQL_MASK = SELECT | UPDATE | INSERT;
        SQL_MANDATORY_MASK = 0;
        SQL_IGNORED_MASK = 0;

        this.default_value = new HashMap<Integer, FieldDefaultValue>();
        this.processor = new HashMap<Integer, FieldProcessor>();

        if (name == null /*|| name.length() == 0*/) {
            table_error_list.add("Field name is null");
        }

        if (nullable == null) {
            table_error_list.add("Field '" + name + "' nullability is not defined");
        }

        if (group == null) {
            table_error_list.add("Field '" + name + "' grouping is not defined");
        }

        if (table_name == null || table_name.length() == 0) {
            table_error_list.add("Table name is null for Field '" + name + "'");
        }

        if (alias == null || alias.length() == 0) {
            table_error_list.add("Field '" + name + "' alias is not defined");
        }

        if (table_error_list != null && table_error_list.size() > 0) {
            return;
        }

        if (alias != null && name != null && table_name != null && alias.isBlank() == false && name.isBlank() == false && table_name.isBlank() == false) {
            select = this.statementName + " AS " + this.statementAalias;
        } else if (name != null && name.isBlank() == false) {
            select = this.statementName;
        }
    }

    public Field defineSet(String... set) {
        this.set = new ArrayList<>(Arrays.asList(set));
        return this;
    }

    public Field defineSelectFormula(String select_formula, JsonArray error_list) {
        this.select_formula = select_formula;
        //disallow(Field.INSERT);
        //disallow(Field.UPDATE);
        if (alias != null && select_formula != null && select_formula.isBlank() == false && alias.isBlank() == false) {
            select = this.select_formula + " AS " + this.statementAalias;
            if (error_list.contains(FNIND) == true) {
                error_list.remove(FNIND);
            }
        }

        return this;
    }

    public Field defineUpdateFormula(String update_formula) {
        this.update_formula = update_formula;
        return this;
    }

    public Field defineInsertFormula(String insert_formula) {
        this.insert_formula = insert_formula;
        return this;
    }

    public Boolean isVariable() {
        return this.is_variable;
    }

    public Field setAsVariable(String variable_id, String variable_default_value, String formula, JsonArray error_list) throws Exception {
        this.is_variable = true;
        this.variable_id = variable_id;
        this.variable_default_value = variable_default_value;
        this.variable_formula = formula;
        //disallow(Field.INSERT);
        //disallow(Field.UPDATE);
        if (alias != null && alias.isBlank() == false) {
            select = " AS " + this.statementAalias;
            if (error_list.contains(FNIND) == true) {
                error_list.remove(FNIND);
            }
        } else {
            throw new Exception("Variable '"+variable_id+"' doesn't has proper alias '"+this.alias+"'.");
        }
        return this;
    }

    public Boolean hasFormulaDefined() {
        return this.insert_formula != null || this.select_formula != null || this.update_formula != null;
    }

    public Boolean isInsertFormulaDefined() {
        return this.insert_formula != null;
    }

    public Boolean isSelectFormulaDefined() {
        return this.select_formula != null;
    }

    public Boolean isUpdateFormulaDefined() {
        return this.update_formula != null;
    }

    public String getInsertFormulaDefined() {
        return this.insert_formula;
    }

    public String getSelectFormulaDefined() {
        return this.select_formula;
    }

    public String getUpdateFormulaDefined() {
        return this.update_formula;
    }

    public String toString() {
        return select;
    }

    public Boolean isPrimaryKey() {
        return pt == PrimartKeyType.PrimaryKey || pt == PrimartKeyType.PrimaryKeyAI || pt == PrimartKeyType.PrimaryKeyMI;
    }

    public Boolean isPrimaryKeyNone() {
        return pt == PrimartKeyType.NotPrimaryKey;
    }

    public Boolean isPrimaryKeyAI() {
        return pt == PrimartKeyType.PrimaryKeyAI;
    }

    public Boolean isPrimaryKeyMI() {
        return pt == PrimartKeyType.PrimaryKeyMI;
    }

    public Field setPrimaryKey(JsonArray error_list) throws Exception {
        this.pt = PrimartKeyType.PrimaryKey;
        this.disallow(UPDATE, error_list);
        return this;
    }

    public Field setPrimaryKeyAI(JsonArray error_list) throws Exception {
        this.pt = PrimartKeyType.PrimaryKeyAI;
        this.disallow(INSERT, error_list);
        this.disallow(UPDATE, error_list);
        return this;
    }

    public Field setPrimaryKeyMI(JsonArray error_list) throws Exception {
        this.pt = PrimartKeyType.PrimaryKeyMI;
        //this.disallow(INSERT);
        this.disallow(UPDATE, error_list);
        return this;
    }

    public Field setUniqueAny() {
        unique_any = true;
        return this;
    }

    public Boolean isUniqueAny() {
        return unique_any;
    }

    public Field setUniqueAll() {
        unique_all = true;
        return this;
    }

    public Boolean isUniqueAll() {
        return unique_all;
    }

    public Field setOrderBy(Integer orderbyOrder) {
        orderby = true;
        this.orderbyOrder = orderbyOrder;
        return this;
    }

    public Integer getOrderByOrder() {
        return orderbyOrder;
    }

    public Boolean isOrderBy() {
        return orderby;
    }

    public Field allow(Integer operation, JsonArray error_list) {
        if (isValidSqlOperation(operation) == false) {
            error_list.add("Field '" + alias + "' 'allow' passed a wrong operation code[" + operation + "]");
        }
        SQL_MASK |= operation;
        return this;
    }

    public Field disallow(Integer operation, JsonArray error_list) {
        if (isValidSqlOperation(operation) == false) {
            error_list.add("Field '" + alias + "' 'disallow' passed a wrong operation code[" + operation + "]");
        }
        SQL_MASK &= ~operation;
        return this;
    }

    public Boolean isAllowedTo(Integer operation) {
        return (SQL_MASK & operation) != 0;
    }

    public Boolean isValidSqlOperation(Integer operation) {
        return (operation & (SELECT | UPDATE | INSERT | WHERE | HAVING)) != 0;
    }
    
    public ArrayList<Integer> getRequestedOperations(Integer operation_register) {
        ArrayList<Integer> requested_operation_list = new ArrayList<Integer>();
        for (Integer op : Field.operation_list) {
            if ((operation_register & op) != 0) {
                requested_operation_list.add(op);
            }
        }
        return requested_operation_list;
    }

    public Field setMandatoryFor(Integer operation, JsonArray error_list) {
        if (isValidSqlOperation(operation) == false) {
            error_list.add("Field '" + alias + "' 'setMandatoryFor' passed a wrong operation code[" + operation + "]");
        }
        SQL_MANDATORY_MASK |= operation;
        return this;
    }

    public Boolean isMandatoryFor(Integer operation) {
        return (SQL_MANDATORY_MASK & operation) != 0;
    }
    
    public Field setIgnoredFor(Integer operation, JsonArray error_list) {
        if (isValidSqlOperation(operation) == false) {
            error_list.add("Field '" + alias + "' 'setMandatoryFor' passed a wrong operation code[" + operation + "]");
        }
        SQL_IGNORED_MASK |= operation;
        return this;
    }

    public Boolean isIgnoredFor(Integer operation) {
        return (SQL_IGNORED_MASK & operation) != 0;
    }

    public Boolean hasDefaultValueFor(Integer operation) {
        return this.default_value.containsKey(operation);
    }

    public Field setDefaultValueFor(Integer operation, Object value, JsonArray error_list) throws Exception {
        if (isValidSqlOperation(operation) == false) {
            error_list.add("Field '" + alias + "' 'setDefaultValue' passed a wrong operation code[" + operation + "]");
        }
        this.default_value.put(operation, new FieldDefaultValue(this.field_type, value, false));
        return this;
    }

    public Field setDefaultExpressionFor(Integer operation, String expression, JsonArray error_list) throws Exception {
        if (isValidSqlOperation(operation) == false) {
            error_list.add("Field '" + alias + "' 'setDefaultValue' passed a wrong operation code[" + operation + "]");
        }
        ArrayList<Integer> operation_list = this.getRequestedOperations(operation);
        for (Integer op : operation_list) {
            this.default_value.put(op, new FieldDefaultValue(this.field_type, expression, true));
        }
        return this;
    }

    public Object getDefaultValueFor(Integer operation, JsonArray error_list) throws Exception {
        if (isValidSqlOperation(operation) == false) {
            error_list.add("Field '" + alias + "' 'setDefaultValue' passed a wrong operation code[" + operation + "]");
        }
        return this.default_value.get(operation).getValue();
    }

    public String getDefaultSQLValueFor(Integer operation, JsonArray error_list) throws Exception {
        if (isValidSqlOperation(operation) == false) {
            error_list.add("Field '" + alias + "' 'setDefaultValue' passed a wrong operation code[" + operation + "]");
        }
        return this.default_value.get(operation).getSQLValue();
    }

    public Field setProcessor(Integer operation, FieldProcessor fieldProcessor, JsonArray error_list) throws Exception {
        if (isValidSqlOperation(operation) == false) {
            error_list.add("Field '" + alias + "' 'setDefaultValue' passed a wrong operation code[" + operation + "]");
        }
        this.processor.put(operation, fieldProcessor);
        return this;
    }

    public String getPreProcessedValue(Integer operation, String value, JsonArray error_list) throws Exception {
        if (isValidSqlOperation(operation) == false) {
            error_list.add("Field '" + alias + "' 'setDefaultValue' passed a wrong operation code[" + operation + "]");
        }
        if (this.processor.size() == 0 || this.processor.get(operation) == null) {
            return value;
        }
        return this.processor.get(operation).preProcessedValue(this, operation, value, error_list);
    }

    public Object getPostProcessedValue(Integer operation, Object value, JsonArray error_list) throws Exception {
        if (isValidSqlOperation(operation) == false) {
            error_list.add("Field '" + alias + "' 'setDefaultValue' passed a wrong operation code[" + operation + "]");
        }
        if (this.processor.size() == 0 || this.processor.get(operation) == null) {
            return value;
        }
        return this.processor.get(operation).postProcessedValue(this, operation, value, error_list);
    }

    public Boolean is_foreign_key() {
        return is_foreign_key;
    }

    public Field setForeignKey() {
        is_foreign_key = true;
        return this;
    }

    public Field setInnerJoin() {
        is_inner_join = true;
        return this;
    }

    @Override
    public int compareTo(Field f) {
        if (this.index.intValue() < f.index.intValue()) {
            return -1;
        } else if (this.index.intValue() == f.index.intValue()) {
            return 0;
        }

        return 1;
    }
    
    public String getSQLInsertName() {
        return name;
    }

    public String getSQLName() {
        if (select_formula != null && select_formula.isBlank() == false) {
            return select_formula;
        }
        return statementName;
    }

    public String getSQLAlias() {
        return statementAalias;
    }

    public String getAlias() {
        return alias;
    }

    public String getName() {
        return name;
    }
    
    public String getSelect() {
        return select;
    }

    public String getSelect(Map<String, String> variable_map) throws Exception {
        if (variable_map == null) {
            return select;
        }
        String variable_value = variable_map.get(this.alias);
        if (variable_value == null) {
            return getQuotable(getFieldString(getFieldObject(variable_default_value))) + select;
        }
        if (this.variable_formula != null && this.variable_formula.length() > 0) {
            return this.variable_formula.replace("?", getQuotable(getFieldString(getFieldObject(variable_value)))) + select;
        }
        return getQuotable(getFieldString(getFieldObject(variable_value))) + select;
    }

    public String getTable() {
        return table_name;
    }

    public String getHaving() {
        if (select_formula == null || select_formula.isBlank() == true) {
            return name;
        }
        return select_formula;
    }

    public String getGroupBy() {
        if (select_formula != null && select_formula.isBlank() == false) {
            return select_formula;
        }
        return statementName;
    }

    public String getOrderBy() {
        if (select_formula != null && select_formula.isBlank() == false) {
            return select_formula;
        }
        return statementName;
    }

    public void overwriteWhereCondition(String overwriteWhereCondition) {
        this.overwriteWhereCondition = overwriteWhereCondition;
    }

    public Boolean hasOverwriteWhereCondition() {
        return this.overwriteWhereCondition != null;
    }

    public String getOverwriteWhereCondition() {
        return this.overwriteWhereCondition;
    }

    public Field setTexLengthRange(Number minn, Number maxn) {
        this.mins = mins;
        this.maxs = maxs;
        return this;
    }

    public Field setNumberRange(Number minn, Number maxn) {
        this.minn = minn;
        this.maxn = maxn;
        return this;
    }

    public Field setDateTimeRange(Timestamp mindts, Timestamp maxdts) throws Exception {
        this.mindts = mindts;
        this.maxdts = maxdts;
        if (field_type == FieldType.Date) {
            this.dtf_mindts = getSqlDate((java.sql.Date) Date.from(this.mindts.toInstant()));
            this.dtf_maxdts = getSqlDate((java.sql.Date) Date.from(this.mindts.toInstant()));
        } else if (field_type == FieldType.Time) {
            this.dtf_mindts = getSqlTime((java.sql.Time) Time.from(this.mindts.toInstant()));
            this.dtf_maxdts = getSqlTime((java.sql.Time) Time.from(this.maxdts.toInstant()));
        } else if (field_type == FieldType.Timestamp) {
            this.dtf_mindts = getSqlTimestamp(this.mindts);
            this.dtf_maxdts = getSqlTimestamp(this.maxdts);
        }
        return this;
    }

    public String getFieldString(Object field_value) throws Exception {
        if (field_value == null) {
            return null;
        } else if (field_value instanceof String) {
            if (field_type != FieldType.String && field_type != FieldType.Set) {
                throw new Exception("Field '" + alias + "' of type '" + field_type.toString() + "' can't accepts value '" + field_value + "' of type 'String'");
            }
            return String.valueOf(field_value);
        } else if (field_value instanceof Long) {
            return String.valueOf(field_value);
        } else if (field_value instanceof Integer) {
            return String.valueOf(field_value);
        } else if (field_value instanceof Double) {
            return String.valueOf(field_value);
        } else if (field_value instanceof Boolean) {
            return String.valueOf(field_value);
        } else if (field_value instanceof Date) {
            return getSqlDate((java.sql.Date) field_value);
        } else if (field_value instanceof Time) {
            return getSqlTime((java.sql.Time) field_value);
        } else if (field_value instanceof Timestamp) {
            return getSqlTimestamp((java.sql.Timestamp) field_value);
        } else if (field_value instanceof java.time.LocalDateTime) {
            return getSqlTimestamp(Timestamp.valueOf((java.time.LocalDateTime) field_value));
        }

        throw new Exception("getFieldString passed unhandeled instance of type => " + field_value.getClass().getName());
    }

    public String getFieldString(String field_value) throws Exception {
        return field_value;
    }

    public String getFieldString(Boolean field_value) throws Exception {
        return String.valueOf(field_value);
    }

    public String getFieldString(Integer field_value) throws Exception {
        return String.valueOf(field_value);
    }

    public String getFieldString(Long field_value) throws Exception {
        return String.valueOf(field_value);
    }

    public String getFieldString(Double field_value) throws Exception {
        return String.valueOf(field_value);
    }

    public String getFieldString(java.sql.Date field_value) throws Exception {
        return getSqlDate(field_value);
    }

    public String getFieldString(java.sql.Time field_value) throws Exception {
        return getSqlTime(field_value);
    }

    public String getFieldString(java.sql.Timestamp field_value) throws Exception {
        return getSqlTimestamp(field_value);
    }

    public Object getFieldObject(String field_value) throws Exception {
        try {
            if (field_value == null) {
                return null;
            } else if (field_type == FieldType.String) {
                return field_value;
            } else if (field_type == FieldType.Set) {
                for (String entry : set) {
                    if (entry.equals(entry) == true) {
                        return field_value;
                    }
                }
            } else if (field_type == FieldType.Long) {
                return Long.parseLong(field_value);
            } else if (field_type == FieldType.Integer) {
                return Integer.parseInt(field_value);
            } else if (field_type == FieldType.Double || field_type == FieldType.BigDecimal) {
                return Double.parseDouble(field_value);
            } else if (field_type == FieldType.Boolean) {
                //return Boolean.parseBoolean(field_value);
                return BooleanParser.parse(field_value);
            } else if (field_type == FieldType.Date) {
                return getSqlDate(field_value);
            } else if (field_type == FieldType.Time) {
                return getSqlTime(field_value);
            } else if (field_type == FieldType.Timestamp) {
                return getSqlTimestamp(field_value);
            }
        } catch (Exception ex) {
            throw new Exception("getFieldObject Formt Exception for field '"+name+" AS "+alias+"' passed unhandeled instance of type " + field_type.toString() + " => " + field_value);
        }

        throw new Exception("getFieldObject field '"+name+" AS "+alias+"' passed unhandeled instance of type " + field_type.toString() + " => " + field_value);
    }

    static public Object getFieldObject(String field_value, String dataType) throws Exception {
        if (field_value == null) {
            return null;
        } else if (dataType.equalsIgnoreCase("String")) {
            return field_value;
        } else if (dataType.equalsIgnoreCase("Long")) {
            return Long.parseLong(field_value);
        } else if (dataType.equalsIgnoreCase("Integer")) {
            return Integer.parseInt(field_value);
        } else if (dataType.equalsIgnoreCase("Double")) {
            return Double.parseDouble(field_value);
        } else if (dataType.equalsIgnoreCase("Boolean")) {
            //return Boolean.parseBoolean(field_value);
            return BooleanParser.parse(field_value);
        } else if (dataType.equalsIgnoreCase("Date")) {
            return getSqlDate(field_value);
        } else if (dataType.equalsIgnoreCase("Time")) {
            return getSqlTime(field_value);
        } else if (dataType.equalsIgnoreCase("Timestamp")) {
            return getSqlTimestamp(field_value);
        }

        throw new Exception("getFieldObject(field_value, dataType) passed unhandeled instance of type '" + dataType + "' => '" + field_value + "'");
    }

    static public java.sql.Date getSqlDate(String date) throws Exception {
        return new java.sql.Date(sdf.parse(date).getTime());
    }

    static public String getSqlDate(java.util.Date date) throws Exception {
        return date == null ? null : sdf.format(date);
    }
    
    static public String getSqlDate(java.sql.Date date) throws Exception {
        return date == null ? null : sdf.format(date);
    }
    
    static public String getSqlDate(Instant instant) throws Exception {
        return instant == null ? null : sdf.format(Date.from(instant));
    }

    static public java.sql.Time getSqlTime(String time) throws Exception {
        return new java.sql.Time(stf.parse(time).getTime());
    }
    
    static public String getSqlTime(java.util.Date time) throws Exception {
        return time == null ? null : stf.format(time);
    }

    static public String getSqlTime(java.sql.Time time) throws Exception {
        return time == null ? null : stf.format(time);
    }
    
    static public String getSqlTime(Instant instant) throws Exception {
        return instant == null ? null : stf.format(Date.from(instant));
    }

    static public java.sql.Timestamp getSqlTimestamp(String timestamp) throws Exception {
        return new java.sql.Timestamp(stsf.parse(timestamp).getTime());
        //return Timestamp.from(Instant.from(OffsetDateTime.parse(timestamp, dtfDT)));
    }
    
    static public String getSqlTimestamp(java.util.Date timestamp) throws Exception {
        return timestamp == null ? null : stsf.format(timestamp);
    }

    static public String getSqlTimestamp(java.sql.Timestamp timestamp) throws Exception {
        return timestamp == null ? null : stsf.format(timestamp);
    }
    
    static public String getSqlTimestamp(Instant instant) throws Exception {
        return instant == null ? null : stsf.format(Date.from(instant));
    }

    static public java.sql.Timestamp getSqlTimeZone(String timezone) throws Exception {
        return new java.sql.Timestamp(stzf.parse(timezone).getTime());
        //return Timestamp.from(Instant.from(OffsetDateTime.parse(timestamp, dtfDT)));
    }
    
    static public String getSqlTimeZone(java.util.Date timezone) throws Exception {
        return timezone == null ? null : stzf.format(timezone);
    }

    static public String getSqlTimeZone(java.sql.Timestamp timezone) throws Exception {
        return timezone == null ? null : stzf.format(timezone);
    }
    
    static public String getSqlTimeZone(Instant instant) throws Exception {
        return instant == null ? null : stzf.format(instant);
    }

    public Boolean isNullable() {
        return nullable;
    }

    public Boolean isValid(Integer operation, String string, StringBuilder error) throws Exception {
        /*if (isSelectFormulaDefined() == true && operation == SELECT && isAllowedTo(operation)) {
            return true;
        } else if (isInsertFormulaDefined() == true && operation == INSERT && isAllowedTo(operation)) {
            return true;
        } else if (isUpdateFormulaDefined()== true && operation == UPDATE && isAllowedTo(operation)) {
            return true;
        }*/
        error.setLength(0);
        error.append("Field '" + this.alias + "' check value '" + string + "' againest [" + field_type.toString() + "] data type, result => ");
        if (string == null) {
            if (nullable == false) {
                error.append("Field '" + this.alias + "' can't be 'null' value");
            }
            return nullable;
        } else if (field_type == FieldType.String) {
            if (string.length() < mins && string.length() > maxs) {
                error.append(" - [String] length out of range (" + this.mins + "," + this.maxs + ")");
                return false;
            }
            return true;
        } else if (field_type == FieldType.Integer) {
            try {
                Integer i = Integer.valueOf(string);
                if (minn != null && maxn != null && (i < minn.intValue() || i > maxn.intValue())) {
                    error.append(" - [Integer] value out of range (" + this.minn + "," + this.maxn + ")");
                    return false;
                }
                return true;
            } catch (Exception x) {
                error.append(" - [Integer] value parsing exception ['" + x.getMessage() + "']");
                return false;
            }
        } else if (field_type == FieldType.Long) {
            try {
                Long l = Long.valueOf(string);
                if (minn != null && maxn != null && (l < minn.intValue() || l > maxn.intValue())) {
                    error.append(" - [Long] value out of range (" + this.minn + "," + this.maxn + ")");
                    return false;
                }
                return true;
            } catch (Exception x) {
                error.append(" - [Long] value parsing exception ['" + x.getMessage() + "']");
                return false;
            }
        } else if (field_type == FieldType.Double || field_type == FieldType.BigDecimal) {
            try {
                Double d = Double.valueOf(string);
                if (minn != null && maxn != null && (d < minn.intValue() || d > maxn.intValue())) {
                    error.append(" - [Double] value out of range (" + this.minn + "," + this.maxn + ")");
                    return false;
                }
                return true;
            } catch (Exception x) {
                error.append(" - [Double] value parsing exception ['" + x.getMessage() + "']");
                return false;
            }
        } else if (field_type == FieldType.Boolean) {
            try {
                //Boolean b = Boolean.valueOf(string);
                Boolean b = BooleanParser.parse(string);
                return true;
            } catch (Exception x) {
                error.append(" - [Boolean] value parsing exception ['" + x.getMessage() + "']");
                return false;
            }
        } else if (field_type == FieldType.Date) {
            try {
                Date d = getSqlDate(string);
                if (mindts != null && maxdts != null && (d.getTime() < mindts.getTime() || d.getTime() > maxdts.getTime())) {
                    error.append(" - [Date] value out of range (" + dtf_mindts + "," + dtf_maxdts + ")");
                    return false;
                }
                return true;
            } catch (Exception x) {
                error.append(" - [Date] value parsing exception ['" + x.getMessage() + "']");
                return false;
            }
        } else if (field_type == FieldType.Time) {
            try {
                Time t = getSqlTime(string);
                if (mindts != null && maxdts != null && (t.getTime() < mindts.getTime() || t.getTime() > maxdts.getTime())) {
                    error.append(" - [Time] value out of range (" + dtf_mindts + "," + dtf_maxdts + ")");
                    return false;
                }
                return true;
            } catch (Exception x) {
                error.append(" - [Time] value parsing exception ['" + x.getMessage() + "']");
                return false;
            }
        } else if (field_type == FieldType.Timestamp) {
            try {
                Timestamp ts = getSqlTimestamp(string);
                if (mindts != null && maxdts != null && (ts.getTime() < mindts.getTime() || ts.getTime() > maxdts.getTime())) {
                    error.append(" - [Timestamp] value out of range (" + dtf_mindts + "," + dtf_maxdts + ")");
                    return false;
                }
                return true;
            } catch (Exception x) {
                error.append(" - [Timestamp] value parsing exception ['" + x.getMessage() + "']");
                return false;
            }
        } else if (field_type == FieldType.TimeZone) {
            try {
                Timestamp ts = getSqlTimeZone(string);
                if (mindts != null && maxdts != null && (ts.getTime() < mindts.getTime() || ts.getTime() > maxdts.getTime())) {
                    error.append(" - [TimeZone] value out of range (" + dtf_mindts + "," + dtf_maxdts + ")");
                    return false;
                }
                return true;
            } catch (Exception x) {
                error.append(" - [TimeZone] value parsing exception ['" + x.getMessage() + "']");
                return false;
            }
        } else if (field_type == FieldType.Set) {
            for (String entry : set) {
                if (entry.equals(string) == true) {
                    return true;
                }
            }
            return false;
        }
        throw new Exception("Undefined field type '" + field_type + "'");
    }

    public Boolean isBoolean() {
        return field_type.isBoolean();
    }

    public Boolean isNumeric() {
        return field_type.isNumeric();
    }

    public Boolean isText() {
        return field_type.isText();
    }
    
    public Boolean isQuotable() {
        return field_type.isQuotable();
    }

    public Boolean isDateTime() {
        return field_type.isDateTime();
    }

    public Boolean isGroup() {
        return this.group;
    }

    public String getQuotable(String value) {
        return field_type.isQuotable() == true ? "'" + value.replace("'", "''") + "'" : value;
    }

    public Boolean parseBoolean(Object value) throws Exception {
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        } else if (value instanceof String) {
            //return Boolean.parseBoolean((String) value);
            return BooleanParser.parse((String) value);
        } else if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return false;
    }
    
    static private Class getClassName(String className) throws Exception {
        if (className.equalsIgnoreCase("java.lang.String") == true
                || className.equalsIgnoreCase("String") == true) {
            return java.lang.String.class;
        } else if (className.equalsIgnoreCase("java.lang.Byte") == true
                || className.equalsIgnoreCase("Byte") == true) {
            return java.lang.Byte.class;
        } else if (className.equalsIgnoreCase("java.lang.Short") == true
                || className.equalsIgnoreCase("Short") == true) {
            return java.lang.Short.class;
        } else if (className.equalsIgnoreCase("java.lang.Long") == true
                || className.equalsIgnoreCase("Long") == true) {
            return java.lang.Long.class;
        } else if (className.equalsIgnoreCase("java.lang.Integer") == true
                || className.equalsIgnoreCase("int") == true
                || className.equalsIgnoreCase("Integer") == true
                || className.equalsIgnoreCase("Number") == true) {
            return java.lang.Integer.class;
        } else if (className.equalsIgnoreCase("java.lang.Float") == true
                || className.equalsIgnoreCase("Float") == true) {
            return java.lang.Float.class;
        } else if (className.equalsIgnoreCase("java.lang.Double") == true
                || className.equalsIgnoreCase("Decimal") == true
                || className.equalsIgnoreCase("Double") == true) {
            return java.lang.Double.class;
        } else if (className.equalsIgnoreCase("java.lang.Date") == true
                || className.equalsIgnoreCase("java.sql.Date") == true
                || className.equalsIgnoreCase("Date") == true) {
            return java.sql.Date.class;
        } else if (className.equalsIgnoreCase("java.sql.Time") == true
                || className.equalsIgnoreCase("Time") == true) {
            return java.sql.Date.class;
        } else if (className.equalsIgnoreCase("java.sql.Timestamp") == true
                || className.equalsIgnoreCase("DateTime") == true
                || className.equalsIgnoreCase("Timestamp") == true) {
            return java.sql.Date.class;
        } else if ((className.equalsIgnoreCase("TINYINT") || className.equalsIgnoreCase("TINYINT UNSIGNED") /*&& size == 1 /*mysql*/)
                || className.equalsIgnoreCase("BOOLEAN"/*informix*/)) {
            return Boolean.class;
        } else if (className.equalsIgnoreCase("TINYINT") || className.equalsIgnoreCase("TINYINT UNSIGNED")) {
            return Byte.class;
        } else if (className.equalsIgnoreCase("SMALLINT") || className.equalsIgnoreCase("SMALLINT UNSIGNED")) {
            return Short.class;
        } else if (className.equalsIgnoreCase("MEDIUMINT") || className.equalsIgnoreCase("INT") || className.equalsIgnoreCase("INTEGER")
                || className.equalsIgnoreCase("MEDIUMINT UNSIGNED") || className.equalsIgnoreCase("INT UNSIGNED") || className.equalsIgnoreCase("INTEGER UNSIGNED")
                || className.equalsIgnoreCase("BIT") || className.equalsIgnoreCase("BIT UNSIGNED")
                || className.equalsIgnoreCase("INT IDENTITY"/*sqlserver*/)
                || className.equalsIgnoreCase("SERIAL"/*informix*/)) {
            return Integer.class;
        } else if (className.equalsIgnoreCase("BIGINT")
                || className.equalsIgnoreCase("BIGINT UNSIGNED")
                || className.equalsIgnoreCase("BIGINT IDENTITY"/*sqlserver*/)
                || className.equalsIgnoreCase("SERIAL8"/*informix*/)
                || className.equalsIgnoreCase("INT8"/*informix*/)
                || className.equalsIgnoreCase("BIGSERIAL"/*informix*/)) {
            return Long.class;
        } else if (className.equalsIgnoreCase("FLOAT") || className.equalsIgnoreCase("FLOAT UNSIGNED")) {
            return Float.class;
        } else if (className.equalsIgnoreCase("DOUBLE") || className.equalsIgnoreCase("DOUBLE UNSIGNED")) {
            return Double.class;
        } else if (className.equalsIgnoreCase("NUMERIC") || className.equalsIgnoreCase("DECIMAL")
            || className.equalsIgnoreCase("NUMERIC UNSIGNED") || className.equalsIgnoreCase("DECIMAL UNSIGNED")
            || className.equalsIgnoreCase("UNIQUEIDENTIFIER")/*sqlserver*/) {
            //if (decimalDigits == 0) {
                return Long.class;
            /*} else {
                return Double.class;
            }*/
        } else if (className.equalsIgnoreCase("YEAR")
            || className.equalsIgnoreCase("DATE")) {
            return java.sql.Date.class;
        } else if (className.equalsIgnoreCase("TIME")) {
            return java.sql.Time.class;
        } else if (className.equalsIgnoreCase("DATETIME")
            || className.equalsIgnoreCase("TIMESTAMP")
            || className.equalsIgnoreCase("SMALLDATETIME")
            || className.toLowerCase().startsWith("datetime"/*informix*/)) {
            return java.sql.Timestamp.class;
        } else if (className.equalsIgnoreCase("CHAR")
            || className.equalsIgnoreCase("ENUM")
            || className.equalsIgnoreCase("SET")
            || className.equalsIgnoreCase("VARCHAR")
            || className.equalsIgnoreCase("TINYTEXT")
            || className.equalsIgnoreCase("TEXT")
            || className.equalsIgnoreCase("MEDIUMTEXT")
            || className.equalsIgnoreCase("LONGTEXT")
            || className.equalsIgnoreCase("NVARCHAR"/*sqlserver*/)
            || className.equalsIgnoreCase("NTEXT"/*sqlserver*/)
            || className.equalsIgnoreCase("NCHAR"/*sqlserver*/)
            || className.equalsIgnoreCase("SYSNAME"/*sqlserver*/)
            || className.equalsIgnoreCase("LVARCHAR"/*informix*/)) {
            return java.lang.String.class;
        } else if (className.equalsIgnoreCase("BINARY")
            || className.equalsIgnoreCase("VARBINARY")
            || className.equalsIgnoreCase("TINYBLOB")
            || className.equalsIgnoreCase("BLOB")
            || className.equalsIgnoreCase("CLOB")
            || className.equalsIgnoreCase("MEDIUMBLOB")
            || className.equalsIgnoreCase("LONGBLOB")
            || className.equalsIgnoreCase("IMAGE"/*sqlserver*/)) {
            return java.lang.Object.class;
        }
        
        throw new Exception("Undefined typeClass '" + className + "'");
    }
    /*
    public static void main(String[] args) throws Exception {
        StringBuilder error = new StringBuilder();
        String isoTime = "2019-06-01 12:16:43.001";
        Field f = new Field(FieldType.Date, 0, false, false, "TABLE_NAME", "FIELD_NAME", "FIELD_ALIAS");
        f.isValid(isoTime, error);
        
        f.getSqlDate(f.getSqlDate(new java.sql.Date(Instant.now().toEpochMilli())));
        f.getSqlTimestamp(f.getSqlTimestamp(f.getSqlTimestamp(isoTime)));
        String datetime = "2019-06-01 12:16:43.000";
        //System.out.println(f.getSqlDate(datetime));
        //System.out.println(f.getSqlTime(datetime));
        System.out.println(f.getSqlTimestamp(datetime));
        
        System.out.println(f.getSqlDate(new java.sql.Date(Instant.now().toEpochMilli())));
        System.out.println(f.getSqlTime(new java.sql.Time(Instant.now().toEpochMilli())));
        System.out.println(f.getSqlTimestamp(new java.sql.Timestamp(Instant.now().toEpochMilli())));
        //System.out.println(f.getSqlDate(f.getSqlDate(f.getSqlDate(new java.sql.Date(Instant.now().toEpochMilli())))));
        //System.out.println(f.getSqlTime(f.getSqlTime(f.getSqlTime(new java.sql.Time(Instant.now().toEpochMilli())))));
        //System.out.println(f.getSqlTimestamp(f.getSqlTimestamp(f.getSqlTimestamp(new java.sql.Timestamp(Instant.now().toEpochMilli())))));
    }
     */
    
    public static Class getClassOfType(String className) throws Exception {
        if (className.equalsIgnoreCase("java.lang.String") == true
                || className.equalsIgnoreCase("String") == true) {
            return java.lang.String.class;
        } else if (className.equalsIgnoreCase("java.lang.Byte") == true
                || className.equalsIgnoreCase("Byte") == true) {
            return java.lang.Byte.class;
        } else if (className.equalsIgnoreCase("java.lang.Short") == true
                || className.equalsIgnoreCase("Short") == true) {
            return java.lang.Short.class;
        } else if (className.equalsIgnoreCase("java.lang.Long") == true
                || className.equalsIgnoreCase("Long") == true) {
            return java.lang.Long.class;
        } else if (className.equalsIgnoreCase("java.lang.Integer") == true
                || className.equalsIgnoreCase("int") == true
                || className.equalsIgnoreCase("Integer") == true
                || className.equalsIgnoreCase("Number") == true) {
            return java.lang.Integer.class;
        } else if (className.equalsIgnoreCase("java.lang.Float") == true
                || className.equalsIgnoreCase("Float") == true) {
            return java.lang.Float.class;
        } else if (className.equalsIgnoreCase("java.lang.Double") == true
                || className.equalsIgnoreCase("Decimal") == true
                || className.equalsIgnoreCase("Double") == true) {
            return java.lang.Double.class;
        } else if (className.equalsIgnoreCase("java.lang.Date") == true
                || className.equalsIgnoreCase("java.sql.Date") == true
                || className.equalsIgnoreCase("Date") == true) {
            return java.sql.Date.class;
        } else if (className.equalsIgnoreCase("java.sql.Time") == true
                || className.equalsIgnoreCase("Time") == true) {
            return java.sql.Date.class;
        } else if (className.equalsIgnoreCase("java.sql.Timestamp") == true
                || className.equalsIgnoreCase("DateTime") == true
                || className.equalsIgnoreCase("Timestamp") == true) {
            return java.sql.Date.class;
        } else if ((className.equalsIgnoreCase("TINYINT") || className.equalsIgnoreCase("TINYINT UNSIGNED") /*&& size == 1 /*mysql*/)
                || className.equalsIgnoreCase("BOOLEAN"/*informix*/)) {
            return Boolean.class;
        } else if (className.equalsIgnoreCase("TINYINT") || className.equalsIgnoreCase("TINYINT UNSIGNED")) {
            return Byte.class;
        } else if (className.equalsIgnoreCase("SMALLINT") || className.equalsIgnoreCase("SMALLINT UNSIGNED")) {
            return Short.class;
        } else if (className.equalsIgnoreCase("MEDIUMINT") || className.equalsIgnoreCase("INT") || className.equalsIgnoreCase("INTEGER")
                || className.equalsIgnoreCase("MEDIUMINT UNSIGNED") || className.equalsIgnoreCase("INT UNSIGNED") || className.equalsIgnoreCase("INTEGER UNSIGNED")
                || className.equalsIgnoreCase("BIT") || className.equalsIgnoreCase("BIT UNSIGNED")
                || className.equalsIgnoreCase("INT IDENTITY"/*sqlserver*/)
                || className.equalsIgnoreCase("SERIAL"/*informix*/)) {
            return Integer.class;
        } else if (className.equalsIgnoreCase("BIGINT")
                || className.equalsIgnoreCase("BIGINT UNSIGNED")
                || className.equalsIgnoreCase("BIGINT IDENTITY"/*sqlserver*/)
                || className.equalsIgnoreCase("SERIAL8"/*informix*/)
                || className.equalsIgnoreCase("INT8"/*informix*/)
                || className.equalsIgnoreCase("BIGSERIAL"/*informix*/)) {
            return Long.class;
        } else if (className.equalsIgnoreCase("FLOAT") || className.equalsIgnoreCase("FLOAT UNSIGNED")) {
            return Float.class;
        } else if (className.equalsIgnoreCase("DOUBLE") || className.equalsIgnoreCase("DOUBLE UNSIGNED")) {
            return Double.class;
        } else if (className.equalsIgnoreCase("NUMERIC") || className.equalsIgnoreCase("DECIMAL")
            || className.equalsIgnoreCase("NUMERIC UNSIGNED") || className.equalsIgnoreCase("DECIMAL UNSIGNED")
            || className.equalsIgnoreCase("UNIQUEIDENTIFIER")/*sqlserver*/) {
            //if (decimalDigits == 0) {
                return Long.class;
            /*} else {
                return Double.class;
            }*/
        } else if (className.equalsIgnoreCase("YEAR")
            || className.equalsIgnoreCase("DATE")) {
            return java.sql.Date.class;
        } else if (className.equalsIgnoreCase("TIME")) {
            return java.sql.Time.class;
        } else if (className.equalsIgnoreCase("DATETIME")
            || className.equalsIgnoreCase("TIMESTAMP")
            || className.equalsIgnoreCase("SMALLDATETIME")
            || className.toLowerCase().startsWith("datetime"/*informix*/)) {
            return java.sql.Timestamp.class;
        } else if (className.equalsIgnoreCase("CHAR")
            || className.equalsIgnoreCase("ENUM")
            || className.equalsIgnoreCase("SET")
            || className.equalsIgnoreCase("VARCHAR")
            || className.equalsIgnoreCase("TINYTEXT")
            || className.equalsIgnoreCase("TEXT")
            || className.equalsIgnoreCase("MEDIUMTEXT")
            || className.equalsIgnoreCase("LONGTEXT")
            || className.equalsIgnoreCase("NVARCHAR"/*sqlserver*/)
            || className.equalsIgnoreCase("NTEXT"/*sqlserver*/)
            || className.equalsIgnoreCase("NCHAR"/*sqlserver*/)
            || className.equalsIgnoreCase("SYSNAME"/*sqlserver*/)
            || className.equalsIgnoreCase("LVARCHAR"/*informix*/)) {
            return java.lang.String.class;
        } else if (className.equalsIgnoreCase("BINARY")
            || className.equalsIgnoreCase("VARBINARY")
            || className.equalsIgnoreCase("TINYBLOB")
            || className.equalsIgnoreCase("BLOB")
            || className.equalsIgnoreCase("CLOB")
            || className.equalsIgnoreCase("MEDIUMBLOB")
            || className.equalsIgnoreCase("LONGBLOB")
            || className.equalsIgnoreCase("IMAGE"/*sqlserver*/)) {
            return java.lang.Object.class;
        }
        
        throw new Exception("Undefined typeClass '" + className + "'");
    }
}
