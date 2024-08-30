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

import java.util.Objects;

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
public class CompositObject {
    Integer x;
    String y;
    
    /**
     *
     * @param x
     * @param y
     */
    public CompositObject(Integer x, String y) {
        this.x = x;
        this.y = y;
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof CompositObject) {
            CompositObject composit_object = (CompositObject) obj;
            return ((x == null && composit_object.x == null) || (x != null && x.equals(composit_object.x)))
                && ((y == null && composit_object.y == null) || (y != null && y.equals(composit_object.y)))
                ;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.x);
        hash = 97 * hash + Objects.hashCode(this.y);
        return hash;
    }
    
    public int compare(CompositObject co) {
        return 
                co == null ? 1 
                : x != null && co.x == null ? 1 
                : y != null && co.y == null ? 1 
                : x == null && co.x != null ? -1 
                : y == null && co.y != null ? -1 
                : Integer.compare(x, co.x) == 0 ? y.compareTo(co.y)
                : Integer.compare(x, co.x);
    }
    
    static public int compare(CompositObject o1, CompositObject o2) {
        if (o1 != null) {
            return o1.compare(o2);
        }
        if (o2 != null) {
            return 1;//o2 not null, higher than null
        }
        return 0;//both null
    }
    
    
    
}
