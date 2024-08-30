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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

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
public class ForeignKey {
    private String key;
    private String database_name;
    private String foreign_table_name;
    private String foreign_table_alias;
    private ArrayList<Field> field_list;
    private HashMap<Field, String> foreignFields;//Foreign Field, Field
    private String foreiness_validation_statement;
    
    public ForeignKey(String key, String database_name, String foreign_table_name, String foreign_table_alias) {
        this.key = key;
        this.database_name = database_name;
        this.foreign_table_name = foreign_table_name;
        this.foreign_table_alias = foreign_table_alias;
        this.foreignFields = new HashMap<Field, String>();
    }
    
    public String getKey() {
        return key;
    }
    
    public void addForeignField(Field field, String foreignField) {
        this.foreignFields.put(field, foreignField);
    }
    
    public String getForeignField(Field field) {
        return this.foreignFields.get(field);
    }
    
    public String getForeignTableName() {
        return foreign_table_name;
    }
    
    public String getForeignTableAlias() {
        return foreign_table_alias;
    }
    
    public ArrayList<Field> getFields() {
        return this.field_list;
    }
    
    public void prepareForeinessValidationStatement() {
        if (foreignFields == null) {
            foreiness_validation_statement = "";
            return;
        }
        StringBuilder sb = new StringBuilder();
        field_list = new ArrayList<>(foreignFields.keySet());
        Collections.sort(field_list);
        sb.append("SELECT * FROM ").append("`").append(database_name).append("`.`").append(foreign_table_name).append("` WHERE ");
        for (int i = 0; i < field_list.size(); i++) {
            Field field = field_list.get(i);
            String foreignFieldName = foreignFields.get(field);
            sb.append("`").append(foreign_table_name).append("`.`").append(foreignFieldName).append("`<=>?").append(i+1 == field_list.size() ? "" : " AND ");
        }
        foreiness_validation_statement = sb.toString();
    }
    
    public String getForeinessValidationStatement() {
        /*if (foreiness_validation_statement == null) {
            prepareForeinessValidationStatement();
        }*/
        return foreiness_validation_statement;
    }
}
