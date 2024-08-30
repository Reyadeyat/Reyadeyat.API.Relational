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
package net.reyadeyat.api.relational.test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.reyadeyat.api.library.json.JsonUtil;
import net.reyadeyat.api.library.util.StringUtil;
import net.reyadeyat.api.relational.annotation.DontJsonAnnotation;
import net.reyadeyat.api.relational.model.Field;
import net.reyadeyat.api.relational.model.ForeignKey;
import net.reyadeyat.api.relational.model.ForeignKeyField;
import net.reyadeyat.api.relational.model.ReferencedKeyField;
import net.reyadeyat.api.relational.model.Table;
import net.reyadeyat.api.relational.model.TableInterfaceImplementationDataStructures;

/**
 *
 * Description
 *
 *
 * @author Mohammad Nabil Mostafa
 * <a href="mailto:code@reyadeyat.net">code@reyadeyat.net</a>
 *
 * @since 2023.07.01
 */
public class UserDefinedTableInterfaceImplementationDataStructures implements TableInterfaceImplementationDataStructures {
    
    @DontJsonAnnotation
    transient public String java_data_structure_class;
    @DontJsonAnnotation
    transient public String typescript_data_structure_class;
    @DontJsonAnnotation
    transient public String typescript_request_send_response;
    @DontJsonAnnotation
    transient public String typescript_form_component_ts;
    @DontJsonAnnotation
    transient public String typescript_form_component_html;
    @DontJsonAnnotation
    transient public String typescript_table_component_ts;
    @DontJsonAnnotation
    transient public String typescript_table_component_html;
    @DontJsonAnnotation
    transient public String http_requests;
    @DontJsonAnnotation
    transient public String database_servlet_class;
    @DontJsonAnnotation
    transient public String database_servlet_uri;
    
    transient private static String nl = "\n";
    transient private static ArrayList<String> lang_suffix_list = new ArrayList<>(Arrays.asList(new String[]{"_ar", "_en"}));

    public UserDefinedTableInterfaceImplementationDataStructures() {}
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
    
    @Override
    public void generateModelDataStructures(Table table) throws Exception {
        String class_name = getModelClassName(table.name, false);
        String class_name_spaced = getModelClassName(table.name, true);
        generateJavaDataStructureClass(table, class_name);
        generateTypescriptDataStructureClass(table, class_name);
        generateTypescriptFormComponent(table, class_name);
        generateDatabaseServletClass(table, class_name, class_name_spaced);
        
        Gson gson = JsonUtil.gsonPretty();
        generateHttpRequests(table, gson);
        JsonUtil.reclaimGsonPretty(gson);
    }
    
    private void generateJavaDataStructureClass(Table table, String class_name) throws Exception {
        String author_name = "AUTHOR_NAME";
        String author_email = "AUTHOR_EMAIL";
        String java_class_header = 
        "package $PACKAGE_NAME;\n" +
"\n" +
"/**\n" +
" *\n" +
" * @author "+author_name+"\n" +
" * <a href=\""+author_email+"\">"+author_email+"</a>\n" +
" */\n";
        StringBuilder java_data_structure_class = new StringBuilder(java_class_header);
        StringBuilder java_data_structure_class_constructor_arguments = new StringBuilder();
        StringBuilder java_data_structure_class_constructor_method = new StringBuilder();
        replace(java_data_structure_class, "$PACKAGE_NAME", table.database.java_package_name);
        java_data_structure_class.append("public class ").append(class_name).append(" {\n");
        for (Field field : table.field_list) {
            String java_datatype = table.data_lookup.lookupJavaDataType(field.data_type_name);
            java_data_structure_class.append("    public ").append(java_datatype).append(" ").append(field.name).append(";\n");
            java_data_structure_class_constructor_arguments.append("\n        ").append(java_datatype).append(" ").append(field.name).append(",");
            java_data_structure_class_constructor_method.append("        this.").append(field.name).append(" = ").append(field.name).append(";\n");
        }
        for (ForeignKey foreign_key : table.foreign_key_list) {
            for (Map.Entry<ForeignKeyField, ReferencedKeyField> entry : foreign_key.foreign_key_referenced_key_map.entrySet()) {
                ForeignKeyField foreignKeyField = entry.getKey();
                ReferencedKeyField referenced_key_field = entry.getValue();
                String foreign_table_name = foreign_key.referenced_key_table_name;
                String foreign_class_name = getModelClassName(foreign_key.referenced_key_table_name, false);
                String var_name = getModelClassVariableName(foreign_class_name);
                java_data_structure_class.append("    public ").append(foreign_class_name).append(" ").append(var_name).append(";\n");
                java_data_structure_class_constructor_arguments.append("\n        ").append(foreign_class_name).append(" ").append(var_name).append(",");
                java_data_structure_class_constructor_method.append("        this.").append(var_name).append(" = ").append(var_name).append(";\n");
            }
        }
        java_data_structure_class_constructor_arguments = java_data_structure_class_constructor_arguments.length() > 0 ? java_data_structure_class_constructor_arguments.deleteCharAt(java_data_structure_class_constructor_arguments.length()-1) : java_data_structure_class_constructor_arguments;
        java_data_structure_class.append("\n    public ").append(class_name).append("(").append(java_data_structure_class_constructor_arguments).append("\n    ) {\n")
                .append(java_data_structure_class_constructor_method).append("    }\n}\n");
        this.java_data_structure_class = java_data_structure_class.toString();
    }
    
