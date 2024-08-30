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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import net.reyadeyat.api.library.jdbc.JDBCSource;
import net.reyadeyat.api.library.json.JsonUtil;
import net.reyadeyat.api.relational.modeler.ModelingRequest;
import net.reyadeyat.api.relational.request.Response;
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
public class TestModelingRequest extends ModelingRequest {
    
    /*
    SELECT DISTINCT JSON_EXTRACT(json_object, '$.data_type_name') AS data_type_name
FROM model.field_list;
    */
    
    public TestModelingRequest() throws Exception {
        super();
    }

    /**
     * @param args the command line arguments
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        CompositObject c1 = new CompositObject(1, "1");
        CompositObject c2 = new CompositObject(2, "2");
        CompositObject c3 = new CompositObject(3, "3");
        CompositObject c4 = new CompositObject(4, "4");
        CompositObject c5 = new CompositObject(5, "5");
        TreeMap<CompositObject, String> tree = new TreeMap(new Comparator<CompositObject>() {
            @Override
            public int compare(CompositObject o1, CompositObject o2) {
                return CompositObject.compare(o1, o2);
            }
        });
        tree.put(c1, "C1");
        tree.put(c2, "C2");
        tree.put(c3, "C3");
        tree.put(c4, "C4");
        tree.put(c5, "C5");
        
        CompositObject cx = new CompositObject(3, "3");
        
        String value = tree.get(cx);
        System.out.println(value);
        
        try {
            registered_jdbcsource_map.put(model_jdbc_source.getDatabaseName(), model_jdbc_source);
            registered_jdbcsource_map.put(data_jdbc_source.getDatabaseName(), data_jdbc_source);
        } catch (Exception ex) {
            Logger.getLogger(TestRelationalRequest.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        
        try (Connection jdbc_connection = data_jdbc_source.getConnection(true)) {
            Integer security_flag = SECURITY_FLAG_RETURN_NOTHING | /*SECURITY_FLAG_FOREING_KEY_MUST_LINK_TO_PRIMARY_KEY |*/ SECURITY_FLAG_ASSERT_VALID_FIELD_NAMES | SECURITY_FLAG_RETURN_DESCRIPTIVE_RESPONSE_MESSAGE | SECURITY_FLAG_RETURN_GENERATED_ID;
            Gson gson = JsonUtil.gson();
            //JsonObject model_transaction_request = gson.fromJson(model_transaction_request_json_text, JsonObject.class);
            JsonArray log_list = new JsonArray();
            
            OutputStream response_output_stream = args.length == 0 || args[0] == null ? new ByteArrayOutputStream() : new FileOutputStream(new File(args[0]));
            
            JsonArray error_list = new JsonArray();
            TestModelingRequest modeling_request = new TestModelingRequest();
            
            //List of external classes implements model members annotated with @DontJsonAnnotation
            Map<String, Class> interface_implementation = new HashMap<String, Class>();
            interface_implementation.put("net.reyadeyat.api.relational.model.TableInterfaceImplementationDataStructures", UserDefinedTableInterfaceImplementationDataStructures.class);
            
            TableInterfaceImplementationDataStructures table_interface_implementation_data_structures = new UserDefinedTableInterfaceImplementationDataStructures();
            
            //Delete Model Request
            JsonObject model_service_delete_json = gson.fromJson(model_service_delete_request_json_text, JsonObject.class);
            Response delete_response = modeling_request.serviceTransaction(security_flag, model_service_delete_json, response_output_stream, jdbc_connection, table_interface_implementation_data_structures, interface_implementation, log_list, error_list);
            
            //Build Model Request
            JsonObject model_service_build_json = gson.fromJson(model_service_build_request_json_text, JsonObject.class);
            Response build_response = modeling_request.serviceTransaction(security_flag, model_service_build_json, response_output_stream, jdbc_connection, table_interface_implementation_data_structures, interface_implementation, log_list, error_list);
            
            //Print Model Request
            JsonObject model_service_print_json = gson.fromJson(model_service_print_request_json_text, JsonObject.class);
            Response print_response = modeling_request.serviceTransaction(security_flag, model_service_print_json, response_output_stream, jdbc_connection, table_interface_implementation_data_structures, interface_implementation, log_list, error_list);
            
