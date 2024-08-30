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

import net.reyadeyat.api.relational.data.DataSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class Database {
    public String name;
    public String engine;
    public ArrayList<Table> table_list;
    public String java_package_name;
    
    transient public Boolean case_sensitive_sql;
    transient public Enterprise enterprise;
    transient public HashMap<String, Table> table_map;
    
    /**no-arg default constructor for jaxb marshalling*/
    public Database() {
        table_list = new ArrayList<Table>();
        table_map = new HashMap<String, Table>();
    }
    
    public void init() {
        for (Table table : table_list) {
            table.init();
            table_map.put(table.name, table);
        }
    }
    
    public Database(String name, String engine, Boolean case_sensitive_sql, String java_package_name) {
        this();
        this.name = name;
        this.engine = engine;
        this.case_sensitive_sql = case_sensitive_sql;
        this.java_package_name = java_package_name;
    }
    
    public void addTable(Table table) {
        table.database = this;
        table_list.add(table);
        table_map.put(table.name, table);
    }
    
    public Table getTable(String table_name) {
        return table_map.get(table_name);
    }
    
    public void extractTableLogic(Boolean is_building_model) throws Exception {
        for (Table table : table_list) {
            table_map.put(table.name, table);
            ArrayList<Table> tablesPath = new ArrayList<Table>();
            ArrayList<ArrayList<Table>> tablesPathReturned = new ArrayList<ArrayList<Table>>();
            table.compileTablePaths(tablesPath, tablesPathReturned, is_building_model);
        }
    }
    
    public String pathToString(ArrayList<Table> tablesPath) throws Exception {
        if (tablesPath == null) {
            throw new Exception("tablePath is null");
        }
        StringBuilder b = new StringBuilder();
        for (int x=0; x < tablesPath.size(); x++) {
            Table tt = tablesPath.get(x);
            b.append(tt.name).append(".");
        }
        if (tablesPath.size() > 0) {
            b.delete(b.length() - 1, b.length());
        }
        return b.toString();
    }
    
    public ArrayList<Table> findPath(String path) throws Exception {
        if (path == null) {
            throw new Exception("path is null");
        }
        ArrayList<Table> tablePath = new ArrayList<Table>();
        String table_list[] = path.indexOf(".") == -1 ? new String[]{path} : path.split("\\.");
        Table parentTable = null;
        for (int i = 0; i < table_list.length; i++) {
            String tableName = table_list[i];
            Table table = table_map.get(tableName);
            if (table != null) {
                if (parentTable != null && parentTable.hasChild(table) == false) {
                    throw new Exception("Table '" + tableName + "' in Path '" + path + "' is not child to Table '' no ForeignKey defined");
                }
            } else {    
                throw new Exception("Table '" + tableName + "' in Path '" + path + "' is not valid");
            }
            tablePath.add(table);
            parentTable = table;
        }
        return tablePath;
    }
    
    public JsonElement getInnerJoinedSelect(String path) throws Exception {
        return getInnerJoinedSelect(null, path, null, "", "", "");
    }
    
    public JsonElement getInnerJoinedSelectMySQL(String path) throws Exception {
        return getInnerJoinedSelect(null, path, null, "", "`", "`");
    }
    
    public JsonElement getInnerJoinedSelectSQLServer(String path) throws Exception {
        return getInnerJoinedSelect(null, path, null, "", "[", "]");
    }
    
    public JsonElement getInnerJoinedSelect(String path, String databaseFieldOpenQuote, String databaseFieldCloseQuote) throws Exception {
        return getInnerJoinedSelect(null, path, null, "", databaseFieldOpenQuote, databaseFieldCloseQuote);
    }
    
    public JsonElement getInnerJoinedSelect(String path, String databaseSchem, String databaseFieldOpenQuote, String databaseFieldCloseQuote) throws Exception {
        return getInnerJoinedSelect(null, path, null, databaseSchem, databaseFieldOpenQuote, databaseFieldCloseQuote);
    }
    
    public JsonElement getInnerJoinedSelect(Connection connection, String path, String whereClause, String databaseSchem, String databaseFieldOpenQuote, String databaseFieldCloseQuote) throws Exception {
        if (path == null) {
            throw new Exception("path is null");
        }
        ArrayList<Table> tablePath = new ArrayList<Table>();
        String table_list[] = path.indexOf(".") == -1 ? new String[]{path} : path.split("\\.");
        Table parentTable = null;
        for (int i = 0; i < table_list.length; i++) {
            String tableName = table_list[i];
            Table table = table_map.get(tableName);
            if (table != null) {
                if (parentTable != null && parentTable.hasChild(table) == false) {
                    throw new Exception("Table '" + tableName + "' in Path '" + path + "' is not child to Table '' no ForeignKey defined");
                }
            } else {
                throw new Exception("Table '" + tableName + "' in Path '" + path + "' is not valid");
            }
            tablePath.add(table);
            parentTable = table;
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithModifiers(Modifier.TRANSIENT).create();
        int tableRandom = 1000;
        int fieldRandom = 100;
        Table oldTable = null;
        String oldTableAlias = null;
        DataSet dataSet = new DataSet(path);
        StringBuilder select = new StringBuilder();
        StringBuilder from = new StringBuilder();
        select.append("SELECT ");
        from.append("FROM ");
        if (databaseSchem == null || databaseSchem.isEmpty() == false) {
            databaseSchem = "." + databaseFieldOpenQuote + databaseSchem + databaseFieldCloseQuote;
        }
        for (int i = 0; i < tablePath.size(); i++) {
            Table table = tablePath.get(i);
            tableRandom++;
            String tableAlias = table.name+"_"+tableRandom;
            dataSet.addTable(tableAlias, table.name);
            if (oldTable != null) {
                from.append(" INNER JOIN ");
            }
            from.append(databaseFieldOpenQuote).append(name).append(databaseFieldCloseQuote).append(databaseSchem).append(".").append(databaseFieldOpenQuote).append(table.name).append(databaseFieldCloseQuote).append(" AS ").append(databaseFieldOpenQuote).append(tableAlias).append(databaseFieldCloseQuote);
            if (oldTable != null) {
                from.append(" ON ");
                for (int x = 0; x < table.foreign_key_list.size(); x++) {
                    ForeignKey foreignKey = table.foreign_key_list.get(x);
                    if (foreignKey.referenced_key_table_name.equalsIgnoreCase(oldTable.name) == false) {
                        continue;
                    }
                    String foreign_key_table_name = foreignKey.foreign_key_table_name;
                    String referenced_key_table_name = foreignKey.referenced_key_table_name;
                    for (int f = 0; f < foreignKey.foreign_key_field_list.size(); f++) {
                        ForeignKeyField foreignKeyField = foreignKey.foreign_key_field_list.get(f);
                        ReferencedKeyField referenced_key_field = foreignKey.referenced_key_field_list.get(f);
                        from.append(databaseFieldOpenQuote).append(/*foreign_key_table_name*/oldTableAlias).append(databaseFieldCloseQuote).append(".").append(databaseFieldOpenQuote).append(foreignKeyField.name).append(databaseFieldCloseQuote).append("=").append(databaseFieldOpenQuote).append(/*referenced_key_table_name*/tableAlias).append(databaseFieldCloseQuote).append(".").append(databaseFieldOpenQuote).append(referenced_key_field.name).append(databaseFieldCloseQuote).append(" AND ");
                    }
                }
                from.delete(from.length() - 5, from.length());
            }
            for (int x = 0; x < table.field_list.size(); x++) {
                Field field = table.field_list.get(x);
                fieldRandom++;
                String fieldAlias = field.name+"_"+fieldRandom;
                dataSet.addField(tableAlias, fieldAlias, field.name, field.getTypeJavaClassPath());
                select.append(databaseFieldOpenQuote).append(tableAlias).append(databaseFieldCloseQuote).append(".").append(databaseFieldOpenQuote).append(field.name).append(databaseFieldCloseQuote).append(" AS ").append(databaseFieldOpenQuote).append(fieldAlias).append(databaseFieldCloseQuote).append(",");
            }
            oldTable = table;
            oldTableAlias = tableAlias;
        }
        select.delete(select.length()-1, select.length());
        select.append(" ").append(from);
        String sql = select.toString();
        dataSet.setSQL(sql);
        if (connection != null && whereClause != null) {
            dataSet.addRecords(connection, whereClause);
        }
        return gson.toJsonTree(dataSet);
    }
    
    @Override
    public String toString() {
        StringBuilder appendable = new StringBuilder();
        try {
            toString(appendable, 0, 4);
            return appendable.toString();
        } catch (Exception exception) {
            appendable.delete(0, appendable.length());
            appendable.append("Database: ").append(name).append(" Tables [").append(table_list.size()).append("]").toString();
            appendable.append("toString '").append(name).append("' error").append(exception.getMessage());
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "toString error", exception);
        }
        return appendable.toString();
    }
    
    public void toString(Appendable appendable, Integer level, Integer shift) throws Exception {
        appendable.append("\n");
        for (int i = 0; i < level * shift; i++) {
            /*if (i%4 == 0) {
                appendable.append("|");
            } else {
                appendable.append("_");
            }*/
            appendable.append(" ");
        }
        appendable.append("|");
        for (int i = 0; i < shift - 1; i++) {
            appendable.append(".");
        }
        appendable.append("Database: ").append(name).append(" Tables [").append(String.valueOf(table_list.size())).append("]");
        for (Table table : table_list) {
            table.toString(appendable, level + 1, shift);
        }
        //appendable.append("Table Logic: ");appendable.append(name);appendable.append(" Root Tables [");appendable.append(rootTables.size());appendable.append("]");
        //for (Table table : rootTables) {
        appendable.append("\n=====================================================================================================\n");
        appendable.append("\n");
        for (int i = 0; i < level * shift; i++) {
            /*if (i%4 == 0) {
                appendable.append("|");
            } else {
                appendable.append("_");
            }*/
            appendable.append(" ");
        }
        appendable.append("|");
        for (int i = 0; i < shift - 1; i++) {
            appendable.append(".");
        }
        appendable.append("Table Logic Tree: Database[").append(name).append("] Tables Count [").append(String.valueOf(table_list.size())).append("]");
        for (Table table : table_list) {
            ArrayList<Table> tablesPath = new ArrayList<Table>();
            tablesPath.add(table);
            table.toStringTableTree(appendable, level + 1, shift, tablesPath);
        }
    }
}
