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

import net.reyadeyat.api.relational.annotation.DontJsonAnnotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.List;
import net.reyadeyat.api.relational.annotation.MetadataAnnotationDefault;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.reyadeyat.api.library.sequence.SequenceNumber;
import net.reyadeyat.api.relational.annotation.MetadataAnnotation;

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
public class DataClass {

    public enum LoadMethod {
        JSON, REFLECTION
    };
    DataClass parent_data_class;
    String package_name;
    /*Integer minCardinality;
    Integer maxCardinality;
    Boolean isIndexed;*/
    /**
     * Table if true, Field if false
     */
    Boolean isTable;
    String name;
    String declared_name;
    String class_name;
    String canonical_path;
    Field field;
    Class clas;
    Class<?> type;
    MetadataAnnotation metadata_annotation;

    List<DataClass> member_list;//members inside this class
    List<DataClass> table_list;//table members inside this class
    List<DataClass> field_list;//field members inside this class// Inserted in parent table

    Map<String, DataClass> member_list_map;//members inside this instace
    Map<String, DataClass> table_list_map;//table members inside this instace
    Map<String, DataClass> field_list_map;//field members inside this instace // Inserted in parent table
    
    DataLookup data_lookup;
    Boolean has_interface_implementation;
    Map<String, Class> interface_implementation;
    Boolean foreing_key_must_link_to_primary_key;
    
    
    public DataClass(DataClass parent_data_class, Field field, DataLookup data_lookup, Map<String, Class> interface_implementation, Boolean foreing_key_must_link_to_primary_key) throws Exception {
        /*if (data_class == null) {
            throw new Exception("DataClass cannot be null");
        }
        if (parent_data_class != null && field == null) {
            throw new Exception("Field cannot be null for ParentDataClass " + parent_data_class.clas.getSimpleName());
        }*/
        this.field = field;
        this.data_lookup = data_lookup;
        this.interface_implementation = interface_implementation;
        this.canonical_path = this.parent_data_class == null ? this.name : this.parent_data_class.canonical_path + "." + this.name;
        /*if (declared_name.equalsIgnoreCase("childTables")) {
            declared_name = declared_name;
        }*/
        this.parent_data_class = parent_data_class;
        this.foreing_key_must_link_to_primary_key = foreing_key_must_link_to_primary_key;
        
        this.member_list = new ArrayList<>();
        this.table_list = new ArrayList<>();
        this.field_list = new ArrayList<>();

        this.member_list_map = new HashMap<>();
        this.table_list_map = new HashMap<>();
        this.field_list_map = new HashMap<>();
        
        this.type = this.field.getType();
        //if (this.field != null) {
        this.field.setAccessible(true);
        List<Class<?>> class_list = getGenericClasses(this.field);
        if (class_list.size() > 1) {
            throw new Exception("Multi Generic Type is not implemented yet");
        }
        Class<?> interface_implementation_class = interface_implementation.get(this.type.getName());
        if (interface_implementation_class != null) {
            this.type = interface_implementation_class;
            this.clas = interface_implementation_class;
        } else {
            this.clas = class_list.get(0);
        }

        if (parent_data_class == null) {
            this.package_name = this.clas.getPackage().getName();
        } else {
            this.package_name = parent_data_class.package_name;
        }
        this.metadata_annotation = this.field.getAnnotation(MetadataAnnotation.class);
        this.metadata_annotation = this.metadata_annotation != null ? this.metadata_annotation : new MetadataAnnotationDefault(this.name, getCompatibleType(this.clas), this.clas.getTypeName(), "");
        this.has_interface_implementation = false;
        this.interface_implementation.forEach(new BiConsumer<String, Class>() {
            @Override
            public void accept(String interface_name, Class class_implementation) {
                has_interface_implementation = has_interface_implementation || clas.getSimpleName().equals(class_implementation.getSimpleName());
            }
        });
        if (this.clas.getPackage().getName().startsWith(this.package_name) == true
                || this.metadata_annotation.table() == true
                || has_interface_implementation == true) {//Table
            this.isTable = true;
            if (this.metadata_annotation.table() == true && this.metadata_annotation.title().isEmpty() == false) {
                this.name = this.metadata_annotation.title();
            } else {
                this.name = this.clas.getSimpleName();
            }
        } else if (isAllowedType()) {//Field
            this.isTable = false;
            this.name = this.field.getName();
        } else {
            throw new Exception("Data Package is '" + package_name + "'; can not traverse class " + clas.getSimpleName());
        }
        
        this.declared_name = field.getName();
        this.class_name = this.name;

        //Dig only inside own package
        if (this.clas.getPackage().getName().startsWith(this.package_name) == true
                || this.metadata_annotation.table() == true
                || has_interface_implementation == true) {
            List<Field> field_list = new ArrayList<>();
            getFields(this.clas, field_list);
            for (Field newDatafield : field_list) {
                //////field.setAccessible(true); // grant access to (protected) field
                //DataClass newDataClass = dataClassWalker(this, nfield);
                DataClass newDataClass = new DataClass(this, newDatafield, this.data_lookup, this.interface_implementation, this.foreing_key_must_link_to_primary_key);
                this.member_list.add(newDataClass);
                this.member_list_map.put(newDataClass.name, newDataClass);
                if (newDataClass.isTable) {//Table
                    this.table_list.add(newDataClass);
                    this.table_list_map.put(newDataClass.name, newDataClass);
                } else {//Field
                    this.field_list.add(newDataClass);
                    this.field_list_map.put(newDataClass.name, newDataClass);
                }
            }
        }
    }

