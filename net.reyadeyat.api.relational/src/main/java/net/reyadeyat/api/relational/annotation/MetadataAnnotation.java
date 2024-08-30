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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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
@Retention(RetentionPolicy.RUNTIME)
public @interface MetadataAnnotation {
    //String element();//table, field
    boolean table() default false;
    boolean field() default false;
    boolean lookup() default false;
    boolean indexed() default true;
    String indexed_expresion() default "";
    int minCardinality() default 0;//minimum occurenmces of records  0==nullable and can be null or no records
    int maxCardinality() default -1;//maximum occurenmces of records -1==unlimited
    //String type();// primary key, foreign key
    String name() default "";//element name "" means use field name
    String title() default "";//element title "" means use capitalized initials of name
    boolean nullable() default false;//true, false
    String type() default "VARCHAR(256)";//string, integer, double, time
    String clas() default "";//string, integer, double, time
    String format() default "";//#,#00.00 YYYY-MM-DDT01:41:01+02:00
    boolean ignore() default false;//ignore walk/save/load this class instance
}
