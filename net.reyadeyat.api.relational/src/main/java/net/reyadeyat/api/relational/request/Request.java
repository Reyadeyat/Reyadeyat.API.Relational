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
public class Request {
    @SerializedName("table")
    public String table_alias;
    /**
     * tabular | tree
     */
    public String response;
    @SerializedName("parameters")
    public Map<String, String> parameter_map;
    @SerializedName("variables")
    public Map<String, String> variable_map;
    @SerializedName("select")
    public List<String> select_list;
    @SerializedName("update")
    public Map<String, String> update_field_map;
    @SerializedName("insert")
    public Map<String, String> insert_field_map;
    @SerializedName("delete")
    public Map<String, String> delete_field_map;
    @SerializedName("values")
    public Map<String, String> value_map;
    public Where where;
    @SerializedName("order_by")
    public List<String> order_by_list;
    public Having having;
    @SerializedName("group_by")
    public List<String> group_by_list;
    @SerializedName("children")
    public List<Request> child_list;
    public Map<String, Request> child_map;
    
    public void init() {
        if (child_list == null || child_map != null) {
            return;
        }
        child_map = new HashMap<>();
        for (Request child : child_list) {
            child_map.put(child.table_alias, child);
        }
    }
}
