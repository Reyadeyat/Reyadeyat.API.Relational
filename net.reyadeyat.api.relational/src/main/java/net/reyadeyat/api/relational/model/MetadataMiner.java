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

package net.reyadeyat.api.relational.model;

import net.reyadeyat.api.relational.data.DataClass;
import net.reyadeyat.api.relational.data.DataLookup;
import net.reyadeyat.api.relational.data.DataModel;
import net.reyadeyat.api.relational.data.DataProcessor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.stream.JsonWriter;
import net.reyadeyat.api.relational.data.ModelDefinition;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import net.reyadeyat.api.library.jdbc.JDBCSource;
import net.reyadeyat.api.library.json.JsonResultset;
import net.reyadeyat.api.library.json.JsonZonedDateTimeAdapter;

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
public class MetadataMiner {
    private Integer model_id;
    private String model_name;
    private String model_description;
    private DataLookup data_lookup;
    private String java_package_name;
    private JDBCSource model_jdbc_source;
    private JDBCSource data_jdbc_source;
    private ModelDefinition model_definition;
    private String model_secret_key;
    private Map<String, Class> interface_implementation;
    private List<String> table_list;
    private Boolean foreing_key_must_link_to_primary_key;
    
    private static String nl = "\n";
    private static String section_separator = "\n-------------------------------------------------------------------------------\n";
    private static String data_separator = "  -------------------------  ";

    public MetadataMiner(Integer model_id, String java_package_name, JDBCSource model_jdbc_source, JDBCSource data_jdbc_source, List<String> table_list, ModelDefinition model_definition, String model_secret_key, Map<String, Class> interface_implementation, Boolean foreing_key_must_link_to_primary_key) throws Exception {
        this.model_id = model_id == null ? -1 : model_id;
        this.java_package_name = java_package_name;
        this.model_jdbc_source = model_jdbc_source;
        this.data_jdbc_source = data_jdbc_source;
        this.table_list = table_list;
        this.model_definition = model_definition;
        this.model_secret_key = model_secret_key;
        this.interface_implementation = interface_implementation;
        this.foreing_key_must_link_to_primary_key = foreing_key_must_link_to_primary_key;
    }
    
