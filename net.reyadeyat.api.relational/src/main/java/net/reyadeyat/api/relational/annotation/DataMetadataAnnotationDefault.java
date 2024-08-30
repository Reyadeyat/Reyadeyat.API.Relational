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

package net.reyadeyat.api.relational.annotation;

import java.lang.annotation.Annotation;

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
public class DataMetadataAnnotationDefault implements DataMetadataAnnotation {
    
    private String name;
    private String type;
    private String clas;
    
    public DataMetadataAnnotationDefault(String name, String type, String clas) {
        this.name = name;
        this.type = type;
        this.clas = clas;
    }

    @Override
    public String element() {
        return "";
    }

    @Override
    public boolean indexed() {
        return true;
    }

    @Override
    public int minCardinality() {
        return 0;
    }

    @Override
    public int maxCardinality() {
        return -1;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean nullable() {
        return false;
    }

    @Override
    public String type() {
        return type;
    }
    
    @Override
    public String clas() {
        return "";
    }

    @Override
    public String format() {
        return "";
    }

    @Override
    public String codeCategory() {
        return "";
    }

    @Override
    public boolean ignore() {
        return false;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
