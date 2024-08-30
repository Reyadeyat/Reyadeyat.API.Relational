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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;
import java.time.Duration;
import net.reyadeyat.api.relational.request.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.reyadeyat.api.library.util.Returns;
import net.reyadeyat.api.relational.request.Request;

/**
 * 
 * Description Record request processor
 * 
 *
 * @author Mohammad Nabil Mostafa
 * <a href="mailto:code@reyadeyat.net">code@reyadeyat.net</a>
 * 
 * @since 2023.01.01
 */
public class RecordProcessor {
    public Request request;
    public String command;
    public JsonWriter response_output_writer;
    public JsonArray error_list;
    public int affected_rows;
    public Boolean is_empty_view;
    public Response response;
    public Returns returns;
    public RecordProcessor parent_record_processor;
    public Map<String, RecordProcessor> child_map;
    public List<RecordProcessor> child_list;
    public List<Map<String, Object>> record_stack_frame_list;
    public Query query;
    
    private Boolean debug = Boolean.TRUE;
    
    public RecordProcessor(Request request, JsonWriter response_output_writer) throws Exception {
        this(null, request, response_output_writer);
    }
    
    public RecordProcessor(RecordProcessor parent_record_processor, Request request, JsonWriter response_output_writer) throws Exception {
        this.parent_record_processor = parent_record_processor;
        this.request = request;
        this.command = is_select() ? "Select" : is_insert() ? "insert" : is_update() ? "Update" : is_delete() ? "Delete" : null;
        if (this.command == null) {
             throw new Exception("Request has invalid command type");
        }
        this.query = new Query();
        this.response_output_writer = response_output_writer;
        this.error_list = new JsonArray();
        this.is_empty_view = true;
        this.affected_rows = 0;
        this.returns = new Returns();
        child_list = new ArrayList<>();
        child_map = new TreeMap<>();
        if (request.child_list != null && request.child_list.size() > 0) {
            record_stack_frame_list = new ArrayList<>();
            for (Request child_request : request.child_list) {
                RecordProcessor child_record_processor = new RecordProcessor(this, child_request, response_output_writer);
                child_list.add(child_record_processor);
                child_map.put(child_record_processor.request.table_alias, child_record_processor);
            }
        }
    }
    
    public void addRecordStackFrame(Map<String, Object> record_stack_frame) {
        record_stack_frame_list.add(record_stack_frame);
    }
    
    public List<Map<String, Object>> getTablePrimaryRecordList(Map<String, Object> table_parimary_record) {
        return record_stack_frame_list;
    }
    
    public String getResponseView() {
        return request.response;
    }
    
    public Request getTableRequest() {
        return request;
    }
    
    public RecordProcessor getChildTableRecordProcessor(String table_alias) {
        return child_map.get(table_alias);
    }
    
    public void addChildRecordProcessor(RecordProcessor child_record_processor) throws Exception {
        if (this.child_map.containsKey(child_record_processor.request.table_alias) == true) {
            throw new Exception("RecordProcessor Key '"+child_record_processor.request.table_alias+"' already exists!!");
        }
        this.child_map.put(child_record_processor.request.table_alias, child_record_processor);
    }
    
    public void deleteChildRecordProcessor(RecordProcessor child_record_processor) {
        child_map.remove(child_record_processor.request.table_alias);
    }
    
    public JsonWriter getResponseOutputWriter() {
        return response_output_writer;
    }
    
    public JsonArray addError(String error) {
        error_list.add(error);
        return error_list;
    }
    
    public JsonArray getErrors() {
        return error_list;
    }
    
    public void setAffectedRows(int affected_rows) {
        this.affected_rows = affected_rows;
    }
    
    public Integer getAffectedRows() {
        return affected_rows;
    }
    
    public <T> void addReturns(String key, T value) {
        returns.Returns(key, value);
    }
    
    public Returns getReturns() {
        return returns;
    }
    
    public Boolean is_select() {
        return request.select_list != null;
    }
    
    public Boolean is_update() {
        return request.update_field_map != null;
    }
    