    private void generateTypescriptDataStructureClass(Table table, String class_name) throws Exception {
        StringBuilder typescript_data_structure_class = new StringBuilder();
        StringBuilder typescript_data_structure_class_constructor_arguments = new StringBuilder();
        StringBuilder typescript_data_structure_class_constructor_method = new StringBuilder();
        StringBuilder typescript_data_structure_class_equals = new StringBuilder();
        StringBuilder typescript_data_structure_class_equals_body = new StringBuilder();
        StringBuilder typescript_data_structure_class_from_json = new StringBuilder();
        StringBuilder typescript_data_structure_class_from_json_object_list = new StringBuilder();
        StringBuilder typescript_data_structure_class_from_json_object_list_arguments = new StringBuilder();
        StringBuilder typescript_data_structure_class_to_json = new StringBuilder();
        StringBuilder typescript_request_send_response_builder = new StringBuilder(request_send_response);
        StringBuilder typescript_request_send_response_select_fields = new StringBuilder();
        StringBuilder typescript_request_send_response_insert_update_delete_fields = new StringBuilder();
        StringUtil.replaceAll(typescript_request_send_response_builder, "$TABLE_NAME", table.name);
        StringUtil.replaceAll(typescript_request_send_response_builder, "$TABLE_CLASS", class_name);
        typescript_data_structure_class.append("export class ").append(class_name).append(" extends RecordControl {\n");
        typescript_data_structure_class_constructor_method.append("\t\tsuper();\n");
        typescript_data_structure_class_equals.append("    equals(").append(table.name).append(": ").append(class_name).append(") {\n        return ").append(table.name).append(" != null");
        typescript_data_structure_class_from_json.append("    static fromJSON(json: any) : ").append(class_name).append(" {\n").append("        return new ").append(class_name).append("(\n");
        typescript_data_structure_class_from_json_object_list.append(") : ").append(class_name).append(" {\n").append("        return new ").append(class_name).append("(\n");
        typescript_data_structure_class_from_json_object_list_arguments.append("    static fromJSONObjectList(json: any, ");
        typescript_data_structure_class_to_json.append("    toJSON() : any {\n").append("        //return JSON.stringify(this);\n").append("        return {\n\t\t\tis_checked: this.is_checked,\n\t\t\tfree_text: this.free_text,\n");
        HashMap<String, ArrayList<String>> i18n_map = new HashMap<String, ArrayList<String>>();
        for (Field field : table.field_list) {
            String java_datatype = table.data_lookup.lookupJavaDataType(field.data_type_name);
            String typescript_datatype = table.data_lookup.lookupTypescriptDataType(field.data_type_name);
            typescript_data_structure_class.append("    ").append(field.name).append("?: ").append(typescript_datatype).append(";\n");
            typescript_data_structure_class_constructor_arguments.append("\n        ").append(field.name).append("?: ").append(typescript_datatype).append(",");
            typescript_data_structure_class_constructor_method.append("        this.").append(field.name).append(" = ").append(field.name).append(";\n");
            typescript_request_send_response_select_fields.append("\t\t\t\t\"").append(field.name).append("\",\n");
            typescript_request_send_response_insert_update_delete_fields.append("\t\t\t\t\t").append(field.name).append(": ").append(table.name).append(".").append(field.name).append(",\n");
            if (field.primary_key == true) {
                typescript_data_structure_class_equals_body.append("&& this.").append(field.name).append(" == ").append(table.name).append(".").append(field.name).append("\n");
            }
            
            if (typescript_datatype.equalsIgnoreCase("Date")) {
                if (java_datatype.equalsIgnoreCase("Date") || java_datatype.equalsIgnoreCase("Timestamp")) {
                    typescript_data_structure_class_from_json.append("            new Date(json.").append(field.name).append("),\n");
                    typescript_data_structure_class_from_json_object_list.append("            new Date(json.").append(field.name).append("),\n");
                } else if (java_datatype.equalsIgnoreCase("Time")) {
                    typescript_data_structure_class_from_json.append("            new Date(new Date().toISOString().substring(0,10)+' '+json.").append(field.name).append("),\n");
                    typescript_data_structure_class_from_json_object_list.append("            new Date(new Date().toISOString().substring(0,10)+' '+json.").append(field.name).append("),\n");
                }
            } else {
                typescript_data_structure_class_from_json.append("            json.").append(field.name).append(",\n");
                typescript_data_structure_class_from_json_object_list.append("            json.").append(field.name).append(",\n");
            }
            
            if (typescript_datatype.equalsIgnoreCase("Date")) {
                typescript_data_structure_class_to_json.append("            ").append(field.name).append(": this.").append(field.name).append(".toISOString(),\n");
            } else {
                typescript_data_structure_class_to_json.append("            ").append(field.name).append(": this.").append(field.name).append(",\n");
            }
            
            for (String lang_suffix : lang_suffix_list) {
                if (field.name.endsWith(lang_suffix) == true) {
                    String var_name = field.name.substring(0, field.name.length()-3) + "_i18";
                    ArrayList<String> string_i18n_list = i18n_map.get(var_name);
                    if (string_i18n_list == null) {
                        string_i18n_list = new ArrayList<String>();
                        i18n_map.put(var_name, string_i18n_list);
                    }
                    string_i18n_list.add(field.name);
                }
            }
        }
        typescript_data_structure_class_equals_body = typescript_data_structure_class_equals_body.length() > 0 ? typescript_data_structure_class_equals_body.deleteCharAt(typescript_data_structure_class_equals_body.length()-1).insert(0, "\n        ").append(";\n") : typescript_data_structure_class_equals_body;
        typescript_data_structure_class_equals.append(typescript_data_structure_class_equals_body);
        typescript_data_structure_class_equals.append("    }\n");
        typescript_request_send_response_select_fields.deleteCharAt(typescript_request_send_response_select_fields.length()-2);
        typescript_request_send_response_insert_update_delete_fields.deleteCharAt(typescript_request_send_response_insert_update_delete_fields.length()-2);
        StringUtil.replaceAll(typescript_request_send_response_builder, "$SELECT_FIELDS", typescript_request_send_response_select_fields.toString());
        StringUtil.replaceAll(typescript_request_send_response_builder, "$INSERT_UPDATE_DELETE_FIELDS", typescript_request_send_response_insert_update_delete_fields.toString());
        for (Map.Entry<String, ArrayList<String>> i18n_entry: i18n_map.entrySet()) {
            String var_name = i18n_entry.getKey();
            ArrayList<String> i18n_var_name_list = i18n_entry.getValue();
            typescript_data_structure_class.append("    ").append(var_name).append("?: ").append("StringI18").append(";\n");
            typescript_data_structure_class_constructor_method.append("        this.").append(var_name).append(" = new StringI18(");
            for (String i18n_var_name : i18n_var_name_list) {
                typescript_data_structure_class_constructor_method.append(i18n_var_name).append(", ");
            }
            typescript_data_structure_class_constructor_method = typescript_data_structure_class_constructor_method.length() > 0 ? typescript_data_structure_class_constructor_method.delete(typescript_data_structure_class_constructor_method.length()-2, typescript_data_structure_class_constructor_method.length()) : typescript_data_structure_class_constructor_method;
            typescript_data_structure_class_constructor_method.append(");\n");
        }
        for (ForeignKey foreign_key : table.foreign_key_list) {
            for (Map.Entry<ForeignKeyField, ReferencedKeyField> entry : foreign_key.foreign_key_referenced_key_map.entrySet()) {
                ForeignKeyField foreignKeyField = entry.getKey();
                ReferencedKeyField referenced_key_field = entry.getValue();
                String foreign_table_name = foreign_key.referenced_key_table_name;
                String foreign_class_type_name = getModelClassName(foreign_key.referenced_key_table_name, false);
                //String var_name = getModelClassVariableName(foreign_class_type_name);
                String var_name_field = referenced_key_field.name.toLowerCase().endsWith("_id") == true ? referenced_key_field.name.substring(0, referenced_key_field.name.length()-3) : referenced_key_field.name;
                String var_name = getModelClassVariableName(var_name_field);
                typescript_data_structure_class.append("    ").append(var_name).append("?: ").append(foreign_class_type_name).append(" | null;\n");
                typescript_data_structure_class_constructor_arguments.append("\n        ").append(var_name).append("?: ").append(foreign_class_type_name).append(" | null,");
                typescript_data_structure_class_constructor_method.append("        this.").append(var_name).append(" = ").append(var_name).append(";\n");
                typescript_data_structure_class_from_json.append("            null,\n");
                typescript_data_structure_class_from_json_object_list.append("            ").append(var_name).append(",\n");
                typescript_data_structure_class_from_json_object_list_arguments.append(var_name).append(": ").append(foreign_class_type_name).append(" | null, ");
            }
        }
        
        /*Complex objects of typescript class are rarely stringified */
        typescript_data_structure_class_from_json = typescript_data_structure_class_from_json.length() > 0 ? typescript_data_structure_class_from_json.delete(typescript_data_structure_class_from_json.length()-2, typescript_data_structure_class_from_json.length()) : typescript_data_structure_class_from_json;
        typescript_data_structure_class_from_json_object_list = typescript_data_structure_class_from_json_object_list.length() > 0 ? typescript_data_structure_class_from_json_object_list.delete(typescript_data_structure_class_from_json_object_list.length()-2, typescript_data_structure_class_from_json_object_list.length()) : typescript_data_structure_class_from_json_object_list;
        typescript_data_structure_class_from_json_object_list_arguments = typescript_data_structure_class_from_json_object_list_arguments.length() > 0 ? typescript_data_structure_class_from_json_object_list_arguments.delete(typescript_data_structure_class_from_json_object_list_arguments.length()-2, typescript_data_structure_class_from_json_object_list_arguments.length()) : typescript_data_structure_class_from_json_object_list_arguments;
        typescript_data_structure_class_to_json = typescript_data_structure_class_to_json.length() > 0 ? typescript_data_structure_class_to_json.delete(typescript_data_structure_class_to_json.length()-2, typescript_data_structure_class_to_json.length()) : typescript_data_structure_class_to_json;
        typescript_data_structure_class_from_json.append("\n        );\n").append("    }\n");
        typescript_data_structure_class_from_json_object_list.append("\n        );\n").append("    }\n");
        typescript_data_structure_class_from_json_object_list = typescript_data_structure_class_from_json_object_list_arguments.append(typescript_data_structure_class_from_json_object_list);
        typescript_data_structure_class_to_json.append("\n        };\n").append("    }\n");
        
        typescript_data_structure_class_constructor_arguments = typescript_data_structure_class_constructor_arguments.length() > 0 ? typescript_data_structure_class_constructor_arguments.deleteCharAt(typescript_data_structure_class_constructor_arguments.length()-1) : typescript_data_structure_class_constructor_arguments;
        typescript_data_structure_class.append("\n    constructor(").append(typescript_data_structure_class_constructor_arguments).append("\n    ) {\n")
                .append(typescript_data_structure_class_constructor_method).append("    \n    }\n    \n")
                .append(typescript_data_structure_class_equals_body.length() == 0 ? "" : typescript_data_structure_class_equals).append("    \n")
                .append(typescript_data_structure_class_from_json).append("    \n")
                .append(typescript_data_structure_class_from_json_object_list).append("    \n")
                .append(typescript_data_structure_class_to_json).append("    \n")
                .append("}\n");
        this.typescript_data_structure_class = typescript_data_structure_class.toString();
        this.typescript_request_send_response = typescript_request_send_response_builder.toString();
    }
    