            if (args.length == 0 || args[0] == null) {
                String reposnse_string = new String(((ByteArrayOutputStream) response_output_stream).toByteArray(), StandardCharsets.UTF_8);
                Logger.getLogger(TestModelingRequest.class.getName()).log(Level.INFO, reposnse_string);
            }
            response_output_stream.close();
        } catch (Exception ex) {
            Logger.getLogger(TestModelingRequest.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    @Override
    public String getDefaultDatasourceName() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public DataSource getDataSource(String datasource_name) throws Exception {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
    
    @Override
    public JDBCSource getJDBCSource(String datasource_name) throws Exception {
        if (datasource_name.equalsIgnoreCase("model") == true) {
            return model_jdbc_source;
        } else if (datasource_name.equalsIgnoreCase(data_database) == true) {
            return data_jdbc_source;
        }
        throw new Exception("JDBC Source '"+datasource_name+"' is not defined in this service container!!");
    }
    
    @Override
    public Connection getDataSourceConnection(String datasource_name) throws Exception {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Connection getJDBCSourceConnection(String datasource_name) throws Exception {
        if (datasource_name.equalsIgnoreCase("model") == true) {
            return model_jdbc_source.getConnection(false);
        } else if (datasource_name.equalsIgnoreCase("data") == true) {
            return data_jdbc_source.getConnection(false);
        }
        throw new Exception("JDBC Source '"+datasource_name+"' is not defined in this service container!!");
    }

    @Override
    public Connection getDatabaseConnection(String datasurce_name) throws Exception {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
    
    private static String data_database = "parental";
    private static String data_database_server = "127.0.0.1:33060";
    private static String data_database_user = "remote";
    private static String data_database_password = "123456";
    
    private static String model_database = "model";
    private static String model_database_server = "127.0.0.1:33060";
    private static String model_database_user = "remote";
    private static String model_database_password = "123456";
    
    private static String model_service_build_request_json_text = """
    {
        "transaction": "build",
        "service_name": "parental_service",
        "default_datasource_name": "%s",
        "database_name": "%s",
        "model_id": "600",
        "model_datasource_name": "model",
        "data_datasource_name": "%s",
        "secret_key": "1234567890",
                                                                  
        "model_id": 600,
        "model_instance_sequence_type_id": 1,
        "model_name": "parental",
        "model_version": 0.0.000001",
        "model_instance_sequence_last_value": "0",
        "model_class_path": "net.reyadeyat.api.relational.model.Enterprise",
        "model_data_lookup_category": "MySQL Data Type",
        "modeled_database_url": "jdbc:mysql://127.0.0.1:33060/parental",
        "modeled_database_url_user_name": "remote",
        "modeled_database_url_user_password": "123456",
        "modeled_database_schem": "",
        "modeled_database_name": "%s",
        "modeled_database_field_open_quote": "`",
        "modeled_database_field_close_quote": "`",
                                                                  
        "modeled_table_interface_implementation_data_structures_class": "net.reyadeyat.relational.test.api.UserDefinedTableDataStructures",
                                                                  
        "table_tree": [
            {
              "table_name": "table_a",
              "children": [
                {
                  "table_name": "table_a_a",
                  "children": []
                },
                {
                  "table_name": "table_a_b",
                  "children": [
                    {
                      "table_name": "table_a_b_a",
                      "children": []
                    }
                  ]
                },
                {
                  "table_name": "table_a_c",
                  "children": []
                }
              ]
            }
        ]
    }
    """.formatted(data_database, data_database, data_database, data_database);
    
    private static String model_service_print_request_json_text = """
    {
        "transaction": "print",
        "print_style": 1,
        "service_name": "parental_service",
        "default_datasource_name": "%s",
        "database_name": "%s",
        "model_id": "600",
        "model_datasource_name": "model",
        "data_datasource_name": "%s",
        "secret_key": "1234567890",
                                                                  
        "model_id": 600,
        "model_instance_sequence_type_id": 1,
        "model_name": "%s",
        "model_version": 0.0.000001",
        "model_instance_sequence_last_value": "0",
        "model_class_path": "net.reyadeyat.api.relational.model.Enterprise",
        "model_data_lookup_category": "MySQL Data Type",
        "modeled_database_url": "jdbc:mysql://127.0.0.1:33060/parental",
        "modeled_database_url_user_name": "remote",
        "modeled_database_url_user_password": "123456",
        "modeled_database_schem": "",
        "modeled_database_name": "%s",
        "modeled_database_field_open_quote": "`",
        "modeled_database_field_close_quote": "`",
                                                                  
        "modeled_table_interface_implementation_data_structures_class": "net.reyadeyat.relational.test.api.UserDefinedTableDataStructures",
                                                                  
        "table_tree": [
            {
              "table_name": "table_a",
              "children": [
                {
                  "table_name": "table_a_a",
                  "children": []
                },
                {
                  "table_name": "table_a_b",
                  "children": [
                    {
                      "table_name": "table_a_b_a",
                      "children": []
                    }
                  ]
                },
                {
                  "table_name": "table_a_c",
                  "children": []
                }
              ]
            }
        ]
    }
    """.formatted(data_database, data_database, data_database, data_database, data_database);

    private static String model_service_delete_request_json_text = """
    {
        "transaction": "delete",
        "service_name": "parental_service",
        "default_datasource_name": "%s",
        "database_name": "%s",
        
        "model_datasource_name": "model",
        "data_datasource_name": "%s",
        "secret_key": "1234567890",
                                                                   
        "model_id": 600,
        "model_instance_sequence_type_id": 1,
        "model_name": "%s",
        "model_version": 0.0.000001",
        "model_instance_sequence_last_value": "0",
        "model_class_path": "net.reyadeyat.api.relational.model.Enterprise",
        "model_data_lookup_category": "MySQL Data Type",
        "modeled_database_url": "jdbc:mysql://127.0.0.1:33060/parental",
        "modeled_database_url_user_name": "remote",
        "modeled_database_url_user_password": "123456",
        "modeled_database_schem": "",
        "modeled_database_name": "%s",
        "modeled_database_field_open_quote": "`",
        "modeled_database_field_close_quote": "`",

        "modeled_table_interface_implementation_data_structures_class": "net.reyadeyat.relational.test.api.UserDefinedTableDataStructures"
    }
    """.formatted(data_database, data_database, data_database, data_database, data_database);
    
    private static HashMap<String, DataSource> registered_datasource_map = new HashMap<>();
    private static HashMap<String, JDBCSource> registered_jdbcsource_map = new HashMap<>();
    
    private static String model_version = "0.0.0.0001";

    private static JDBCSource data_jdbc_source = new JDBCSource() {
        private static final String data_database_server = TestModelingRequest.data_database_server;
        private static final String data_database_user = TestModelingRequest.data_database_user;
        private static final String data_database_password = TestModelingRequest.data_database_password;
        private static final String data_database_schema = TestModelingRequest.data_database;
        private static final String database_schema = "";
        private static final String mysql_database_field_open_quote = "`";
        private static final String mysql_database_field_close_quote = "`";
        
        @Override
        public String getDataSourceName() throws Exception {
            return getDatabaseName();
        }

        @Override
        public Connection getConnection(Boolean auto_commit) throws Exception {
            //CREATE DATABASE `data` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
            Connection database_connection = DriverManager.getConnection("jdbc:mysql://" + data_database_server + "/" + data_database_schema, data_database_user, data_database_password);
            database_connection.setAutoCommit(auto_commit);
            return database_connection;
        }

        @Override
        public String getUserName() throws Exception {
            return data_database_user;
        }

        @Override
        public String getUserPassword() throws Exception {
            return data_database_password;
        }

        @Override
        public String getDatabaseEngine() throws Exception {
            return "mysql";
        }

        @Override
        public String getURL() throws Exception {
            return "jdbc:mysql://" + data_database_server + "/" + data_database_schema;
        }

        @Override
        public String getDatabaseName() throws Exception {
            return data_database_schema;
        }

        @Override
        public String getDatabaseServer() throws Exception {
            return data_database_server;
        }

        @Override
        public String getDatabaseSchema() throws Exception {
            return "";
        }

        @Override
        public String getDatabaseOpenQuote() throws Exception {
            return mysql_database_field_open_quote;
        }

        @Override
        public String getDatabaseCloseQuote() throws Exception {
            return mysql_database_field_close_quote;
        }
    };
    
    private static JDBCSource model_jdbc_source = new JDBCSource() {
        private static String model_database_server = TestModelingRequest.model_database_server;
        private static String model_database_user = TestModelingRequest.model_database_user;
        private static String model_database_password = TestModelingRequest.model_database_password;
        private static String model_database_schema = TestModelingRequest.model_database;
        private static final String database_schema = "";
        private static final String mysql_database_field_open_quote = "`";
        private static final String mysql_database_field_close_quote = "`";

        @Override
        public String getDataSourceName() throws Exception {
            return getDatabaseName();
        }
        
        @Override
        public Connection getConnection(Boolean auto_commit) throws Exception {
            //CREATE DATABASE `data` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
            Connection database_connection = DriverManager.getConnection("jdbc:mysql://" + model_database_server + "/" + model_database_schema, model_database_user, model_database_password);
            database_connection.setAutoCommit(auto_commit);
            return database_connection;
        }

        @Override
        public String getUserName() throws Exception {
            return model_database_user;
        }

        @Override
        public String getUserPassword() throws Exception {
            return model_database_password;
        }

        @Override
        public String getDatabaseEngine() throws Exception {
            return "mysql";
        }

        @Override
        public String getURL() throws Exception {
            return "jdbc:mysql://" + model_database_server + "/" + model_database_schema;
        }

        @Override
        public String getDatabaseName() throws Exception {
            return model_database_schema;
        }

        @Override
        public String getDatabaseServer() throws Exception {
            return model_database_server;
        }

        @Override
        public String getDatabaseSchema() throws Exception {
            return "";
        }

        @Override
        public String getDatabaseOpenQuote() throws Exception {
            return mysql_database_field_open_quote;
        }

        @Override
        public String getDatabaseCloseQuote() throws Exception {
            return mysql_database_field_close_quote;
        }
    };

}
