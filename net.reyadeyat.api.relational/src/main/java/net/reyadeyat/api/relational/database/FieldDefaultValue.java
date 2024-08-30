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

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;

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
public class FieldDefaultValue {
    Boolean is_expression;
    Object static_default_value;
    String expression;
    FieldType field_type;
    
    public FieldDefaultValue(FieldType field_type, Object static_default_value, Boolean is_expression) throws Exception {
        this.is_expression = is_expression;
        this.field_type = field_type;
        if (is_expression == false) {
            this.static_default_value = static_default_value;
            return;
        }
        
        if (static_default_value instanceof String == false) {
            throw new Exception("FieldDefaultValue expression must be String object");
        }

        if (field_type == FieldType.String) {
            throw new Exception("FieldDefaultValue expression allowed for Date and Time field type only; Field Type [String]");
        } else if (field_type == FieldType.Set) {
            throw new Exception("FieldDefaultValue expression allowed for Date and Time field type only; Field Type [Set]");
        } else if (field_type == FieldType.Long) {
            throw new Exception("FieldDefaultValue expression allowed for Date and Time field type only; Field Type [Long]");
        } else if (field_type == FieldType.Integer) {
            throw new Exception("FieldDefaultValue expression allowed for Date and Time field type only; Field Type [Integer]");
        } else if (field_type == FieldType.Double) {
            throw new Exception("FieldDefaultValue expression allowed for Date and Time field type only; Field Type [Double]");
        } else if (field_type == FieldType.Boolean) {
            throw new Exception("FieldDefaultValue expression allowed for Date and Time field type only; Field Type [Boolean]");
        } else if (field_type == FieldType.Date) {
            this.expression = (String) static_default_value;
            //this.static_default_value = getSqlDate(field_value);
        } else if (field_type == FieldType.Time) {
            this.expression = (String) static_default_value;
            //this.static_default_value = getSqlTime(field_value);
        } else if (field_type == FieldType.Timestamp) {
            this.expression = (String) static_default_value;
            //this.static_default_value = getSqlTimestamp(field_value);
        } else {
            throw new Exception("FieldDefaultValue passed unhandeled field of type " + field_type.toString());
        }
    }
    
    public Boolean isExpression() {
        return is_expression;
    }
    
    public Object getValue() throws Exception {
        if (is_expression == false) {
            return static_default_value;
        }
        
        if (field_type == FieldType.Date) {
            if (this.expression.equalsIgnoreCase("TODAY")) {
                return Date.from(Instant.now());
            } else {
                throw new Exception("FieldDefaultValue.getValue undefined Expression " + this.expression + " for Field Type [" + field_type.toString() + "]");
            }
        } else if (field_type == FieldType.Time) {
            if (this.expression.equalsIgnoreCase("NOW")) {
                return Time.from(Instant.now());
            } else {
                throw new Exception("FieldDefaultValue.getValue undefined Expression " + this.expression + " for Field Type [" + field_type.toString() + "]");
            }
        } else if (field_type == FieldType.Timestamp) {
            if (this.expression.equalsIgnoreCase("NOW")
                    || this.expression.equalsIgnoreCase("CURRENT_TIMESTAMP")
                    || this.expression.equalsIgnoreCase("UTC_TIMESTAMP")) {
                return Timestamp.from(Instant.now());
            } else {
                throw new Exception("FieldDefaultValue.getValue undefined Expression " + this.expression + " for Field Type [" + field_type.toString() + "]");
            }
        }
        
        throw new Exception("FieldDefaultValue.getValue wrong behaviour is_expression=" + is_expression + " expression='" + this.expression + "'");
    }
    
    public String getSQLValue() throws Exception {
        if (is_expression == false) {
            return (static_default_value == null ? null : static_default_value.toString());
        }
        
        if (field_type == FieldType.Date) {
            if (this.expression.equalsIgnoreCase("TODAY")) {
                return Field.getSqlDate(Instant.now());
            } else {
                throw new Exception("FieldDefaultValue.getValue undefined Expression " + this.expression + " for Field Type [" + field_type.toString() + "]");
            }
        } else if (field_type == FieldType.Time) {
            if (this.expression.equalsIgnoreCase("NOW")) {
                return Field.getSqlTime(Instant.now());
            } else {
                throw new Exception("FieldDefaultValue.getValue undefined Expression " + this.expression + " for Field Type [" + field_type.toString() + "]");
            }
        } else if (field_type == FieldType.Timestamp) {
            if (this.expression.equalsIgnoreCase("NOW")
                    || this.expression.equalsIgnoreCase("CURRENT_TIMESTAMP")
                    || this.expression.equalsIgnoreCase("UTC_TIMESTAMP")) {
                return Field.getSqlTimestamp(Instant.now());
            } else {
                throw new Exception("FieldDefaultValue.getValue undefined Expression " + this.expression + " for Field Type [" + field_type.toString() + "]");
            }
        }
        
        throw new Exception("FieldDefaultValue.getValue wrong behaviour is_expression=" + is_expression + " expression='" + this.expression + "'");
    }
}
