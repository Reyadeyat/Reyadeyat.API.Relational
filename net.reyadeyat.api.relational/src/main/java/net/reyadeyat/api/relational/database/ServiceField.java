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
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

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
public class ServiceField {
    private String formula;
    private String type;
    private String alias;
    
    private FieldType fieldType;
    
    public ServiceField(String formula, String type, String alias, JsonArray errorList) {
        this.formula = formula;
        this.type = type;
        this.alias = alias;
        
        if (this.type.equalsIgnoreCase("String")) {
            fieldType = FieldType.String;
        } else if (this.type.equalsIgnoreCase("Boolean")) {
            fieldType = FieldType.Boolean;
        } else if (this.type.equalsIgnoreCase("Integer")) {
            fieldType = FieldType.Integer;
        } else if (this.type.equalsIgnoreCase("Long")) {
            fieldType = FieldType.Long;
        } else if (this.type.equalsIgnoreCase("Double")) {
            fieldType = FieldType.Double;
        } else if (this.type.equalsIgnoreCase("Date")) {
            fieldType = FieldType.Date;
        } else if (this.type.equalsIgnoreCase("Time")) {
            fieldType = FieldType.Time;
        } else if (this.type.equalsIgnoreCase("Timestamp")) {
            fieldType = FieldType.Timestamp;
        } else if (this.type.equalsIgnoreCase("TimeZone")) {
            fieldType = FieldType.TimeZone;
        } else {
            errorList.add("ServiceField passed unknown data type '" + this.type + "'");
        }
    }
    
    public FieldType getFieldDataType() {
        return this.fieldType;
    }
    
    public String getSelectStatement() {
        return this.formula + " AS " + this.alias;
    }
    
    public String getAlias() {
        return this.alias;
    }
    
    public Boolean isBoolean() {
        return fieldType.isBoolean();
    }
    
    public Boolean isNumeric() {
        return fieldType.isNumeric();
    }
    
    public Boolean isText() {
        return fieldType.isText();
    }
    
    public Boolean isQuotable() {
        return fieldType.isQuotable();
    }
    
    public Boolean isDateTime() {
        return fieldType.isDateTime();
    }
    
    public Boolean parseBoolean(Object value) throws Exception {
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        } else if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return false;
    }
    
    public String getFieldString(Object field_value) throws Exception {
        if (field_value == null) {
            return null;
        } else if (field_value instanceof String) {
            if (fieldType != FieldType.String && fieldType != FieldType.Set) {
                throw new Exception("Field '" + alias + "' of type '" + fieldType.toString() + "' can't accepts value '" + field_value + "' of type 'String'");
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
            return Field.getSqlDate((java.sql.Date)field_value);
        } else if (field_value instanceof Time) {
            return Field.getSqlTime((java.sql.Time)field_value);
        } else if (field_value instanceof Timestamp) {
            return Field.getSqlTimestamp((java.sql.Timestamp)field_value);
        }
        
        throw new Exception("getFieldString passed unhandeled instance of type => " + field_value.getClass().getName());
    }
    
}