    private void generateTypescriptFormComponent(Table table, String class_name) throws Exception {
        
        StringBuilder typescript_form_component_ts_builder = new StringBuilder(form_component_ts);
        StringBuilder typescript_form_component_html_builder = new StringBuilder(form_component_html);
        StringBuilder typescript_form_field_control_class_members_builder = new StringBuilder();
        StringBuilder typescript_form_foreign_field_control_class_members_builder = new StringBuilder();
        StringBuilder typescript_form_foreign_field_array_class_members_builder = new StringBuilder();
        StringBuilder typescript_form_field_control_definition_builder = new StringBuilder();
        StringBuilder typescript_form_foreign_field_control_definition_builder = new StringBuilder();
        StringBuilder typescript_form_foreign_field_array_definition_builder = new StringBuilder();
        StringBuilder typescript_form_field_control_builder = new StringBuilder();
        StringBuilder typescript_form_foreign_field_control_builder = new StringBuilder();
        StringBuilder typescript_form_field_html_text_box_control_builder = new StringBuilder();
        StringBuilder typescript_form_field_html_date_box_control_builder = new StringBuilder();
        StringBuilder typescript_form_foreign_field_html_check_box_control_builder = new StringBuilder();
        StringBuilder typescript_form_foreign_field_html_list_box_control_builder = new StringBuilder();
        StringBuilder typescript_form_foreign_field_check_box_builder = new StringBuilder();
        
        StringBuilder typescript_table_component_ts_builder = new StringBuilder(table_component_ts);
        StringBuilder typescript_table_component_html_builder = new StringBuilder(table_component_html);
        StringBuilder typescript_table_field_list_builder = new StringBuilder();
        StringBuilder typescript_table_foreign_field_list_builder = new StringBuilder();
        StringBuilder typescript_table_field_html_primary_key_icon_control_builder = new StringBuilder();
        StringBuilder typescript_table_field_html_i18_control_builder = new StringBuilder();
        StringBuilder typescript_table_foreign_field_html_i18_control_builder = new StringBuilder();
        
        HashMap<String, HashMap<String, String>> i18n_map = new HashMap<String, HashMap<String, String>>();
        for (Field field : table.field_list) {
            String java_datatype = table.data_lookup.lookupJavaDataType(field.data_type_name);
            String typescript_datatype = table.data_lookup.lookupTypescriptDataType(field.data_type_name);
            if (field.foreign_reference == true && field.primary_key == true) {
                typescript_table_field_html_primary_key_icon_control_builder.append(typescript_table_field_html_primary_key_icon_control.replaceAll("\\$FIELD_NAME", field.name).replaceAll("\\$TABLE_NAME", table.name).replaceAll("\\$TABLE_CLASS", class_name)).append(nl);
            }
            
            String control_name = field.name;
            if (field.foreign_reference == true && field.primary_key == false) {
                int index = field.name.lastIndexOf("_id");
                if (index > -1) {
                    control_name = control_name.substring(0, index) + "_list_option";
                }
            }
            typescript_form_field_control_class_members_builder.append("\t").append(typescript_form_field_control_class_members.replaceAll("\\$FIELD_NAME", control_name)).append(nl);
            typescript_form_field_control_definition_builder.append("\t").append(typescript_form_field_control_definition.replaceAll("\\$FIELD_NAME", control_name)).append(nl);
            typescript_form_field_control_builder.append("\t\t\t").append(typescript_form_field_control.replaceAll("\\$FIELD_NAME", control_name)).append(nl);
            
            typescript_table_field_list_builder.append("\t\t\"").append(field.name).append("\",").append(nl);
            
            if (typescript_datatype.equalsIgnoreCase("Date")) {
                if (java_datatype.equalsIgnoreCase("Date")) {
                    typescript_form_field_html_date_box_control_builder.append(typescript_form_field_html_date_box_control.replaceAll("\\$TABLE_NAME", table.name).replaceAll("\\$FIELD_NAME", field.name)).append(nl);
                } else if (java_datatype.equalsIgnoreCase("Time") || java_datatype.equalsIgnoreCase("Timestamp")) {
                    typescript_form_field_html_text_box_control_builder.append(typescript_form_field_html_text_box_control.replaceAll("\\$TABLE_NAME", table.name).replaceAll("\\$FIELD_NAME", field.name).replaceAll("\\$FIELD_HTML_TYPE", typescript_datatype).replaceAll("\\$FIELD_IS_NOT_NULL", String.valueOf(!field.nullable).toLowerCase()).replaceAll("\\$PRIMARY_KEY_TEXT_BOX", "")).append(nl);
                }
            } else {
                if (field.primary_key == true) {
                    typescript_form_field_html_text_box_control_builder.append(typescript_form_field_html_text_box_control.replaceAll("\\$TABLE_NAME", table.name).replaceAll("\\$FIELD_NAME", field.name).replaceAll("\\$FIELD_HTML_TYPE", typescript_datatype).replaceAll("\\$FIELD_IS_NOT_NULL", String.valueOf(false).toLowerCase()).replaceAll("\\$PRIMARY_KEY_TEXT_BOX", "[readonly]=\"true\" [disabled]=\"true\"")).append(nl);
                } else {
                    typescript_form_field_html_text_box_control_builder.append(typescript_form_field_html_text_box_control.replaceAll("\\$TABLE_NAME", table.name).replaceAll("\\$FIELD_NAME", field.name).replaceAll("\\$FIELD_HTML_TYPE", typescript_datatype.equalsIgnoreCase("Boolean") ? "checkbox" : typescript_datatype).replaceAll("\\$FIELD_IS_NOT_NULL", String.valueOf(!field.nullable).toLowerCase()).replaceAll("\\$PRIMARY_KEY_TEXT_BOX", "")).append(nl);
                }
            }
            
            for (String lang_suffix : lang_suffix_list) {
                if (field.name.endsWith(lang_suffix) == true) {
                    String field_i18_var_name = field.name.substring(0, field.name.length()-3) + "_i18";
                    HashMap<String, String> string_i18n_map = i18n_map.get(field_i18_var_name);
                    if (string_i18n_map == null) {
                        string_i18n_map = new HashMap<String, String>();
                        i18n_map.put(field_i18_var_name, string_i18n_map);
                    }
                    string_i18n_map.put("$FIELD_NAME", field_i18_var_name);
                    string_i18n_map.put("$TABLE_NAME", table.name);
                    string_i18n_map.put("$TABLE_CLASS", class_name);
                }
            }
        }
        for (Map.Entry<String, HashMap<String, String>> i18n_entry: i18n_map.entrySet()) {
            String var_name = i18n_entry.getKey();
            HashMap<String, String> string_i18n_map = i18n_entry.getValue();
            typescript_table_field_list_builder.append("\t\t\"").append(var_name).append("\",").append(nl);
            typescript_table_field_html_i18_control_builder.append(typescript_table_field_html_i18_control.replaceAll("\\$FIELD_NAME", string_i18n_map.get("$FIELD_NAME")).replaceAll("\\$TABLE_NAME", string_i18n_map.get("$TABLE_NAME")).replaceAll("\\$TABLE_CLASS", string_i18n_map.get("$TABLE_CLASS"))).append(nl);
        }
        
        for (ForeignKey foreign_key : table.foreign_key_list) {
            for (Map.Entry<ForeignKeyField, ReferencedKeyField> entry : foreign_key.foreign_key_referenced_key_map.entrySet()) {
                ForeignKeyField foreignKeyField = entry.getKey();
                ReferencedKeyField referenced_key_field = entry.getValue();
                String foreign_table_name = foreign_key.referenced_key_table_name;
                String foreign_class_type_name = getModelClassName(foreign_key.referenced_key_table_name, false);
                //String var_name = getModelClassVariableName(foreign_class_type_name);
                String var_name_field = referenced_key_field.name.toLowerCase().endsWith("_id") == true ? referenced_key_field.name.substring(0, referenced_key_field.name.length()-3) : referenced_key_field.name;
                String foreign_field_var_name = getModelClassVariableName(var_name_field);
                
                typescript_form_foreign_field_control_class_members_builder.append("\t").append(typescript_form_foreign_field_control_class_members.replaceAll("\\$FOREIGN_FIELD_NAME", foreign_field_var_name)).append(nl);
                typescript_form_foreign_field_array_class_members_builder.append("\t").append(typescript_form_foreign_field_array_class_members.replaceAll("\\$FOREIGN_FIELD_NAME", foreign_field_var_name)).append(nl);
                
                typescript_form_foreign_field_control_definition_builder.append("\t").append(typescript_form_foreign_field_control_definition.replaceAll("\\$FOREIGN_TABLE_NAME", foreign_table_name).replaceAll("\\$FOREIGN_FIELD_NAME", foreign_field_var_name)).append(nl);
                typescript_form_foreign_field_array_definition_builder.append("\t").append(typescript_form_foreign_field_array_definition.replaceAll("\\$FOREIGN_TABLE_NAME", foreign_table_name).replaceAll("\\$FOREIGN_FIELD_NAME", foreign_field_var_name)).append(nl);
                
                typescript_form_foreign_field_control_builder.append("\t").append(typescript_form_foreign_field_control.replaceAll("\\$FOREIGN_TABLE_NAME", foreign_table_name).replaceAll("\\$FOREIGN_FIELD_NAME", foreign_field_var_name)).append(nl);
                
                typescript_form_foreign_field_html_check_box_control_builder.append(typescript_form_foreign_field_html_check_box_control.replaceAll("\\$FOREIGN_TABLE_NAME", foreign_table_name).replaceAll("\\$FOREIGN_FIELD_NAME", foreign_field_var_name)).append(nl);
                typescript_form_foreign_field_html_list_box_control_builder.append(typescript_form_foreign_field_html_list_box_control.replaceAll("\\$FOREIGN_TABLE_NAME", foreign_table_name).replaceAll("\\$FOREIGN_FIELD_NAME", foreign_field_var_name)).append(nl);
                
                typescript_form_foreign_field_check_box_builder.append(typescript_form_foreign_field_check_box.replaceAll("\\$FOREIGN_TABLE_NAME", foreign_table_name).replaceAll("\\$FOREIGN_TABLE_CLASS", foreign_class_type_name)).append(nl);
                
                String referenced_key_field_name = referenced_key_field.name.toLowerCase().endsWith("_id") == true ? referenced_key_field.name.substring(0, referenced_key_field.name.lastIndexOf("_id")) : referenced_key_field.name;
                typescript_table_foreign_field_list_builder.append("\t\t\"").append(referenced_key_field_name).append("\",").append(nl);

                typescript_table_field_list_builder.append("\t\t\"").append(foreign_field_var_name).append("\",").append(nl);
                typescript_table_foreign_field_html_i18_control_builder.append(typescript_table_foreign_field_html_i18_control.replaceAll("\\$TABLE_NAME", table.name).replaceAll("\\$FOREIGN_FIELD_NAME", foreign_field_var_name).replaceAll("\\$FOREIGN_TABLE_NAME", foreign_table_name).replaceAll("\\$FOREIGN_TABLE_CLASS", foreign_class_type_name)).append(nl);
            }
        }
        
        typescript_table_field_list_builder = typescript_table_field_list_builder.length() > 0 ? typescript_table_field_list_builder.deleteCharAt(typescript_table_field_list_builder.length()-2) : typescript_table_field_list_builder;
        typescript_table_foreign_field_list_builder = typescript_table_foreign_field_list_builder.length() > 0 ? typescript_table_foreign_field_list_builder.deleteCharAt(typescript_table_foreign_field_list_builder.length()-2) : typescript_table_foreign_field_list_builder;
        
        /*Complex objects of typescript class are rarely stringified */
        typescript_form_component_ts_builder.insert(0, "\n\n----------- $TABLE_NAME Form Controls ---------------------\n\n");
        StringUtil.replaceAll(typescript_form_component_ts_builder, "$TABLE_NAME", table.name);
        StringUtil.replaceAll(typescript_form_component_ts_builder, "$TABLE_CLASS", class_name);
        StringUtil.replaceAll(typescript_form_component_ts_builder, "$TYPESCRIPT_FORM_FIELD_CONTROL_CLASS_MEMBERS", typescript_form_field_control_class_members_builder.toString());
        StringUtil.replaceAll(typescript_form_component_ts_builder, "$TYPESCRIPT_FORM_FOREIGN_FIELD_CONTROL_CLASS_MEMBERS", typescript_form_foreign_field_control_class_members_builder.toString());
        StringUtil.replaceAll(typescript_form_component_ts_builder, "$TYPESCRIPT_FORM_FOREIGN_FIELD_ARRAY_CLASS_MEMBERS", typescript_form_foreign_field_array_class_members_builder.toString());
        StringUtil.replaceAll(typescript_form_component_ts_builder, "$TYPESCRIPT_FORM_FIELD_CONTROL_DEFINITION", typescript_form_field_control_definition_builder.toString());
        StringUtil.replaceAll(typescript_form_component_ts_builder, "$TYPESCRIPT_FORM_FOREIGN_FIELD_CONTROL_DEFINITION", typescript_form_foreign_field_control_definition_builder.toString());
        StringUtil.replaceAll(typescript_form_component_ts_builder, "$TYPESCRIPT_FORM_FOREIGN_FIELD_ARRAY_DEFINITION", typescript_form_foreign_field_array_definition_builder.toString());
        StringUtil.replaceAll(typescript_form_component_ts_builder, "$TYPESCRIPT_FORM_FOREIGN_FIELD_CONTROL_DEFINITION", typescript_form_foreign_field_control_definition_builder.toString());
        StringUtil.replaceAll(typescript_form_component_ts_builder, "$TYPESCRIPT_FORM_FIELD_CONTROL", typescript_form_field_control_builder.toString());
        StringUtil.replaceAll(typescript_form_component_ts_builder, "$TYPESCRIPT_FORM_FOREIGN_FIELD_CONTROL", typescript_form_foreign_field_control_builder.toString());
        StringUtil.replaceAll(typescript_form_component_ts_builder, "$TYPESCRIPT_FORM_FOREIGN_FIELD_CHECK_BOX", typescript_form_foreign_field_check_box_builder.toString());
        typescript_form_component_html_builder.append(nl).append("\n\n------------------ $TABLE_NAME Form HTML Controls ---------------\n\n");
        StringUtil.replaceAll(typescript_form_component_html_builder, "$TABLE_NAME", table.name);
        StringUtil.replaceAll(typescript_form_component_html_builder, "$TABLE_CLASS", class_name);
        StringUtil.replaceAll(typescript_form_component_html_builder, "$TYPESCRIPT_FORM_FIELD_HTML_TEXT_BOX_CONTROL", typescript_form_field_html_text_box_control_builder.toString());
        StringUtil.replaceAll(typescript_form_component_html_builder, "$TYPESCRIPT_FORM_FIELD_HTML_DATE_BOX_CONTROL", typescript_form_field_html_date_box_control_builder.toString());
        StringUtil.replaceAll(typescript_form_component_html_builder, "$TYPESCRIPT_FORM_FOREIGN_FIELD_HTML_CHECK_BOX_CONTROL", typescript_form_foreign_field_html_check_box_control_builder.toString());
        StringUtil.replaceAll(typescript_form_component_html_builder, "$TYPESCRIPT_FORM_FOREIGN_FIELD_HTML_LIST_BOX_CONTROL", typescript_form_foreign_field_html_list_box_control_builder.toString());
        
        this.typescript_form_component_ts = typescript_form_component_ts_builder.toString();
        this.typescript_form_component_html = typescript_form_component_html_builder.toString();
        
        typescript_table_component_ts_builder.insert(0, "\n\n----------- $TABLE_NAME MAT Table Controls ---------------------\n\n");
        StringUtil.replaceAll(typescript_table_component_ts_builder, "$TABLE_NAME", table.name);
        StringUtil.replaceAll(typescript_table_component_ts_builder, "$TABLE_CLASS", class_name);
        StringUtil.replaceAll(typescript_table_component_ts_builder, "$FIELD_LIST", typescript_table_field_list_builder.toString());
        StringUtil.replaceAll(typescript_table_component_ts_builder, "$FOREIGN_FIELD_LIST", typescript_table_foreign_field_list_builder.toString());
        //StringUtil.replaceAll(typescript_table_component_ts_builder, "$TYPESCRIPT_FORM_FIELD_CONTROL_DEFINITION", typescript_table_field_control_definition_builder.toString());
        //StringUtil.replaceAll(typescript_table_component_ts_builder, "$TYPESCRIPT_FORM_FOREIGN_FIELD_CONTROL_DEFINITION", typescript_table_foreign_field_control_definition_builder.toString());
        //StringUtil.replaceAll(typescript_table_component_ts_builder, "$TYPESCRIPT_FORM_FIELD_CONTROL", typescript_table_field_control_builder.toString());
        //StringUtil.replaceAll(typescript_table_component_ts_builder, "$TYPESCRIPT_FORM_FOREIGN_FIELD_CONTROL", typescript_table_foreign_field_control_builder.toString());
        typescript_table_component_html_builder.append(nl).append("------------------ $TABLE_NAME MAT Table HTML Controls ---------------").append(nl).append(nl);
        StringUtil.replaceAll(typescript_table_component_html_builder, "$TABLE_NAME", table.name);
        StringUtil.replaceAll(typescript_table_component_html_builder, "$TABLE_CLASS", class_name);
        StringUtil.replaceAll(typescript_table_component_html_builder, "$TYPESCRIPT_TABLE_FIELD_HTML_PRIMARY_KEY_ICON_CONTROL", typescript_table_field_html_primary_key_icon_control_builder.toString());
        StringUtil.replaceAll(typescript_table_component_html_builder, "$TYPESCRIPT_TABLE_FIELD_HTML_I18_CONTROL", typescript_table_field_html_i18_control_builder.toString());
        StringUtil.replaceAll(typescript_table_component_html_builder, "$TYPESCRIPT_TABLE_FOREIGN_FIELD_HTML_I18_CONTROL", typescript_table_foreign_field_html_i18_control_builder.toString());
        
        this.typescript_table_component_ts = typescript_table_component_ts_builder.toString();
        this.typescript_table_component_html = typescript_table_component_html_builder.toString();
    }
    
