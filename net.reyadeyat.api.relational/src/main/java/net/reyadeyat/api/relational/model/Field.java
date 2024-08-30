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

package net.reyadeyat.api.relational.model;

import net.reyadeyat.api.relational.data.DataLookup;
import net.reyadeyat.api.relational.annotation.MetadataAnnotation;

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
public class Field {
    public String name;
    @MetadataAnnotation (lookup=true)
    public String data_type_name;
    //public String typeCode;
    public Boolean primary_key;
    public Boolean nullable;
    public Boolean auto_increment;
    public Boolean foreign_reference;
    @MetadataAnnotation (field=true, nullable=true)
    public String default_value;
    public Integer list_order;
    public Integer size;
    public Integer decimal_digits;
    
    transient public Boolean case_sensitive_sql;
    transient public Table table;
    
    /**no-arg default constructor for jaxb marshalling*/
    public Field() {}

    public Field(Table table, String name,
            String data_type_name,
            String dataTypeCode,
            Boolean nullable,
            Boolean auto_increment,
            String default_value,
            Integer list_order,
            Integer size,
            Integer decimal_digits, Boolean case_sensitive_sql,
            DataLookup dataLookup) throws Exception {
        this.table = table;
        this.name = name;
        this.data_type_name = data_type_name.trim();
        /*if (this.table.name.equalsIgnoreCase("XYZ") == true) {
            this.data_type_name = this.data_type_name;
        }*/
        if (dataLookup.lookupID(this.data_type_name) == null) {
            throw new Exception("Data Type '" + this.data_type_name + "' is not defined in Lookup Category '" + dataLookup.getDataLookupCategory() + "'");
        }
        //this.typeCode = dataTypeCode;
        this.primary_key = false;
        this.nullable = nullable;
        this.auto_increment = auto_increment;
        this.foreign_reference = false;
        this.default_value = default_value;
        this.list_order = list_order;
        this.size = size;
        this.decimal_digits = decimal_digits;
        this.case_sensitive_sql = case_sensitive_sql;
    }
    
    public void init() {
    }
    
    public void setPrimaryKey() {
        primary_key = true;
    }
    
    public void setForeignReference() {
        foreign_reference = true;
    }

    public String toString(Integer level, Integer shift) {
        StringBuilder b = new StringBuilder();

        b.append("\n");
        for (int i = 0; i < level * shift; i++) {
            /*if (i%4 == 0) {
                b.append("|");
            } else {
                b.append("_");
            }*/
            b.append(" ");
        }
        b.append("|");
        for (int i = 0; i < shift - 1; i++) {
            b.append(".");
        }
        /*.append(dataTypeCode)*/
        b.append("Field: ").append("[").append(list_order).append("] `").append(name).append("` ")
                .append(data_type_name).append("(").append(size).append(decimal_digits > 0 ? ","+decimal_digits : "").append(")")
                .append(nullable == true ? " NULL" : "")
                .append(auto_increment == true ? " AUTOINCREMENT" : "")
                .append(default_value == null ? "" : " DEFAULT '" + default_value + "'");
        return b.toString();
    }

    @Override
    public String toString() {
        return new StringBuilder().append("Field: ").append("[").append(list_order).append("] `").append(name).append("` ")
                .append(data_type_name).append("(").append(size).append(decimal_digits > 0 ? ","+decimal_digits : "").append(")")
                .append(nullable == true ? " NULL" : "")
                .append(auto_increment == true ? " AUTOINCREMENT" : "")
                .append(default_value == null ? "" : " '" + default_value + "'").toString();
    }
    
