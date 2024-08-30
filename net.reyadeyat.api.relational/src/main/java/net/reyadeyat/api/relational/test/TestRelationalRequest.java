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
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import net.reyadeyat.api.library.jdbc.JDBCSource;
import net.reyadeyat.api.library.json.JsonUtil;
import net.reyadeyat.api.library.security.SecuredPipedReader;
import net.reyadeyat.api.library.security.SecuredPipedWriter;
import net.reyadeyat.api.library.security.Security;
import net.reyadeyat.api.library.security.SecurityAES;
import net.reyadeyat.api.relational.database.RecordProcessor;
import net.reyadeyat.api.relational.request.RelationalRequest;
import net.reyadeyat.api.relational.request.Request;
import net.reyadeyat.api.relational.request.RequestDefinition;

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
public class TestRelationalRequest extends RelationalRequest {

    public TestRelationalRequest(RequestDefinition request_definition, HashMap<String, Class> interface_implementation, Integer security_flag)
            throws Exception {
        super(request_definition, interface_implementation, security_flag);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            registered_jdbcsource_map.put(model_jdbc_source.getDatabaseName(), model_jdbc_source);
            registered_jdbcsource_map.put(data_jdbc_source.getDatabaseName(), data_jdbc_source);
        } catch (Exception ex) {
            Logger.getLogger(TestRelationalRequest.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        Logger gsonLogger = Logger.getLogger(Gson.class.getName());
        gsonLogger.setLevel(Level.FINE);

        try (Connection jdbc_connection = data_jdbc_source.getConnection(true)) {
            // .registered_datasource_map.put(name, datasource);
            Security security = new SecurityAES();

            //collect encrypted stream output from service then decrypt and input it to file
            String separator = "~";
            FileWriter plain_writer = new FileWriter(new File("/linux/reyadeyat/yanobel/open-source/Relational.API/response.txt"));
            SecuredPipedReader plain_reader_pipe = new SecuredPipedReader(plain_writer, security, separator);
            SecuredPipedWriter secured_writer = new SecuredPipedWriter(plain_reader_pipe, security, separator);
            JsonWriter response_output_writer = new JsonWriter(secured_writer);
            
            Gson gson = JsonUtil.gson();
            Integer security_flag = SECURITY_FLAG_RETURN_NOTHING | SECURITY_FLAG_FOREING_KEY_MUST_LINK_TO_PRIMARY_KEY | SECURITY_FLAG_ASSERT_VALID_FIELD_NAMES | SECURITY_FLAG_RETURN_DESCRIPTIVE_RESPONSE_MESSAGE | SECURITY_FLAG_RETURN_GENERATED_ID;
            JsonArray log_list = new JsonArray();
            JsonArray error_list = new JsonArray();
            RequestDefinition request_definition = gson.fromJson(service_definition_json_text, RequestDefinition.class);
            request_definition.init();

            List<Request> request_list = gson.fromJson(service_request_json_text, new TypeToken<List<Request>>() {}.getType());
            HashMap<String, Class> interface_implementation = new HashMap<>();
            interface_implementation.put("net.reyadeyat.api.relational.model.TableDataStructures", UserDefinedTableInterfaceImplementationDataStructures.class);

            TestRelationalRequest relational_request = new TestRelationalRequest(request_definition, interface_implementation, security_flag);
            relational_request.serviceTransaction(security_flag, request_list, response_output_writer, jdbc_connection, log_list, error_list);
            
            plain_reader_pipe.flush();
            plain_reader_pipe.close();
            plain_writer.close();
        } catch (Exception ex) {
            Logger.getLogger(TestRelationalRequest.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    @Override
    public DataSource getDataSource(String datasource_name) throws Exception {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public JDBCSource getJDBCSource(String datasource_name) throws Exception {
        return registered_jdbcsource_map.get(datasource_name);
    }

    @Override
    public Connection getDatabaseConnection(String datasource_name) throws Exception {
        return registered_jdbcsource_map.get(datasource_name).getConnection(false);
    }

    @Override
    public Boolean insertPreLogic(RecordProcessor record_processor, Connection connection) throws Exception {
        return true;
    }

    @Override
    public Boolean insertPostLogic(RecordProcessor record_processor, Connection connection) throws Exception {
        return true;
    }

    @Override
    public Boolean selectPreLogic(RecordProcessor record_processor, Connection connection) throws Exception {
        return true;
    }

    @Override
    public Boolean selectPerRecordLogic(RecordProcessor record_processor, ResultSet rs, JsonObject record_object)
            throws Exception {
        return true;
    }

    @Override
    public Boolean selectPerRecordLogic(RecordProcessor record_processor, ResultSet rs, JsonArray record_list)
            throws Exception {
        return true;
    }

    @Override
    public Boolean selectPostLogic(RecordProcessor record_processor, Connection connection, JsonArray resultset_json)
            throws Exception {
        return true;
    }

    @Override
    public Boolean updatePreLogic(RecordProcessor record_processor, Connection connection) throws Exception {
        return true;
    }

    @Override
    public Boolean updatePostLogic(RecordProcessor record_processor, Connection connection) throws Exception {
        return true;
    }

    @Override
    public Boolean deletePreLogic(RecordProcessor record_processor, Connection connection) throws Exception {
        return true;
    }

    @Override
    public Boolean deletePostLogic(RecordProcessor record_processor, Connection connection) throws Exception {
        return true;
    }

    @Override
    public Boolean insertInject(RecordProcessor record_processor) throws Exception {
        // Inject data critical fields before backend operations inside
        // preInsertfunction
        return true;
    }

    @Override
    public Boolean updateInject(RecordProcessor record_processor) throws Exception {
        // Inject data critical fields before backend operations inside preUpdate
        // function
        return true;
    }

    @Override
    public Boolean selectInject(RecordProcessor record_processor) throws Exception {
        // Inject data critical fields before backend operations inside preInsert
        // function
        return true;
    }

    @Override
    public Boolean deleteInject(RecordProcessor record_processor) throws Exception {
        // Inject data critical fields before backend operations inside preDelete
        // function
        return true;
    }

    @Override
    public Boolean insertEject(RecordProcessor record_processor) throws Exception {
        // Eject data critical fields after backend operations inside postInsert
        // function
        return true;
    }

    @Override
    public Boolean updateEject(RecordProcessor record_processor) throws Exception {
        // Eject data critical fields after backend operations inside postUpdate
        // function
        return true;
    }

    @Override
    public Boolean selectEject(RecordProcessor record_processor) throws Exception {
        // Eject data critical fields after backend operations inside postSelect
        // function
        return true;
    }

    @Override
    public Boolean deleteEject(RecordProcessor record_processor) throws Exception {
        // Eject data critical fields after backend operations inside postDelete
        // function
        return true;
    }

    private static String data_database = "parental";

    private static String service_request_json_text = """
			[
			    {
			        "table": "table_a_x",
			        "response": "tree",
			        "select": [
			          "id_a_x",
			          "name_ar_a_x",
			          "name_en_a_x",
			          "date_a_x",
			          "time_a_x",
			          "timestamp_a_x",
			          "boolean_a_x"
			        ],
			        "where": {
			          "clause": "id_a_x>?",
			          "values": [
			            0
			          ]
			        },
			        "order_by": [
			          "id_a_x"
			        ],
			        "children": [
			            {
			                "table": "table_a_a_x",
			                "response": "tree",
			                "select": [
			                    "id_a_a_x",
			                    "name_ar_a_a_x",
			                    "name_en_a_a_x",
			                    "date_a_a_x",
			                    "time_a_a_x",
			                    "timestamp_a_a_x",
			                    "boolean_a_a_x"
			                ],
			                "where": {
			                    "clause": "id_a_a_x>?",
			                    "values": [
			                        0
			                    ]
			                },
			                "order_by": [
			                    "id_a_a_x"
			                ]
			            },
			            {
			                "table": "table_a_b_x",
			                "response": "tree",
			                "select": [
			                    "id_a_b_x",
			                    "name_ar_a_b_x",
			                    "name_en_a_b_x",
			                    "date_a_b_x",
			                    "time_a_b_x",
			                    "timestamp_a_b_x",
			                    "boolean_a_b_x"
			                ],
			                "where": {
			                    "clause": "id_a_b_x>?",
			                    "values": [
			                        0
			                    ]
			                },
			                "order_by": [
			                    "id_a_b_x"
			                ],
			                "children": [
			                    {
			                        "table": "table_a_b_a_x",
			                        "response": "tree",
			                        "select": [
			                            "id_a_b_a_x",
			                            "name_ar_a_b_a_x",
			                            "name_en_a_b_a_x",
			                            "date_a_b_a_x",
			                            "time_a_b_a_x",
			                            "timestamp_a_b_a_x",
			                            "boolean_a_b_a_x"
			                        ],
			                        "where": {
			                            "clause": "id_a_b_a_x>?",
			                            "values": [
			                                0
			                            ]
			                        },
			                        "order_by": [
			                            "id_a_b_a_x"
			                        ]
			                    }
			                ]
			            },
			            {
			                "table": "table_a_c_x",
			                "response": "tree",
			                "select": [
			                    "id_a_c_x",
			                    "name_ar_a_c_x",
			                    "name_en_a_c_x",
			                    "date_a_c_x",
			                    "time_a_c_x",
			                    "timestamp_a_c_x",
			                    "boolean_a_c_x"
			                ],
			                "where": {
			                    "clause": "id_a_c_x>?",
			                    "values": [
			                        0
			                    ]
			                },
			                "order_by": [
			                    "id_a_c_x"
			                ]
			            }
			        ]
			    }
			]
			""";

    private static String service_definition_json_text = """
			{
			    "service_name": "parental_service",
			    "model_id": "500",
			    "model_datasource_name": "model",
			    "data_datasource_name": "%s",
			    "data_database_name": "%s",
			    "secret_key": "1234567890",
			    "transaction_type": ["insert","select","update","delete"],
			    "request_table": {
			        "table_name": "table_a",
			        "table_alias": "table_a_x",
			        "fields": [
			            {"name": "id", "alias": "id_a_x", "group_by": false},
			            {"name": "name_ar", "alias": "name_ar_a_x", "group_by": false},
			            {"name": "name_en", "alias": "name_en_a_x", "group_by": false},
			            {"name": "date", "alias": "date_a_x", "group_by": false},
			            {"name": "time", "alias": "time_a_x", "group_by": false},
			            {"name": "timestamp", "alias": "timestamp_a_x", "group_by": false},
			            {"name": "boolean", "alias": "boolean_a_x", "group_by": false}
			        ],
			        "children": [
			            {
			                "table_name": "table_a_a",
			                "table_alias": "table_a_a_x",
			                "fields": [
			                    {"name": "id", "alias": "id_a_a_x", "group_by": false},
			                    {"name": "name_ar", "alias": "name_ar_a_a_x", "group_by": false},
			                    {"name": "name_en", "alias": "name_en_a_a_x", "group_by": false},
			                    {"name": "date", "alias": "date_a_a_x", "group_by": false},
			                    {"name": "time", "alias": "time_a_a_x", "group_by": false},
			                    {"name": "timestamp", "alias": "timestamp_a_a_x", "group_by": false},
			                    {"name": "boolean", "alias": "boolean_a_a_x", "group_by": false}
			                ],
			                "children": []
			            },
			            {
			                "table_name": "table_a_b",
			                "table_alias": "table_a_b_x",
			                "fields": [
			                    {"name": "id", "alias": "id_a_b_x", "group_by": false},
			                    {"name": "name_ar", "alias": "name_ar_a_b_x", "group_by": false},
			                    {"name": "name_en", "alias": "name_en_a_b_x", "group_by": false},
			                    {"name": "date", "alias": "date_a_b_x", "group_by": false},
			                    {"name": "time", "alias": "time_a_b_x", "group_by": false},
			                    {"name": "timestamp", "alias": "timestamp_a_b_x", "group_by": false},
			                    {"name": "boolean", "alias": "boolean_a_b_x", "group_by": false}
			                ],
			                "children": [
			                    {
			                        "table_name": "table_a_b_a",
			                        "table_alias": "table_a_b_a_x",
			                        "fields": [
			                            {"name": "id", "alias": "id_a_b_a_x", "group_by": false},
			                            {"name": "name_ar", "alias": "name_ar_a_b_a_x", "group_by": false},
			                            {"name": "name_en", "alias": "name_en_a_b_a_x", "group_by": false},
			                            {"name": "date", "alias": "date_a_b_a_x", "group_by": false},
			                            {"name": "time", "alias": "time_a_b_a_x", "group_by": false},
			                            {"name": "timestamp", "alias": "timestamp_a_b_a_x", "group_by": false},
			                            {"name": "boolean", "alias": "boolean_a_b_a_x", "group_by": false}
			                        ],
			                        "children": []
			                    }
			                ]
			            },
			            {
			                "table_name": "table_a_c",
			                "table_alias": "table_a_c_x",
			                "fields": [
			                    {"name": "id", "alias": "id_a_c_x", "group_by": false},
			                    {"name": "name_ar", "alias": "name_ar_a_c_x", "group_by": false},
			                    {"name": "name_en", "alias": "name_en_a_c_x", "group_by": false},
			                    {"name": "date", "alias": "date_a_c_x", "group_by": false},
			                    {"name": "time", "alias": "time_a_c_x", "group_by": false},
			                    {"name": "timestamp", "alias": "timestamp_a_c_x", "group_by": false},
			                    {"name": "boolean", "alias": "boolean_a_c_x", "group_by": false}
			                ],
			                "children": []
			            }
			        ]
			    }
			}
			""".formatted(data_database, data_database);

    private static HashMap<String, DataSource> registered_datasource_map = new HashMap<>();
    private static HashMap<String, JDBCSource> registered_jdbcsource_map = new HashMap<>();

    private static String model_version = "0.0.0.0001";

    private static JDBCSource model_jdbc_source = new JDBCSource() {
        private static String model_database_server = "127.0.0.1:33060";
        private static String model_database_user_name = "remote";
        private static String model_database_password = "123456";
        private static String model_database_schema = "model";
        private static final String database_schema = "";
        private static final String mysql_database_field_open_quote = "`";
        private static final String mysql_database_field_close_quote = "`";

        @Override
        public String getDataSourceName() throws Exception {
            return getDatabaseName();
        }

        @Override
        public Connection getConnection(Boolean auto_commit) throws Exception {
            // CREATE DATABASE `data` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
            Connection database_connection = DriverManager.getConnection(
                    "jdbc:mysql://" + model_database_server + "/" + model_database_schema, model_database_user_name,
                    model_database_password);
            database_connection.setAutoCommit(auto_commit);
            return database_connection;
        }

        @Override
        public String getUserName() throws Exception {
            return model_database_user_name;
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

    private static JDBCSource data_jdbc_source = new JDBCSource() {
        private static final String data_database_server = "127.0.0.1:33060";
        private static final String data_database_user_name = "remote";
        private static final String data_database_password = "123456";
        private static final String data_database_schema = data_database;
        private static final String database_schema = "";
        private static final String mysql_database_field_open_quote = "`";
        private static final String mysql_database_field_close_quote = "`";

        @Override
        public String getDataSourceName() throws Exception {
            return getDatabaseName();
        }

        @Override
        public Connection getConnection(Boolean auto_commit) throws Exception {
            // CREATE DATABASE `data` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
            Connection database_connection = DriverManager.getConnection(
                    "jdbc:mysql://" + data_database_server + "/" + data_database_schema, data_database_user_name,
                    data_database_password);
            database_connection.setAutoCommit(auto_commit);
            return database_connection;
        }

        @Override
        public String getUserName() throws Exception {
            return data_database_user_name;
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

}
