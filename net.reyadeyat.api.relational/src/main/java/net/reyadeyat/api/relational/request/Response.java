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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import static net.reyadeyat.api.relational.modeler.ModelingRequest.SECURITY_FLAG_RETURN_DESCRIPTIVE_RESPONSE_MESSAGE;

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
public class Response {
    Boolean success;
    String transactionType;
    Integer code;
    Integer state;
    String technical_message;
    String technical_message_i18n;
    JsonObject response;
    
    public Response(Boolean success, String transactionType, Integer code, String technical_message) {
        this(success, transactionType, code, code, technical_message, "");
    }
    
    public Response(Boolean success, String transactionType, Integer code, Integer state, String technical_message, String technical_message_i18n) {
        this.success = success;
        this.transactionType = transactionType;
        this.code = code;
        this.state = state;
        this.technical_message = technical_message;
        this.technical_message_i18n = technical_message_i18n;

        JsonObject jsonStatus = new JsonObject();
        jsonStatus.addProperty("transaction", this.transactionType);
        jsonStatus.addProperty("status", (this.success == true ? "success" : "error"));
        jsonStatus.addProperty("code", this.code);
        jsonStatus.addProperty("state", this.state);
        jsonStatus.addProperty("technical_message", this.technical_message);
        jsonStatus.addProperty("technical_message_i18n", this.technical_message_i18n);
        this.response = new JsonObject();
        this.response.add("response", jsonStatus);
    }
    
    public String getTransactionType() {
        return transactionType;
    }
    
    public String getTechnicalMessage() {
        return technical_message;
    }
    
    public String getTechnicalMessagei18n() {
        return technical_message_i18n;
    }
    
    public Integer getCode() {
        return this.code;
    }
    
    public Boolean getSuccess() {
        return this.success;
    }
    
    public JsonElement add(String property, JsonElement value) {
        this.response.add(property, value);
        return value;
    }
    
    public JsonObject addObject(String property, JsonObject value) {
        this.response.add(property, value);
        return value;
    }
    
    public JsonArray addArray(String property, JsonArray value) {
        this.response.add(property, value);
        return value;
    }
    
    public void addProperty(String property, Boolean value) {
        this.response.addProperty(property, value);
    }
    
    public void addProperty(String property, String value) {
        this.response.addProperty(property, value);
    }
    
    public void addProperty(String property, Number value) {
        this.response.addProperty(property, value);
    }
    
    public JsonObject getResponse() {
        return this.response;
    }
    
    public JsonObject getResponse(String property) {
        return (property == null || this.response.get(property) == null || this.response.get(property).isJsonNull() ? null : this.response.get(property).getAsJsonObject());
    }
}
