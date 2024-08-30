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
public class ChildTable {
    public String table_name;
    public String parent_table_name;
    public String foreig_key_name;
    
    transient public Boolean case_sensitive_sql;
    transient public Table parentTable;
    transient public Table table;
    transient public ForeignKey foreignKey;
    
    /**no-arg default constructor for jaxb marshalling*/
    public ChildTable() {}

    public ChildTable(Table parentTable, Table table, ForeignKey foreignKey, String parent_table_name, String table_name, String foreig_key_name, Boolean case_sensitive_sql) {
        this.parentTable = parentTable;
        this.table = table;
        this.foreignKey = foreignKey;
        this.parent_table_name = parent_table_name;
        this.table_name = table_name;
        this.foreig_key_name = foreig_key_name;
        this.case_sensitive_sql = case_sensitive_sql;
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
        b.append("Child Table: ").append("[").append(table_name).append(" -> ").append(parent_table_name).append("] [");
        for (int i = 0; i < foreignKey.foreign_key_field_list.size(); i++) {
            ForeignKeyField foreignKeyFieldName = foreignKey.foreign_key_field_list.get(i);
            ReferencedKeyField referenced_key_fieldName = foreignKey.referenced_key_field_list.get(i);
            b.append("`").append(table_name).append("`.`").append(foreignKeyFieldName.name).append("` -> `").append(parent_table_name).append("`.`").append(referenced_key_fieldName.name).append("`,");
        }
        if (foreignKey.foreign_key_field_list.size() > 0) {
            b.delete(b.length()-1, b.length());
        }
        b.append("]");
        return b.toString();
    }

    @Override
    public String toString() {
        return new StringBuilder().append("ChildTable: ").append("[").append(table_name).append("] -> Parent Table: [").append(parent_table_name).append("] ForeignKey: [").append(foreig_key_name).append("] On").toString();
    }
}
