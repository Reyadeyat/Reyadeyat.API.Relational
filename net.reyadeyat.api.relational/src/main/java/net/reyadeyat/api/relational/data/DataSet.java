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

package net.reyadeyat.api.relational.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
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
public class DataSet {
    class Table {
        class Field {
            String name;
            String alias;
            String class_path;
            public Field(String name, String alias, String class_path) {
                this.name = name;
                this.alias = alias;
                this.class_path = class_path;
            }
        }
        String name;
        String alias;
        ArrayList<Field> field_list;
        public Table(String name, String alias) {
            this.name = name;
            this.alias = alias;
            this.field_list = new ArrayList<Field>();
        }
        public void addField(String fieldAlias, String fieldName, String classPath) {
            field_list.add(new Field(fieldAlias, fieldName, classPath));
        }
    }
    String sql;
    String table_list_path;
    ArrayList<Table> table_list;
    ArrayList<HashMap<String, Object>> recordset;
    
    public DataSet(String table_list_path) {
        this.table_list_path = table_list_path;
        table_list = new ArrayList<Table>();
        recordset = new ArrayList<HashMap<String, Object>>();
    }
    
    public void addTable(String tableAlias, String tableName) {
        table_list.add(new Table(tableName, tableAlias));
    }
    
    public void addField(String tableAlias, String fieldAlias, String fieldName, String classPath) throws Exception {
        for (Table table : table_list) {
            if (table.alias.equals(tableAlias)) {
                table.addField(fieldName, fieldAlias, classPath);
            }
        }
    }
    
    public void setSQL(String sql) throws Exception {
        if (sql == null) {
            throw new Exception("SQL statement is null!");
        }
        this.sql = sql;
    }
    
    public void addRecords(Connection connection, String whereClause) throws Exception {
        String sqlStatement = sql + (whereClause == null ? "" : whereClause);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement)) {
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    HashMap<String, Object> record = new HashMap<String, Object>();
                    for (int i = 0; i < table_list.size(); i++) {
                        Table table = table_list.get(i);
                        for (int x = 0; x < table.field_list.size(); x++) {
                            Table.Field field = table.field_list.get(x);
                            record.put(field.alias, rs.getObject(field.alias));
                        }
                    }
                    recordset.add(record);
                }
                rs.close();
            }
            preparedStatement.close();
        } catch (Exception sqlx) {
            throw sqlx;
        }
    }
}
