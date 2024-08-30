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

import java.util.ArrayList;

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
public class PrimaryKey {
    public String name;
    public ArrayList<PrimaryKeyField> primary_key_field_list;
    //private ArrayList<ModelField> Fields;
    
    transient public Boolean case_sensitive_sql;
    transient public Table table;
    
    /**no-arg default constructor for jaxb marshalling*/
    public PrimaryKey() {
        primary_key_field_list = new ArrayList<PrimaryKeyField>();
    }
    
    public PrimaryKey(Table table, String name, Boolean case_sensitive_sql) {
        this();
        this.table = table;
        this.name = name;
        this.case_sensitive_sql = case_sensitive_sql;
    }
    
    public void addField(PrimaryKeyField primary_key_field) {
        primary_key_field_list.add(primary_key_field);
    }
    
    public boolean isFieldPrimaryKey(String field_name) {
        for (PrimaryKeyField primary_key_field : this.primary_key_field_list) {
            if (case_sensitive_sql == true && primary_key_field.name.equals(field_name) == true) {
                return true;
            } else if (case_sensitive_sql == false && primary_key_field.name.equalsIgnoreCase(field_name) == true) {
                return true;
            }
        }
        return false;
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
        b.append("Primary Key: ").append(name).append(" {");
        for (PrimaryKeyField field : primary_key_field_list) {
            b.append("`").append(field.name).append("`,");
        }
        if (primary_key_field_list.size() > 0) {
            b.delete(b.length()-1, b.length());
        }
        b.append("}");
        return b.toString();
    }
    
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Primary Key: ").append(name).append(" {");
        for (PrimaryKeyField primary_key_field : primary_key_field_list) {
            b.append(primary_key_field.name).append(",");
        }
        b.delete(b.length()-1, b.length());
        b.append("}");
        return b.toString();
    }
}
