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

import static net.reyadeyat.api.relational.data.DataProcessor.zonedDateTimeAdapter;
import net.reyadeyat.api.relational.model.ForeignKeyField;
import net.reyadeyat.api.relational.model.ReferencedKeyField;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.TreeMap;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.reyadeyat.api.library.json.JsonZonedDateTimeAdapter;
import net.reyadeyat.api.library.sequence.SequenceNumber;

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
public class DataInstance {
    public enum State{NEW,LOADED,UPDATED,DELETED};
    private State state;
    DataClass data_class;
    DataInstance parent_data_instance;
    SequenceNumber sequence_number;
    Boolean is_null;
    Integer parent_id;
    Object instance_object;
    HashMap<Object, Integer> instances_id_map;
    ArrayList<Object> instances;
    String runtime_path;
    
    String database_name;
    
    //used to get field array of an instance
    Map<Object, ArrayList<DataInstance>> member_list_map;//members inside this instace
    Map<Object, ArrayList<DataInstance>> table_list_map;//table members inside this instace
    Map<Object, ArrayList<DataInstance>> field_list_map;//field members inside this instace // Inserted in parent table
    
    //used to get field by its name from inside an instance
    Map<Object, HashMap<String, DataInstance>> member_instance_list_map;//members inside this instace
    Map<Object, HashMap<String, DataInstance>> table_instance_list_map;//table members inside this instace
    Map<Object, HashMap<String, DataInstance>> field_instance_list_map;//field members inside this instace // Inserted in parent table
    
    TreeMap<String, HashMap<String, Integer>> code_list_map;
    
    static final public Gson gson = new GsonBuilder()/*.setPrettyPrinting()*/.registerTypeAdapter(ZonedDateTime.class, new JsonZonedDateTimeAdapter()).create();
    static final public ArrayList<String> ignore_field_list = new ArrayList<String>(
            Arrays.asList(new String[]{
                "java_package_name",
                "java_data_structure_class",
                "typescript_data_structure_class",
                "typescript_request_send_response",
                "typescript_form_component_ts",
                "typescript_form_component_ts",
                "typescript_form_component_html",
                "typescript_table_component_ts",
                "typescript_table_component_html",
                "database_servlet_class",
                "database_servlet_uri",
                "http_requests"
            }));
    
