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

import com.google.gson.Gson;
import net.reyadeyat.api.relational.database.Table;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.reyadeyat.api.relational.database.RecordHandler;
import net.reyadeyat.api.relational.database.RecordProcessor;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.reyadeyat.api.library.jdbc.JDBCSource;
import net.reyadeyat.api.library.json.JsonUtil;

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
public abstract class RelationalRequest implements RecordHandler {
    
    private RequestDefinition request_definition;
    private Table table;
    
    //Security
    private Integer security_flag;
    static final public Integer SECURITY_FLAG_ASSERT_VALID_FIELD_NAMES = 1;
    static final public Integer SECURITY_FLAG_DONT_RETURN_RESPONSE_MESSAGE = 2;
    static final public Integer SECURITY_FLAG_RETURN_TECHNICAL_RESPONSE_MESSAGE = 4;
    static final public Integer SECURITY_FLAG_RETURN_DESCRIPTIVE_RESPONSE_MESSAGE = 8;
    static final public Integer SECURITY_FLAG_DONT_RETURN_GENERATED_ID = 16;
    static final public Integer SECURITY_FLAG_RETURN_GENERATED_ID = 32;
    static final public Integer SECURITY_FLAG_RETURN_GENERATED_ID_ENCRYPTED = 64;
    static final public Integer SECURITY_FLAG_RETURN_RESPONSE_ENCRYPTED = 128;
    static final public Integer SECURITY_FLAG_RETURN_NOTHING = 256;
    static final public Integer SECURITY_FLAG_FOREING_KEY_MUST_LINK_TO_PRIMARY_KEY = 512;
    
    public RelationalRequest(RequestDefinition request_definition, HashMap<String, Class> interface_implementation, Integer security_flag) throws Exception {
        this.request_definition = request_definition;
        this.security_flag = security_flag;
        try {
            //this.data_database_name = data_database_name;
            JDBCSource model_jdbc_source = getJDBCSource(request_definition.model_datasource_name);
            JDBCSource data_jdbc_source = getJDBCSource(request_definition.data_datasource_name);
            JsonArray error_list = new JsonArray();
            Table.loadDataModel(request_definition.secret_key, model_jdbc_source, data_jdbc_source, request_definition.model_id, interface_implementation, error_list, (this.security_flag & SECURITY_FLAG_FOREING_KEY_MUST_LINK_TO_PRIMARY_KEY) != 0);
            JsonUtil.throwJsonExceptionOnError("Table service definition has errors:", error_list);
            table = new Table(request_definition.data_datasource_name, request_definition.data_database_name, request_definition.model_id, null, request_definition.request_table, error_list);
            JsonUtil.throwJsonExceptionOnError("Table initialize has errors:", error_list);
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error: defineServlet '"+request_definition.service_name+"'", ex);
            throw ex;
        }
    }
    
    public Boolean isSecurityFlagSwitched(int SECURITY_FLAG) {
        return (security_flag & SECURITY_FLAG) != 0;
    }
    
    protected List<Request> serviceContent(InputStream json_request_stream) throws Exception {
        Gson gson = JsonUtil.gson();
        List<Request> request_list = null;
        try (JsonReader json_reader = new JsonReader(new InputStreamReader(json_request_stream, StandardCharsets.UTF_8))) {
            request_list = gson.fromJson(json_reader, new TypeToken<List<Request>>(){}.getType());
        } catch (Exception exception) {
            JsonUtil.reclaimGson(gson);
            throw exception;
        }
        JsonUtil.reclaimGson(gson);
        return request_list;
    }

    public void serviceTransaction(Integer security_flag, InputStream json_request_stream, JsonWriter response_json_writer, Connection jdbc_connection, JsonArray log_list, JsonArray error_list) throws Exception {
        List<Request> request_list = serviceContent(json_request_stream);
        serviceTransaction(security_flag, request_list, response_json_writer, jdbc_connection, log_list, error_list);
    }
    
    public void serviceTransaction(Integer security_flag, List<Request> request_list, JsonWriter response_json_writer, Connection jdbc_connection, JsonArray log_list, JsonArray error_list) throws Exception {
        log_list.add("Start-Process");
        this.security_flag = security_flag;
        Gson gson = JsonUtil.gson();
        try {
            for (Request request : request_list) {
                request.init();
                RecordProcessor record_processor = new RecordProcessor(request, response_json_writer);
                RecordHandler record_handler= this;
                table.process(gson, record_processor, record_handler);
                if (record_processor.hasErrors()) {
                    record_processor.printErrors(gson);
                }
            }
        } catch (Exception ex) {
            JsonUtil.reclaimGson(gson);
            throw ex;
        }
        JsonUtil.reclaimGson(gson);
        log_list.add("End-Process");
    }
}
