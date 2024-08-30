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
package net.reyadeyat.api.relational.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;

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
public class RelationalServiceDefinition {
    public String service_name;
    public Integer model_id;
    public String model_datasource_name;
    public String data_datasource_name;
    public String data_database_name;
    public String secret_key;
    @SerializedName("transaction_type")
    public List<String> transaction_type_list;
    @SerializedName("request_table")
    public RequestTable request_table;
    
    public void init() {
        request_table.init(null, transaction_type_list);
    }
}
