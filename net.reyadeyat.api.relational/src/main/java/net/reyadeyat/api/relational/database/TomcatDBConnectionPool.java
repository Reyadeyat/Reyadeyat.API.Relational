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

//import java.sql.Connection;
//import org.apache.tomcat.jdbc.pool.DataSource;
//import org.apache.tomcat.jdbc.pool.PoolProperties;

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
public class TomcatDBConnectionPool {

    //private PoolProperties poolProperties;
    //private DataSource dataSource;
    
    public TomcatDBConnectionPool(String DBDriver, String DBURL, String username, String password, String validationQuery) {
        /*this.poolProperties = new PoolProperties();
        this.poolProperties.setUrl(DBURL);
        this.poolProperties.setDriverClassName(DBDriver);
        this.poolProperties.setUsername(username);
        this.poolProperties.setPassword(password);
        this.poolProperties.setJmxEnabled(true);
        this.poolProperties.setTestWhileIdle(true);
        this.poolProperties.setTestOnBorrow(true);
        this.poolProperties.setValidationQuery(validationQuery);
        this.poolProperties.setTestOnReturn(true);
        this.poolProperties.setValidationInterval(30000);
        this.poolProperties.setTimeBetweenEvictionRunsMillis(30000);
        this.poolProperties.setMaxActive(100);
        this.poolProperties.setInitialSize(10);
        this.poolProperties.setMaxWait(10000);
        this.poolProperties.setRemoveAbandonedTimeout(60);
        this.poolProperties.setMinEvictableIdleTimeMillis(30000);
        this.poolProperties.setMinIdle(10);
        this.poolProperties.setLogAbandoned(true);
        this.poolProperties.setRemoveAbandoned(true);
        this.poolProperties.setJdbcInterceptors(
                "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
                + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
        this.dataSource = new DataSource();
        dataSource.setPoolProperties(poolProperties);*/
    }

    /*public Connection getPooledConnection() throws Exception {
        return dataSource.getConnection();
    }*/
}
