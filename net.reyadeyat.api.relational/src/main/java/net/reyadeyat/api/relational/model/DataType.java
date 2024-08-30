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
import java.util.Arrays;
import java.util.HashMap;

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
public class DataType {

    public ArrayList<String> kw = new ArrayList<>();
    public String symbol = "./*-+()[]|<>,;:\\'\"";
    public HashMap<String, String> imtf = new HashMap<String, String>();
    public HashMap<String, String> imtkw = new HashMap<String, String>();

    public DataType() throws Exception {
        kw = new ArrayList<>(Arrays.asList(new String[]{
        "select", "insert", "update", "delete", "distinct",
        "as", "if",
        "from", "inner", "outer", "left", "right", "join", "on",
        "where", "order", "by", "group", "having", "asc", "desc",
        "limit", "skip", "first", "offset", "unique", "between",
        "in", "like", "and", "or", "order"
        }));
        symbol = "./*-+()[]|<>,;:\\'\"";
        
        imtf = new HashMap<String, String>();
        imtf.put("TO_CHAR", "DATE_FORMAT");
        imtf.put("TO_DATE", "DATE_FORMAT");
        imtf.put("NVL", "IFNULL");

        imtkw = new HashMap<String, String>();
        imtkw.put("UNIQUE", "DISTINCT");
        imtkw.put("TRUNC", "TRUNCATE");
        imtkw.put("INTEGER", "SIGNED");
        imtkw.put("NUMERIC", "SIGNED INTEGER");
        imtkw.put("TEMP", "TEMPORARY");
    }
}
