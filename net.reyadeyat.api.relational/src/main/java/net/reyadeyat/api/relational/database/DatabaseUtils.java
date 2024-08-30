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
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class DatabaseUtils {

    static public void endTransaction(Connection database_connection) throws SQLException {
        if (database_connection.getAutoCommit() == false) {
            database_connection.commit();
        }
    }

    static public void endTransactionWithException(Boolean rethrow, Exception exception, Connection database_connection, Class log_class, String log_message) throws Exception {
        endTransactionWithException(rethrow, exception, database_connection, log_class, Level.WARNING, log_message);
    }

    static public void endTransactionWithException(Boolean rethrow, Exception exception, Connection database_connection, Class log_class, Level log_level, String log_message) throws Exception {
        Boolean sql_exception = true;
        Logger.getLogger(log_class.getName()).log(log_level == null ? Level.WARNING : log_level, log_message, exception);
        if (database_connection.getAutoCommit() == false) {
            database_connection.rollback();
        }
        if (rethrow == true) {
            throw exception;
        }
    }
}