    public Boolean is_insert() {
        return request.insert_field_map != null;
    }
    
    public Boolean is_delete() {
        return request.delete_field_map != null;
    }
    
    public String getCommand() throws Exception {
        return command;
    }
    
    public Boolean hasErrors() {
        Boolean has_errors = false;
        for (int i = 0; i < child_list.size(); i++) {
            RecordProcessor child_record_processor = child_list.get(i);
            has_errors = has_errors || child_record_processor.hasErrors();
        }
        return has_errors || error_list.size() > 0;
    }
    
    public void printErrors(Gson gson) throws Exception {
        name("Record Processing Errors");
        beginArray();
        printChildErrorList(gson);
        endArray();
        flush();
    }
    
    private void printChildErrorList(Gson gson) throws Exception {
        beginObject();
        name("table");
        value(request.table_alias);
        name("erorrs");
        writeJsonElement(gson, error_list);
        endObject();
        if (child_list == null || child_list.size() == 0) {
            return;
        }
        beginObject();
        name("children");
        beginArray();
        for (int i = 0; i < child_list.size(); i++) {
            RecordProcessor child_record_processor = child_list.get(i);
            child_record_processor.printChildErrorList(gson);
        }
        endArray();
        endObject();
        
    }
    
    public Boolean hasGroupBy() throws Exception {
        return request.group_by_list != null && request.group_by_list.size() > 0;
    }
    
    public void writeJsonElement(Gson gson, JsonElement json_element) throws Exception {
        gson.toJson(json_element, response_output_writer);
        system_out(json_element);
    }
    
    public JsonArray extractJsonObjectKeyList(JsonObject json_object) throws Exception {
        JsonArray json_array = new JsonArray();
        for (Map.Entry<String, JsonElement> element : json_object.entrySet()) {
            json_array.add(element.getKey());
        }
        return json_array;
    }
    
    public JsonArray extractJsonObjectValueList(JsonObject json_object) throws Exception {
        JsonArray json_array = new JsonArray();
        for (Map.Entry<String, JsonElement> element : json_object.entrySet()) {
            json_array.add(element.getValue());
        }
        return json_array;
    }
    
    public void mergeJsonElement(JsonElement json_element) throws Exception {
        mergeJsonElement(null, json_element);
    }
    
    public void mergeJsonElement(String name, JsonElement json_element) throws Exception {
        mergeJsonElement(name, json_element, false);
    }
    
    public void mergeJsonElement(String name, JsonElement json_element, Boolean keep_open) throws Exception {
        if (json_element.isJsonObject() == true) {
            if (name != null) {
                name(name);
            }
            beginObject();
            for (Map.Entry<String, JsonElement> element : json_element.getAsJsonObject().entrySet()) {
                name(element.getKey());
                JsonElement json_object_element = element.getValue();
                if (json_object_element.isJsonObject()) {
                    mergeJsonElement(json_object_element.getAsJsonObject());
                } else if (json_object_element.isJsonPrimitive()) {
                    mergeJsonPrimitive(json_object_element.getAsJsonPrimitive());
                } else if (json_object_element.isJsonNull() == true) {
                    nullValue();
                }
            }
            if (keep_open == false) {
                endObject();
            }
        } else if (json_element.isJsonArray() == true) {
            JsonArray json_array = json_element.getAsJsonArray();
            if (name != null) {
                name(name);
            }
            beginArray();
            for (int i = 0; i < json_array.size(); i++) {
                JsonElement json_array_element = json_array.get(i);
                mergeJsonElement(json_array_element);
            }
            if (keep_open == false) {
                endArray();
            }
        } else if (json_element.isJsonPrimitive() == true) {
            mergeJsonPrimitive(name, json_element.getAsJsonPrimitive());
        } else if (json_element.isJsonNull() == true) {
            nullValue();
        }
    }
    
