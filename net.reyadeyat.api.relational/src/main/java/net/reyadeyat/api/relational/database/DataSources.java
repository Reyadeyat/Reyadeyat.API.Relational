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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import net.reyadeyat.api.library.util.Hunter;

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
public class DataSources {
    
    //Datasource
    protected String default_jdbc_datasource_name;
    protected Context JDBC_CONTEXT;
    protected String[] SERVER_POOLED_JDBCs;
    protected Boolean isDataSourceUp;
    protected String data_sources_title;
    
    public DataSources(String data_sources_title) {
        this.data_sources_title = data_sources_title;
        this.default_jdbc_datasource_name = null;
        init();
    }
    
    public DataSources(String data_sources_title, String default_jdbc_datasource_name) {
        this.data_sources_title = data_sources_title;
        this.default_jdbc_datasource_name = default_jdbc_datasource_name;
        init();
    }
    
    private void init() {
        isDataSourceUp = false;
        Boolean initerror = false;
        ArrayList<String> initerrormsg = new ArrayList<String>();
        int defaultDataSourceContextLoaded = 0;
        StringBuilder loaded_data_sources = new StringBuilder();
        try {
            Context context = new InitialContext();
            String jdbcs = (String) context.lookup("java:comp/env/SERVER_POOLED_JDBC");
            /*if (jdbcs == null || jdbcs.length() == 0) {
                initerror = true;
                initerrormsg.add("Server JDBC Contexts are not defined");
            }*/
            if (jdbcs != null && jdbcs.length() > 0) {
                this.JDBC_CONTEXT = (Context) context.lookup("java:comp/env/jdbc/");
                this.SERVER_POOLED_JDBCs = jdbcs.split(",");
                for (String jdbc : SERVER_POOLED_JDBCs) {
                    Hunter hunter = new Hunter();
                    DataSource dataSource = getDataSource(jdbc, hunter);
                    if (dataSource == null) {
                        initerror = true;
                        initerrormsg.add("Error loading JNDI JDBC '" + jdbc + "'; Check JDBC connection 'url' parameter for all 'Context.Resource' paths in application 'Context.xml'; Cause '" + hunter.message() + "'");
                    }
                }
            }
        } catch (Exception exception) {
            initerror = true;
            initerrormsg.add("InitialContext error revise 'Context.xml';" + exception.getMessage());
        }
        loaded_data_sources = loaded_data_sources.length() > 0 ? loaded_data_sources.deleteCharAt(loaded_data_sources.length() - 1) : loaded_data_sources;
        if (initerror == true) {
            isDataSourceUp = false;
            StringBuilder errors = new StringBuilder();
            for (String error : initerrormsg) {
                errors.append(error).append(",");
            }
            errors.delete(errors.length() - 1, errors.length());
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Initialization Error, DataSources '" + data_sources_title + "', Contact Administrator<br/>\n"+errors.toString(), new Throwable());
        }
        isDataSourceUp = true;
    }
    
    public Boolean isDataSourceUp() {
        return isDataSourceUp;
    }
        
    public Connection getDatabaseConnection() throws Exception {
        return getDatabaseConnection(this.default_jdbc_datasource_name);
    }

    public Connection getDatabaseConnection(String jdbc_datasource_name) throws Exception {
        if (jdbc_datasource_name == null) {
            throw new Exception("Data Source '"+data_sources_title+"' - JDBC Data Source Name is null");
        }

        DataSource dataSource = getDataSource(jdbc_datasource_name);
        if (dataSource == null) {
            throw new Exception("JDBC Context '" + jdbc_datasource_name + "' is not exist on this container");
        }
        Connection connection;
        try {
            connection = dataSource.getConnection();
        } catch (Exception ex) {
            throw new Exception("Failed to get connection from '" + jdbc_datasource_name + "' jndi jdbc pool", ex);
        }
        return connection;
    }

    public DataSource getDataSource(String dataSourceName) {
        return getDataSource(dataSourceName, null);
    }

    public DataSource getDataSource(String dataSourceName, Hunter hunter) {
        DataSource dataSource = null;
        try {
            dataSource = (DataSource) this.JDBC_CONTEXT.lookup(dataSourceName);
        } catch (NamingException exception) {
            if (hunter != null) {
                hunter.hunt(exception);
            }
            dataSource = null;
        }
        return dataSource;
    }
    
    public DataSource waitForDataSource(long seconds, String dataSourceName) throws Exception {
        DataSource dataSource = null;
        long seconds_part = seconds / 10;
        long seconds_break = 0;
        while(dataSource == null && seconds_break < seconds) {
            try {
                dataSource = (DataSource) this.JDBC_CONTEXT.lookup(dataSourceName);
                if (dataSource != null) {
                    continue;
                }
            } catch (NamingException exception) {
                dataSource = null;
            }
            seconds_break += seconds_part;
            TimeUnit.SECONDS.sleep(seconds_part);
        }
        
        return dataSource;
    }
    
    public DataSource getDataSourceFast(String dataSourceName) {
        DataSource dataSource = null;
        try {
            dataSource = (DataSource) this.JDBC_CONTEXT.lookup(dataSourceName);
        } catch (NamingException exception) {
            dataSource = null;
        }
        return dataSource;
    }
    
     public Connection getDatabaseConnectionFast() throws Exception {
        return getDatabaseConnectionFast(this.default_jdbc_datasource_name);
    }

    public Connection getDatabaseConnectionFast(String jdbc_datasource_name) throws Exception {
        if (jdbc_datasource_name == null) {
            throw new Exception("Data Source '"+data_sources_title+"' - JDBC Data Source Name is null");
        }

        DataSource dataSource = getDataSource(jdbc_datasource_name);
        if (dataSource == null) {
            return null;
        }
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
        } catch (Exception ex) {
            return null;
        }
        return connection;
    }
}
