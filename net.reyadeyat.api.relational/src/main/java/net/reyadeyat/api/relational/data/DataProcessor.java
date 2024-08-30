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

import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.reyadeyat.api.library.jdbc.JDBCSource;
import net.reyadeyat.api.library.json.JsonZonedDateTimeAdapter;
import net.reyadeyat.api.library.sequence.SequenceNumber;

/**
 * 
 * Description
 * 
 *
 * @author Mohammad Nabil Mostafa
 * <a href="mailto:code@reyadeyat.net">code@reyadeyat.net</a>
 * @param <Model>
 * 
 * @since 2023.01.01
 */
public class DataProcessor<Model> {
    Class data_model_class;
    Class model_class;
    DataModel<Model> data_model;
    JDBCSource model_jdbc_source;
    ModelDefinition model_definition;
    DataLookup data_lookup;
    DataClass data_class;
    Map<DataModel<Model>, DataInstance> dataModelDataInstanceMap;
    Map<String, Class> interface_implementation;
    Boolean foreing_key_must_link_to_primary_key;
    
    final static public JsonZonedDateTimeAdapter zonedDateTimeAdapter = new JsonZonedDateTimeAdapter();
    
    @SuppressWarnings("unchecked")
    public DataProcessor(Class data_model_class, Class model_class, JDBCSource model_jdbc_source, ModelDefinition model_definition, DataLookup data_lookup, Map<String, Class> interface_implementation, Boolean foreing_key_must_link_to_primary_key) throws Exception {
        this.data_model_class = data_model_class;
        this.model_class = model_class;
        this.model_jdbc_source = model_jdbc_source;
        this.model_definition = model_definition;
        this.data_lookup = data_lookup;
        this.interface_implementation = interface_implementation;
        this.foreing_key_must_link_to_primary_key = foreing_key_must_link_to_primary_key;

        Boolean foundDataModelInterface = false;
        for (Class intrface : this.data_model_class.getInterfaces()) {
            if (intrface.getCanonicalName().equalsIgnoreCase(DataModel.class.getCanonicalName())) {
                foundDataModelInterface = true;
            }
        }
        if (foundDataModelInterface == false) {
            throw new Exception("data_model_class class must implement DataModel class");
        }
        this.data_model = (DataModel<Model>) this.data_model_class.getConstructor(model_class, ModelDefinition.class).newInstance(null, model_definition);
        Method methodGetDeclaredField = this.data_model_class.getDeclaredMethod("getDeclaredField");
        Field modelDeclaredField = (Field) methodGetDeclaredField.invoke(this.data_model);
        if (model_class.getCanonicalName().equals(modelDeclaredField.getType().getCanonicalName()) == false) {
            throw new Exception("data_model_class '" + data_model_class.getCanonicalName() + "' declared model field '" + modelDeclaredField.getName() + "' is of type class '" + modelDeclaredField.getType().getCanonicalName() + "' that is not same as model_class '" + model_class.getCanonicalName() + "'");
        }
        this.data_class = new DataClass(null, modelDeclaredField, this.data_lookup, this.interface_implementation, this.foreing_key_must_link_to_primary_key);
        
        dataModelDataInstanceMap = new HashMap<DataModel<Model>, DataInstance>();
    }
    
