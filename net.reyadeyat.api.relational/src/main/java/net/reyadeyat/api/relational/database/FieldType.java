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

import com.google.gson.JsonElement;

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
public enum FieldType {
    String("String"),
    Boolean("Boolean"), 
    Byte("Byte"),
    Short("Short"),
    Long("Long"), 
    Integer("Integer"), 
    Float("Float"), 
    Double("Double"), 
    Date("Date"), 
    Time("Time"), 
    Timestamp("Timestamp"),
    TimeZone("TimeZone"),
    Set("Set"),
    BigInteger("BigInteger"),
    BigDecimal("BigDecimal"),
    Object("Object"),
    Json("Json");
    
    private String name;
    
    private FieldType(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
    
    public Boolean isBoolean() {
        return (this == FieldType.Boolean);
    }
    
    public Boolean isNumeric() {
        return (this == FieldType.Integer || this == FieldType.Long || this == FieldType.Double || this == FieldType.BigDecimal);
    }
    
    public Boolean isText() {
        return (this == FieldType.String);
    }
    
    public Boolean isQuotable() {
        return (this == FieldType.String || this == FieldType.Date || this == FieldType.Time || this == FieldType.Timestamp || this == FieldType.Set);
    }
    
    public Boolean isDateTime() {
        return (this == FieldType.Date || this == FieldType.Time || this == FieldType.Timestamp);
    }
    
    public static FieldType getClassFieldType(Class<?> java_class) throws Exception {
        if (java_class.equals(Boolean.class)) {
            return FieldType.Boolean;
        } else if (java_class.equals(Byte.class)) {
            return FieldType.Byte;
        } else if (java_class.equals(Short.class)) {
            return FieldType.Short;
        } else if (java_class.equals(Integer.class)) {
            return FieldType.Integer;
        } else if (java_class.equals(Long.class)) {
            return FieldType.Long;
        } else if (java_class.equals(Float.class)) {
            return FieldType.Float;
        } else if (java_class.equals(Double.class)) {
            return FieldType.Double;
        } else if (java_class.equals(java.sql.Date.class)) {
            return FieldType.Date;
        } else if (java_class.equals(java.sql.Time.class)) {
            return FieldType.Time;
        } else if (java_class.equals(java.sql.Timestamp.class)) {
            return FieldType.Timestamp;
        } else if (java_class.equals(String.class)) {
            return FieldType.String;
        } else if (java_class.equals(Object.class)) {
            return FieldType.Object;
        } else if (java_class.equals(JsonElement.class)) {
            return FieldType.Json;
        }
        throw new Exception("Field data type getClassFieldType '" + java_class.getCanonicalName() + "' is not implemented yet");
    }
}