    @SuppressWarnings("unchecked")
    public DataInstance(State state, String database_name, DataClass data_class, DataInstance parent_data_instance, Object parentInstanceObject, Object instance_object, SequenceNumber sequence_number, Boolean traverse, Boolean foreing_key_must_link_to_primary_key) throws Exception {
        this.state = state;
        this.database_name = database_name;
        this.data_class = data_class;
        if (this.data_class == null) {
            throw new Exception("DataClass is null !!!");
        }
        if (data_class.declared_name.equalsIgnoreCase("childTables")) {
            data_class.declared_name = data_class.declared_name;
        }
        this.parent_data_instance = parent_data_instance;
        this.sequence_number = sequence_number;
        this.parent_id = /*sequence_number == null ? 0 : */parent_data_instance == null ? 0 : parent_data_instance.instances_id_map.get(parentInstanceObject);
        //this.nextChildTableID = travers == false ? 0 : parent_data_instance == null ? 0 : parent_data_instance.nextChildTableID;
        //this.parentInstanceObject = parentInstanceObject;
        this.instance_object = instance_object;
        if (this.instance_object == null && this.data_class.isNotNull() == true) {
            if (this.data_class.declared_name.equalsIgnoreCase("referenced_key_name") == true) {
                if (data_class.field.getName().equalsIgnoreCase("referenced_key_name")) {
                    if (parentInstanceObject instanceof net.reyadeyat.api.relational.model.ForeignKey foreign_key) {
                        StringBuilder foreign_key_field_list_strb = new StringBuilder();
                        Boolean is_primary_key = true;
                        for (ForeignKeyField foreign_key_field : foreign_key.foreign_key_field_list) {
                            foreign_key_field_list_strb.append("`").append(foreign_key_field.name).append("`,");
                        }
                        if (foreign_key.foreign_key_field_list.size() > 0) {
                            foreign_key_field_list_strb.delete(foreign_key_field_list_strb.length()-1, foreign_key_field_list_strb.length());
                        }
                        StringBuilder referenced_key_field_list_strb = new StringBuilder();
                        for (ReferencedKeyField referenced_key_field : foreign_key.referenced_key_field_list) {
                            referenced_key_field_list_strb.append("`").append(referenced_key_field.name).append("`,");
                            is_primary_key = is_primary_key && referenced_key_field.is_primary_key_field;
                        }
                        if (foreign_key.referenced_key_field_list.size() > 0) {
                            referenced_key_field_list_strb.delete(referenced_key_field_list_strb.length()-1, referenced_key_field_list_strb.length());
                        }
                        if (is_primary_key == false && foreing_key_must_link_to_primary_key == true) {
                            throw new Exception("Database Foreign Key Constraint sufferes integgrity check failure Table.[Field List] `"+foreign_key.foreign_key_table_name+"`.["+foreign_key_field_list_strb.toString()+"] has Foreing Key '"+foreign_key.name+"' linked to a none Primary Key Table.[Field List] `"+foreign_key.referenced_key_table_name+"`.["+referenced_key_field_list_strb.toString()+"], please fix this issue first !!!");
                        }
                    }
                }
            }
            //throw new Exception("Instance Object is null in '" + this.data_class.name + "'");
        }
        if (parent_data_instance == null && this.data_class.clas.isInstance(this.instance_object) == false) {
            throw new ClassCastException("DataInstance '" + this.data_class.clas.getCanonicalName() + "' required class but got instance object of class '" + this.instance_object.getClass().getCanonicalName() + "'");
        }
        this.instances_id_map = new HashMap<Object, Integer>();
        
        this.is_null = instance_object == null;
        
        this.member_list_map = new HashMap<>();
        this.table_list_map = new HashMap<>();
        this.field_list_map = new HashMap<>();

        this.member_instance_list_map = new HashMap<>();
        this.table_instance_list_map = new HashMap<>();
        this.field_instance_list_map = new HashMap<>();
        
        if (traverse == false) {
            this.instances = new ArrayList<>();
            return;
        }
        
        if (isList(data_class.field)) {
            this.instances = (ArrayList<Object>) this.instance_object;
        } else if (isArray(data_class.field)) {
            this.instances = new ArrayList<Object>(Arrays.asList((Object[]) this.instance_object));
        } else {
            this.instances = new ArrayList<Object>(Arrays.asList(new Object[]{this.instance_object}));
        }
        
        //If class is not in the package use it field name
        //Dig only inside own package
        if (this.data_class.clas.getPackage().getName().startsWith(this.data_class.package_name) == false
            && this.data_class.metadata_annotation.table() == false
            && this.data_class.has_interface_implementation == false) {
            return;
        }
        
        this.sequence_number.createSequence(this.data_class.clas);
        for (Object instance : this.instances) {
            this.instances_id_map.put(instance, this.sequence_number.nextSequence(this.data_class.clas));
            ArrayList<DataInstance> membersList = new ArrayList<>();
            ArrayList<DataInstance> tablesList = new ArrayList<>();
            ArrayList<DataInstance> fieldsList = new ArrayList<>();

            HashMap<String, DataInstance> membersMap = new HashMap<>();
            HashMap<String, DataInstance> tablesMap = new HashMap<>();
            HashMap<String, DataInstance> fieldsMap = new HashMap<>();
            
            for (DataClass newDataClass : this.data_class.member_list) {
                Object newInstanceObject = newDataClass.field.get(instance);
                DataInstance newDataInstance = new DataInstance(this.state, this.database_name, newDataClass, this, instance, newInstanceObject, this.sequence_number, true, foreing_key_must_link_to_primary_key);
                membersList.add(newDataInstance);
                membersMap.put(newDataInstance.data_class.name, newDataInstance);
                if (newDataInstance.data_class.isTable) {//Table
                    tablesList.add(newDataInstance);
                    tablesMap.put(newDataInstance.data_class.name, newDataInstance);
                } else {//Field
                    fieldsList.add(newDataInstance);
                    fieldsMap.put(newDataInstance.data_class.name, newDataInstance);
                }
            }
            this.addMemeber(instance, membersList, tablesList, fieldsList, membersMap, tablesMap, fieldsMap);
        }
    }
    
    public void changeState(State state, Boolean propagateToChildren) throws Exception {
        changeState(this, state, propagateToChildren);
    }
    
