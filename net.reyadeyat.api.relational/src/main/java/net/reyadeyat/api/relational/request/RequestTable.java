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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class RequestTable {
    public RequestTable parent_request_table;
    public String table_alias;
    public String table_name;
    @SerializedName("transaction_type")
    public List<String> transaction_type_list;
    @SerializedName("fields")
    public List<RequestField> request_field_list;
    public Map<String, RequestField> request_field_alias_map;
    public Map<String, RequestField> request_field_name_map;
    @SerializedName("children")
    public List<RequestTable> child_request_table_list;
    public Map<String, RequestTable> child_request_table_map;
    
    public void init(RequestTable parent_request_table, List<String> parent_transaction_type_list) {
        this.parent_request_table = parent_request_table;
        if (transaction_type_list == null) {
            transaction_type_list = parent_transaction_type_list;
        }
        if (request_field_list != null) {
            request_field_name_map = new HashMap<>();
            request_field_alias_map = new HashMap<>();
            for(RequestField request_field : request_field_list) {
                request_field_name_map.put(request_field.field_name, request_field);
                request_field_alias_map.put(request_field.field_alias, request_field);
            }
        }
        if (child_request_table_list != null && child_request_table_map == null) {
            child_request_table_map = new HashMap<>();
            for (RequestTable child : child_request_table_list) {
                child_request_table_map.put(child.table_alias, child);
                child.init(this, parent_transaction_type_list);
            }
        }
    }
}
