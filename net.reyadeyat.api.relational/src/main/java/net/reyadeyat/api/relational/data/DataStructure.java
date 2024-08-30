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
public class DataStructure {

    private Integer id;
    private String code;
    private String java_datatype;
    private String typescript_datatype;
    
    public DataStructure(Integer id, String code, String java_datatype, String typescript_datatype) {
        this.id = id;
        this.code = code;
        this.java_datatype = java_datatype;
        this.typescript_datatype = typescript_datatype;
    }
    
    public Integer getID() {
        return id;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getJavaDataType() {
        return java_datatype;
    }
    
    public String getTypescriptDataType() {
        return typescript_datatype;
    }
}