    private void changeState(DataInstance data_instance, State state, Boolean propagateToChildren) throws Exception {
        this.state = state;
        if (data_instance.is_null == true) {
            return;
        }
        for (Object object : data_instance.instances) {
            if (data_instance.member_list_map.size() > 0) {
                for (DataInstance memberDataInstance : data_instance.member_list_map.get(object)) {
                    changeState(memberDataInstance, state, propagateToChildren);
                }
            }
        }
    }
    
    public Object getInstanceObject() {
        return this.instance_object;
    }
    
    public void addInstanceObject(Object instance, Integer child_id) throws Exception {
        this.instances.add(instance);
        this.instances_id_map.put(instance, child_id);
        ArrayList<DataInstance> membersList = new ArrayList<>();
        ArrayList<DataInstance> tablesList = new ArrayList<>();
        ArrayList<DataInstance> fieldsList = new ArrayList<>();

        HashMap<String, DataInstance> membersMap = new HashMap<>();
        HashMap<String, DataInstance> tablesMap = new HashMap<>();
        HashMap<String, DataInstance> fieldsMap = new HashMap<>();
        /*for (DataClass newDataClass : this.data_class.membersList) {
            Object newInstanceObject = newDataClass.field.get(instance);
            DataInstance newDataInstance = new DataInstance(newDataClass, this, instance, newInstanceObject/*, sequence_number*//*, false);
            membersList.add(newDataInstance);
            membersMap.put(newDataInstance.data_class.name, newDataInstance);
            if (newDataInstance.data_class.isTable) {//Table
                tablesList.add(newDataInstance);
                tablesMap.put(newDataInstance.data_class.name, newDataInstance);
            } else {//Field
                fieldsList.add(newDataInstance);
                fieldsMap.put(newDataInstance.data_class.name, newDataInstance);
            }
        }*/
        this.addMemeber(instance, membersList, tablesList, fieldsList, membersMap, tablesMap, fieldsMap);
    }
    
    public void addChildInstanceObject(State childState, DataClass newDataClass, Object parentInstanceObject, Object newInstanceObject, SequenceNumber sequence_number, Boolean traverse, Boolean foreing_key_must_link_to_primary_key) throws Exception {
        ArrayList<DataInstance> membersList = this.member_list_map.get(parentInstanceObject);
        ArrayList<DataInstance> tablesList = this.table_list_map.get(parentInstanceObject);
        ArrayList<DataInstance> fieldsList = this.field_list_map.get(parentInstanceObject);
        
        HashMap<String, DataInstance> membersMap = this.member_instance_list_map.get(parentInstanceObject);
        HashMap<String, DataInstance> tablesMap = this.table_instance_list_map.get(parentInstanceObject);
        HashMap<String, DataInstance> fieldsMap = this.field_instance_list_map.get(parentInstanceObject);
        
        DataInstance newDataInstance = new DataInstance(childState, this.database_name, newDataClass, this, parentInstanceObject, newInstanceObject, sequence_number, traverse, foreing_key_must_link_to_primary_key);
        membersList.add(newDataInstance);
        membersMap.put(newDataInstance.data_class.name, newDataInstance);
        if (newDataInstance.data_class.isTable) {//Table
            tablesList.add(newDataInstance);
            tablesMap.put(newDataInstance.data_class.name, newDataInstance);
        } else {//Field
            fieldsList.add(newDataInstance);
            fieldsMap.put(newDataInstance.data_class.name, newDataInstance);
        }
    }
    
    public void addMemeber(Object instance, 
            ArrayList<DataInstance> membersList,
            ArrayList<DataInstance> tablesList,
            ArrayList<DataInstance> fieldsList,
            HashMap<String, DataInstance> membersMap,
            HashMap<String, DataInstance> tablesMap,
            HashMap<String, DataInstance> fieldsMap) {
        this.member_list_map.put(instance, membersList);
        this.table_list_map.put(instance, tablesList);
        this.field_list_map.put(instance, fieldsList);
        
        this.member_instance_list_map.put(instance, membersMap);
        this.table_instance_list_map.put(instance, tablesMap);
        this.field_instance_list_map.put(instance, fieldsMap);
    }
    
    public void setCodeMap(TreeMap<String, HashMap<String, Integer>> code_list_map) {
        this.code_list_map = code_list_map;
    }
    
    public Integer getCodeID(String categoryName, String code) {
        return code_list_map.get(categoryName).get(code);
    }
    