    public String getTypeJavaClassPath() throws Exception {
        if ((data_type_name.equalsIgnoreCase("BIT") || data_type_name.equalsIgnoreCase("TINYINT") || data_type_name.equalsIgnoreCase("TINYINT UNSIGNED") && size == 1 /*mysql*/)
                || data_type_name.equalsIgnoreCase("BOOLEAN"/*informix*/)) {
            return Boolean.class.getCanonicalName();
        } else if (data_type_name.equalsIgnoreCase("TINYINT") || data_type_name.equalsIgnoreCase("TINYINT UNSIGNED")) {
            return Byte.class.getCanonicalName();
        } else if (data_type_name.equalsIgnoreCase("SMALLINT") || data_type_name.equalsIgnoreCase("SMALLINT UNSIGNED")) {
            return Short.class.getCanonicalName();
        } else if (data_type_name.equalsIgnoreCase("MEDIUMINT") || data_type_name.equalsIgnoreCase("INT") || data_type_name.equalsIgnoreCase("INTEGER")
                || data_type_name.equalsIgnoreCase("MEDIUMINT UNSIGNED") || data_type_name.equalsIgnoreCase("INT UNSIGNED") || data_type_name.equalsIgnoreCase("INTEGER UNSIGNED")
                || data_type_name.equalsIgnoreCase("BIT") || data_type_name.equalsIgnoreCase("BIT UNSIGNED")
                || data_type_name.equalsIgnoreCase("INT IDENTITY"/*sqlserver*/)
                || data_type_name.equalsIgnoreCase("SERIAL"/*informix*/)) {
            return Integer.class.getCanonicalName();
        } else if (data_type_name.equalsIgnoreCase("BIGINT")
                || data_type_name.equalsIgnoreCase("BIGINT UNSIGNED")
                || data_type_name.equalsIgnoreCase("BIGINT IDENTITY"/*sqlserver*/)
                || data_type_name.equalsIgnoreCase("SERIAL8"/*informix*/)
                || data_type_name.equalsIgnoreCase("INT8"/*informix*/)
                || data_type_name.equalsIgnoreCase("BIGSERIAL"/*informix*/)) {
            return Long.class.getCanonicalName();
        } else if (data_type_name.equalsIgnoreCase("FLOAT") || data_type_name.equalsIgnoreCase("FLOAT UNSIGNED")) {
            return Float.class.getCanonicalName();
        } else if (data_type_name.equalsIgnoreCase("DOUBLE") || data_type_name.equalsIgnoreCase("DOUBLE UNSIGNED")) {
            return Double.class.getCanonicalName();
        } else if (data_type_name.equalsIgnoreCase("NUMERIC") || data_type_name.equalsIgnoreCase("DECIMAL")
            || data_type_name.equalsIgnoreCase("NUMERIC UNSIGNED") || data_type_name.equalsIgnoreCase("DECIMAL UNSIGNED")
            || data_type_name.equalsIgnoreCase("UNIQUEIDENTIFIER")/*sqlserver*/
            || data_type_name.equalsIgnoreCase("MONEY")/*sqlserver*/
            || data_type_name.equalsIgnoreCase("numeric() identity")/*sqlserver*/) {
            if (decimal_digits == 0) {
                return Long.class.getCanonicalName();
            } else {
                return Double.class.getCanonicalName();
            }
        } else if (data_type_name.equalsIgnoreCase("YEAR")
            || data_type_name.equalsIgnoreCase("DATE")) {
            return java.sql.Date.class.getCanonicalName();
        } else if (data_type_name.equalsIgnoreCase("TIME")) {
            return java.sql.Time.class.getCanonicalName();
        } else if (data_type_name.equalsIgnoreCase("DATETIME")
            || data_type_name.equalsIgnoreCase("TIMESTAMP")
            || data_type_name.equalsIgnoreCase("SMALLDATETIME")
            || data_type_name.toLowerCase().startsWith("datetime"/*informix*/)) {
            return java.sql.Timestamp.class.getCanonicalName();
        } else if (data_type_name.equalsIgnoreCase("CHAR")
            || data_type_name.equalsIgnoreCase("ENUM")
            || data_type_name.equalsIgnoreCase("SET")
            || data_type_name.equalsIgnoreCase("VARCHAR")
            || data_type_name.equalsIgnoreCase("TINYTEXT")
            || data_type_name.equalsIgnoreCase("TEXT")
            || data_type_name.equalsIgnoreCase("MEDIUMTEXT")
            || data_type_name.equalsIgnoreCase("LONGTEXT")
            || data_type_name.equalsIgnoreCase("NVARCHAR"/*sqlserver*/)
            || data_type_name.equalsIgnoreCase("NTEXT"/*sqlserver*/)
            || data_type_name.equalsIgnoreCase("NCHAR"/*sqlserver*/)
            || data_type_name.equalsIgnoreCase("SYSNAME"/*sqlserver*/)
            || data_type_name.equalsIgnoreCase("GEOGRAPHY"/*sqlserver*/)
            || data_type_name.equalsIgnoreCase("LVARCHAR"/*informix*/)) {
            return java.lang.String.class.getCanonicalName();
        } else if (data_type_name.equalsIgnoreCase("BINARY")
            || data_type_name.equalsIgnoreCase("VARBINARY")
            || data_type_name.equalsIgnoreCase("TINYBLOB")
            || data_type_name.equalsIgnoreCase("BLOB")
            || data_type_name.equalsIgnoreCase("CLOB")
            || data_type_name.equalsIgnoreCase("MEDIUMBLOB")
            || data_type_name.equalsIgnoreCase("LONGBLOB")
            || data_type_name.equalsIgnoreCase("IMAGE"/*sqlserver*/)) {
            return java.lang.Object.class.getCanonicalName();
        } else if (data_type_name.equalsIgnoreCase("JSON")) {
            return com.google.gson.JsonElement.class.getCanonicalName();
        }
        throw new Exception("Field data type '" + data_type_name + "' is not implemented yet");
    }
    