    public void addJsonElement(JsonElement json_element) throws Exception {
        if (json_element.isJsonObject() == true) {
            for (Map.Entry<String, JsonElement> element : json_element.getAsJsonObject().entrySet()) {
                name(element.getKey());
                JsonElement json_object_element = element.getValue();
                if (json_object_element.isJsonObject()) {
                    mergeJsonElement(json_object_element.getAsJsonObject());
                } else if (json_object_element.isJsonPrimitive()) {
                    mergeJsonPrimitive(json_object_element.getAsJsonPrimitive());
                } else if (json_object_element.isJsonNull() == true) {
                    nullValue();
                }
            }
        } else if (json_element.isJsonArray() == true) {
            JsonArray json_array = json_element.getAsJsonArray();
            for (int i = 0; i < json_array.size(); i++) {
                JsonElement json_array_element = json_array.get(i);
                mergeJsonElement(json_array_element);
            }
        } else if (json_element.isJsonPrimitive() == true) {
            mergeJsonPrimitive(json_element.getAsJsonPrimitive());
        } else if (json_element.isJsonNull() == true) {
            nullValue();
        }
    }
    
    public void mergeJsonPrimitive(JsonPrimitive json_primitive) throws Exception {
        mergeJsonPrimitive(null, json_primitive);
    }
    
    public void mergeJsonPrimitive(String name, JsonPrimitive json_primitive) throws Exception {
        if (name != null) {
            name(name);
        }
        if (json_primitive.isBoolean()) {
            response_output_writer.value(json_primitive.getAsBoolean());
            system_out(json_primitive.getAsBoolean());
        } else if (json_primitive.isString()) {
            response_output_writer.value(json_primitive.getAsString());
            system_out(json_primitive.getAsString());
        } else if (json_primitive.isNumber()) {
            response_output_writer.value(json_primitive.getAsNumber());
            system_out(json_primitive.getAsNumber());
        }
    }
    
    public void writeJsonElement(Gson gson, String name, JsonElement json_element) throws Exception {
        name(name);
        gson.toJson(json_element, response_output_writer);
        system_out(gson.toJson(json_element));
    }
    
    public void beginObject() throws Exception {
        response_output_writer.beginObject();
        system_out("{");
    }
    
    public void name(String name) throws Exception {
        response_output_writer.name(name);
        system_out(name);
    }
    
    public void beginArray() throws Exception {
        response_output_writer.beginArray();
        system_out("[");
    }
    
    public void endArray() throws Exception {
        response_output_writer.endArray();
        system_out("]");
    }
    
    public void endObject() throws Exception {
        response_output_writer.endObject();
        system_out("}");
    }
    
    public void nullValue() throws Exception {
        nullValue(null);
    }
    
    public void nullValue(String name) throws Exception {
        if (name != null) {
            name(name);
        }
        response_output_writer.nullValue();
        system_out("null");
    }
    
    public void value(Boolean value) throws Exception {
        response_output_writer.value(value);
        system_out(value);
    }
    
    public void value(Number value) throws Exception {
        response_output_writer.value(value);
        system_out(value);
    }
    
    public void value(String value) throws Exception {
        response_output_writer.value(value);
        system_out(value);
    }
    
    public void value(boolean value) throws Exception {
        response_output_writer.value(value);
        system_out(value);
    }
    
    public void value(double value) throws Exception {
        response_output_writer.value(value);
        system_out(value);
    }
    
    public void value(float value) throws Exception {
        response_output_writer.value(value);
        system_out(value);
    }
    
    public void value(long value) throws Exception {
        response_output_writer.value(value);
        system_out(value);
    }
    
    public void flush() throws Exception {
        response_output_writer.flush();
    }
    
    private void system_out(Object text) {
        if (debug == true) {
            System.out.println(text.toString());
        }
    }
    
    public void query_stats() throws Exception {
        name("prepare ");
        value((Duration.between(query.t1, query.t2).toNanos()/1000000d) + " ms");
        name("query ");
        value((Duration.between(query.t2, query.t3).toNanos()/1000000d) + " ms");
        name("process ");
        value((Duration.between(query.t3, query.t4).toNanos()/1000000d) + " ms");
    }
    
}