    public Boolean hasParent() {
        return parent_data_instance != null;
    }
    
    public String getParentName() {
        return parent_data_instance.data_class.name;
    }
    
    private Boolean isList(Field field) throws Exception {
        return Arrays.asList(field.getType().getInterfaces()).contains(List.class);
    }
    
    private Boolean isArray(Field field) throws Exception {
        return field.getType().isArray();
    }
    
    private Boolean isArrayType(Class clas, Field field) throws Exception {
        List<Class> list = Arrays.asList(field.getType().getInterfaces());
        if (field.getType().isArray()
                //|| list.contains(List.class)
                || field.getType().equals(List.class)
                || field.getType().equals(ArrayList.class)) {
            //isArray = true;
            return true;
        } else if (field.getType().getCanonicalName().endsWith("boolean")
                || field.getType().getCanonicalName().endsWith("byte")
                || field.getType().getCanonicalName().endsWith("char")
                || field.getType().getCanonicalName().endsWith("short")
                || field.getType().getCanonicalName().endsWith("int")
                || field.getType().getCanonicalName().endsWith("long")
                || field.getType().getCanonicalName().endsWith("float")
                || field.getType().getCanonicalName().endsWith("double")
                ) {
        } else if (field.getType().getPackage().getName().startsWith("java.util")) {
            throw new Exception("Only {Array, List, ArrayList} containers are implemented");
        }
        
        //isArray = false;
        return false;
    }
    
    public String getFieldInstanceDatabaseString(DataLookup dataLookup) throws Exception {
        Object theObject = null;
        //Check Annotation for lookup
        if (data_class.metadata_annotation.lookup() == true) {
            //Lookup ID with String
            Object string = instances.get(0);
            if (string instanceof String) {
                theObject = dataLookup.lookupID((String) instances.get(0));
            } else {
                throw new Exception("Only String field_list can be used with metadataAnnotation");
            }
        } else {
            theObject = instances.get(0);
        }
        if (theObject instanceof String) {
            StringBuilder string = new StringBuilder(theObject.toString());
            int pos = 0;
            while ((pos = string.indexOf("\"", pos)) > -1) {
                string.insert(pos, "\\");
                pos += 2;
            }
            string.insert(0, "\"");
            string.append("\"");
            return string.toString();
        } else if (theObject instanceof Boolean) {
            return String.valueOf((Boolean) theObject);
        } else if (theObject instanceof Number) {
            return String.valueOf((Number) theObject);
        } else if (theObject instanceof ZonedDateTime) {
            //return "\"" + zonedDateTimeAdapter.marshal((ZonedDateTime) fieldObject) + "\"" ;
            return "\"" + zonedDateTimeAdapter.toJDBCDateTime((ZonedDateTime) theObject) + "\"" ;
        } else if (theObject == null) {
            throw new Exception("Null value passed for DB field => " + data_class.name + " => " + runtime_path);
            //Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Null value passed for DB field => " + data_class.name);
            //return "null";
        }
        throw new Exception("Unsupported Database data type => " + instance_object.getClass().getName());
    }
    
