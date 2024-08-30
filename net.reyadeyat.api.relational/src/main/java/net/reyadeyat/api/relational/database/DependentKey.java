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
public class DependentKey {
    private String key;
    private String database_name;
    private String dependent_table;
    private ArrayList<Field> field_list;
    private HashMap<Field, String> dependentFields;//Dependent Field, Field
    private String dependency_validation_statement;
    
    public DependentKey(String key, String database_name, String dependent_table) {
        this.key = key;
        this.database_name = database_name;
        this.dependent_table = dependent_table;
        this.dependentFields = new HashMap<Field, String>();
    }
    
    public String getKey() {
        return key;
    }
    
    public void addDependentField(Field field, String dependentField) {
        this.dependentFields.put(field, dependentField);
    }
    
    public String getDependentField(Field field) {
        return this.dependentFields.get(field);
    }
    
    public String getDependentTable() {
        return dependent_table;
    }
    
    public ArrayList<Field> getFields() {
        return this.field_list;
    }
    
    public void prepareDependencyValidationStatement() {
        if (dependentFields == null) {
            dependency_validation_statement = "";
            return;
        }
        StringBuilder sb = new StringBuilder();
        field_list = new ArrayList<>(dependentFields.keySet());
        Collections.sort(field_list);
        sb.append("SELECT * FROM ").append("`").append(database_name).append("`.`").append(dependent_table).append("` WHERE ");
        for (int i = 0; i < field_list.size(); i++) {
            Field field = field_list.get(i);
            String dependentFieldName = dependentFields.get(field);
            sb.append("`").append(dependent_table).append("`.`").append(dependentFieldName).append("`=?").append(i+1 == field_list.size() ? "" : " AND ");
        }
        dependency_validation_statement = sb.toString();
    }
    
    public String getDependencyValidationStatement() {
        /*if (dependency_validation_statement == null) {
            prepareDependencyValidationStatement();
        }*/
        return dependency_validation_statement;
    }
}