    private void generateDatabaseServletClass(Table table, String class_name, String class_name_spaced) throws Exception {
        String user_package = "AUTHOR_PACKAGE";
        String author_name = "AUTHOR_NAME";
        String author_email = "AUTHOR_EMAIL";
        String servlet_class = 
        "package $PACKAGE_NAME;\n" +
"\n" +
"import "+user_package+".database.FieldType;\n" +
"import "+user_package+".servlet.DatabaseServlet;\n" +
"import jakarta.servlet.annotation.WebServlet;\n" +
"\n" +
"/**\n" +
" *\n" +
" * @author "+author_name+"\n" +
" * <a href=\"mailto:"+author_email+"\">"+author_email+"</a>\n" +
" */\n" +
"@WebServlet(\"$SERVLET_CONTEXT\")\n" +
"public class $CLASS_NAME extends DatabaseServlet {\n" +
"    \n" +
"    @Override\n" +
"    protected void doInit() throws Exception {\n" +
"        defineServlet(\"$CLASS_NAME_SPACED\", \"$DATA_SOURCE_NAME\", \"$DATABASE_NAME\", \"$TABLE_NAME\");//, true, false);\n" +
"        defineTransactions(\"insert\", \"select\", \"update\", \"delete\");\n" +
"        $FIELDS\n" +
"        $FOREIGN_KEYS\n" +
"    }\n" +
"}";
    
        StringBuilder database_servlet_class = new StringBuilder(servlet_class);
        this.database_servlet_uri = "/"+table.name;
        replace(database_servlet_class, "$PACKAGE_NAME", table.database.java_package_name);
        replace(database_servlet_class, "$SERVLET_CONTEXT", "/"+table.name);
        replace(database_servlet_class, "$CLASS_NAME", class_name);
        replace(database_servlet_class, "$CLASS_NAME_SPACED", class_name_spaced);
        replace(database_servlet_class, "$DATA_SOURCE_NAME", table.database.name);
        replace(database_servlet_class, "$DATABASE_NAME", table.database.name);
        replace(database_servlet_class, "$TABLE_NAME", table.name);
        for (Field field : table.field_list) {
            String java_datatype = table.data_lookup.lookupJavaDataType(field.data_type_name);
            String typescript_datatype = table.data_lookup.lookupTypescriptDataType(field.data_type_name);
            StringBuilder field_method = new StringBuilder(this.field_method);
            replace(field_method, "$FIELD_ALIAS", field.name);
            replace(field_method, "$FIELD_TYPE", java_datatype);
            replace(field_method, "$FIELD_NULLABLE", field.nullable.toString());
            replace(field_method, "$FIELD_NAME", field.name);
            if (field.primary_key == true) {
                if (field.auto_increment == true) {
                    field_method.append(".setPrimaryKeyAI()");
                } else if (typescript_datatype.equalsIgnoreCase("number")) {
                    field_method.append(".setPrimaryKeyMI()");
                } else {
                    field_method.append(".setPrimaryKey()");
                }
            }
            if (field.data_type_name.equalsIgnoreCase("VARCHAR")) {
                field_method.append(".setTexLengthRange(4, ").append(field.size).append(")");
            }
            field_method.append(";\n        ");
            insertBefore(database_servlet_class, "$FIELDS", field_method.toString());
        }
        for (ForeignKey foreign_key : table.foreign_key_list) {
            String var_name = foreign_key.foreign_key_table_name.replaceAll("(.)([A-Z])", "$1_$2").trim().toLowerCase();
            for (Map.Entry<ForeignKeyField, ReferencedKeyField> entry : foreign_key.foreign_key_referenced_key_map.entrySet()) {
                ForeignKeyField foreignKeyField = entry.getKey();
                ReferencedKeyField referenced_key_field = entry.getValue();
                String foreign_table_name = foreign_key.referenced_key_table_name;
                StringBuilder foreign_key_method = new StringBuilder(this.foreign_key_method);
                replace(foreign_key_method , "$FOREIGN_KEY_NAME", foreign_key.name);
                replace(foreign_key_method , "$TABLE_FIELD_NAME", referenced_key_field.name);
                replace(foreign_key_method , "$REFERENCE_TABLE_NAME", foreign_key.referenced_key_table_name);
                replace(foreign_key_method , "$REFERENCE_TABLE_FIELD_NAME", foreignKeyField.name);
                foreign_key_method.append(";\n        ");
                insertBefore(database_servlet_class, "$FOREIGN_KEYS", foreign_key_method.toString());
            }
        }
        delete(database_servlet_class, "$FIELDS");
        delete(database_servlet_class, "$FOREIGN_KEYS");
        this.database_servlet_class = database_servlet_class.toString();
    }
    