    public Class getTypeJavaClass() throws Exception {
        if ((data_type_name.equalsIgnoreCase("BIT") || data_type_name.equalsIgnoreCase("TINYINT") || data_type_name.equalsIgnoreCase("TINYINT UNSIGNED") && size == 1 /*mysql*/)
                || data_type_name.equalsIgnoreCase("BOOLEAN"/*informix*/)) {
            return Boolean.class;
        } else if (data_type_name.equalsIgnoreCase("TINYINT") || data_type_name.equalsIgnoreCase("TINYINT UNSIGNED")) {
            return Byte.class;
        } else if (data_type_name.equalsIgnoreCase("SMALLINT") || data_type_name.equalsIgnoreCase("SMALLINT UNSIGNED")) {
            return Short.class;
        } else if (data_type_name.equalsIgnoreCase("MEDIUMINT") || data_type_name.equalsIgnoreCase("INT") || data_type_name.equalsIgnoreCase("INTEGER")
                || data_type_name.equalsIgnoreCase("MEDIUMINT UNSIGNED") || data_type_name.equalsIgnoreCase("INT UNSIGNED") || data_type_name.equalsIgnoreCase("INTEGER UNSIGNED")
                || data_type_name.equalsIgnoreCase("BIT") || data_type_name.equalsIgnoreCase("BIT UNSIGNED")
                || data_type_name.equalsIgnoreCase("INT IDENTITY"/*sqlserver*/)
                || data_type_name.equalsIgnoreCase("SERIAL"/*informix*/)) {
            return Integer.class;
        } else if (data_type_name.equalsIgnoreCase("BIGINT")
                || data_type_name.equalsIgnoreCase("BIGINT UNSIGNED")
                || data_type_name.equalsIgnoreCase("BIGINT IDENTITY"/*sqlserver*/)
                || data_type_name.equalsIgnoreCase("SERIAL8"/*informix*/)
                || data_type_name.equalsIgnoreCase("INT8"/*informix*/)
                || data_type_name.equalsIgnoreCase("BIGSERIAL"/*informix*/)) {
            return Long.class;
        } else if (data_type_name.equalsIgnoreCase("FLOAT") || data_type_name.equalsIgnoreCase("FLOAT UNSIGNED")) {
            return Float.class;
        } else if (data_type_name.equalsIgnoreCase("DOUBLE") || data_type_name.equalsIgnoreCase("DOUBLE UNSIGNED")) {
            return Double.class;
        } else if (data_type_name.equalsIgnoreCase("NUMERIC") || data_type_name.equalsIgnoreCase("DECIMAL")
            || data_type_name.equalsIgnoreCase("NUMERIC UNSIGNED") || data_type_name.equalsIgnoreCase("DECIMAL UNSIGNED")
            || data_type_name.equalsIgnoreCase("UNIQUEIDENTIFIER")/*sqlserver*/
            || data_type_name.equalsIgnoreCase("MONEY")/*sqlserver*/
            || data_type_name.equalsIgnoreCase("numeric() identity")/*sqlserver*/) {
            if (decimal_digits == 0) {
                return Long.class;
            } else {
                return Double.class;
            }
        } else if (data_type_name.equalsIgnoreCase("YEAR")
            || data_type_name.equalsIgnoreCase("DATE")) {
            return java.sql.Date.class;
        } else if (data_type_name.equalsIgnoreCase("TIME")) {
            return java.sql.Time.class;
        } else if (data_type_name.equalsIgnoreCase("DATETIME")
            || data_type_name.equalsIgnoreCase("TIMESTAMP")
            || data_type_name.equalsIgnoreCase("SMALLDATETIME")
            || data_type_name.toLowerCase().startsWith("datetime"/*informix*/)) {
            return java.sql.Timestamp.class;
        } else if (data_type_name.equalsIgnoreCase("CHAR")
            || data_type_name.equalsIgnoreCase("ENUM")
            || data_type_name.equalsIgnoreCase("SET")
            || data_type_name.equalsIgnoreCase("VARCHAR")
            || data_type_name.equalsIgnoreCase("TINYTEXT")
            || data_type_name.equalsIgnoreCase("TEXT")
            || data_type_name.equalsIgnoreCase("MEDIUMTEXT")
            || data_type_name.equalsIgnoreCase("LONGTEXT")
            || data_type_name.equalsIgnoreCase("NVARCHAR"/*sqlserver*/)
            || data_type_name.equalsIgnoreCase("NTEXT"/*sqlserver*/)
            || data_type_name.equalsIgnoreCase("NCHAR"/*sqlserver*/)
            || data_type_name.equalsIgnoreCase("SYSNAME"/*sqlserver*/)
            || data_type_name.equalsIgnoreCase("GEOGRAPHY"/*sqlserver*/)
            || data_type_name.equalsIgnoreCase("LVARCHAR"/*informix*/)) {
            return java.lang.String.class;
        } else if (data_type_name.equalsIgnoreCase("BINARY")
            || data_type_name.equalsIgnoreCase("VARBINARY")
            || data_type_name.equalsIgnoreCase("TINYBLOB")
            || data_type_name.equalsIgnoreCase("BLOB")
            || data_type_name.equalsIgnoreCase("CLOB")
            || data_type_name.equalsIgnoreCase("MEDIUMBLOB")
            || data_type_name.equalsIgnoreCase("LONGBLOB")
            || data_type_name.equalsIgnoreCase("IMAGE"/*sqlserver*/)) {
            return java.lang.Object.class;
        } else if (data_type_name.equalsIgnoreCase("JSON")) {
            return com.google.gson.JsonElement.class;
        }
        throw new Exception("Field data type '" + data_type_name + "' is not implemented yet");
    }
}