    public Integer generateModel(PrintWriter writer, JsonArray generating_time_elements, TableInterfaceImplementationDataStructures table_interface_implementation_data_structures) throws Exception {
        ArrayList<String> models = new ArrayList<String>(Arrays.asList(new String[]{data_jdbc_source.getDatabaseName()}));
        long t1, t2;
        Boolean write_output = true;
        if (writer == null) {
            writer = new PrintWriter(PrintWriter.nullWriter());
            write_output = false;
        }
        try (Connection model_connection = model_jdbc_source.getConnection(false)) {
            try (Statement st = model_connection.createStatement()) {
                String sql = "SELECT `enum_name`, `enum_element_id`, `enum_element_code`, `enum_element_java_datatype`, `enum_element_typescript_datatype` FROM `model`.`lookup_enum` INNER JOIN `model`.`lookup_enum_element` ON `lookup_enum`.`enum_id` = `lookup_enum_element`.`enum_id` WHERE `lookup_enum`.`enum_name`='"+model_definition.model_data_lookup_category+"' ORDER BY `enum_name`, `enum_element_code`";
                try (ResultSet rs = st.executeQuery(sql)) {
                    data_lookup = new DataLookup(rs, model_definition.model_data_lookup_category, "enum_name", "enum_element_id", "enum_element_code", "enum_element_java_datatype", "enum_element_typescript_datatype");
                    rs.close();
                } catch (Exception ex) {
                    throw ex;
                }
                st.close();
            } catch (Exception ex) {
                throw ex;
            }
        } catch (Exception ex) {
            throw ex;
        }
        
        DatabaseMetaData databaseMetaData = null;
        String databaseEngine = null;
        String databaseURL = null;
        Boolean case_sensitive_sql = true;
        String source_url = null;
        String model_url = null;
        Enterprise model_enterprise = null;
        try (Connection data_database_connection = data_jdbc_source.getConnection(false)) {
            t1 = System.nanoTime();
            databaseMetaData = data_database_connection.getMetaData();
            databaseEngine = databaseMetaData.getDatabaseProductName();
            databaseURL = databaseMetaData.getURL();
            source_url = databaseMetaData.getURL();
            model_url = databaseMetaData.getURL();
            if (databaseEngine.toLowerCase().contains("mysql")) {
                data_database_connection.createStatement().execute("USE " + data_jdbc_source.getDatabaseOpenQuote() + data_jdbc_source.getDatabaseName() + data_jdbc_source.getDatabaseCloseQuote());
                databaseEngine = "mysql";
            } else if (databaseEngine.toLowerCase().contains("sql server")) {
                data_database_connection.createStatement().execute("USE " + data_jdbc_source.getDatabaseOpenQuote() + data_jdbc_source.getDatabaseName() + data_jdbc_source.getDatabaseCloseQuote());
                databaseEngine = "sql server";
            } else if (databaseEngine.toLowerCase().contains("informix")) {
                data_database_connection.createStatement().execute("DATABASE " + data_jdbc_source.getDatabaseOpenQuote() + data_jdbc_source.getDatabaseName() + data_jdbc_source.getDatabaseCloseQuote());
                databaseEngine = "informix";
            } else {
                throw new Exception("Database '"+databaseEngine+"' is not implemented yet");
            }
        
            model_enterprise = new Enterprise(data_jdbc_source.getDatabaseName(), databaseEngine, databaseURL, case_sensitive_sql);

            ResultSet dbrs = databaseMetaData.getCatalogs();
            ResultSetMetaData dbrsmd = dbrs.getMetaData();
            int dbfeilds_count = dbrsmd.getColumnCount();
            //extract record field_list
            while (dbrs.next()) {
                //analyze record field_list
                /*for (int i = 1; i <= dbfeilds_count; i++) {
                    writer.appendln("Field [" + i + ", " + dbrsmd.getColumnName(i) + ", " + dbrsmd.getColumnLabel(i) + "] [" + dbrs.getString(i) + "]-{" + dbrsmd.getColumnTypeName(i) + "-" + dbrsmd.getColumnDisplaySize(i) + "-(" + dbrsmd.getPrecision(i) + "," + dbrsmd.getScale(i) + ")}-" + dbrsmd.getColumnClassName(i) + "}");
                }*/
                String dbName = dbrs.getString("TABLE_CAT");//Catalogue
                for (String model : models) {
                    if ((case_sensitive_sql == true && model.equals(dbName) == true)
                            || (case_sensitive_sql == false && model.equalsIgnoreCase(dbName) == true)){
                        Database tModelDatabase = new Database(dbName, databaseEngine, case_sensitive_sql, java_package_name);
                        model_enterprise.addDatabase(tModelDatabase);
                    }
                }
            }
            dbrs.close();

            for (Database tModelDatabase : model_enterprise.database_list) {
                //writer.appendln("Database [" + modelDatabaseName + "]");
                ResultSetMetaData tbrsmd;
                int tbfeilds_count = 0;
                //Extract Tables
                ResultSet tbrs = databaseMetaData.getTables(tModelDatabase.name, null, null, new String[]{"TABLE"});
                tbrsmd = tbrs.getMetaData();
                tbfeilds_count = tbrsmd.getColumnCount();
                //extract record field_list
                while (tbrs.next()) {
                    //analyze record field_list
                    /*for (int i = 1; i <= tbfeilds_count; i++) {
                        writer.appendln("Field [" + i + ", " + tbrsmd.getColumnName(i) + ", " + tbrsmd.getColumnLabel(i) + "] [" + tbrs.getString(i) + "]-{" + tbrsmd.getColumnTypeName(i) + "-" + tbrsmd.getColumnDisplaySize(i) + "-(" + tbrsmd.getPrecision(i) + "," + tbrsmd.getScale(i) + ")}-" + tbrsmd.getColumnClassName(i) + "}");
                    }*/
                    String tableName = tbrs.getString("TABLE_NAME");
                    String tableSchem = tbrs.getString("TABLE_SCHEM");
                    
                    if (table_list != null && table_list.size() > 0 && table_list.contains(tableName) == false) {
                        continue;//ignore
                    }
                    
                    if (data_jdbc_source.getDatabaseSchema() != null && tableSchem != null && data_jdbc_source.getDatabaseSchema().length() > 0 && tableSchem.equalsIgnoreCase(data_jdbc_source.getDatabaseSchema()) == false) {
                        continue;
                    }
                    /*if (tableName.equalsIgnoreCase("sysdiagrams")) {
                        String x = tbrs.getString("TABLE_NAME");
                        x = tbrs.getString(tbrsmd.getColumnName(1));
                        x = tbrs.getString(tbrsmd.getColumnName(2));
                        x = tbrs.getString(tbrsmd.getColumnName(3));
                        x = tbrs.getString(tbrsmd.getColumnName(4));
                        x = tbrs.getString(tbrsmd.getColumnName(5));
                        x = x;
                    }*/
                    Integer rows = 0;
                    /*try (Statement st = model_database_connection.createStatement()) {
                        String sql = null;
                        if (databaseEngine.equalsIgnoreCase("informix")) {
                            sql = "SELECT count(*) as " + model_database_field_open_quote + "rows_count" + model_database_field_close_quote + " FROM " + model_database_field_open_quote + tableName + model_database_field_close_quote;
                        } else {
                            sql = "SELECT count(*) as " + model_database_field_open_quote + "rows_count" + model_database_field_close_quote + " FROM " + model_database_field_open_quote + model_database + model_database_field_close_quote + "." + model_database_field_open_quote + (model_database_schem.isEmpty() ? "" : model_database_schem + model_database_field_close_quote + "." + model_database_field_open_quote) + tableName + model_database_field_close_quote;
                        }
                        try (ResultSet rs = st.executeQuery(sql)) {
                            rs.next();
                            rows = rs.getInt(1);
                            rows = rows == null ? 0 : rows;
                            rs.close();
                        } catch (Exception ex) {
                            throw ex;
                        }
                        st.close();
                    } catch (Exception ex) {
                        throw ex;
                    }*/
                    Table table = new Table(tableName, case_sensitive_sql, rows, data_lookup, table_interface_implementation_data_structures);
                    tModelDatabase.addTable(table);
                    //writer.appendln("Table [" + tableName + "]");
                }
                tbrs.close();
            }
        

            for (Database tModelDatabase : model_enterprise.database_list) {
                for (Table table : tModelDatabase.table_list) {
                    //Exract Table Fields
                    ResultSet rs = databaseMetaData.getColumns(tModelDatabase.name, null, table.name, null);
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int feilds_count = rsmd.getColumnCount();
                    //extract record field_list
                    while (rs.next()) {
                        //analyze record field_list
                        /*for (int i = 1; i <= feilds_count; i++) {
                            writer.appendln("Field [" + i + ", " + rsmd.getColumnName(i) + ", " + rsmd.getColumnLabel(i) + "] [" + rs.getString(i) + "]-{" + rsmd.getColumnTypeName(i) + "-" + rsmd.getColumnDisplaySize(i) + "-(" + rsmd.getPrecision(i) + "," + rsmd.getScale(i) + ")}-" + rsmd.getColumnClassName(i) + "}");
                        }*/
                        String name = rs.getString("COLUMN_NAME");
                        String dataTypeName = rs.getString("TYPE_NAME");
                        String dataTypeCode = rs.getString("DATA_TYPE");
                        Boolean nullable = rs.getString("IS_NULLABLE").equalsIgnoreCase("YES") ? true : false;
                        Boolean autoIncrment = rs.getString("IS_AUTOINCREMENT").equalsIgnoreCase("YES") ? true : false;
                        String defaultValue = rs.getString("COLUMN_DEF");
                        Integer list_order = rs.getInt("ORDINAL_POSITION");
                        Integer size = rs.getInt("COLUMN_SIZE");
                        Integer decimalDigits = rs.getObject("DECIMAL_DIGITS") == null ? 0 : rs.getInt("DECIMAL_DIGITS");
                        /*if (dataTypeName.equalsIgnoreCase("sysname")) {
                            dataTypeCode = dataTypeCode;
                        }*/
                        Field modelField = new Field(table, name, dataTypeName, dataTypeCode, nullable, autoIncrment, defaultValue, list_order, size, decimalDigits, case_sensitive_sql, data_lookup);
                        table.addField(modelField);
                    }
                    rs.close();
                }
            }

            for (Database tModelDatabase : model_enterprise.database_list) {
                for (Table table : tModelDatabase.table_list) {
                    //Extract Primary keys
                    ResultSet rs = databaseMetaData.getPrimaryKeys(tModelDatabase.name, null, table.name);
                    //analyze record field_list
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int feilds_count = rsmd.getColumnCount();
                    /*for (int i = 1; i <= feilds_count; i++) {
                        writer.appendln(rsmd.getColumnName(i) + "-" + rsmd.getColumnLabel(i) + " {" + rsmd.getColumnTypeName(i) + " - {" + rsmd.getColumnDisplaySize(i) + " (" + rsmd.getPrecision(i) + "," + rsmd.getScale(i) + ")} - " + rsmd.getColumnClassName(i) + "}");
                    }*/
                    String currentPrimaryKeyName = "";
                    PrimaryKey modelPrimaryKey = null;
                    //extract record field_list
                    while (rs.next()) {

                        String primaryKeyName = rs.getString("PK_NAME");
                        //writer.appendln("------------TABLE " + tableName + " PRIMARY KEY " + primaryKeyName + "-------------");
                        if (modelPrimaryKey == null || 
                                (case_sensitive_sql == false && primaryKeyName.equals(currentPrimaryKeyName) == false)
                                || (case_sensitive_sql == true && primaryKeyName.equalsIgnoreCase(currentPrimaryKeyName) == false)){
                            modelPrimaryKey = new PrimaryKey(table, primaryKeyName, case_sensitive_sql);
                            //table.addPrimaryKey(modelPrimaryKey);
                        }
                        //writer.appendln(PK.getString("COLUMN_NAME") + "===" + PK.getString("PK_NAME"));
                        /*for (int i = 1; i <= feilds_count; i++) {
                            writer.appendln(rsmd.getColumnName(i) + "-" + rsmd.getColumnLabel(i) + " {" + rsmd.getColumnTypeName(i) + " [" + rs.getString(i) + "] - {" + rsmd.getColumnDisplaySize(i) + " (" + rsmd.getPrecision(i) + "," + rsmd.getScale(i) + ")} - " + rsmd.getColumnClassName(i) + "}");
                        }*/
                        PrimaryKeyField primary_key_field = new PrimaryKeyField(rs.getString("COLUMN_NAME"), case_sensitive_sql);
                        modelPrimaryKey.addField(primary_key_field);
                        currentPrimaryKeyName = primaryKeyName;
                    }
                    rs.close();
                    if (modelPrimaryKey == null) {
                        continue;
                    }
                    table.addPrimaryKey(modelPrimaryKey);
                }
            }

            for (Database tModelDatabase : model_enterprise.database_list) {
                for (Table table : tModelDatabase.table_list) {
                    //Extract Foreign keys
                    ResultSet rs = databaseMetaData.getImportedKeys(tModelDatabase.name, null, table.name);
                    //analyze record field_list
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int feilds_count = rsmd.getColumnCount();
                    /*for (int i = 1; i <= feilds_count; i++) {
                        writer.appendln(rsmd.getColumnName(i) + "-" + rsmd.getColumnLabel(i) + " {" + rsmd.getColumnTypeName(i) + " - {" + rsmd.getColumnDisplaySize(i) + " (" + rsmd.getPrecision(i) + "," + rsmd.getScale(i) + ")} - " + rsmd.getColumnClassName(i) + "}");
                    }*/
                    String currentForeignKeyName = "";
                    ForeignKey modelForeignKey = null;
                    //extract record field_list
                    while (rs.next()) {
                        String foreignKeyName = rs.getString("FK_NAME");
                        //writer.appendln("------------TABLE " + tableName + " FOREIGN KEY " + foreignKeyName + "-------------");
                        //Validate Foreign Key Tables
                        String table_name = rs.getString("FKTABLE_NAME");
                        String referenced_key_table_name = rs.getString("PKTABLE_NAME");
                        Table parentTable = tModelDatabase.table_list.stream().filter(o -> o.name.equals(referenced_key_table_name)).findAny().orElse(null);
                        if (parentTable == null) {
                            writer.append("------------Warning - Foreign Key inconsitency detected - referenced table '" + referenced_key_table_name + "' for FOREIGN KEY " + foreignKeyName + " in table '" + table_name + "'-------------").append("\n");
                            continue;
                        }
                        try {
                            if (modelForeignKey == null || 
                                    (case_sensitive_sql == true && foreignKeyName.equals(currentForeignKeyName) == false)
                                    || (case_sensitive_sql == false && foreignKeyName.equalsIgnoreCase(currentForeignKeyName) == false)) {
                                modelForeignKey = new ForeignKey(table, rs.getString("FK_NAME"),
                                        rs.getString("PK_NAME"),
                                        rs.getString("PKTABLE_NAME"),
                                        rs.getString("FKTABLE_NAME"),
                                        rs.getInt("KEY_SEQ"),
                                        rs.getString("UPDATE_RULE"),
                                        rs.getString("DELETE_RULE"),
                                        rs.getString("DEFERRABILITY"),
                                        case_sensitive_sql);
                                table.addForeignKey(modelForeignKey);
                            }
                            String foreign_key_referenced_field_name = rs.getString("FKCOLUMN_NAME");
                            Boolean is_primary_key_field = parentTable.isFieldPrimaryKey(foreign_key_referenced_field_name);
                            ReferencedKeyField referenced_key_field = new ReferencedKeyField(foreign_key_referenced_field_name, case_sensitive_sql, is_primary_key_field);
                            ForeignKeyField foreignKeyField = new ForeignKeyField(rs.getString("PKCOLUMN_NAME"), case_sensitive_sql);
                            if (foreignKeyName.equalsIgnoreCase("pur_tender_analysis_fin_items_spec_pur_tender_items_financial_fk")) {
                                foreignKeyName = foreignKeyName;
                            }
                            modelForeignKey.addForeignFieldReferencedField(foreignKeyField, referenced_key_field);
                            //modelForeignKey.addReferencedField(referenced_key_field);
                            //modelForeignKey.addForeignField(foreignKeyField);

                            /*for (int i = 1; i <= feilds_count; i++) {
                                writer.appendln(rsmd.getColumnName(i) + "-" + rsmd.getColumnLabel(i) + " {" + rsmd.getColumnTypeName(i) + " - {" + rsmd.getColumnDisplaySize(i) + " (" + rsmd.getPrecision(i) + "," + rsmd.getScale(i) + ")} - " + rsmd.getColumnClassName(i) + "}");
                            }*/
                            currentForeignKeyName = foreignKeyName;
                        } catch (Exception ex) {
                            throw ex;
                        }
                    }
                    rs.close();
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
        t2 = System.nanoTime();
        generating_time_elements.add("01- Analyze Database Schema Metadata = " + TimeUnit.MILLISECONDS.convert(t2 - t1, TimeUnit.NANOSECONDS) + " ms");
        
        /*for (Database model_database : model_enterprise.database_list) {
            model_database.extractTableLogic();
        }*/
        
        model_enterprise.toString(writer, 0, 4);
        writer.flush();
        
        t1 = System.nanoTime();
        for (Database database : model_enterprise.database_list) {
            int level = 0, shift = 4;
            database.extractTableLogic(true);
        }
        if (write_output == true) {
            writer.append("\n");
            for (Database database : model_enterprise.database_list) {
                int level = 0, shift = 4;
                database.extractTableLogic(true);
                for (Table table : database.table_list) {
                    writer.append("Table [");
                    writer.append(table.name);
                    writer.append("][Parent Paths]\n");
                    for (int i = 0; i < table.parent_path_list.size(); i++) {
                        ArrayList<Table> tablesPath = table.parent_path_list.get(i);
                        for (int x = 0; x < level * shift; x++) {
                            writer.append(" ");
                        }
                        writer.append("|");
                        for (int x = 0; x < shift - 1; x++) {
                            writer.append(".");
                        }
                        for (int x=0; x < tablesPath.size(); x++) {
                            Table tt = tablesPath.get(x);
                            writer.append(tt.name);
                            writer.append(".");
                        }
                        if (tablesPath.size() > 0) {
                            //b.delete(b.length() - 1, b.length());
                        }
                        writer.append("\n");
                    }
                    writer.append("Table [");writer.append(table.name);writer.append("][All Paths]\n");
                    for (int i = 0; i < table.path_list.size(); i++) {
                        ArrayList<Table> tablesPath = table.path_list.get(i);
                        for (int x = 0; x < level * shift; x++) {
                            writer.append(" ");
                        }
                        writer.append("|");
                        for (int x = 0; x < shift - 1; x++) {
                            writer.append(".");
                        }
                        for (int x=0; x < tablesPath.size(); x++) {
                            Table tt = tablesPath.get(x);
                            writer.append(tt.name);writer.append(".");
                        }
                        if (tablesPath.size() > 0) {
                            //b.delete(b.length() - 1, b.length());
                        }
                        writer.append("\n");
                    }
                    if (table.cyclic_reference_paths.size() > 0) {
                        writer.append("Table [");writer.append(table.name);writer.append("][Cyclic Reference Paths]\n");
                        for (int i = 0; i < table.cyclic_reference_paths.size(); i++) {
                            ArrayList<Table> tablesPath = table.cyclic_reference_paths.get(i);
                            for (int x = 0; x < level * shift; x++) {
                                writer.append(" ");
                            }
                            writer.append("|");
                            for (int x = 0; x < shift - 1; x++) {
                                writer.append(".");
                            }
                            for (int x=0; x < tablesPath.size(); x++) {
                                Table tt = tablesPath.get(x);
                                writer.append(tt.name);writer.append(".");
                            }
                            if (tablesPath.size() > 0) {
                                //b.delete(b.length() - 1, b.length());
                            }
                            writer.append("\n");
                        }
                    }
                    writer.append("Table [");writer.append(table.name);writer.append("][All Paths Inner Joined]\n");
                    for (int i = 0; i < table.path_list.size(); i++) {
                        /*save model in InstanceId that acts as versionID
                        Alter Database to memic versionID
                        or
                        Create new Database to memic versionID*/

                        ArrayList<Table> tablesPath = table.path_list.get(i);
                        String pathString = database.pathToString(tablesPath);
                        writer.append(pathString);
                        writer.append("\n");
                        ArrayList<Table> foundTablesPath = database.findPath(pathString);
                        String foundTablesPathString = database.pathToString(foundTablesPath);
                        writer.append(foundTablesPathString);
                        writer.append("\n");
                        String datasetJSON = database.getInnerJoinedSelect(foundTablesPathString, data_jdbc_source.getDatabaseSchema(), data_jdbc_source.getDatabaseOpenQuote(), data_jdbc_source.getDatabaseCloseQuote()).toString();
                        /*create the path
                        search table_list for this path
                        if path not found throw exception
                        if found send it to getPathInnerJoinSelect
                        static table.getPathInnerJoinedSelect();
                        returns the select statement sql string
                                Outer Map with table name and unique table name for cyclic table links
                                Inner Map between field names and unique aliases generated for this sql.
                                select `table`.`field` AS `unique alias`
                                map.put(`table`.`field`, unique alias)*/
                        writer.append(datasetJSON);
                        writer.append("\n");
                        writer.append("\n");
                    }
                }
            }
        }
        t2 = System.nanoTime();
        generating_time_elements.add("02- Extract Schema Table Logic = " + TimeUnit.MILLISECONDS.convert(t2 - t1, TimeUnit.NANOSECONDS) + " ms");
        
        t1 = System.nanoTime();
        Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithModifiers(Modifier.TRANSIENT).create();
        gson.toJson(model_enterprise, Enterprise.class, new JsonWriter(writer));
        t2 = System.nanoTime();
        generating_time_elements.add("03- Printing Json To Writer = " + TimeUnit.MILLISECONDS.convert(t2 - t1, TimeUnit.NANOSECONDS) + " ms");
        
        /*Gson gsonN = new GsonBuilder().setPrettyPrinting().excludeFieldsWithModifiers(Modifier.TRANSIENT).create();
        String txt = gsonN.toJson(new DataType(), DataType.class);
        writer.appendln(txt);*/

        /*Gson gsonN = new GsonBuilder().setPrettyPrinting().excludeFieldsWithModifiers(Modifier.TRANSIENT).create();
        Enterprise enterprise = gsonN.fromJson(jsonText, Enterprise.class);
        enterprise.prepareInstance();

        Gson gsonX = new GsonBuilder().setPrettyPrinting().excludeFieldsWithModifiers(Modifier.TRANSIENT).create();
        String jsonTextX = gsonX.toJson(enterprise);
        writer.appendln(jsonTextX);
        
        //XML
        //String testZonedDateTime = billInqRqMessage.EFBPS.SignonRq.ClientDt.toString();
        Class<Enterprise> marshalClass = Enterprise.class;
        JAXBContext jaxbContext = JAXBContext.newInstance(marshalClass);
        
        //convert model instance to model xml
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        ByteArrayOutputStream jaxbOutputStream = new ByteArrayOutputStream();
        jaxbMarshaller.marshal(model_enterprise, jaxbOutputStream);
        String xmlEnterprise = new String(jaxbOutputStream.toByteArray(), StandardCharsets.UTF_8);
        Document enterpriseXMLDocument = Walker.createXMLDocumentFromText(new ByteArrayInputStream(xmlEnterprise.getBytes(StandardCharsets.UTF_8)));
        String xmlTextW = Walker.createTextFromXMLDocument(enterpriseXMLDocument);
        writer.appendln(xmlTextW);
        
        //convert model xml to model instance
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        Enterprise enterpriseInstance = (Enterprise) jaxbUnmarshaller.unmarshal(enterpriseXMLDocument);
        
        Gson gsonXml = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(ZonedDateTime.class, new JsonZonedDateTimeAdapter()).create();
        String jsonTextXml = gsonXml.toJson(enterpriseInstance);
        writer.appendln(jsonTextXml);
        */
        
        //Analysing Database model
        Integer instance_sequence_type_id = 1;
        String instance_sequence_last_value = "0";
        String model_name = data_jdbc_source.getDatabaseName() + " - Enterprise Model";
        String model_description = data_jdbc_source.getDatabaseName() + " - Database Enterprise Model";
        
        t1 = System.nanoTime();
        DataProcessor<Enterprise> dataProcessor = new DataProcessor<Enterprise>(EnterpriseModel.class, Enterprise.class, model_jdbc_source, model_definition, data_lookup, interface_implementation, foreing_key_must_link_to_primary_key);
        //SchemaClass dataClass = dataProcessor.getSchemaClass();
        //t2 = System.nanoTime();
        //generating_time_elements.add("Schema Class Walk = " + TimeUnit.MILLISECONDS.convert(t2 - t1, TimeUnit.NANOSECONDS) + " ms");
        
        /*
        t1 = System.nanoTime();
        model_id = dataProcessor.selectModelIdFromDatabase(data_source);
        t2 = System.nanoTime();
        generating_time_elements.add("Select Model ID [" + (model_id == -1 ? "New" : model_id) + "] From Database = " + TimeUnit.MILLISECONDS.convert(t2 - t1, TimeUnit.NANOSECONDS) + " ms");
        */
        
        t1 = System.nanoTime();
        
        model_id = dataProcessor.generateModel(model_jdbc_source, data_jdbc_source, model_id, instance_sequence_type_id, instance_sequence_last_value, model_secret_key, table_interface_implementation_data_structures.getClass().getName());
        t2 = System.nanoTime();
        generating_time_elements.add("04- Create Model ID [" + model_id + "] Data Class in Database = " + TimeUnit.MILLISECONDS.convert(t2 - t1, TimeUnit.NANOSECONDS) + " ms");
        
        EnterpriseModel<Enterprise> enterprise_model = new EnterpriseModel<Enterprise>(model_enterprise, model_definition);
        
        /*t1 = System.nanoTime();
        dataProcessor.addDataModel(enterprise_model);
        t2 = System.nanoTime();
        generating_time_elements.add("Schema Instance Walk = " + TimeUnit.MILLISECONDS.convert(t2 - t1, TimeUnit.NANOSECONDS) + " ms");*/
        
        //String toString = dataProcessor.toString();
        dataProcessor.toString(writer, enterprise_model);
        writer.flush();

        t1 = System.nanoTime();
        String data_mysql_database_field_open_quote = "`";
        String data_mysql_database_field_close_quote = "`";
        //String DataSqlServerDatabaseFieldOpenQuote = "[";
        //String DataSqlServerDatabaseFieldCloseQuote = "]";
        DataModel<Enterprise> savedModel = dataProcessor.saveModelToDatabase(writer, data_jdbc_source, enterprise_model, data_mysql_database_field_open_quote, data_mysql_database_field_close_quote);
        t2 = System.nanoTime();
        generating_time_elements.add("05- Save Schema To Database = " + TimeUnit.MILLISECONDS.convert(t2 - t1, TimeUnit.NANOSECONDS) + " ms");
        t1 = System.nanoTime();
        
        writer.append("Saved Model").append("\n");
        savedModel.getInstance().toString(writer);
        writer.flush();
        t2 = System.nanoTime();
        generating_time_elements.add("06- Saved Model Schema print out = " + TimeUnit.MILLISECONDS.convert(t2 - t1, TimeUnit.NANOSECONDS) + " ms");

        return model_id;
    }
    
    public void loadModel(JDBCSource model_jdbc_source, PrintWriter writer) throws Exception {
        long t1, t2;
        String timeText = "";
        writer.append("Loaded Models").append("\n");
        t1 = System.nanoTime();
        DataProcessor<Enterprise> dataProcessor = new DataProcessor<Enterprise>(EnterpriseModel.class, Enterprise.class, model_jdbc_source, model_definition, data_lookup, interface_implementation, foreing_key_must_link_to_primary_key);
        ArrayList<Integer> model_instance_ids = dataProcessor.selectModelInstanceIDsFromDatabase(model_definition.modeled_database_name);
        t2 = System.nanoTime();
        StringBuilder ids = new StringBuilder();
        for (Integer id : model_instance_ids) {
            ids.append(id).append(",");
        }
        ids = ids.length() == 0 ? ids : ids.delete(ids.length()-1, ids.length());
        timeText += "Select Model Instance IDs [" + ids.toString() + "] From Database = " + TimeUnit.MILLISECONDS.convert(t2 - t1, TimeUnit.NANOSECONDS) + " ms" + "\n";
        for (Integer model_instance_id : model_instance_ids) {
            t1 = System.nanoTime();
            //SchemaClass.LoadMethod loadMethod = SchemaClass.LoadMethod.JSON;
            DataClass.LoadMethod loadMethod = DataClass.LoadMethod.REFLECTION;
            EnterpriseModel<Enterprise> newEnterpriseModel = (EnterpriseModel<Enterprise>) dataProcessor.loadModelFromDatabase(model_id, model_instance_id, loadMethod);
            Enterprise loadedEnterprise = newEnterpriseModel.getInstance();
            t2 = System.nanoTime();
            timeText += "Load [" + model_name + "-" + model_definition.model_version + "-" + data_jdbc_source.getDatabaseName() + "] DataInstance [" + model_instance_id + "] From Database = " + ((t2 - t1)/1000000d) + "\n";
            t1 = System.nanoTime();
            
            writer.append("Loaded Model [");
            newEnterpriseModel.getInstance().toString(writer, 0, 4);
            writer.append("]\n");
            writer.flush();
            
            Gson gsonLoaded = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(ZonedDateTime.class, new JsonZonedDateTimeAdapter()).create();
            String jsonTextLoaded = gsonLoaded.toJson(loadedEnterprise);
            writer.append(jsonTextLoaded).append("\n");
        }

        writer.append("---------------- Time -------------------").append("\n");
        writer.append(timeText).append("\n");
    }

    Object createReportObject(Object object, Class<?> clss) throws Exception {
        Constructor<?> ctor = clss.getConstructor();
        return ctor.newInstance();
    }

    void setObjectProperty(Object object, String property, Object value) throws Exception {
        java.lang.reflect.Field declaredField = object.getClass().getDeclaredField(property);
        declaredField.setAccessible(true);
        declaredField.set(object, value);
    }
    
    public static void deleteDataModel(JDBCSource model_jdbc_source, ModelDefinition model_definition) throws Exception {
        try (Connection model_database_connection = model_jdbc_source.getConnection(false)) {
            String delete_sql;
            delete_sql = "DELETE FROM `model`.`referenced_key_field_list` WHERE `model_id`=?";
            deleteDataModel(model_database_connection, delete_sql, model_definition.model_id);
            delete_sql = "DELETE FROM `model`.`foreign_key_field_list` WHERE `model_id`=?";
            deleteDataModel(model_database_connection, delete_sql, model_definition.model_id);
            delete_sql = "DELETE FROM `model`.`foreign_key_list` WHERE `model_id`=?";
            deleteDataModel(model_database_connection, delete_sql, model_definition.model_id);
            delete_sql = "DELETE FROM `model`.`primary_key_field_list` WHERE `model_id`=?";
            deleteDataModel(model_database_connection, delete_sql, model_definition.model_id);
            delete_sql = "DELETE FROM `model`.`primary_key_list` WHERE `model_id`=?";
            deleteDataModel(model_database_connection, delete_sql, model_definition.model_id);
            delete_sql = "DELETE FROM `model`.`field_list` WHERE `model_id`=?";
            deleteDataModel(model_database_connection, delete_sql, model_definition.model_id);
            delete_sql = "DELETE FROM `model`.`child_table_list` WHERE `model_id`=?";
            deleteDataModel(model_database_connection, delete_sql, model_definition.model_id);

            //String table_interface_implementation_data_structures_name = model_definition.modeled_table_interface_implementation_data_structures_class.substring(model_definition.modeled_table_interface_implementation_data_structures_class.lastIndexOf(".")+1, model_definition.modeled_table_interface_implementation_data_structures_class.length()-1);
            String table_interface_implementation_data_structures_name = "table_interface_implementation_data_structures";
            delete_sql = "DELETE FROM `model`.`"+table_interface_implementation_data_structures_name+"` WHERE `model_id`=?";
            deleteDataModel(model_database_connection, delete_sql, model_definition.model_id);

            delete_sql = "DELETE FROM `model`.`table_list` WHERE `model_id`=?";
            deleteDataModel(model_database_connection, delete_sql, model_definition.model_id);
            delete_sql = "DELETE FROM `model`.`database_list` WHERE `model_id`=?";
            deleteDataModel(model_database_connection, delete_sql, model_definition.model_id);
            delete_sql = "DELETE FROM `model`.`enterprise` WHERE `model_id`=?";
            deleteDataModel(model_database_connection, delete_sql, model_definition.model_id);

            delete_sql = "DELETE FROM `model`.model_sequence WHERE `model_id`=?";
            deleteDataModel(model_database_connection, delete_sql, model_definition.model_id);
            delete_sql = "DELETE FROM `model`.model_instance WHERE `model_id`=?";
            deleteDataModel(model_database_connection, delete_sql, model_definition.model_id);

            delete_sql = "DELETE FROM `model`.`model` WHERE `model_id`=?";
            deleteDataModel(model_database_connection, delete_sql, model_definition.model_id);

            model_database_connection.commit();
        } catch (Exception ex) {
            throw ex;
        }
    }
    
    public static void deleteDataModelInstance(JDBCSource model_jdbc_source, ModelDefinition model_definition) throws Exception {
        try (Connection model_database_connection = model_jdbc_source.getConnection(false)) {
            String delete_sql;
            Integer instance_id = Integer.valueOf(model_definition.model_instance_sequence_last_value);
            delete_sql = "DELETE FROM `model`.`referenced_key_field_list` WHERE `model_id`=? AND `model_instance_id`=?";
            deleteDataModelInstance(model_database_connection, delete_sql, model_definition.model_id, instance_id);
            delete_sql = "DELETE FROM `model`.`foreign_key_field_list` WHERE `model_id`=? AND `model_instance_id`=?";
            deleteDataModelInstance(model_database_connection, delete_sql, model_definition.model_id, instance_id);
            delete_sql = "DELETE FROM `model`.`foreign_key_list` WHERE `model_id`=? AND `model_instance_id`=?";
            deleteDataModelInstance(model_database_connection, delete_sql, model_definition.model_id, instance_id);
            delete_sql = "DELETE FROM `model`.`primary_key_field_list` WHERE `model_id`=? AND `model_instance_id`=?";
            deleteDataModelInstance(model_database_connection, delete_sql, model_definition.model_id, instance_id);
            delete_sql = "DELETE FROM `model`.`primary_key_list` WHERE `model_id`=? AND `model_instance_id`=?";
            deleteDataModelInstance(model_database_connection, delete_sql, model_definition.model_id, instance_id);
            delete_sql = "DELETE FROM `model`.`field_list` WHERE `model_id`=? AND `model_instance_id`=?";
            deleteDataModelInstance(model_database_connection, delete_sql, model_definition.model_id, instance_id);
            delete_sql = "DELETE FROM `model`.`child_table_list` WHERE `model_id`=? AND `model_instance_id`=?";
            deleteDataModelInstance(model_database_connection, delete_sql, model_definition.model_id, instance_id);

            //String table_interface_implementation_data_structures_name = model_definition.modeled_table_interface_implementation_data_structures_class.substring(model_definition.modeled_table_interface_implementation_data_structures_class.lastIndexOf(".")+1, model_definition.modeled_table_interface_implementation_data_structures_class.length()-1);
            String table_interface_implementation_data_structures_name = "table_interface_implementation_data_structures";
            delete_sql = "DELETE FROM `model`.`"+table_interface_implementation_data_structures_name+"` WHERE `model_id`=? AND `model_instance_id`=?";
            deleteDataModelInstance(model_database_connection, delete_sql, model_definition.model_id, instance_id);

            delete_sql = "DELETE FROM `model`.`table_list` WHERE `model_id`=? AND `model_instance_id`=?";
            deleteDataModelInstance(model_database_connection, delete_sql, model_definition.model_id, instance_id);
            delete_sql = "DELETE FROM `model`.`database_list` WHERE `model_id`=? AND `model_instance_id`=?";
            deleteDataModelInstance(model_database_connection, delete_sql, model_definition.model_id, instance_id);
            delete_sql = "DELETE FROM `model`.`enterprise` WHERE `model_id`=? AND `model_instance_id`=?";
            deleteDataModelInstance(model_database_connection, delete_sql, model_definition.model_id, instance_id);

            delete_sql = "DELETE FROM `model`.model_sequence WHERE `model_id`=? AND `model_instance_id`=?";
            deleteDataModelInstance(model_database_connection, delete_sql, model_definition.model_id, instance_id);
            delete_sql = "DELETE FROM `model`.model_instance WHERE `model_id`=? AND `model_instance_id`=?";
            deleteDataModelInstance(model_database_connection, delete_sql, model_definition.model_id, instance_id);

            delete_sql = "DELETE FROM `model`.`model` WHERE `model_id`=? AND `model_instance_id`=?";
            deleteDataModelInstance(model_database_connection, delete_sql, model_definition.model_id, instance_id);

            model_database_connection.commit();
        } catch (Exception ex) {
            throw ex;
        }
    }
    
    public static void deleteDataModelInstance(Connection model_jdbc_source_connection, String delete_sql, Integer model_id, Integer model_instance_id) throws Exception {
        try ( PreparedStatement delete_stmt = model_jdbc_source_connection.prepareStatement(delete_sql)) {
            delete_stmt.setInt(1, model_id);
            delete_stmt.setInt(2, model_instance_id);
            delete_stmt.executeUpdate();
        } catch (Exception ex) {
            throw ex;
        }
    }
    
    public static void deleteDataModel(Connection model_jdbc_source_connection, String delete_sql, Integer model_id, Integer model_instance_id) throws Exception {
        try ( PreparedStatement delete_stmt = model_jdbc_source_connection.prepareStatement(delete_sql)) {
            delete_stmt.setInt(1, model_id);
            delete_stmt.setInt(2, model_instance_id);
            delete_stmt.executeUpdate();
        } catch (Exception ex) {
            throw ex;
        }
    }
    
    public static void deleteDataModel(Connection model_jdbc_source_connection, String delete_sql, Integer model_id) throws Exception {
        try ( PreparedStatement delete_stmt = model_jdbc_source_connection.prepareStatement(delete_sql)) {
            delete_stmt.setInt(1, model_id);
            delete_stmt.executeUpdate();
        } catch (Exception ex) {
            throw ex;
        }
    }
    
    public static void printModelDataStructures(JDBCSource model_jdbc_source, Integer model_id, Integer model_instance_id, PrintWriter writer, Integer print_styel) throws Exception {
        String select_model_instance = "SELECT `table_interface_implementation_data_structures`.* FROM `model`.`table_list` INNER JOIN `model`.`table_interface_implementation_data_structures` ON `table_list`.`model_id`=`table_interface_implementation_data_structures`.`model_id` AND `table_list`.`model_instance_id`=`table_interface_implementation_data_structures`.`model_instance_id` AND `table_list`.`child_id`=`table_interface_implementation_data_structures`.`parent_id` WHERE `table_list`.`model_id` = ? AND `table_list`.`model_instance_id` = ?";
        ArrayList<DataStructure> data_structure_list = null;
        try ( Connection model_database_connection = model_jdbc_source.getConnection(false)) {
            try ( PreparedStatement select_model_instance_stmt = model_database_connection.prepareStatement(select_model_instance)) {
                select_model_instance_stmt.setInt(1, model_id);
                select_model_instance_stmt.setInt(2, model_instance_id);
                ResultSet rs = select_model_instance_stmt.executeQuery();
                data_structure_list =  JsonResultset.resultset(rs, DataStructure.class);
            } catch (Exception ex) {
                throw ex;
            }
        } catch (Exception ex) {
            throw ex;
        }
        if (data_structure_list == null) {
            throw new Exception("Data Model id '"+model_id+"' instance '"+model_instance_id+"' is not exist!");
        }
        if (print_styel == 1) {
            StringBuilder database_servlet_uri = new StringBuilder();
            StringBuilder java_data_structure_class = new StringBuilder();
            StringBuilder database_servlet_class = new StringBuilder();
            StringBuilder typescript_data_structure_class = new StringBuilder();
            StringBuilder typescript_request_send_response = new StringBuilder();
            StringBuilder typescript_form_component_ts = new StringBuilder();
            StringBuilder typescript_form_component_html = new StringBuilder();
            StringBuilder typescript_table_component_ts = new StringBuilder();
            StringBuilder typescript_table_component_html = new StringBuilder();
            StringBuilder http_requests = new StringBuilder();
            for (DataStructure data_structure : data_structure_list) {
                java_data_structure_class.append(nl).append(data_structure.java_data_structure_class).append(nl);
                typescript_data_structure_class.append(nl).append(data_structure.typescript_data_structure_class).append(nl);
                typescript_request_send_response.append(nl).append(data_structure.typescript_request_send_response).append(nl);
                typescript_form_component_ts.append(nl).append(data_structure.typescript_form_component_ts).append(nl);
                typescript_form_component_html.append(nl).append(data_structure.typescript_form_component_html).append(nl);
                typescript_table_component_ts.append(nl).append(data_structure.typescript_table_component_ts).append(nl);
                typescript_table_component_html.append(nl).append(data_structure.typescript_table_component_html).append(nl);
                database_servlet_class.append(nl).append(data_structure.database_servlet_class).append(nl);
                http_requests
                        .append(nl).append(data_separator).append(data_structure.name).append(" - ").append(database_servlet_uri).append(" - HTTP Request").append(data_separator).append(nl)
                        .append(nl).append(data_separator).append("http requests").append(data_separator).append(nl)
                        .append(data_structure.http_requests).append(nl);
            }

            writer.append("\n\n ***** Java Data Structure Class ******\n\n");
            writer.append(java_data_structure_class);
            
            writer.append("\n\n ***** Database Servlet Class ******\n\n");
            writer.append(database_servlet_class);

            writer.append("\n\n ***** Typescript Data Structure Class ******\n\n");
            writer.append(typescript_data_structure_class);

            writer.append("\n\n ***** Typescript Request Send Respnonse ******\n\n");
            writer.append(typescript_request_send_response);

            writer.append("\n\n ***** Typescript Form Component .ts ******\n\n");
            writer.append(typescript_form_component_ts);
            
            writer.append("\n\n ***** Typescript Form Component .html ******\n\n");
            writer.append(typescript_form_component_html);
            
            writer.append("\n\n ***** Typescript MAT Table Component .ts ******\n\n");
            writer.append(typescript_table_component_ts);
            
            writer.append("\n\n ***** Typescript MAT Table Component .html ******\n\n");
            writer.append(typescript_table_component_html);

            writer.append("\n\n ***** Http Request ******\n\n");
            writer.append(http_requests);
            
        } else if (print_styel == 2) {
            for (DataStructure data_structure : data_structure_list) {
                StringBuilder java_data_structure_class = new StringBuilder();
                StringBuilder typescript_data_structure_class = new StringBuilder();
                StringBuilder typescript_request_send_response = new StringBuilder();
                StringBuilder typescript_form_component_ts = new StringBuilder();
                StringBuilder typescript_form_component_html = new StringBuilder();
                StringBuilder typescript_table_component_ts = new StringBuilder();
                StringBuilder typescript_table_component_html = new StringBuilder();
                StringBuilder database_servlet_class = new StringBuilder();
                StringBuilder database_servlet_uri = new StringBuilder();
                StringBuilder http_requests = new StringBuilder();

                java_data_structure_class.append(nl).append(data_structure.java_data_structure_class).append(nl);
                typescript_data_structure_class.append(nl).append(data_structure.typescript_data_structure_class).append(nl);
                typescript_request_send_response.append(nl).append(data_structure.typescript_request_send_response).append(nl);
                typescript_form_component_ts.append(nl).append(data_structure.typescript_form_component_ts).append(nl);
                typescript_form_component_html.append(nl).append(data_structure.typescript_form_component_html).append(nl);
                typescript_table_component_ts.append(nl).append(data_structure.typescript_table_component_ts).append(nl);
                typescript_table_component_html.append(nl).append(data_structure.typescript_table_component_html).append(nl);
                database_servlet_class.append(nl).append(data_structure.database_servlet_class).append(nl);
                http_requests
                        .append(nl).append(data_separator).append("Insert http request").append(data_separator).append(nl)
                        .append(data_structure.http_requests).append(nl);
                writer.append("\n\n ***** ").append(data_structure.name).append(" - Java Class ******\n\n");
                writer.append(java_data_structure_class);
                writer.append("\n\n ***** ").append(data_structure.name).append(" - ").append(database_servlet_uri).append(" DatabaseServlet Class ******\n\n");
                writer.append(database_servlet_class);
                writer.append("\n\n ***** ").append(data_structure.name).append(" - Typescript Class ******\n\n");
                writer.append(typescript_data_structure_class);
                writer.append("\n\n ***** ").append(data_structure.name).append(" - Typescript Request Send Respnonse ******\n\n");
                writer.append(typescript_request_send_response);
                writer.append("\n\n ***** ").append(data_structure.name).append(" - Typescript Form Component .ts ******\n\n");
                writer.append(typescript_form_component_ts);
                writer.append("\n\n ***** ").append(data_structure.name).append(" - Typescript Form Component .html ******\n\n");
                writer.append(typescript_form_component_html);
                writer.append("\n\n ***** ").append(data_structure.name).append(" - Typescript MAT Table Component .ts ******\n\n");
                writer.append(typescript_table_component_ts);
                writer.append("\n\n ***** ").append(data_structure.name).append(" - Typescript MAT Table Component .html ******\n\n");
                writer.append(typescript_table_component_html);
                writer.append("\n\n ***** ").append(data_structure.name).append(" - ").append(database_servlet_uri).append(" - HTTP Request ******\n\n");
                writer.append(http_requests);
            }
        }
        
        writer.append("\n\n ***** Data Structure Print Completed Successfully ***** \n\n");
    }

    
}