    private void generateHttpRequests(Table table, Gson gson) throws Exception {
        StringBuilder http_requests_builder = new StringBuilder();
        JsonObject insert_request_json = gson.fromJson(insert_request, JsonObject.class);
        JsonArray insert_values = insert_request_json.get("values").getAsJsonArray();
        JsonObject insert_field_list = new JsonObject();
        insert_values.add(insert_field_list);
        
        JsonObject select_request_json = gson.fromJson(select_request, JsonObject.class);
        JsonArray select_select = select_request_json.get("select").getAsJsonArray();
        JsonObject select_where = select_request_json.get("where").getAsJsonObject();
        //JsonObject select_where_clause = where.get("clause").getAsJsonObject();
        JsonArray select_values = select_where.get("values").getAsJsonArray();
        
        JsonObject update_request_json = gson.fromJson(update_request, JsonObject.class);
        JsonArray update_values = update_request_json.get("values").getAsJsonArray();
        JsonObject update_where = update_request_json.get("where").getAsJsonObject();
        //JsonObject update_where_clause = where.get("clause").getAsJsonObject();
        JsonArray update_where_fields = update_where.get("field_list").getAsJsonArray();
        JsonObject update_field_list = new JsonObject();
        update_values.add(update_field_list);
        
        JsonObject delete_request_json = gson.fromJson(delete_request, JsonObject.class);
        JsonArray delete_values = delete_request_json.get("values").getAsJsonArray();
        JsonObject delete_where = delete_request_json.get("where").getAsJsonObject();
        //JsonObject delete_where_clause = delete_where.get("clause").getAsJsonObject();
        JsonArray delete_where_fields = delete_where.get("field_list").getAsJsonArray();
        JsonObject delete_field_list = new JsonObject();
        delete_values.add(delete_field_list);
        
        for (Field field : table.field_list) {
            
            insert_field_list.addProperty(field.name, field.data_type_name);
            
            select_select.add(field.name);
            if (field.primary_key == true) {
                select_where.addProperty("clause", field.name + ">?");
                select_values.add(field.data_type_name);
            }
            
            update_field_list.addProperty(field.name, field.data_type_name);
            if (field.primary_key == true) {
                update_where.addProperty("clause", field.name + "=?");
                update_where_fields.add(field.name);
            }
            
            delete_field_list.addProperty(field.name, field.data_type_name);
            if (field.primary_key == true) {
                delete_where.addProperty("clause", field.name + "=?");
                delete_where_fields.add(field.name);
            }
            
        }
        http_requests_builder.append(gson.toJson(insert_request_json)).append(nl).append(nl);
        http_requests_builder.append(gson.toJson(select_request_json)).append(nl).append(nl);
        http_requests_builder.append(gson.toJson(update_request_json)).append(nl).append(nl);
        http_requests_builder.append(gson.toJson(delete_request_json)).append(nl).append(nl);
        this.http_requests = http_requests_builder.toString();
    }
    
