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

import java.util.ArrayList;
import jakarta.xml.bind.annotation.XmlRootElement;
import net.reyadeyat.api.relational.annotation.MetadataAnnotation;
import java.util.TreeMap;
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
@XmlRootElement(name = "Enterprise")
public class Enterprise {
    @MetadataAnnotation (field=true, type="VARCHAR(256)")
    public String name;
    public String database_engine;
    @MetadataAnnotation (field=true, type="LONGTEXT", indexed=false, indexed_expresion="((SUBSTRING(`databaseUrl`, 1, 64)))")
    public String database_url;
    public Boolean case_sensitive_sql;
    public ArrayList<Database> database_list;
    transient public TreeMap<String, Database> database_map;
    
    /**no-arg default constructor for json jaxb marshalling*/
    public Enterprise() {
        database_list = new ArrayList<Database>();
        database_map = new TreeMap<String, Database>();
    }
    
    public Enterprise(String name, String database_engine, String database_url, Boolean case_sensitive_sql) {
        this();
        this.name = name;
        this.database_engine = database_engine;
        this.database_url = database_url;
        this.case_sensitive_sql = case_sensitive_sql;
    }
    
    public void init() {
        for (Database database : database_list) {
            database.init();
            database_map.put(database.name, database);
        }
    }
    
    public void addDatabase(Database database) {
        database.enterprise = this;
        database_list.add(database);
        database_map.put(database.name, database);
    }
    
    public Database getDatabase(String database_name) {
        return database_map.get(database_name);
        
    }
    
    @Override
    public String toString() {
        StringBuilder appendable = new StringBuilder();
        try {
            toString(appendable, 0, 4);
            return appendable.toString();
        } catch (Exception exception) {
            appendable.delete(0, appendable.length());
            appendable.append("Enterprise: ").append(name).append(" : Databases [").append(database_list.size()).append("]");
            appendable.append("toString error").append(exception.getMessage());
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "toString error", exception);
        }
        return appendable.toString();
    }
    
    public void toString(Appendable appendable) throws Exception {
        toString(appendable, 0, 4);
    }
    
    public void toString(Appendable appendable, Integer level, Integer shift) throws Exception {
        appendable.append("\n");
        for (int i = 0; i < level * shift; i++) {
            /*if (i%4 == 0) {
                b.append("|");
            } else {
                b.append("_");
            }*/
            appendable.append(" ");
        }
        appendable.append("|");
        for (int i = 0; i < shift - 1; i++) {
            appendable.append(".");
        }
        appendable.append("Enterprise: ").append(name).append(" Databases [").append(String.valueOf(database_list.size())).append("]");
        for (Database database : database_list) {
            database.toString(appendable, level + 1, shift);
        }
    }
}
