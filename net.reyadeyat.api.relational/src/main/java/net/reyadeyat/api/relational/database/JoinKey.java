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
public class JoinKey {
    private String key;
    private String database_name;
    private String primary_table;
    private String primary_table_alias;
    private String join_table;
    private String join_table_alias;
    private JoinType join_type;
    private HashMap<Field, String> joinFields;//Join Field, Field
    private String join_statement;
    
    public JoinKey(String key, String database_name, String primary_table, String primary_table_alias, String join_table, String join_table_alias, JoinType join_type) {
        this.key = key;
        this.database_name = database_name;
        this.primary_table = primary_table;
        this.primary_table_alias = primary_table_alias;
        this.join_table = join_table;
        this.join_table_alias = join_table_alias;
        this.join_type = join_type;
        this.joinFields = new HashMap<Field, String>();
    }
    
    public String getKey() {
        return key;
    }
    
    public void addJoinField(Field field, String joinField) {
        this.joinFields.put(field, joinField);
    }
    
    public String getJoinField(Field field) {
        return this.joinFields.get(field);
    }
    
    public void prepareJoinStatement() {
        StringBuilder sb = new StringBuilder();
        ArrayList<Field> field_list = new ArrayList<>(joinFields.keySet());
        Collections.sort(field_list);
        sb.append(join_type == JoinType.INNER_JOIN ? " INNER JOIN " : join_type == JoinType.LEFT_JOIN ? " LEFT JOIN " : "RIGHT JOIN").append("`").append(database_name).append("`.`").append(join_table);
        if (join_table_alias != null) {
            sb.append("` AS `").append(join_table_alias);
            
        }
        sb.append("` ON ");
        for (int i = 0; i < field_list.size(); i++) {
            Field field = field_list.get(i);
            String joinFieldName = joinFields.get(field);
            sb.append("`").append(join_table_alias == null ? join_table : join_table_alias).append("`.`").append(joinFieldName).append("`=`").append(primary_table_alias == null ? primary_table : primary_table_alias).append("`.`").append(field.getName()).append("`").append(i+1 == field_list.size() ? "" : " AND ");
        }
        join_statement = sb.toString();
    }
    
    public String getJoinStatement() {
        /*if (join_statement == null) {
            prepareJoinStatement();
        }*/
        return join_statement;
    }
    
    public enum JoinType {INNER_JOIN, LEFT_JOIN, RIGHT_JOIN};
}