    @Override
    public String toString() {
        StringBuilder appendable = new StringBuilder();
        try {
            toString(appendable);
            return appendable.toString();
        } catch (Exception exception) {
            appendable.delete(0, appendable.length());
            appendable.append("toString '").append(data_model.getModelDefinition().model_name).append("-").append(data_model.getModelDefinition().model_name).append("' error").append(exception.getMessage());
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "toString error", exception);
        }
        return appendable.toString();
    }
    
    public void toString(Appendable appendable) throws Exception {
        appendable.append("Model: ").append(data_model.getModelDefinition().model_name).append("\n");
        try {
            Iterator<DataModel<Model>> iterator = dataModelDataInstanceMap.keySet().iterator();
            while (iterator.hasNext()) {
                DataModel<Model> tempDataModel = iterator.next();
                DataInstance data_instance = dataModelDataInstanceMap.get(tempDataModel);
                appendable.append(data_instance.toString());
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
    
    public void toString(Appendable appendable, DataModel<Model> data_model) throws Exception {
        if (this.data_class.clas.getCanonicalName().equalsIgnoreCase(data_model.getInstance().getClass().getCanonicalName()) == false) {
            throw new Exception("Required model of type '" + this.data_class.clas.getCanonicalName() + "' while found model of type '" + data_model.getInstance().getClass().getCanonicalName() + "'");
        }
        appendable.append("Model: ");
        appendable.append(this.data_model.getModelDefinition().model_name);
        appendable.append("\n");

        //DataInstance data_instance = dataModelDataInstanceMap.get(data_model);
        SequenceNumber sequenceNumber = new SequenceNumber(0, 1, false);
        DataInstance data_instance = new DataInstance(DataInstance.State.NEW, data_model.getModelDefinition().modeled_database_name, this.data_class, null, null, data_model.getInstance(), sequenceNumber, true, foreing_key_must_link_to_primary_key);
        data_instance.toString(appendable);
    }
    
    public Integer selectModelIdFromDatabase(String modeled_database_name) throws Exception {
        Integer modelId = -1;
        String select_model_sql = "SELECT `id` FROM `model`.`model` WHERE `model`.`version`=? AND `model`.`database_name`=? AND `model`.`name`=? AND `model`.`root_class_path`=?";
        try ( Connection data_connection = model_jdbc_source.getConnection(false)) {
            try (PreparedStatement data_stmt = data_connection.prepareStatement(select_model_sql)) {
                data_stmt.setString(1, model_definition.model_version);
                data_stmt.setString(2, modeled_database_name);
                data_stmt.setString(3, model_definition.model_name);
                data_stmt.setString(4, this.data_class.clas.getCanonicalName());
                try (ResultSet modelResultset = data_stmt.executeQuery()) {
                    if (modelResultset.next()) {
                        modelId = modelResultset.getInt("id");
                    }
                    data_stmt.close();
                } catch (Exception sqlx) {
                    throw sqlx;
                }
            } catch (Exception sqlx) {
                throw sqlx;
            }
        } catch (Exception sqlx) {
            throw sqlx;
        }
        return modelId;
    }
    
    public ArrayList<Integer> selectModelInstanceIDsFromDatabase(String modeled_database_name) throws Exception {
        return selectModelInstanceIDsFromDatabase(modeled_database_name, null, null);
    }
    
    public ArrayList<Integer> selectModelInstanceIDsFromDatabase(String modeled_database_name, Integer fromId, Integer toId) throws Exception {
        ArrayList<Integer> modelInstanceIds = new ArrayList<Integer>();
        String select_model_sql = "SELECT `model_instance_id`, `model_instance_extra_info` FROM `model`.`model` INNER JOIN `model`.`model_instance` ON `model`.`model`.`model_id`=`model`.`model_instance`.`model_id` WHERE `model`.`model_version`=? AND `model`.`modeled_database_name`=? AND `model`.`model_name`=? AND `model`.`model_class_path`=? " + (fromId == null || toId == null ? "" : " AND `model_instance`.`model_instance_id` BETWEEN ? AND ?");
        try (Connection data_connection = model_jdbc_source.getConnection(false)) {
            try (PreparedStatement data_stmt = data_connection.prepareStatement(select_model_sql)) {
                data_stmt.setString(1, model_definition.model_version);
                data_stmt.setString(2, modeled_database_name);
                data_stmt.setString(3, model_definition.model_name);
                data_stmt.setString(4, this.data_class.clas.getCanonicalName());
                if (fromId != null && toId != null) {
                    data_stmt.setInt(5, fromId);
                    data_stmt.setInt(6, toId);
                }
                try (ResultSet modelResultset = data_stmt.executeQuery()) {
                    while (modelResultset.next()) {
                        modelInstanceIds.add(modelResultset.getInt("model_instance_id"));
                    }
                    data_stmt.close();
                } catch (Exception sqlx) {
                    throw sqlx;
                }
            } catch (Exception sqlx) {
                throw sqlx;
            }
        } catch (Exception sqlx) {
            throw sqlx;
        }
        return modelInstanceIds;
    }
    
    public Integer generateModel(JDBCSource model_jdbc_source, JDBCSource data_jdbc_source, Integer model_id, Integer instance_sequence_type_id, String instance_sequence_last_value, String secret_key, String modeled_table_data_structures_class) throws Exception {
        //String model_name = this.data_model.getName();
        
        ArrayList<String> creates = new ArrayList<String>();
        ArrayList<String> dataClasses = new ArrayList<String>();
        try (Connection data_model_connection = model_jdbc_source.getConnection(false)) {
            data_class.createDatabaseSchema(data_model_connection, model_jdbc_source.getDatabaseName(), data_class, dataClasses, creates);
            //try (Connection data_model_connection = data_model_source.getConnection(false)) {
                String insert_model_sql = "INSERT INTO `model`.`model`(`model_id`, `model_instance_sequence_type_id`, `model_instance_sequence_last_value`, `model_name`, `model_version`, `model_class_path`, `model_data_lookup_category`, `modeled_database_url`, `modeled_database_url_user_name`, `modeled_database_url_user_password`, `modeled_database_schem`, `modeled_database_name`, `modeled_database_field_open_quote`, `modeled_database_field_close_quote`, `modeled_table_data_structures_class`) VALUES (?,?,?,?,?,?,?,?,?,TO_BASE64(AES_ENCRYPT(?, '"+secret_key+"')),?,?,?,?,?)";
                try (PreparedStatement data_stmt = data_model_connection.prepareStatement(insert_model_sql, Statement.RETURN_GENERATED_KEYS)) {
                    data_stmt.setObject(1, model_id == -1 || model_id == null ? null : model_id);
                    data_stmt.setObject(2, instance_sequence_type_id);
                    data_stmt.setObject(3, instance_sequence_last_value);
                    data_stmt.setObject(4, model_definition.model_name);
                    data_stmt.setObject(5, model_definition.model_version);
                    data_stmt.setObject(6, data_class.clas.getCanonicalName());
                    data_stmt.setObject(7, model_definition.model_data_lookup_category);
                    data_stmt.setObject(8, data_jdbc_source.getURL());
                    data_stmt.setObject(9, data_jdbc_source.getUserName());
                    data_stmt.setObject(10, data_jdbc_source.getUserPassword());
                    data_stmt.setObject(11, data_jdbc_source.getDatabaseSchema() == null ? "" : model_jdbc_source.getDatabaseSchema());
                    data_stmt.setObject(12, data_jdbc_source.getDatabaseName());
                    data_stmt.setObject(13, data_jdbc_source.getDatabaseOpenQuote());
                    data_stmt.setObject(14, data_jdbc_source.getDatabaseCloseQuote());
                    data_stmt.setObject(15, modeled_table_data_structures_class);
                    data_stmt.executeUpdate();
                    /*try (ResultSet generatedKeys = data_stmt.getGeneratedKeys()) {
                        generatedKeys.next();
                        model_id = Integer.valueOf(generatedKeys.getString(1));
                        generatedKeys.close();
                    } catch (Exception sqlx) {
                        throw sqlx;
                    }*/
                    StringBuilder b = new StringBuilder();
                    try (Statement modelStatement = data_model_connection.createStatement()) {
                        for (int i = 0; i < creates.size(); i++) {
                            modelStatement.addBatch(creates.get(i));
                            b.append(creates.get(i)).append("\n");
                        }
                        int[] rows = modelStatement.executeBatch();
                        if (rows != null && rows.length > 0) {
                            ////System.out.println("Success: tables created = " + rows.length + "\n" + b.toString());
                        }
                        if (data_model_connection.getAutoCommit() == false) {
                            data_model_connection.commit();
                        }
                        modelStatement.close();
                    } catch (Exception sqlx) {
                        b.insert(0, "create database sql statements carshed :\n");
                        System.out.println(b.toString());
                        if (data_model_connection.getAutoCommit() == false) {
                            data_model_connection.rollback();
                        }
                        throw sqlx;
                    }
                    if (data_model_connection.isClosed() == false && data_model_connection.getAutoCommit() == false) {
                        data_model_connection.commit();
                    }
                    if (data_stmt.isClosed() == false) {
                        data_stmt.close();
                    }
                } catch (Exception sqlx) {
                    if (data_model_connection.isClosed() == false && data_model_connection.getAutoCommit() == false) {
                        data_model_connection.rollback();
                    }
                    throw sqlx;
                }
            //} catch (Exception sqlx) {
            //    throw sqlx;
            //}
        } catch (Exception sqlx) {
                throw sqlx;
        }
        return model_id;
    }
    
    /**Save sequence last value in memory, if server crashed you have to update latest model sequence when server starts up again*/
    synchronized public DataModel<Model> saveModelToDatabase(Appendable appendable, JDBCSource data_jdbc_source, DataModel<Model> data_model, String database_field_open_quote, String database_field_close_quote) throws Exception {
        Integer instanceID = null;
        Exception exception = null;
        appendable = appendable == null ? new PrintWriter(Writer.nullWriter()) : appendable;
        try ( Connection locked_data_model_connection = model_jdbc_source.getConnection(false)) {
            String lockTables = "LOCK TABLE `model` WRITE, `sequence_type` WRITE, `model_sequence` WRITE, `model_instance` WRITE";
            try (Statement lockStatement = locked_data_model_connection.createStatement()) {
                lockStatement.execute(lockTables);
            } catch (Exception sqlx) {
                throw sqlx;
            }
            String select_model_sql = "SELECT `model`.`model_id` AS `model_id`, `model_instance_sequence_type_id`, `model_instance_sequence_last_value`, `sequence_type`.`sequence_type_name`, `sequence_type`.`sequence_type`, `sequence_type`.`sequence_type_ordered_chars`, `sequence_type`.`sequence_type_chars_width`, `sequence_type`.`sequence_type_padding_char`, `sequence_type`.`sequence_type_rewind`, `sequence_type`.`sequence_type_initial_value`, `sequence_type`.`sequence_type_increment_value` FROM `model`.`model` INNER JOIN `model`.`sequence_type` ON `model`.`model_instance_sequence_type_id`=`sequence_type`.`sequence_type_id` WHERE `model`.`model_version`=? AND `model`.`modeled_database_name`=? AND `model`.`model_name`=? AND `model`.`model_class_path`=?";
            try (PreparedStatement data_stmt = locked_data_model_connection.prepareStatement(select_model_sql)) {
                data_stmt.setString(1, model_definition.model_version);
                data_stmt.setString(2, data_jdbc_source.getDatabaseName());
                data_stmt.setString(3, model_definition.model_name);
                data_stmt.setString(4, this.data_class.clas.getCanonicalName());
                try (ResultSet data_model_resultset = data_stmt.executeQuery()) {
                    if (data_model_resultset.next() == false) {
                        throw new Exception("DataModel '" + data_model_class.getCanonicalName() + "' is not defined in Data.DataModel Table");
                    }
                    Integer dataModelId = data_model_resultset.getInt("model_id");
                    String sequence_type = data_model_resultset.getString("sequence_type");
                    Integer instance_last_value = data_model_resultset.getInt("model_instance_sequence_last_value");
                    Integer sequence_initial_value = data_model_resultset.getInt("sequence_type_initial_value");
                    Integer sequence_increment_value = data_model_resultset.getInt("sequence_type_increment_value");

                    if (sequence_type.equalsIgnoreCase("Integer") == false) {
                        throw new Exception("Sequence Type must be 'Integer' type");
                    }

                    Model instanceObject = data_model.getInstance();
                    //Set InstanceID to Root Object in model sequence
                    SequenceNumber sequenceNumber = new SequenceNumber(sequence_initial_value, sequence_increment_value, false);
                    sequenceNumber.initSequence(this.data_class.clas, instance_last_value);

                    //Prpeare DataInstance
                    DataInstance data_instance = new DataInstance(DataInstance.State.NEW, model_jdbc_source.getDatabaseName(), this.data_class, null, null, instanceObject, sequenceNumber, true, foreing_key_must_link_to_primary_key);
                    instanceID = sequenceNumber.getSequenceState(this.data_class.clas);

                    String update_model_sql = "UPDATE `model`.`model` SET `model_instance_sequence_last_value` = ? WHERE `model_id` = ?";
                    try (PreparedStatement modelUpdateStatement = locked_data_model_connection.prepareStatement(update_model_sql)) {

                        modelUpdateStatement.setInt(1, instanceID);
                        modelUpdateStatement.setInt(2, dataModelId);
                        int rows = modelUpdateStatement.executeUpdate();
                        if (rows == 0) {
                            throw new Exception("Can not update data model record with last instance id value");
                        }
                    } catch (Exception sqlx) {
                        throw sqlx;
                    }

                    //Register Model new instanceID
                    String instance_id_sql = "INSERT INTO `model`.`model_instance` (`model_instance_id`, `model_id`, `model_instance_extra_info`) VALUES (?,?,?)";
                    try (PreparedStatement instanceIdStatement = locked_data_model_connection.prepareStatement(instance_id_sql)) {
                        instanceIdStatement.setInt(1, instanceID);
                        instanceIdStatement.setInt(2, dataModelId);
                        instanceIdStatement.setString(3, "model_instance_extra_info");
                        int modelInstanceIdRows = instanceIdStatement.executeUpdate();
                        if (modelInstanceIdRows > 0) {
                            //Save Model Sequence State
                            ArrayList<String> sequenceInserts = new ArrayList<String>();
                            HashMap<Class, Integer> sequenceNumberState = sequenceNumber.getSequenceState();
                            Iterator<Class> iterator = sequenceNumberState.keySet().iterator();
                            StringBuilder inserts = new StringBuilder();
                            while (iterator.hasNext()) {
                                Class key = iterator.next();
                                Integer sequenceStateValue = sequenceNumberState.get(key);
                                inserts.setLength(0);
                                inserts.append("INSERT INTO `model`.`model_sequence`(`model_id`, `model_instance_id`,`model_class_path`,`model_instance_sequence_last_value`)VALUES(")
                                        .append(dataModelId).append(",")
                                        .append(instanceID).append(",")
                                        .append("'").append(key.getCanonicalName()).append("',")
                                        .append(sequenceStateValue).append(") ON DUPLICATE KEY UPDATE `model_instance_sequence_last_value`=").append(sequenceStateValue);
                                sequenceInserts.add(inserts.toString());
                            }
                            try (Statement sequenceStatement = locked_data_model_connection.createStatement()) {
                                //////StringBuilder bb = new StringBuilder();
                                appendable.append("Add Inert Batch").append("\n");
                                for (int i = 0; i < sequenceInserts.size(); i++) {
                                    String batch = sequenceInserts.get(i);
                                    appendable.append(batch).append("\n");
                                    sequenceStatement.addBatch(batch);
                                    //////bb.append(inserts.get(i)).append(";\n");
                                    ////System.out.println(sequenceInserts.get(i) + ";");
                                }
                                int[] sequenceRows = sequenceStatement.executeBatch();
                                if (sequenceRows != null && sequenceRows.length > 0) {
                                    //Save ModelInstance
                                    ArrayList<String> modelInserts = new ArrayList<String>();
                                    data_instance.saveToDatabase(dataModelId, this.data_lookup, instanceID, data_instance, modelInserts, database_field_open_quote, database_field_close_quote);
                                    int modelRows = 0;
                                    try (Connection unlocked_data_model_connection = model_jdbc_source.getConnection(false)) {
                                        try (Statement modelStatement = unlocked_data_model_connection.createStatement()) {
                                            for (int i = 0; i < modelInserts.size(); i++) {
                                                appendable.append("\n*sql*\n").append(modelInserts.get(i)).append("\n");
                                                //debug never delete
                                                try {
                                                    modelRows++;
                                                    modelStatement.executeUpdate(modelInserts.get(i));
                                                    /*modelStatement.addBatch(modelInserts.get(i));
                                                    if (i % 100 == 0) {
                                                        modelRows += modelStatement.executeBatch().length;
                                                    }*/
                                                } catch (Exception ex) {
                                                    throw new Exception("Crashed on Insert Statement \n" + modelInserts.get(i), ex);
                                                }
                                                
                                            }
                                            if (modelRows != modelInserts.size()) {
                                                modelRows += modelStatement.executeBatch().length;
                                            }
                                            if (modelRows != modelInserts.size()) {
                                                throw new Exception("Inserted Rows = "+modelRows+" while Total Rows = " + modelInserts.size());
                                                ////System.out.println("Success: records inserted = " + modelRows.length);// + "\n" + b.toString());
                                            }
                                            
                                            if (unlocked_data_model_connection.isClosed() == false && unlocked_data_model_connection.getAutoCommit() == false) {
                                                unlocked_data_model_connection.commit();
                                            }
                                            modelStatement.close();
                                        } catch (Exception sqlx) {
                                            appendable.append("----------- insert instance data sql statments crashed -----------").append("\n");
                                            if (unlocked_data_model_connection.isClosed() == false && unlocked_data_model_connection.getAutoCommit() == false) {
                                                unlocked_data_model_connection.rollback();
                                            }
                                            throw sqlx;
                                        }
                                    } catch (Exception sqlx) {
                                        throw sqlx;
                                    }
                                }
                                sequenceStatement.close();
                            } catch (Exception sqlx) {
                                if (locked_data_model_connection.isClosed() == false && locked_data_model_connection.getAutoCommit() == false) {
                                    locked_data_model_connection.rollback();
                                }
                                throw sqlx;
                            }
                            data_model_resultset.close();
                            data_stmt.close();
                            //Cache
                            data_instance.changeState(DataInstance.State.LOADED, true);
                            dataModelDataInstanceMap.put(data_model, data_instance);
                        }
                        if (locked_data_model_connection.isClosed() == false && locked_data_model_connection.getAutoCommit() == false) {
                            locked_data_model_connection.commit();
                        }
                        instanceIdStatement.close();
                    } catch (Exception sqlx) {
                        if (locked_data_model_connection.isClosed() == false && locked_data_model_connection.getAutoCommit() == false) {
                            locked_data_model_connection.rollback();
                        }
                        throw sqlx;
                    }
                } catch (Exception sqlx) {
                    throw sqlx;
                }
            } catch (Exception sqlx) {
                //throw exception = sqlx;
                exception = sqlx;
            }
            try (Statement lockStatement = locked_data_model_connection.createStatement()) {
                String unlockTables = "UNLOCK TABLES";
                Boolean execute_result = lockStatement.execute(unlockTables);
            } catch (Exception sqlx) {
                exception = sqlx;
            }
            if (exception != null) {
                throw exception;
            }
        } catch (Exception sqlx) {
            throw sqlx;
        }
        
        return data_model;
    }
    
    @SuppressWarnings("unchecked")
    public DataModel<Model> loadModelFromDatabase(Integer model_id, Integer model_instance_id, DataClass.LoadMethod loadMethod) throws Exception {
        String select_model_sql = "SELECT `model`.`model_id` AS `model_id`, `model_instance_sequence_type_id`, `model_instance_sequence_last_value`, `sequence_type`.`sequence_type_name`, `sequence_type`.`sequence_type`, `sequence_type`.`sequence_type_ordered_chars`, `sequence_type`.`sequence_type_chars_width`, `sequence_type`.`sequence_type_padding_char`, `sequence_type`.`sequence_type_rewind`, `sequence_type`.`sequence_type_initial_value`, `sequence_type`.`sequence_type_increment_value` FROM `model`.`model` INNER JOIN `model`.`sequence_type` ON `model`.`model_instance_sequence_type_id`=`sequence_type`.`sequence_type_id` WHERE `model`.`model_id`=?";
        SequenceNumber sequenceNumber = null;
        try (Connection data_model_connection = model_jdbc_source.getConnection(false)) {
            try (PreparedStatement data_stmt = data_model_connection.prepareStatement(select_model_sql)) {
                data_stmt.setInt(1, model_id);
                try (ResultSet data_model_resultset = data_stmt.executeQuery()) {
                    if (data_model_resultset.next() == false) {
                        throw new Exception("DataModel '" + data_model_class.getCanonicalName() + "' is not defined in Data.DataModel Table");
                    }
                    String sequence_type = data_model_resultset.getString("sequence_type");
                    Integer sequence_initial_value = data_model_resultset.getInt("sequence_type_initial_value");
                    Integer sequence_increment_value = data_model_resultset.getInt("sequence_type_increment_value");

                    if (sequence_type.equalsIgnoreCase("Integer") == false) {
                        throw new Exception("Sequence Type must be 'Integer' type");
                    }

                    sequenceNumber = new SequenceNumber(sequence_initial_value, sequence_increment_value, false);

                    //Initialize Model Sequence state
                    String select_model_sequences_sql = "SELECT `model_class_path`, `model_instance_sequence_last_value` FROM `model`.`model_sequence` WHERE `model_id`=? AND `model_instance_id`=?";
                    try (PreparedStatement modelSequenceStatement = data_model_connection.prepareStatement(select_model_sequences_sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                        modelSequenceStatement.setInt(1, model_id);
                        modelSequenceStatement.setInt(2, model_instance_id);
                        try (ResultSet sequenceResultset = modelSequenceStatement.executeQuery()) {
                            while (sequenceResultset.next() == true) {
                                String classPath = sequenceResultset.getString("model_class_path");
                                Integer value = Integer.parseInt(sequenceResultset.getString("model_instance_sequence_last_value"));
                                sequenceNumber.initSequence(Class.forName(classPath), value);
                            }
                        } catch (Exception sqlx) {
                            throw sqlx;
                        }
                        modelSequenceStatement.close();
                    } catch (Exception sqlx) {
                        throw sqlx;
                    }
                } catch (Exception sqlx) {
                    throw sqlx;
                }
                ArrayList<String> selects = new ArrayList<String>();
                if (sequenceNumber == null) {
                    throw new Exception("Sequence is not initialized!");
                }
                
                //try (Connection data_model_connection = data_model_source.getConnection(false)) {
                    Object instanceObject = this.data_class.loadFromDatabase(data_model_connection, model_id, model_jdbc_source.getDatabaseName(), this.data_lookup, model_instance_id, loadMethod, sequenceNumber, selects);
                    @SuppressWarnings("unchecked")
                    DataModel<Model> data_model = (DataModel<Model>) this.data_model_class.getConstructor(model_class, ModelDefinition.class).newInstance(null, model_definition);
                    Method methodGetDeclaredField = this.data_model_class.getDeclaredMethod("getDeclaredField");
                    Field modelDeclaredField = (Field) methodGetDeclaredField.invoke(this.data_model);
                    modelDeclaredField.setAccessible(true);
                    modelDeclaredField.set(data_model, instanceObject);
                    data_model.prepareInstance();
                    //Class runtimeModelClass = modelDeclaredField.getType();

                    return data_model;
                //} catch (Exception sqlx) {
                //    throw sqlx;
                //}
            } catch (Exception sqlx) {
                throw sqlx;
            }
        } catch (Exception sqlx) {
            throw sqlx;
        }
    }
    
    public Boolean isDataModelInstanceRegistered(DataModel<Model> data_model) throws Exception {
        if (data_model == null) {
            throw new Exception("DataModel is null");
        }
        if (data_model.getInstance() == null) {
            throw new Exception("DataModel is null");
        }
        if (data_model.getInstance().getClass().getCanonicalName().equals(this.data_class.clas.getCanonicalName())) {
            throw new Exception("DataProcessor handles DataModel of type '" + data_class.clas.getCanonicalName() + "' while passed DataModel is of type '" + data_model.getInstance().getClass().getCanonicalName() + "'");
        }
        return dataModelDataInstanceMap.containsValue(data_model);
    }
    
    /**Update DataInstance with new/deleted InstanceObjects */
    public DataModel<Model> updateDataModelInstance(DataModel<Model> data_model) throws Exception {
        
        if (data_model == null) {
            throw new Exception("DataModel is null");
        }
        if (isDataModelInstanceRegistered(data_model)== false) {
            throw new Exception("DataModel '" + this.data_class.package_name + "' is not registered yer , first call either registerDataModelInstance or loadDataModelFromDatabase ");
        }
        /*DataInstance data_instance = dataModelDataInstanceMap.get(data_model);
        ArrayList<String> selects = new ArrayList<String>();
        Object instanceObject = data_instance.update(this.dataLookup, instanceID, loadMethod, selects);
        
        DataModel<Model> data_model = (DataModel<Model>) this.data_model_class.getConstructor().newInstance();
        Method methodGetDeclaredField = this.data_model_class.getDeclaredMethod("getDeclaredField");
        Field modelDeclaredField = (Field) methodGetDeclaredField.invoke(this.data_model);
        modelDeclaredField.setAccessible(true);
        modelDeclaredField.set(data_model, instanceObject);*/
        
        throw new Exception("Method not completed yet");
    }
}