    private void getFields(Class clas, List<Field> field_list) {
        if (clas.getSuperclass() != null && clas.getSuperclass().getName().equalsIgnoreCase("java.lang.Object") == false) {
            getFields(clas.getSuperclass(), field_list);
        }
        Field[] declaredfields = clas.getDeclaredFields();
        for (Field field : declaredfields) {
            
            if ((field.isAnnotationPresent(DontJsonAnnotation.class)
                    && field.getAnnotation(DontJsonAnnotation.class).dontJson() == false
                    && Modifier.isTransient(field.getModifiers()) == true)
                ||
                    (field.isAnnotationPresent(DontJsonAnnotation.class) == false
                    && Modifier.isTransient(field.getModifiers()) == true)) {
                continue;
            }
            field_list.add(field);
        }
    }

    /**
     * prevent empty initialization
     */
    private DataClass() {
    }

    @Override
    public String toString() {
        StringBuilder appendable = new StringBuilder();
        try {
            toString(appendable, 0, this);
            return appendable.toString();
        } catch (Exception exception) {
            appendable.delete(0, appendable.length());
            appendable.append("toString '").append(name).append("' error").append(exception.getMessage());
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "toString error", exception);
        }
        return appendable.toString();
    }

    public void toString(Appendable appendable) {
        try {
            appendable.append("DataClass [").append(name).append("]");
            toString(appendable, 0, this);
        } catch (Exception exception) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "toString error", exception);
        }
    }

    private void toString(Appendable appendable, Integer indentation, DataClass data_class) throws Exception {
        for (int i = 0; i < indentation; i++) {
            appendable.append(" ");
        }
        if (data_class.isTable == true) {
            appendable.append("~T-[").append(data_class.name).append("]\n");
        } else if (data_class.isTable == false) {
            appendable.append("~F-[").append(data_class.name).append("]\n");
        }
        for (int i = 0; i < field_list.size(); i++) {
            toString(appendable, indentation + 6, field_list.get(i));
        }
        for (int i = 0; i < table_list.size(); i++) {
            toString(appendable, indentation + 6, table_list.get(i));
        }
    }

    public boolean isNotNull() {
        return (metadata_annotation != null && metadata_annotation.nullable() == false);
    }

    public Boolean hasParent() {
        return parent_data_class != null;
    }

    public String getParentName() {
        return parent_data_class.name;
    }

    public String getFieldType() throws Exception {
        /*if (metadata_annotation.lookupCategory().isEmpty() == false) {
            //Convert String field to a Lookup ID field
            return "INT";
        } else if (metadata_annotation.type().isEmpty() == false) {
            return metadata_annotation.type();
        }*/
        return getCompatibleType(clas);
    }

    private Boolean isList(Field field) throws Exception {
        return Arrays.asList(field.getType().getInterfaces()).contains(List.class);
    }

    private Boolean isArray(Field field) throws Exception {
        return field.getType().isArray();
    }

    private Boolean isAllowedType() {
        if (clas.getTypeName().equalsIgnoreCase("java.lang.String")
                || clas.getTypeName().equalsIgnoreCase("java.lang.Boolean")
                || clas.getTypeName().equalsIgnoreCase("java.lang.Byte")
                || clas.getTypeName().equalsIgnoreCase("java.lang.Short")
                || clas.getTypeName().equalsIgnoreCase("java.lang.Integer")
                || clas.getTypeName().equalsIgnoreCase("java.lang.Long")
                || clas.getTypeName().equalsIgnoreCase("java.lang.Float")
                || clas.getTypeName().equalsIgnoreCase("java.lang.Double")
                || clas.getTypeName().equalsIgnoreCase("java.util.Date")
                || clas.getTypeName().equalsIgnoreCase("java.sql.String")
                || clas.getTypeName().equalsIgnoreCase("java.sql.Date")
                || clas.getTypeName().equalsIgnoreCase("java.sql.Time")
                || clas.getTypeName().equalsIgnoreCase("java.lang.Timestamp")
                || clas.getTypeName().equalsIgnoreCase("java.time.ZonedDateTime")) {
            return true;
        }
        return false;
    }

    static public List<Class<?>> getGenericClasses(Field field) {

        ArrayList<Class<?>> class_list = new ArrayList<>();
        /*if (field.getGenericType() instanceof Class) {//Class
            if (field.getType().isArray()) {
                class_list.add((Class) field.getType().getComponentType());
            } else {
                class_list.add((Class) field.getType());
            }
        } else */
        if (field.getGenericType() instanceof ParameterizedType) {//Generic
            ParameterizedType genericType = (ParameterizedType) field.getGenericType();
            Type[] types = genericType.getActualTypeArguments();
            for (Type type : types) {
                class_list.add((Class) type);
            }
        } else {
            if (field.getType().isArray()) {
                class_list.add((Class) field.getType().getComponentType());
            } else {
                class_list.add((Class) field.getType());
            }
        }

        return class_list;
    }

    static public String getCompatibleType(Class clas) {
        if (clas.getTypeName().equalsIgnoreCase("java.lang.String") == true) {
            return "VARCHAR(256)";
        } else if (clas.getTypeName().equalsIgnoreCase("java.lang.Boolean") == true) {
            return "TINYINT";
        } else if (clas.getTypeName().equalsIgnoreCase("java.lang.Byte") == true) {
            return "TINYINT";
        } else if (clas.getTypeName().equalsIgnoreCase("java.lang.Short") == true) {
            return "SMALLINT";
        } else if (clas.getTypeName().equalsIgnoreCase("java.lang.Integer") == true) {
            return "INTEGER";
        } else if (clas.getTypeName().equalsIgnoreCase("java.lang.Long") == true) {
            return "BIGINT";
        } else if (clas.getTypeName().equalsIgnoreCase("java.lang.Float") == true) {
            return "FLOAT";
        } else if (clas.getTypeName().equalsIgnoreCase("java.lang.Double") == true) {
            return "DOUBLE";
        } else if (clas.getTypeName().equalsIgnoreCase("java.util.Date") == true) {
            return "DATE";
        } else if (clas.getTypeName().equalsIgnoreCase("java.sql.Date") == true) {
            return "DATE";
        } else if (clas.getTypeName().equalsIgnoreCase("java.sql.Time") == true) {
            return "TIME";
        } else if (clas.getTypeName().equalsIgnoreCase("java.sql.Timestamp") == true) {
            return "TIMESTAMP";
        } else if (clas.getTypeName().equalsIgnoreCase("java.time.ZonedDateTime") == true) {
            return "TIMESTAMP";
        }

        return "VARCHAR(256)";
    }

    private DataInstance createDataInstance(Object instanceObject, String databaseName) throws Exception {
        if (clas.equals(instanceObject.getClass())) {
            throw new Exception("object class does not equals to data class");
        }
        SequenceNumber sequenceNumber = new SequenceNumber(1, 1, false);
        DataInstance data_instance = new DataInstance(DataInstance.State.NEW, databaseName, this, null, null, instanceObject, sequenceNumber, true, foreing_key_must_link_to_primary_key);
        return data_instance;
    }

    public void createDatabaseSchema(Connection connection, String databaseName, DataClass data_class, ArrayList<String> dataClasses, ArrayList<String> creates) throws Exception {
        if (dataClasses.contains(data_class.clas.getCanonicalName())) {
            throw new Exception("Error: Polymorphic Associations Detected with class '" + data_class.clas.getCanonicalName() + "', use one to one class composition relation; refactor this class '" + data_class.clas.getCanonicalName() + "' name into 2 distinct names");
        }
        if (data_class.isTable == false) {
            throw new Exception("createDatabaseSchema takes table element only");
        }
        dataClasses.add(data_class.clas.getCanonicalName());
        StringBuilder sql = new StringBuilder();
        //Create data_class table with member field_list 
        sql.append("CREATE TABLE IF NOT EXISTS `").append(databaseName).append("`.`").append(data_class.declared_name).append("` (\n");
        sql.append(" ").append("`model_id` SMALLINT UNSIGNED NOT NULL,\n");
        sql.append(" ").append("`model_instance_id` BIGINT UNSIGNED NOT NULL,\n");
        sql.append(" ").append("`child_id` SMALLINT UNSIGNED NOT NULL,\n");
        sql.append(" ").append("`parent_id` SMALLINT UNSIGNED").append(data_class.hasParent() ? " NOT" : "").append(" NULL,\n");
        sql.append(" ").append("`declared_field_name` VARCHAR(256) NOT NULL,\n");
        sql.append(" ").append("`class_name` VARCHAR(256) NOT NULL,\n");
        sql.append(" ").append("`json_object` LONGTEXT NOT NULL,\n");
        StringBuilder indexes = new StringBuilder();
        for (DataClass dbField : data_class.field_list) {
            sql.append(" `").append(dbField.declared_name).append("` ").append(dbField.getFieldType());
            if (dbField.isNotNull() == true) {
                sql.append(" NOT NULL");
                if (dbField.metadata_annotation.indexed() == true) {
                    if (dbField.metadata_annotation.indexed_expresion().isEmpty() == false) {
                        indexes.append("  INDEX `").append(dbField.declared_name).append("` " + dbField.metadata_annotation.indexed_expresion() + ",\n");
                    } else {
                        indexes.append("  INDEX(`").append(dbField.declared_name).append("`),\n");
                    }
                }
            }
            sql.append(",\n");
        }
        //indexes = indexes.length() > 2 ? indexes.delete(indexes.length()-2, indexes.length()): indexes;
        sql.append(indexes);
        /** no need to make parent a primary since child is unique, but parent key is primary to be reference in foreign keys*/
        sql.append("  PRIMARY KEY (`model_id`,`model_instance_id`,`child_id`").append(data_class.hasParent() ? ",`parent_id`" : "").append(")");
        sql.append(data_class.hasParent() ? "," : "").append("\n");
        if (data_class.hasParent() == true) {
            sql.append("  FOREIGN KEY `fk_").append(data_class.parent_data_class.declared_name).append("` (`model_id`,`model_instance_id`,`parent_id`)\n");
            sql.append("    REFERENCES `").append(data_class.parent_data_class.declared_name).append("` (`model_id`,`model_instance_id`,`child_id`)\n");
            sql.append("    ON UPDATE CASCADE\n");
            sql.append("    ON DELETE RESTRICT\n");
        }
        sql.append(") ENGINE=InnoDB CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;");
        creates.add(sql.toString());

        //Create data_class child tables with member field_list
        for (DataClass dbTable : data_class.table_list) {
            createDatabaseSchema(connection, databaseName, dbTable, dataClasses, creates);
        }
    }
    
    public Object loadFromDatabase(Connection modelConnection, Integer model_id, String databaseName, DataLookup dataLookup, Object model_instance_id, LoadMethod loadMethod, SequenceNumber sequence, ArrayList<String> selects) throws Exception {
        Object instanceObject = null;//this.clas.getConstructor().newInstance();
        DataInstance data_instance = null;//new DataInstance(this, null, null, instanceObject, false);
        //Handle Saved Sequences
        data_instance = loadFromDatabase(modelConnection, model_id, databaseName, dataLookup, model_instance_id, loadMethod, selects, data_instance, instanceObject, 1, sequence);
        return data_instance.instances.get(0);
    }

    @SuppressWarnings("unchecked")
    private DataInstance loadFromDatabase(Connection connection, Integer model_id, String databaseName, DataLookup dataLookup, Object model_instance_id, LoadMethod loadMethod, ArrayList<String> selects, DataInstance parentDataInstance, Object parentInstanceObject, Integer parentID, SequenceNumber sequence) throws Exception {
        if (loadMethod != LoadMethod.JSON && loadMethod != LoadMethod.REFLECTION) {
            throw new Exception("Load Method '" + loadMethod + "' is not implemented");
        }
        if (isTable == false) {
            throw new Exception("Can't Load DataClass Field '~F-[" + name + "]', load only DataClass Table");
        }
        String sql = loadFromDatabase(model_id, databaseName, dataLookup, model_instance_id, loadMethod, parentID);
        selects.add(sql);
        ////System.out.println(sql);
        /*for (int i = 0; i < table_list.size(); i++) {
            table_list.get(i).loadFromDatabase(lookup, model_instance_id, databaseName, loadMethod, selects);
        }*/
        ArrayList<HashMap<String, Object>> dataset = new ArrayList<HashMap<String, Object>>();
        //Model modelInstance = null;
        try (Statement st = connection.createStatement()) {
            try (ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    HashMap<String, Object> record = new HashMap<String, Object>();
                    if (loadMethod == LoadMethod.JSON) {
                        record.put("model_instance_id", rs.getLong("model_instance_id"));
                        record.put("child_id", rs.getInt("child_id"));
                        record.put("parent_id", rs.getInt("parent_id"));
                        record.put("json_object", rs.getString("json_object").replaceAll("\"\"", "\""));
                    } else if (loadMethod == LoadMethod.REFLECTION) {
                        record.put("model_instance_id", rs.getLong("model_instance_id"));
                        record.put("child_id", rs.getInt("child_id"));
                        record.put("parent_id", rs.getInt("parent_id"));
                        record.put("declared_field_name", rs.getString("declared_field_name"));
                        record.put("class_name", rs.getString("class_name"));
                        for (int i = 0; i < field_list.size(); i++) {
                            DataClass sc = field_list.get(i);
                            if (sc.clas.getCanonicalName().equalsIgnoreCase("java.lang.Boolean")) {
                                record.put(sc.declared_name, rs.getBoolean(sc.declared_name));
                            } else {
                                record.put(sc.declared_name, rs.getObject(sc.declared_name));
                            }
                        }
                    }
                    dataset.add(record);
                }
            } catch (Exception ex) {
                throw ex;
            }
        } catch (Exception ex) {
            throw ex;
        }
        DataInstance newDataInstance = null;
        
        if (loadMethod == LoadMethod.JSON) {
            for (int i = 0; i < dataset.size(); i++) {
                HashMap<String, Object> record = dataset.get(i);
                Long instance_id = (Long) record.get("model_instance_id");
                Integer child_id = (Integer) record.get("child_id");
                Integer parent_id = (Integer) record.get("parent_id");
                String json_object = (String) record.get("json_object");
                Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT).create();
                @SuppressWarnings("unchecked")
                Object instanceObject = gson.fromJson(json_object, this.clas);
                /*modelInstance = gsonN.fromJson(json_object, modelClass);
                model.setInstance(modelInstance);
                model.prepareInstance();*/
                newDataInstance = new DataInstance(DataInstance.State.LOADED, databaseName, this, parentDataInstance, parentInstanceObject, instanceObject, sequence, true, foreing_key_must_link_to_primary_key);
            }
        } else if (loadMethod == LoadMethod.REFLECTION) {
            Object instance_object = null;
            //if (this.type.getCanonicalName().equals("net.reyadeyat.api.relational.model.TableInterfaceImplementationDataStructures") == true) {
            /*if (has_interface_implementation == true) {
                instance_object = this.type.getConstructor().newInstance();
            } else {
                instance_object = this.type.getConstructor().newInstance();
            }*/
            instance_object = this.type.getConstructor().newInstance();
            
            if (parentInstanceObject != null) {
                this.field.set(parentInstanceObject, instance_object);
            }
            DataInstance recordDataInstance = null;
            if (parentInstanceObject == null) {//parent
                recordDataInstance = new DataInstance(DataInstance.State.LOADED, databaseName, this, parentDataInstance, parentInstanceObject, this.clas.getConstructor().newInstance(), sequence, false, foreing_key_must_link_to_primary_key);
            } else {
                recordDataInstance = new DataInstance(DataInstance.State.LOADED, databaseName, this, parentDataInstance, parentInstanceObject, instance_object, sequence, false, foreing_key_must_link_to_primary_key);
            }
            recordDataInstance.parent_id = parentID;
            //Load Instances into list
            for (int i = 0; i < dataset.size(); i++) {
                HashMap<String, Object> record = dataset.get(i);
                Long instance_id = (Long) record.get("model_instance_id");
                Integer child_id = (Integer) record.get("child_id");
                Integer parent_id = (Integer) record.get("parent_id");
                String declared_field_name = (String) record.get("declared_field_name");
                String class_name = (String) record.get("class_name");
                
                Object recordInstanceObject = null;
                if (parentInstanceObject == null) {//parent
                    recordInstanceObject = recordDataInstance.getInstanceObject();
                } else if (instance_object instanceof ArrayList<?>) {
                    recordInstanceObject = this.clas.getConstructor().newInstance();
                    Method add = ArrayList.class.getDeclaredMethod("add", Object.class);
                    add.invoke(instance_object, recordInstanceObject);
                } else if (this.has_interface_implementation == true) {
                    recordInstanceObject = this.clas.getConstructor().newInstance();
                } else {
                    throw new Exception("Undefined Data Instance Load Behaviour.");
                }
                recordDataInstance.addInstanceObject(recordInstanceObject, child_id);
                for (int y = 0; y < field_list.size(); y++) {
                    DataClass fieldDataClass = field_list.get(y);
                    Object fieldInstanceObject = null;
                    if (fieldDataClass.metadata_annotation.lookup() == true) {
                        ////fieldInstanceObject = data_lookup.lookupCode((Integer) record.get(fieldDataClass.declared_name));
                        fieldInstanceObject = data_lookup.lookupCode(Integer.valueOf((String) record.get(fieldDataClass.declared_name)));
                        fieldDataClass.field.set(recordInstanceObject, fieldInstanceObject);
                    } else if (fieldDataClass.clas.getCanonicalName().equalsIgnoreCase("java.lang.Boolean")) {
                        fieldInstanceObject = (Boolean) record.get(fieldDataClass.declared_name);
                        fieldDataClass.field.set(recordInstanceObject, fieldInstanceObject);
                    } else {
                        fieldInstanceObject = record.get(fieldDataClass.declared_name);
                        fieldDataClass.field.set(recordInstanceObject, fieldInstanceObject);
                    }
                    recordDataInstance.addChildInstanceObject(DataInstance.State.LOADED, fieldDataClass, recordInstanceObject, fieldInstanceObject, sequence, false, foreing_key_must_link_to_primary_key);
                }
                for (int x = 0; x < table_list.size(); x++) {
                    DataClass subRecordDataClass = table_list.get(x);
                    subRecordDataClass.loadFromDatabase(connection, model_id, databaseName, dataLookup, model_instance_id, loadMethod, selects, recordDataInstance, recordInstanceObject, child_id, sequence);
                }
            }
            newDataInstance = recordDataInstance;
        }
        
        /*if (modelInstance == null) {
            throw new Exception("Can't Load DataClass, Instance Records not exist in Database");
        }
        return modelInstance;*/
        return newDataInstance;
    }

    private String loadFromDatabase(Integer model_id, String databaseName, DataLookup dataLookup, Object model_instance_id, LoadMethod loadMethod, Integer parentID) throws Exception {

        if (isTable == false) {
            throw new Exception("toSQL takes table element only");
        }
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT");
        if (loadMethod == LoadMethod.JSON) {
            sql.append(" `").append(declared_name).append("`.`model_instance_id`,`").append(declared_name).append("`.`child_id`,`").append(declared_name).append("`.`parent_id`,`").append(declared_name).append("`.`declared_field_name`,`").append(declared_name).append("`.`class_name`,`").append(declared_name).append("`.`json_object`");
        } else if (loadMethod == LoadMethod.REFLECTION) {
            sql.append(" `").append(declared_name).append("`.`model_instance_id`,`").append(declared_name).append("`.`child_id`,`").append(declared_name).append("`.`parent_id`,`").append(declared_name).append("`.`declared_field_name`,`").append(declared_name).append("`.`class_name`,");//,`json_object`");
            for (int i = 0; i < field_list.size(); i++) {
                DataClass fieldDataClass = field_list.get(i);
                sql.append("`").append(declared_name).append("`.`").append(fieldDataClass.declared_name).append("`,");
            }
            sql.delete(sql.length() - 1, sql.length());
        }
        sql.append(" FROM `").append(databaseName).append("`.`").append(declared_name).append("`");
        if (parent_data_class != null) {
            sql.append(" INNER JOIN `").append(databaseName).append("`.`").append(parent_data_class.declared_name).append("`");
            sql.append(" ON `").append(declared_name).append("`.`model_id`=`").append(parent_data_class.declared_name).append("`.`model_id`");
            sql.append(" AND `").append(declared_name).append("`.`model_instance_id`=`").append(parent_data_class.declared_name).append("`.`model_instance_id`");
            sql.append(" AND `").append(declared_name).append("`.`parent_id`=`").append(parent_data_class.declared_name).append("`.`child_id`");
        }
        sql.append(" WHERE `").append(declared_name).append("`.`model_id`=").append(model_id);
        sql.append(" AND `").append(declared_name).append("`.`model_instance_id`=");
        if (model_instance_id instanceof Number) {
            sql.append(model_instance_id);
        } else if (model_instance_id instanceof String) {
            sql.append("'").append(model_instance_id).append("'");
        }
        if (parent_data_class != null) {
            sql.append(" AND `").append(declared_name).append("`.`parent_id`=").append(parentID);
        }

        return sql.toString();
    }
}