    public String getModelClassName(String model_class_name, Boolean space) {
        Pattern p = Pattern.compile( "(^([a-z])|_([a-zA-Z]))" );
        Matcher m = p.matcher(model_class_name);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, m.group(1).toUpperCase());
        }
        m.appendTail(sb);
        return sb.toString().replaceAll("_", space == true ? " " : "");
    }
    
    public String getModelClassVariableName(String model_class_name) {
        return model_class_name.replaceAll("(.)([A-Z])", "$1_$2").trim().toLowerCase();
    }
    
    public String getJavaDataStructureClass() {
        return java_data_structure_class;
    }
    
    public String getTypescriptDataStructureClass() {
        return typescript_data_structure_class;
    }
    
    private void replace(StringBuilder string, String replace, String with) {
        Integer replace_position = string.indexOf(replace);
        string.replace(replace_position, replace_position+replace.length(), with);
    }
    
    private void insertBefore(StringBuilder string, String find, String insert) {
        Integer find_position = string.indexOf(find);
        string.insert(find_position, insert);
    }
    
    private void delete(StringBuilder string, String find) {
        replace(string, find, "");
    }
    
    //To-Do
    //Foreign List onChange
    //fillForm with selected icons
    //submitForm()
    //resetForm
    //Flag Record for Update New Deleted
    //insertSubscriptionMethod
    //selectSubscriptionMethod
    //updateSubscriptionMethod
    //deleteSubscriptionMethod
    //TinyInt(1) checkbox
    
    private static transient String field_method = "addField(\"$FIELD_ALIAS\", FieldType.$FIELD_TYPE, $FIELD_NULLABLE, false, \"$FIELD_NAME\")";
    private static transient String foreign_key_method = "addForeignKey(\"$FOREIGN_KEY_NAME\", \"$TABLE_FIELD_NAME\", \"$REFERENCE_TABLE_NAME\", \"$REFERENCE_TABLE_FIELD_NAME\")";
    private static transient String insert_request = "{\"transaction\": \"insert\",\"values\": [],\"parameters\":[]}";
    private static transient String select_request = "{\"transaction\":\"select\",\"engine\":\"memory\",\"view\":\"object\",\"select\":[],\"where\":{\"clause\":\"\", \"values\":[]},\"orderby\":[]}";
    private static transient String update_request = "{\"transaction\":\"update\",\"values\":[],\"where\":{\"clause\":\"\", \"field_list\":[]},\"parameters\":[]}";
    private static transient String delete_request = "{\"transaction\":\"delete\",\"values\":[],\"where\":{\"clause\":\"\", \"field_list\":[]}}";
    
    private static transient String request_send_response = "	insert$TABLE_CLASS($TABLE_NAME: $TABLE_CLASS[]) {\n		this.insert_$TABLE_NAME_record_list.push($TABLE_NAME);\n		let servlet_url: string = Constants.tariff_url + \"/$TABLE_NAME\";\n		let http_headers: HttpHeaders = new HttpHeaders();\n		http_headers.set(\"Content-Type\", \"application/json; charset=UTF-8\");\n		let http_parameters: HttpParams = new HttpParams();\n		//http_parameters.set(\"\", \"\");\n		const request: DBInsert = <DBInsert><unknown>{\n			transaction: \"insert\",\n			values: [\n				{\n$INSERT_UPDATE_DELETE_FIELDS\n				}\n			],\n			parameters: []\n		}\n		this.sendPost(this, \"insert_$TABLE_NAME\", servlet_url, request, http_headers, http_parameters);\n	}\n\n" +
    "	select$TABLE_CLASS($TABLE_NAME: $TABLE_CLASS[]) {\n		let servlet_url: string = Constants.tariff_url + \"/$TABLE_NAME\";\n		let http_headers: HttpHeaders = new HttpHeaders();\n		http_headers.set(\"Content-Type\", \"application/json; charset=UTF-8\");\n		let http_parameters: HttpParams = new HttpParams();\n		//http_parameters.set(\"\", \"\");\n		const request: DBSelect = {\n			transaction: \"select\",\n			engine: \"memory\",\n			view: \"object\",\n			select: [\n$SELECT_FIELDS\n			],\n			where: {\n				clause: \"\",\n				values: []\n			},\n			orderby: []\n		}\n		this.sendPost(this, \"select_$TABLE_NAME\", servlet_url, request, http_headers, http_parameters);\n	}\n\n" +
    "	update$TABLE_CLASS($TABLE_NAME: $TABLE_CLASS[]) {\n		let servlet_url: string = Constants.tariff_url + \"/$TABLE_NAME\";\n		let http_headers: HttpHeaders = new HttpHeaders();\n		http_headers.set(\"Content-Type\", \"application/json; charset=UTF-8\");\n		let http_parameters: HttpParams = new HttpParams();\n		//http_parameters.set(\"\", \"\");\n		const request: DBUpdate = <DBUpdate><unknown>{\n			transaction: \"update\",\n			returns: 'id',\n			//variable?: {};\n			values: [\n				{\n$INSERT_UPDATE_DELETE_FIELDS\n				}\n			],\n			where: {\n				clause: \"\",\n				field_list: [\n					\n				]\n			},\n			parameters: {}\n		}\n		this.sendPost(this, \"update_$TABLE_NAME\", servlet_url, request, http_headers, http_parameters);\n	}\n\n" +
    "	delete$TABLE_CLASS($TABLE_NAME: $TABLE_CLASS[]) {\n		let servlet_url: string = Constants.tariff_url + \"/$TABLE_NAME\";\n		let http_headers: HttpHeaders = new HttpHeaders();\n		http_headers.set(\"Content-Type\", \"application/json; charset=UTF-8\");\n		let http_parameters: HttpParams = new HttpParams();\n		//http_parameters.set(\"\", \"\");\n		const request: DBUpdate = <DBUpdate><unknown>{\n			transaction: \"delete\",\n			values: [\n				{\n$INSERT_UPDATE_DELETE_FIELDS\n				}\n			],\n			where: {\n			  clause: \"\",\n			  field_list: [\n				\n			  ]\n			}\n		}\n		this.sendPost(this, \"delete_$TABLE_NAME\", servlet_url, request, http_headers, http_parameters);\n	}\n\n" +
    "	$TABLE_NAME_list: $TABLE_CLASS[] = [];\n	selected_$TABLE_NAME_option: $TABLE_CLASS;\n	selected_$TABLE_NAME_record: $TABLE_CLASS;\n\n	insert_$TABLE_NAME_record_list: $TABLE_CLASS[] = [];\n	update_$TABLE_NAME_record_list: $TABLE_CLASS[] = [];\n	delete_$TABLE_NAME_record_list: $TABLE_CLASS[] = [];\n\n	//selected_$TABLE_NAME_list_icon: any = blank_icon;\n	\n	} else if (key === \"insert_$TABLE_NAME\") {\n		//let resultset: any[] = response.resultset;\n		//let resultset: any = response.resultset;\n		//let generated_id: any = response.generated_id;\n		this.resetForm();\n		for (let i: number = 0; i < this.insert_$TABLE_NAME_record_list.length; i++) {\n			this.$TABLE_NAME_list.push(this.insert_$TABLE_NAME_record_list[i]);\n		}\n		this.insert_$TABLE_NAME_record_list = [];\n		this.$TABLE_NAME_table_data_source.data = this.$TABLE_NAME_list;\n		//this.snackbar.open(this.i18.insert_success, \"x\", {duration: 5 * 1000});\n	} else if (key === \"select_$TABLE_NAME\") {\n		let resultset: any[] = response.resultset;\n		this.$TABLE_NAME_list = [];\n		for (let i: number = 0; resultset != null && i < resultset.length; i++) {\n			this.$TABLE_NAME_list.push($TABLE_CLASS.fromJSON(resultset[i]));\n			//Manipulate.loadImage(this.$TABLE_NAME_list[i].$TABLE_NAME_icon);\n		}\n\n		this.$TABLE_NAME_table_data_source.data = this.$TABLE_NAME_list;\n		//this.snackbar.open(this.i18.select_success, \"x\", {duration: 5 * 1000});\n	} else if (key === \"update_$TABLE_NAME\") {\n		//this.snackbar.open(this.i18.update_success, \"x\", {duration: 5 * 1000});\n	} else if (key === \"delete_$TABLE_NAME\") {\n		//this.snackbar.open(this.i18.delete_success, \"x\", {duration: 5 * 1000});\n	}\n\n";
    
    private static transient String typescript_form_field_control_class_members = "$FIELD_NAME_control: FormControl;"; //$TYPESCRIPT_FORM_FIELD_CONTROL_CLASS_MEMBERS
    private static transient String typescript_form_foreign_field_control_class_members = "$FOREIGN_FIELD_NAME_list_control: FormControl;"; //$TYPESCRIPT_FORM_FOREIGN_FIELD_CONTROL_CLASS_MEMBERS
    private static transient String typescript_form_foreign_field_array_class_members = "$FOREIGN_FIELD_NAME_array_control: FormArray;"; //$TYPESCRIPT_FORM_FOREIGN_FIELD_ARRAY_CLASS_MEMBERS
    private static transient String typescript_form_field_control_definition = "this.$FIELD_NAME_control = new FormControl([]);"; //$TYPESCRIPT_FORM_FIELD_CONTROL_DEFINITION
    private static transient String typescript_form_foreign_field_control_definition = "this.$FOREIGN_FIELD_NAME_list_control = new FormControl([]);"; //$TYPESCRIPT_FORM_FOREIGN_FIELD_CONTROL_DEFINITION
    private static transient String typescript_form_foreign_field_array_definition = "this.$FOREIGN_FIELD_NAME_array_control = new FormArray([]);"; //$TYPESCRIPT_FORM_FOREIGN_FIELD_ARRAY_DEFINITION
    private static transient String typescript_form_field_control = "$FIELD_NAME: this.$FIELD_NAME_control,"; //$TYPESCRIPT_FORM_FIELD_CONTROL
    private static transient String typescript_form_foreign_field_control = "$FOREIGN_FIELD_NAME_check_list: this.$FOREIGN_FIELD_NAME_array_control,"; //$TYPESCRIPT_FORM_FOREIGN_FIELD_CONTROL
    private static transient String form_component_ts = 
"	$TABLE_NAME_form: FormGroup;\n" +
"$TYPESCRIPT_FORM_FIELD_CONTROL_CLASS_MEMBERS\n\n" +
"$TYPESCRIPT_FORM_FOREIGN_FIELD_CONTROL_CLASS_MEMBERS\n\n" +
"$TYPESCRIPT_FORM_FOREIGN_FIELD_ARRAY_CLASS_MEMBERS\n\n" +
"	constructor() {\n\n" +
"$TYPESCRIPT_FORM_FIELD_CONTROL_DEFINITION\n" +
"$TYPESCRIPT_FORM_FOREIGN_FIELD_CONTROL_DEFINITION\n" +
"$TYPESCRIPT_FORM_FOREIGN_FIELD_CONTROL_DEFINITION\n\n" +
"$TYPESCRIPT_FORM_FOREIGN_FIELD_ARRAY_DEFINITION\n\n" +
"\n" +
"	this.$TABLE_NAME_form = form_builder.group({\n" +
"		$TABLE_NAME_group: form_builder.group({\n" +
"$TYPESCRIPT_FORM_FIELD_CONTROL\n" +
"$TYPESCRIPT_FORM_FOREIGN_FIELD_CONTROL\n" +
"		}),\n" +
"	},\n" +
"	//{ validators: passwordMatchValidator, asyncValidators: otherValidator }\n" +
"	);\n" +
"	}\n\n" +
"	setSelected$TABLE_CLASSRecord(selected_$TABLE_NAME_record: $TABLE_CLASS) {\n" +
"		this.selected_$TABLE_NAME_record = selected_$TABLE_NAME_record;\n" +
"	}\n" +
"	\n" +
"	ngOnChanges(changes: SimpleChanges): void {\n" +
"	\n" +
"$TYPESCRIPT_FORM_FOREIGN_FIELD_CHECK_BOX\n" +
"	\n" +
"	}";
    
    private static transient String typescript_form_foreign_field_check_box = 
