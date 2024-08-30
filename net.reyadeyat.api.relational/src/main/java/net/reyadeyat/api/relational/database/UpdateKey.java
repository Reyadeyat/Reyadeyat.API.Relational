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
public class UpdateKey {
    private String database_name;
    private String update_table;
    private ArrayList<Field> field_list;
    private HashMap<Field, String> updateFields;//Update Field, Field
    private String updating_validation_statement;
    
    public UpdateKey(String database_name, String update_table) {
        this.database_name = database_name;
        this.update_table = update_table;
    }
    
    public void addUpdateField(Field field, String updateField) {
        this.updateFields.put(field, updateField);
    }
    
    public String getUpdateField(Field field) {
        return this.updateFields.get(field);
    }
    
    public void prepareUpdatingStatement() {
        StringBuilder sb = new StringBuilder();
        field_list = new ArrayList<>(updateFields.keySet());
        Collections.sort(field_list);
        sb.append("SELECT * FROM ").append("`").append(database_name).append("`.`").append(update_table).append("` WHERE ");
        for (int i = 0; i < field_list.size(); i++) {
            Field field = field_list.get(i);
            String updateFieldName = updateFields.get(field);
            sb.append("`").append(update_table).append("`.`").append(updateFieldName).append("`=?").append(i+1 == field_list.size() ? "" : " AND ");
        }
        updating_validation_statement = sb.toString();
    }
    
    public String getUpdateValidationStatement() {
        return updating_validation_statement;
    }
}
