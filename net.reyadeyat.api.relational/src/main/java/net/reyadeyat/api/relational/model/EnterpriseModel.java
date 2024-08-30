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

import net.reyadeyat.api.relational.data.ModelDefinition;
import net.reyadeyat.api.relational.data.DataModel;

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
public class EnterpriseModel<Model extends Enterprise> implements DataModel<Model> {

    private Model enterprise;

    public ModelDefinition model_definition;

    private EnterpriseModel() {
    }

    public EnterpriseModel(Model enterprise, ModelDefinition model_definition) {
        this.enterprise = enterprise;
        this.model_definition = model_definition;
    }

    @Override
    public Model getInstance() {
        return this.enterprise;
    }

    @Override
    public ModelDefinition getModelDefinition() {
        return this.model_definition;
    }

    @Override
    public java.lang.reflect.Field getDeclaredField() throws NoSuchFieldException {
        return EnterpriseModel.class.getDeclaredField("enterprise");
    }

    @Override
    public void prepareInstance() throws Exception {
        for (Database database : enterprise.database_list) {
            database.enterprise = enterprise;
            database.case_sensitive_sql = enterprise.case_sensitive_sql;
            for (Table table : database.table_list) {
                table.database = database;
                table.case_sensitive_sql = database.case_sensitive_sql;
                for (Field field : table.field_list) {
                    field.table = table;
                    field.case_sensitive_sql = table.case_sensitive_sql;
                }
                for (PrimaryKey primary_key : table.primary_key_list) {
                    primary_key.table = table;
                    primary_key.case_sensitive_sql = table.case_sensitive_sql;
                    for (PrimaryKeyField primary_key_field : primary_key.primary_key_field_list) {
                        primary_key_field.parentPrimaryKey = primary_key;
                        primary_key_field.case_sensitive_sql = primary_key.case_sensitive_sql;
                    }
                }
                for (ForeignKey foreign_key : table.foreign_key_list) {
                    foreign_key.table = table;
                    foreign_key.case_sensitive_sql = table.case_sensitive_sql;
                    for (ForeignKeyField foreignKeyField : foreign_key.foreign_key_field_list) {
                        foreignKeyField.foreignKey = foreign_key;
                        foreignKeyField.case_sensitive_sql = foreign_key.case_sensitive_sql;
                    }
                    for (ReferencedKeyField referenced_key_field : foreign_key.referenced_key_field_list) {
                        referenced_key_field.foreign_key = foreign_key;
                        referenced_key_field.case_sensitive_sql = foreign_key.case_sensitive_sql;
                    }
                }
                for (ChildTable child_table : table.child_table_list) {
                    child_table.parentTable = table;
                    child_table.case_sensitive_sql = table.case_sensitive_sql;
                    String tableName = new String(child_table.table_name);
                    child_table.table = database.table_list.stream().filter(o -> o.name.equals(tableName)).findAny().orElse(null);
                    String foreigKeyName = new String(child_table.foreig_key_name);
                    child_table.foreignKey = child_table./*parentTable*/table.foreign_key_list.stream().filter(o -> o.name.equals(foreigKeyName)).findAny().orElse(null);
                    /*if (child_table.table.name.equalsIgnoreCase("ap_payment")) {
                        child_table = child_table;
                    }*/
                }
            }
            
            database.extractTableLogic(false);
        }
    }
}