"		this.$FOREIGN_TABLE_NAME_list.forEach( ($FOREIGN_TABLE_NAME: $FOREIGN_TABLE_CLASS) => {\n" +
"			$FOREIGN_TABLE_NAME.is_checked = false;\n" +
"			let $FOREIGN_TABLE_NAME_control: FormControl = new FormControl($FOREIGN_TABLE_NAME);\n" +
"			$FOREIGN_TABLE_NAME_control.['option_value'] = $FOREIGN_TABLE_NAME;\n" +
"			$FOREIGN_TABLE_NAME_control.setValue($FOREIGN_TABLE_NAME_control['option_value']);\n" +
"			this.$FOREIGN_TABLE_NAME_array_control.push($FOREIGN_TABLE_NAME_control);\n" +
"		});";//$TYPESCRIPT_FORM_FOREIGN_FIELD_CHECK_BOX
    
    private static transient String form_component_html = 
"<div class=\"container\">\n" +
"    <p class=\"card\">{{i18.$TABLE_NAME.title')}}</p>\n" +
"<ng-container *ngIf=\"show_confirmation==false\">\n"+
"<form class=\"form yes-mouse\" [formGroup]=\"$TABLE_NAME_form\" [dir]=\"direction\">\n" +
"	<div role=\"group\" formGroupName=\"$TABLE_NAME_group\" [hidden]=\"false\" [dir]=\"direction\">\n" +
"		<div class=\"form-group\" [dir]=\"direction\">\n" +
"		\n" +
"$TYPESCRIPT_FORM_FIELD_HTML_TEXT_BOX_CONTROL\n" +
"			\n" +
"$TYPESCRIPT_FORM_FIELD_HTML_DATE_BOX_CONTROL\n" +
"		\n" +
"$TYPESCRIPT_FORM_FOREIGN_FIELD_HTML_CHECK_BOX_CONTROL\n" +
"			\n" +
"$TYPESCRIPT_FORM_FOREIGN_FIELD_HTML_LIST_BOX_CONTROL\n" +
"			\n" +
"        </div>\n" +
"	</div>\n" +
"</form>\n" +
"<div class=\"tool-box\" [dir]=\"direction\">{{'Controls - ' + i18.$TABLE_CLASS.title}}</div>\n" +
"<div class=\"form-group yes-mouse\" [dir]=\"direction\">\n" +
"<div class=\"tool-box yes-mouse\" [dir]=\"direction\">\n" +
"	<!--{{'Controls - ' + i18.$TABLE_CLASS.title}}-->\n" +
"	<img *ngIf=\"selected_$TABLE_NAME_record.record_state == null\" src=\"{{ICON.blank_icon}}\" (error)=\"ICON.blank_icon\" class=\"icon\" />\n" +
"	<img *ngIf=\"selected_$TABLE_NAME_record.record_state == INSERT\" src=\"{{ICON.insert_record_icon}}\" (error)=\"ICON.blank_icon\" class=\"icon\" />\n" +
"	<img *ngIf=\"selected_$TABLE_NAME_record.record_state == SELECT\" src=\"{{ICON.true_icon}}\" (error)=\"ICON.blank_icon\" class=\"icon\" />\n" +
"	<img *ngIf=\"selected_$TABLE_NAME_record.record_state == UPDATE\" src=\"{{ICON.update_record_icon}}\" (error)=\"ICON.blank_icon\" class=\"icon\" />\n" +
"	<img *ngIf=\"selected_$TABLE_NAME_record.record_state == DELETE\" src=\"{{ICON.delete_record_icon}}\" (error)=\"ICON.blank_icon\" class=\"icon\" />\n" +
"	<button class=\"tool-box-button-pushable yes-mouse\" matTooltip=\"{{i18.first_record}}\" [disabled]=\"cursor == 0\" (click)=\"firstRecord()\">\n" +
"		<span class=\"tool-box-button-front\" [dir]=\"direction\">\n" +
"			{{ i18.first }}\n" +
"		</span>\n" +
"	</button>\n" +
"	<button class=\"tool-box-button-pushable yes-mouse\" matTooltip=\"{{i18.previous_record}}\" [disabled]=\"cursor == 0\" (click)=\"previousRecord()\">\n" +
"		<span class=\"tool-box-button-front\" [dir]=\"direction\">\n" +
"			{{ i18.previous }}\n" +
"		</span>\n" +
"	</button>\n" +
"	<button class=\"tool-box-button-pushable yes-mouse\" matTooltip=\"{{i18.next_record}}\" [disabled]=\"cursor == this.$TABLE_NAME_list.length-1\" (click)=\"nextRecord()\">\n" +
"		<span class=\"tool-box-button-front\" [dir]=\"direction\">\n" +
"			{{ i18.next }}\n" +
"		</span>\n" +
"	</button>\n" +
"	<button class=\"tool-box-button-pushable yes-mouse\" matTooltip=\"{{i18.last_record}}\" [disabled]=\"cursor == this.$TABLE_NAME_list.length-1\" (click)=\"lastRecord()\">\n" +
"		<span class=\"tool-box-button-front\" [dir]=\"direction\">\n" +
"			{{ i18.last }}\n" +
"		</span>\n" +
"	</button>\n" +
"</div>\n" +
"<div class=\"form-group yes-mouse\" [dir]=\"direction\">\n" +
"	<button class=\"pushable-blue\" (click)=\"submitForm()\">\n" +
"		<span class=\"front-blue\" [dir]=\"direction\">\n" +
"			{{ i18.submit }}\n" +
"		</span>\n" +
"	</button>\n" +
"	<button class=\"pushable-red\" (click)=\"resetForm()\">\n" +
"		<span class=\"front-red\" [dir]=\"direction\">\n" +
"			{{ i18.reset }}\n" +
"		</span>\n" +
"	</button>\n" +
"</div>" +
"</ng-container>\n" +
"    <confirmation-component *ngIf=\"show_confirmation==true\" (onConfirmationAcknowledged)=\"confirmationAcknowledged($event)\"></confirmation-component>\n" +
"</div>\n";
    
    private static transient String typescript_form_field_html_text_box_control =
"			<!-- Text Box $FIELD_NAME -->\n" +
"			<mat-form-field [appearance]=\"outline\" [dir]=\"direction\">\n" +
"				<input formControlName=\"$FIELD_NAME\" matInput type=\"$FIELD_HTML_TYPE\"\n" +
"					placeholder=\"{{i18.$TABLE_NAME.$FIELD_NAME}}\"\n" +
"					required=\"$FIELD_IS_NOT_NULL\" $PRIMARY_KEY_TEXT_BOX \n" +
"					[ngClass]=\"{ 'is-invalid': isValidFieldValue('$TABLE_NAME_group.$FIELD_NAME') === false }\" />\n" +
"				<div *ngIf=\"isModified('$TABLE_NAME_group.$FIELD_NAME') && isValid('$TABLE_NAME_group.$FIELD_NAME') === false\" class=\"alert alert-danger\">\n" +
"					<ng-container *ngFor=\"let field_error of getFieldErrors('$TABLE_NAME_group.$FIELD_NAME')\">\n" +
"						<div class=\"warning\">{{field_error.error_message[lang]}}></div>\n" +
"					<ng-container>\n" +
"				</div>\n" +
"			</mat-form-field>\n"; //$TYPESCRIPT_FORM_FIELD_HTML_TEXT_BOX_CONTROL

    private static transient String typescript_form_field_html_date_box_control = 
"			<!-- Date Box $FIELD_NAME -->\n" +
"			<mat-form-field [appearance]=\"outline\" [dir]=\"direction\">\n" +
"				<mat-label>{{i18.$TABLE_NAME.$FIELD_NAME}}</mat-label>\n" +
"				<input formControlName=\"$FIELD_NAME\" matInput [matDatepicker]=\"datepicker\" placeholder=\"{{ i18.$TABLE_NAME.$FIELD_NAME }}\">\n" +
"				<mat-datepicker-toggle matPrefix [for]=\"datepicker\"></mat-datepicker-toggle>\n" +
"				<mat-datepicker #datepicker></mat-datepicker>\n" +
"			</mat-form-field>\n"; //$TYPESCRIPT_FORM_FIELD_HTML_DATE_BOX_CONTROL


    private static transient String typescript_form_foreign_field_html_check_box_control = 
