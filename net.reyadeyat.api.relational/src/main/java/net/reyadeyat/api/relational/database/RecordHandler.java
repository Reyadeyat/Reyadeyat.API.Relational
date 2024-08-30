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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.sql.Connection;
import java.sql.ResultSet;
import javax.sql.DataSource;
import net.reyadeyat.api.library.jdbc.JDBCSource;

/**
 * 
 * Description Recordset response handler
 * 
 *
 * @author Mohammad Nabil Mostafa
 * <a href="mailto:code@reyadeyat.net">code@reyadeyat.net</a>
 * 
 * @since 2023.01.01
 */
public interface RecordHandler {
    public DataSource getDataSource(String datasource_name) throws Exception;
    public JDBCSource getJDBCSource(String datasource_name) throws Exception;
    public Connection getDatabaseConnection(String datasource_name) throws Exception;
    public Boolean insertPreLogic(RecordProcessor record_processor, Connection connection) throws Exception;
    public Boolean insertPostLogic(RecordProcessor record_processor, Connection connection) throws Exception;
    public Boolean selectPreLogic(RecordProcessor record_processor, Connection connection) throws Exception;
    public Boolean selectPerRecordLogic(RecordProcessor record_processor, ResultSet rs, JsonObject record_object) throws Exception;
    public Boolean selectPerRecordLogic(RecordProcessor record_processor, ResultSet rs, JsonArray record_list) throws Exception;
    public Boolean selectPostLogic(RecordProcessor record_processor, Connection connection, JsonArray resultset_json) throws Exception;
    public Boolean updatePreLogic(RecordProcessor record_processor, Connection connection) throws Exception;
    public Boolean updatePostLogic(RecordProcessor record_processor, Connection connection) throws Exception;
    public Boolean deletePreLogic(RecordProcessor record_processor, Connection connection) throws Exception;
    public Boolean deletePostLogic(RecordProcessor record_processor, Connection connection) throws Exception;
    public Boolean insertInject(RecordProcessor record_processor) throws Exception;
    public Boolean updateInject(RecordProcessor record_processor) throws Exception;
    public Boolean selectInject(RecordProcessor record_processor) throws Exception;
    public Boolean deleteInject(RecordProcessor record_processor) throws Exception;
    public Boolean insertEject(RecordProcessor record_processor) throws Exception;
    public Boolean updateEject(RecordProcessor record_processor) throws Exception;
    public Boolean selectEject(RecordProcessor record_processor) throws Exception;
    public Boolean deleteEject(RecordProcessor record_processor) throws Exception;
}
