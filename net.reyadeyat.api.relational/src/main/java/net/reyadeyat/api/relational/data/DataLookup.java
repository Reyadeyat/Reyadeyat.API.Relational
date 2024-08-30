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

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.TreeMap;

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
public class DataLookup {
    private TreeMap<String, HashMap<String, DataStructure>> lookupCode;
    private TreeMap<String, HashMap<Integer, DataStructure>> lookupID;
    private String data_lookup_category;
    
    /**Resultset must be sorted by Category Name, Category_Elements or undefined behavior will occur*/
    public DataLookup(ResultSet resultSet, String data_lookup_category, String categoryFieldName, String idFieldName, String codeFieldName, String javaFieldName, String typescriptFieldName) throws Exception {
        lookupCode = new TreeMap<>();
        lookupID = new TreeMap<>() ;
        this.data_lookup_category = data_lookup_category;
        
        HashMap<String, DataStructure> codeMap = null;
        HashMap<Integer, DataStructure> idMap = null;
        
        String currentCategory = "";
        while (resultSet.next()) {
            String category = resultSet.getString(categoryFieldName);
            if (currentCategory.equalsIgnoreCase(category) == false) {
                if (currentCategory.isEmpty() == false) {//first round
                    lookupCode.put(currentCategory, codeMap);
                    lookupID.put(currentCategory, idMap);
                }
                
                currentCategory = category;
                codeMap = new HashMap<>();
                idMap = new HashMap<>();
            }
            Integer id = resultSet.getInt(idFieldName);
            String code = resultSet.getString(codeFieldName);
            String java_datatype = resultSet.getString(javaFieldName);
            String typescript_datatype = resultSet.getString(typescriptFieldName);
            DataStructure data_type = new DataStructure(id, code, java_datatype, typescript_datatype);
            codeMap.put(code.toLowerCase(), data_type);
            idMap.put(id, data_type);
        }
        lookupCode.put(currentCategory, codeMap);
        lookupID.put(currentCategory, idMap);
    }
    
    public Integer lookupID(String code) throws Exception {
        if (lookupCode.get(data_lookup_category) == null) {
            throw new Exception("lookupID: Category '"+data_lookup_category+"' is not loaded in enum lookup for code '"+code+"'");
        }
        DataStructure data_structure = lookupCode.get(data_lookup_category).get(code.toLowerCase());
        if (data_structure == null) {
            throw new Exception("lookupJavaDataType: Category '"+data_lookup_category+"' doesn't have enum lookup code '"+code+"'");
        }
        return data_structure.getID();
    }
    
    public String lookupCode(Integer id) throws Exception {
        if (lookupCode.get(data_lookup_category) == null) {
            throw new Exception("lookupCode: Category '"+data_lookup_category+"' is not loaded in enum lookup for id '"+id+"'");
        }
        DataStructure data_structure = lookupID.get(data_lookup_category).get(id);
        if (data_structure == null) {
            throw new Exception("lookupCode: Category '"+data_lookup_category+"' doesn't have enum lookup id '"+id+"'");
        }
        return data_structure.getCode();
    }
    
    public String lookupJavaDataType(Integer id) throws Exception {
        if (lookupCode.get(data_lookup_category) == null) {
            throw new Exception("lookupJavaDataType: Category '"+data_lookup_category+"' is not loaded in enum lookup for id '"+id+"'");
        }
        DataStructure data_structure = lookupID.get(data_lookup_category).get(id);
        if (data_structure == null) {
            throw new Exception("lookupJavaDataType: Category '"+data_lookup_category+"' doesn't have enum lookup id '"+id+"'");
        }
        return data_structure.getJavaDataType();
    }
    
    public String lookupJavaDataType(String code) throws Exception {
        if (lookupCode.get(data_lookup_category) == null) {
            throw new Exception("lookupJavaDataType: Category '"+data_lookup_category+"' is not loaded in enum lookup for code '"+code+"'");
        }
        DataStructure data_structure = lookupCode.get(data_lookup_category).get(code.toLowerCase());
        if (data_structure == null) {
            throw new Exception("lookupJavaDataType: Category '"+data_lookup_category+"' doesn't have enum lookup code '"+code+"'");
        }
        return data_structure.getJavaDataType();
    }
    
    public String lookupTypescriptDataType(Integer id) throws Exception {
        if (lookupCode.get(data_lookup_category) == null) {
            throw new Exception("lookupTypescriptDataType: Category '"+data_lookup_category+"' is not loaded in enum lookup for id '"+id+"'");
        }
        DataStructure data_structure = lookupID.get(data_lookup_category).get(id);
        if (data_structure == null) {
            throw new Exception("lookupTypescriptDataType: Category '"+data_lookup_category+"' doesn't have enum lookup id '"+id+"'");
        }
        return data_structure.getTypescriptDataType();
    }
    
    public String lookupTypescriptDataType(String code) throws Exception {
        if (lookupCode.get(data_lookup_category) == null) {
            throw new Exception("lookupTypescriptDataType: Category '"+data_lookup_category+"' is not loaded in enum lookup for code '"+code+"'");
        }
        DataStructure data_structure = lookupCode.get(data_lookup_category).get(code.toLowerCase());
        if (data_structure == null) {
            throw new Exception("lookupTypescriptDataType: Category '"+data_lookup_category+"' doesn't have enum lookup code '"+code+"'");
        }
        return data_structure.getTypescriptDataType();
    }
    
    public String getDataLookupCategory() {
        return data_lookup_category;
    }
}