"			<!-- $FOREIGN_FIELD_NAME_list -->\n" +
"			<ng-container formArrayName=\"$FOREIGN_FIELD_NAME_list\">\n" +
"			  <div>\n" +
"				<span>\n" +
"				  <mat-checkbox [checked]=\"isAllListChecked($FOREIGN_FIELD_NAME_check_list)\" [color]=\"primary\"\n" +
"					[indeterminate]=\"isIndeterminate($FOREIGN_FIELD_NAME_list)\"\n" +
"					(change)=\"selectAllList($event, $FOREIGN_FIELD_NAME_list)\">\n" +
"					<img src=\"{{$FOREIGN_FIELD_NAME_icon}}\" class=\"icon\" />\n" +
"					{{ i18.$FOREIGN_FIELD_NAME }}\n" +
"				  </mat-checkbox>\n" +
"				</span>\n" +
"			  </div>\n" +
"			  <div *ngFor=\"let $FOREIGN_FIELD_NAME_control of $FOREIGN_FIELD_NAME_array_control.controls; let i=index\">\n" +
"				<span>\n" +
"				  <mat-checkbox class=\"margin\" [formControl]=\"$FOREIGN_FIELD_NAME_control\"\n" +
"					[color]=\"primary\" [(ngModel)]=\"$FOREIGN_FIELD_NAME_list[i].is_checked\">\n" +
"					<img class=\"icon\" src=\"{{$FOREIGN_FIELD_NAME_list[i].advertising_method_icon}}\" (error)=\"onImageError($event)\">\n" +
"					{{$FOREIGN_FIELD_NAME_list[i].advertising_method_name_i18[lang]}}\n" +
"				  </mat-checkbox>\n" +
"				</span>\n" +
"			  </div>\n" +
"			</ng-container>\n"; //$TYPESCRIPT_FORM_FOREIGN_FIELD_HTML_CHECK_BOX_CONTROL

    private static transient String typescript_form_foreign_field_html_list_box_control = 
"			<!-- $FOREIGN_FIELD_NAME List Box -->\n" +
"			<mat-form-field [appearance]=\"outline\" [dir]=\"direction\">\n" +
"				<mat-label>{{ i18.$FOREIGN_FIELD_NAME }}</mat-label>\n" +
"					<span matPrefix [style.display]=\"'flex'\">\n" +
"					<img src=\"{{selected_$FOREIGN_TABLE_NAME_list_icon}}\" class=\"icon\" />\n" +
"				</span>\n" +
"				<mat-select formControlName=\"$FOREIGN_FIELD_NAME_list_option\" (selectionChange)=\"onChange$FOREIGN_TABLE_CLASS($event)\">\n" +
"					<mat-option *ngFor=\"let option of $FOREIGN_TABLE_NAME_list\" [value]=\"option\">\n" +
"					<span><img class=\"icon\" src=\"{{option.$FOREIGN_TABLE_NAME_icon}}\"\n" +
"					(error)=\"onImageError($event)\">{{option.$FOREIGN_TABLE_NAME_name_i18[lang]}}</span>\n" +
"					</mat-option>\n" +
"				</mat-select>\n" +
"			</mat-form-field>\n"; //$TYPESCRIPT_FORM_FOREIGN_FIELD_HTML_LIST_BOX_CONTROL

    private static transient String table_component_ts = 
"	$TABLE_NAME_table_data_source: MatTableDataSource<$TABLE_CLASS>;\n" +
"	$TABLE_NAME_table_columns: string[] = [\n" +
"$FIELD_LIST\n" +
"$FOREIGN_FIELD_LIST\n" +
"		\"select_record\",\n" +
"		\"delete_record\""+
"	];\n" +
"	\n" +
"	\n" +
"	constructor() {\n" +
"		this.$TABLE_NAME_list = [];\n" +
"		this.$TABLE_NAME_table_data_source = new MatTableDataSource(this.$TABLE_NAME_list);\n" +
"		this.$TABLE_NAME_table_data_source.data = this.$TABLE_NAME_list;\n" +
"	}\n" +
"	\n" +
"	\n";
    
    private static transient String table_component_html = 
"<mat-table class=\"just-table mat-elevation-z8\" [dataSource]=\"$TABLE_NAME_table_data_source\" [dir]=\"direction\">\n" +
"\n" +
"$TYPESCRIPT_TABLE_FIELD_HTML_PRIMARY_KEY_ICON_CONTROL\n"+
"	\n" +
"$TYPESCRIPT_TABLE_FIELD_HTML_I18_CONTROL\n"+
"\n" +
"$TYPESCRIPT_TABLE_FOREIGN_FIELD_HTML_I18_CONTROL\n" +
"	\n" +
"	<!-- extra controls -->\n" +
"	<ng-container matColumnDef=\"select_record\">\n" +
"		<mat-header-cell *matHeaderCellDef class=\"table-nice-back-blue\">{{i18.select_record}}</mat-header-cell>\n" +
"			<mat-cell *matCellDef=\"let $TABLE_NAME_record;\">\n" +
"				<button mat-raised-button color=\"nice-green\" class=\"yes-mouse\" (click)=\"selectRecord($TABLE_NAME_record)\">\n" +
"					<mat-icon>content_copy</mat-icon>\n" +
"				</button>\n" +
"		</mat-cell>\n" +
"	</ng-container>\n" +
"	<ng-container matColumnDef=\"delete_record\">\n" +
"		<mat-header-cell *matHeaderCellDef class=\"table-nice-back-blue\">{{i18.delete_record}}</mat-header-cell>\n" +
"			<mat-cell *matCellDef=\"let $TABLE_NAME_record;\">\n" +
"				<button mat-raised-button color=\"nice-green\" class=\"yes-mouse\" (click)=\"deleteRecord($TABLE_NAME_record)\">\n" +
"					<mat-icon>delete</mat-icon>\n" +
"				</button>\n" +
"		</mat-cell>\n" +
"	</ng-container>\n" +
"\n" +
"	<mat-header-row *matHeaderRowDef=\"$TABLE_NAME_table_columns\">\n" +
"	</mat-header-row>\n" +
"	<mat-row *matRowDef=\"let $TABLE_NAME_record; columns: $TABLE_NAME_table_columns\"></mat-row>\n" +
"</mat-table>";
    
    private static transient String typescript_table_field_html_primary_key_icon_control = 
"	<!-- Table Field $FIELD_NAME with _name_LANG -->\n" +
"	<ng-container matColumnDef=\"$FIELD_NAME\">\n" +
"		<mat-header-cell *matHeaderCellDef class=\"table-nice-back-blue\">{{i18.$TABLE_CLASS.$FIELD_NAME}}</mat-header-cell>\n" +
"		<mat-cell *matCellDef=\"let $TABLE_NAME_record;\">\n" +
"			<img src=\"{{$TABLE_NAME_record.$TABLE_NAME_icon}}\" class=\"icon\" />\n" +
"			{{$TABLE_NAME_record.$TABLE_NAME_name_i18[lang]}}\n" +
"		</mat-cell>\n" +
"	</ng-container>\n";//$TYPESCRIPT_TABLE_FIELD_HTML_PRIMARY_KEY_ICON_CONTROL
    
    private static transient String typescript_table_field_html_i18_control = 
"	<!-- Table -Field $FIELD_NAME -->\n" +
"	<ng-container matColumnDef=\"$TABLE_NAME_name_i18\">\n" +
"		<mat-header-cell *matHeaderCellDef class=\"table-nice-back-blue\">{{i18.$TABLE_CLASS.$TABLE_NAME_name_i18}}</mat-header-cell>\n" +
"		<mat-cell *matCellDef=\"let $TABLE_NAME_record;\">\n" +
"			{{$TABLE_NAME_record.$TABLE_NAME_name_i18[lang]}}\n" +
"		</mat-cell>\n" +
"	</ng-container>\n";//$TYPESCRIPT_TABLE_FIELD_HTML_I18_CONTROL
    
    private static transient String typescript_table_foreign_field_html_i18_control = 
"	<!-- Table ~Field $FOREIGN_FIELD_NAME with _name_LANG -->\n" +
"	<ng-container matColumnDef=\"$FOREIGN_FIELD_NAME\">\n" +
"		<mat-header-cell *matHeaderCellDef class=\"table-nice-back-blue\">{{i18.$FOREIGN_TABLE_CLASS.$FOREIGN_FIELD_NAME}}</mat-header-cell>\n" +
"		<mat-cell *matCellDef=\"let $TABLE_NAME_record;\">\n" +
"			<img src=\"{{$TABLE_NAME_record.$FOREIGN_TABLE_NAME.$FOREIGN_TABLE_NAME_icon}}\" class=\"icon\" />\n" +
"			{{$TABLE_NAME_record.$FOREIGN_TABLE_NAME.$FOREIGN_TABLE_NAME_name_i18[lang]}}\n" +
"		</mat-cell>\n" +
"	</ng-container>\n";//$TYPESCRIPT_TABLE_FOREIGN_FIELD_HTML_I18_CONTROL
}
