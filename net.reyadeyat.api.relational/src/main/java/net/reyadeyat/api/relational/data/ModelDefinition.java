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
public class ModelDefinition {
    public Integer model_id;
    public Integer model_instance_sequence_type_id;
    public String model_instance_sequence_last_value;
    public String model_name;
    public String model_version;
    public String model_class_path;
    public String model_data_lookup_category;
    public String modeled_database_engine;
    public String modeled_database_url;
    public String modeled_database_url_user_name;
    public String modeled_database_url_user_password;
    public String modeled_database_schem;
    public String modeled_database_name;
    public String modeled_database_field_open_quote;
    public String modeled_database_field_close_quote;
    public String modeled_table_data_structures_class;
    
    public ModelDefinition() {
        
    }
    
    public ModelDefinition(String model_data_lookup_category, String model_version, String modeled_database_schem, String modeled_database_name, String modeled_database_field_open_quote, String modeled_database_field_close_quote) {
        this.model_data_lookup_category = model_data_lookup_category;
        this.model_version = model_version;
        this.modeled_database_schem = modeled_database_schem;
        this.modeled_database_name = modeled_database_name;
        this.modeled_database_field_open_quote = modeled_database_field_open_quote;
        this.modeled_database_field_close_quote = modeled_database_field_close_quote;
        
        this.model_name = (this.modeled_database_schem == null ? this.modeled_database_name: this.modeled_database_schem + "." + this.modeled_database_name) + " - " + this.model_version;
    }
}