    @Override
    public String toString() {
        StringBuilder appendable = new StringBuilder();
        try {
            toString(appendable);
            return appendable.toString();
        } catch (Exception exception) {
            appendable.delete(0, appendable.length());
            appendable.append("toString error").append(exception.getMessage());
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "toString error", exception);
        }
        return appendable.toString();
    }
    
    public void toString(Appendable appendable) {
        try {
            toString(appendable, 0, this);
        } catch (Exception exception) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "toString error" , exception);
        }
    }
    
    private void toString(Appendable appendable, Integer indentation, DataInstance data_instance) throws Exception {
        if (data_class.declared_name.equalsIgnoreCase("childTables")) {
            data_class.declared_name = data_class.declared_name;
        }
        if (data_instance.is_null == true) {
            for (int i = 0; i < indentation; i++) {
                appendable.append(" ");
            }
            if (data_instance.data_class.isTable) {
                appendable.append("T-[").append(data_instance.data_class.name).append(" #-0-] (no records)\"\n");
            } else {
                appendable.append("F-[").append(data_instance.data_class.name).append("] (null)\"\n");
            }
            return;
        }
        for (Object object : data_instance.instances) {
            for (int i = 0; i < indentation; i++) {
                appendable.append(" ");
            }
            if (data_instance.data_class.isTable == true) {
                appendable.append("T-[").append(data_instance.data_class.name).append(" #").append(data_instance.instances_id_map.get(object).toString()).append("]\n");
            } else if (data_instance.data_class.isTable == false) {
                appendable.append("F-[").append(data_instance.data_class.name).append("]-\"").append(ignore_field_list.contains(data_instance.data_class.name) == true ? "..." : object.toString()).append("\"\n");
            }
            if (object.getClass().getName().startsWith(data_instance.data_class.package_name) == true) {
                if (data_instance.field_list_map.size() > 0) {
                    for (DataInstance fieldDataInstance : data_instance.field_list_map.get(object)) {
                        toString(appendable, indentation + 6, fieldDataInstance);
                    }
                } else {
                    for (DataClass fieldDataClass : data_instance.data_class.field_list) {
                        appendable.append("F-[").append(fieldDataClass.name).append("] (null)\n");
                    }
                }
                if (data_instance.table_list_map.size() > 0) {
                    for (DataInstance tableDataInstance : data_instance.table_list_map.get(object)) {
                        toString(appendable, indentation + 6, tableDataInstance);
                    }
                } else {
                    for (DataClass tableDataClass : data_instance.data_class.table_list) {
                        appendable.append("T-[").append(tableDataClass.name).append(" #-0-] (no records)\"\n");
                    }
                }
            }
        }
    }
    
    public void saveToDatabase(Integer dataModelId, DataLookup dataLookup, Object instanceID, DataInstance data_instance, ArrayList<String> inserts, String databaseFieldOpenQuote, String databaseFieldCloseQuote) throws Exception {
        if (data_instance.is_null == true) {
            return;
        }

        StringBuilder insert = new StringBuilder();
        for (Object instance_object : data_instance.instances) {
            insert.setLength(0);
            if (data_instance.data_class.isTable == true) {
                insert.append(saveToDatabase(dataModelId, dataLookup, instanceID, data_instance, instance_object, databaseFieldOpenQuote, databaseFieldCloseQuote));
                inserts.add(insert.toString());
            }
            for (DataInstance tableDataInstance : data_instance.table_list_map.get(instance_object)) {
                saveToDatabase(dataModelId, dataLookup, instanceID, tableDataInstance, inserts, databaseFieldOpenQuote, databaseFieldCloseQuote);
            }
        }
    }

    private String saveToDatabase(Integer dataModelId, DataLookup dataLookup, Object instanceID, DataInstance data_instance, Object instance_object, String databaseFieldOpenQuote, String databaseFieldCloseQuote) throws Exception {

        if (data_instance.data_class.isTable == false) {
            throw new Exception("toSQL takes table element only");
        }

        StringBuilder sql = new StringBuilder();
        String instance_objectJSON = gson.toJson(instance_object);
        sql.append("INSERT INTO `").append(database_name).append("`.`").append(data_instance.data_class.declared_name).append("`");
        sql.append("(`model_id`,`model_instance_id`,`child_id`,`parent_id`,`declared_field_name`,`class_name`,`json_object`,");
        for (DataInstance fieldDataInstance : data_instance.field_list_map.get(instance_object)) {
            sql.append(databaseFieldOpenQuote).append(fieldDataInstance.data_class.declared_name).append(databaseFieldCloseQuote).append(",");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(") VALUES (");
        sql.append(dataModelId).append(",");
        if (instanceID instanceof Number) {
            sql.append(instanceID).append(",");
        } else if (instanceID instanceof String) {
            sql.append("'").append(instanceID).append("',");
        }
        sql.append(data_instance.instances_id_map.get(instance_object)).append(",").append(data_instance.hasParent() == null ? "null" : data_instance.parent_id).append(",'").append(data_instance.data_class.declared_name).append("','").append(data_instance.data_class.name).append("','").append(instance_objectJSON).append("',");
        for (DataInstance fieldDataInstance : data_instance.field_list_map.get(instance_object)) {
            if (fieldDataInstance.is_null == true) {
                sql.append("null,");
            } else if (fieldDataInstance.instances.size() == 1) {
                sql.append(fieldDataInstance.getFieldInstanceDatabaseString(dataLookup)).append(",");
            } else {
                throw new Exception("Field must have one instance only per record");
            }
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(")");
        return sql.toString();
    }
}
