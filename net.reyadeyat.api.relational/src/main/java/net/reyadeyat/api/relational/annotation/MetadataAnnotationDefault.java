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
public class MetadataAnnotationDefault implements MetadataAnnotation {
    
    private String name;
    private String type;
    private String clas;
    private String indexed_expresion;
    
    public MetadataAnnotationDefault(String name, String type, String clas, String indexed_expresion) {
        this.name = name;
        this.type = type;
        this.clas = clas;
        this.indexed_expresion = indexed_expresion == null ? "" : indexed_expresion;
    }
    
    @Override
    public boolean table() {
        return false;
    }
    
    @Override
    public boolean field() {
        return false;
    }
    
    @Override
    public boolean lookup() {
        return false;
    }

    @Override
    public boolean indexed() {
        return true;
    }
    
    @Override
    public String indexed_expresion() {
        return indexed_expresion;
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
    public String title() {
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
    public boolean ignore() {
        return false;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
