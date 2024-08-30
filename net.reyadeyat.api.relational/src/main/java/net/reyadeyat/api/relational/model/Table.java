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

import net.reyadeyat.api.relational.data.DataLookup;
import net.reyadeyat.api.relational.annotation.DontJsonAnnotation;
import java.util.ArrayList;
import java.util.Arrays;
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
public class Table {
    public String name;
    public Integer rows;
    
    @DontJsonAnnotation
    transient public TableInterfaceImplementationDataStructures table_interface_implementation_data_structures;
    
    public ArrayList<Field> field_list;
    public ArrayList<PrimaryKey> primary_key_list;
    public ArrayList<ForeignKey> foreign_key_list;
    //@MetadataAnnotation (table=true, title="ChildTable", name="child_table", nullable=true)
    //public ArrayList<String> child_table_list;
    public ArrayList<ChildTable> child_table_list;
    
    /**Analyse foreign_key_Map to get child table_list and generate possible paths for conceptual model*/
    transient public Boolean case_sensitive_sql;
    transient public Database database;
    transient public ArrayList<ArrayList<Table>> path_list;
    transient public ArrayList<ArrayList<Table>> parent_path_list;
    transient public ArrayList<ArrayList<Table>> cyclic_reference_paths;
    transient public TreeMap<String, Field> field_map;
    
    transient public DataLookup data_lookup;
    transient private static ArrayList<String> lang_suffix_list = new ArrayList<>(Arrays.asList(new String[]{"_ar", "_en"}));
    
    public Table() {
        field_list = new ArrayList<Field>();
        primary_key_list = new ArrayList<PrimaryKey>();
        foreign_key_list = new ArrayList<ForeignKey>();
        child_table_list = new ArrayList<ChildTable>();
        path_list = new ArrayList<ArrayList<Table>>();
        parent_path_list = new ArrayList<ArrayList<Table>>();
        cyclic_reference_paths = new ArrayList<ArrayList<Table>>();
        field_map = new TreeMap<String, Field>();
    }
    
    /**no-arg default constructor for jaxb marshalling*/
    public Table(TableInterfaceImplementationDataStructures table_interface_implementation_data_structures) {
        this();
        this.table_interface_implementation_data_structures = table_interface_implementation_data_structures;
    }
    
    public Table(String name, Boolean case_sensitive_sql, Integer rows, DataLookup data_lookup, TableInterfaceImplementationDataStructures table_interface_implementation_data_structures) {
        this(table_interface_implementation_data_structures);
        this.name = name;
        this.rows = rows;
        this.case_sensitive_sql = case_sensitive_sql;
        this.data_lookup = data_lookup;
    }
    
    public void init() {
        for (Field field : field_list) {
            field.init();
            field_map.put(field.name, field);
        }
    }
    
    public void addField(Field field) throws Exception {
        field.table = this;
        field_list.add(field);
    }
    
    public void addPrimaryKey(PrimaryKey primary_key) throws Exception {
        primary_key.table = this;
        primary_key_list.add(primary_key);
        for (Field field : field_list) {
            for (PrimaryKeyField primary_key_field : primary_key.primary_key_field_list) {
                if (field.name.equalsIgnoreCase(primary_key_field.name)) {
                    field.setPrimaryKey();
                }
            }
        }
    }
    
    public void addForeignKey(ForeignKey foreignKey) throws Exception {
        foreignKey.table = this;
        foreign_key_list.add(foreignKey);
        
        String referenced_key_table_name = new String(foreignKey.referenced_key_table_name);
        Table parentTable = database.table_list.stream().filter(o -> o.name.equals(referenced_key_table_name)).findAny().orElse(null);
        ChildTable child_table = new ChildTable(parentTable, this, foreignKey, parentTable.name, this.name, foreignKey.name, this.case_sensitive_sql);
        parentTable.addChildTable(child_table);
    }
    
    public void addChildTable(ChildTable child_table) {
        child_table.parentTable = this;
        child_table_list.add(child_table);
    }
    
    public void generateModelDataStructures() throws Exception {
        this.table_interface_implementation_data_structures.generateModelDataStructures(this);
    }
    
    public boolean isFieldPrimaryKey(String field_name) {
        for (PrimaryKey primary_key : this.primary_key_list) {
            if (primary_key.isFieldPrimaryKey(field_name) == true) {
                return true;
            }
        }
        return false;
    }
    
    public Boolean hasParent(Table table) throws Exception {
        if (table == null) {
            throw new Exception("table cann not be null");
        }
        for (ChildTable child_table : table.child_table_list) {
            if ((case_sensitive_sql == true && name.equals(child_table.table_name))
                    || (case_sensitive_sql == false && name.equalsIgnoreCase(child_table.table_name))) {
                return true;
            }
        }
        return false;
    }
    
    public Boolean hasChild(Table table) throws Exception {
        if (table == null) {
            throw new Exception("table cann not be null");
        }
        for (ChildTable child_table : child_table_list) {
            if ((case_sensitive_sql == true && table.name.equals(child_table.table_name))
                    || (case_sensitive_sql == false && table.name.equalsIgnoreCase(child_table.table_name))) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String toString() {
        StringBuilder appendable = new StringBuilder();
        try {
            toString(appendable, 0, 4);
            return appendable.toString();
        } catch (Exception exception) {
            appendable.delete(0, appendable.length());
            appendable.append("Table: ").append(name).append(" Rows [").append(rows).append("] Fields [").append(field_list.size()).append("] Primary Keys [").append("] Foerign Keys [").append("]").append("] Child Tables [").append(child_table_list.size()).append("]").toString();
            appendable.append("toString '").append(name).append("' error").append(exception.getMessage());
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "toString error", exception);
        }
        return appendable.toString();
    }
    
    public void toString(Appendable appendable, Integer level, Integer shift) throws Exception {
        appendable.append("\n");
        for (int i = 0; i < level * shift; i++) {
            /*if (i%4 == 0) {
                appendable.append("|");
            } else {
                appendable.append("_");
            }*/
            appendable.append(" ");
        }
        appendable.append("|");
        for (int i = 0; i < shift - 1; i++) {
            appendable.append(".");
        }
        appendable.append("Table: ").append(name).append(" Rows [").append(String.valueOf(rows)).append("] Fields [").append(String.valueOf(field_list.size())).append("] Primary Keys [").append(String.valueOf(primary_key_list.size())).append("] Foerign Keys [").append(String.valueOf(String.valueOf(foreign_key_list.size()))).append("]").append("] Child Tables [").append(String.valueOf(String.valueOf(child_table_list.size()))).append("]");
        for (Field field : field_list) {
            appendable.append(field.toString(level + 1, shift));
        }
        for (PrimaryKey primary_key : primary_key_list) {
            appendable.append(primary_key.toString(level + 1, shift));
        }
        for (ForeignKey foreignKey : foreign_key_list) {
            foreignKey.toString(appendable, level + 1, shift);
        }
        for (ChildTable child_table : child_table_list) {
            appendable.append(child_table.toString(level + 1, shift));
        }
    }
    
    public void toStringTableTree(Appendable appendable, Integer level, Integer shift, ArrayList<Table> table_path_list) throws Exception {
        appendable.append("\n");
        for (int i = 0; i < level * shift; i++) {
            /*if (i%4 == 0) {
                appendable.append("|");
            } else {
                appendable.append("_");
            }*/
            appendable.append(" ");
        }
        appendable.append("|");
        for (int i = 0; i < shift - 1; i++) {
            appendable.append(".");
        }
        appendable.append("[").append(String.valueOf(table_path_list.size())).append("]:[");
        for (int i=0; i < table_path_list.size(); i++) {
            Table t = table_path_list.get(i);
            appendable.append(t.name);appendable.append(".");
        }
        appendable.append("]:[").append(String.valueOf(child_table_list.size())).append("]");
        for (ChildTable child_table : child_table_list) {
            if (table_path_list.contains(child_table.table)) {
                appendable.append(" [Cyclic Child `" + child_table.parentTable.name + "` To Parent `" + child_table.table.name + "` Reference] - Stop Tree Traversing");
            } else {
                ArrayList<Table> table_path_list_copy = new ArrayList<>(table_path_list);
                table_path_list_copy.add(child_table.table);
                child_table.table.toStringTableTree(appendable, level + 1, shift, table_path_list_copy);
            }
        }
    }
    
    public void compileTablePaths(ArrayList<Table> table_path_list, ArrayList<ArrayList<Table>> returnedTablesPaths, Boolean is_building_model) throws Exception {
        if (table_path_list.size() > 0) {
            parent_path_list.add(new ArrayList<>(table_path_list));
        }
        table_path_list.add(this);
        returnedTablesPaths.add(table_path_list);
        for (ChildTable child_table : child_table_list) {
            if (table_path_list.contains(child_table.table)) {
                cyclic_reference_paths.add(new ArrayList<>(table_path_list));
            } else {
                ArrayList<Table> tables_path_list_copy = new ArrayList<>(table_path_list);
                child_table.table.compileTablePaths(tables_path_list_copy, returnedTablesPaths, is_building_model);
                /*if ((case_sensitive_sql == true && name.equals("sys_measurement_system"))
                        || (case_sensitive_sql == false && name.equalsIgnoreCase("sys_measurement_system"))){
                    name = name;
                }*/
            }
        }
        for (int i = 0; i < returnedTablesPaths.size(); i++) {
            ArrayList<Table> returned_path = returnedTablesPaths.get(i);
            if (returned_path.contains(this)) {
                path_list.add(returned_path);
            }
        }
        
        if (is_building_model == true) {
            generateModelDataStructures();
        }
    }
    
    private void replace(StringBuilder string, String replace, String with) {
        Integer replace_position = string.indexOf(replace);
        string.replace(replace_position, replace_position+replace.length(), with);
    }
    
    private void insertBefore(StringBuilder string, String find, String insert) {
        Integer find_position = string.indexOf(find);
        string.insert(find_position, insert);
    }
    
    private void delete(StringBuilder string, String find) {
        replace(string, find, "");
    }
    
    private static transient String nl = "\n";
    /*
    //To-Do
    //Foreign List onChange
    //fillForm with selected icons
    //submitForm()
    //resetForm
    //Flag Record for Update New Deleted
    //insertSubscriptionMethod
    //selectSubscriptionMethod
    //updateSubscriptionMethod
    //deleteSubscriptionMethod
    //TinyInt(1) checkbox
    
    private static transient String field_method = "addField(\"$FIELD_ALIAS\", FieldType.$FIELD_TYPE, $FIELD_NULLABLE, false, \"$FIELD_NAME\")";
    private static transient String foreign_key_method = "addForeignKey(\"$FOREIGN_KEY_NAME\", \"$TABLE_FIELD_NAME\", \"$REFERENCE_TABLE_NAME\", \"$REFERENCE_TABLE_FIELD_NAME\")";
    private static transient String insert_request = "{\"transaction\": \"insert\",\"values\": [],\"parameters\":[]}";
    private static transient String select_request = "{\"transaction\":\"select\",\"engine\":\"memory\",\"view\":\"object\",\"select\":[],\"where\":{\"clause\":\"\", \"values\":[]},\"orderby\":[]}";
    private static transient String update_request = "{\"transaction\":\"update\",\"values\":[],\"where\":{\"clause\":\"\", \"field_list\":[]},\"parameters\":[]}";
    private static transient String delete_request = "{\"transaction\":\"delete\",\"values\":[],\"where\":{\"clause\":\"\", \"field_list\":[]}}";
    
    private static transient String request_send_response = "	insert$TABLE_CLASS($TABLE_NAME: $TABLE_CLASS[]) {\n		this.insert_$TABLE_NAME_record_list.push($TABLE_NAME);\n		let servlet_url: string = Constants.tariff_url + \"/$TABLE_NAME\";\n		let http_headers: HttpHeaders = new HttpHeaders();\n		http_headers.set(\"Content-Type\", \"application/json; charset=UTF-8\");\n		let http_parameters: HttpParams = new HttpParams();\n		//http_parameters.set(\"\", \"\");\n		const request: DBInsert = <DBInsert><unknown>{\n			transaction: \"insert\",\n			values: [\n				{\n$INSERT_UPDATE_DELETE_FIELDS\n				}\n			],\n			parameters: []\n		}\n		this.sendPost(this, \"insert_$TABLE_NAME\", servlet_url, request, http_headers, http_parameters);\n	}\n\n" +
    "	select$TABLE_CLASS($TABLE_NAME: $TABLE_CLASS[]) {\n		let servlet_url: string = Constants.tariff_url + \"/$TABLE_NAME\";\n		let http_headers: HttpHeaders = new HttpHeaders();\n		http_headers.set(\"Content-Type\", \"application/json; charset=UTF-8\");\n		let http_parameters: HttpParams = new HttpParams();\n		//http_parameters.set(\"\", \"\");\n		const request: DBSelect = {\n			transaction: \"select\",\n			engine: \"memory\",\n			view: \"object\",\n			select: [\n$SELECT_FIELDS\n			],\n			where: {\n				clause: \"\",\n				values: []\n			},\n			orderby: []\n		}\n		this.sendPost(this, \"select_$TABLE_NAME\", servlet_url, request, http_headers, http_parameters);\n	}\n\n" +
    "	update$TABLE_CLASS($TABLE_NAME: $TABLE_CLASS[]) {\n		let servlet_url: string = Constants.tariff_url + \"/$TABLE_NAME\";\n		let http_headers: HttpHeaders = new HttpHeaders();\n		http_headers.set(\"Content-Type\", \"application/json; charset=UTF-8\");\n		let http_parameters: HttpParams = new HttpParams();\n		//http_parameters.set(\"\", \"\");\n		const request: DBUpdate = <DBUpdate><unknown>{\n			transaction: \"update\",\n			returns: 'id',\n			//variable?: {};\n			values: [\n				{\n$INSERT_UPDATE_DELETE_FIELDS\n				}\n			],\n			where: {\n				clause: \"\",\n				field_list: [\n					\n				]\n			},\n			parameters: {}\n		}\n		this.sendPost(this, \"update_$TABLE_NAME\", servlet_url, request, http_headers, http_parameters);\n	}\n\n" +
    "	delete$TABLE_CLASS($TABLE_NAME: $TABLE_CLASS[]) {\n		let servlet_url: string = Constants.tariff_url + \"/$TABLE_NAME\";\n		let http_headers: HttpHeaders = new HttpHeaders();\n		http_headers.set(\"Content-Type\", \"application/json; charset=UTF-8\");\n		let http_parameters: HttpParams = new HttpParams();\n		//http_parameters.set(\"\", \"\");\n		const request: DBUpdate = <DBUpdate><unknown>{\n			transaction: \"delete\",\n			values: [\n				{\n$INSERT_UPDATE_DELETE_FIELDS\n				}\n			],\n			where: {\n			  clause: \"\",\n			  field_list: [\n				\n			  ]\n			}\n		}\n		this.sendPost(this, \"delete_$TABLE_NAME\", servlet_url, request, http_headers, http_parameters);\n	}\n\n" +
    "	$TABLE_NAME_list: $TABLE_CLASS[] = [];\n	selected_$TABLE_NAME_option: $TABLE_CLASS;\n	selected_$TABLE_NAME_record: $TABLE_CLASS;\n\n	insert_$TABLE_NAME_record_list: $TABLE_CLASS[] = [];\n	update_$TABLE_NAME_record_list: $TABLE_CLASS[] = [];\n	delete_$TABLE_NAME_record_list: $TABLE_CLASS[] = [];\n\n	//selected_$TABLE_NAME_list_icon: any = blank_icon;\n	\n	} else if (key === \"insert_$TABLE_NAME\") {\n		//let resultset: any[] = response.resultset;\n		//let resultset: any = response.resultset;\n		//let generated_id: any = response.generated_id;\n		this.resetForm();\n		for (let i: number = 0; i < this.insert_$TABLE_NAME_record_list.length; i++) {\n			this.$TABLE_NAME_list.push(this.insert_$TABLE_NAME_record_list[i]);\n		}\n		this.insert_$TABLE_NAME_record_list = [];\n		this.$TABLE_NAME_table_data_source.data = this.$TABLE_NAME_list;\n		//this.snackbar.open(this.i18.insert_success, \"x\", {duration: 5 * 1000});\n	} else if (key === \"select_$TABLE_NAME\") {\n		let resultset: any[] = response.resultset;\n		this.$TABLE_NAME_list = [];\n		for (let i: number = 0; resultset != null && i < resultset.length; i++) {\n			this.$TABLE_NAME_list.push($TABLE_CLASS.fromJSON(resultset[i]));\n			//Manipulate.loadImage(this.$TABLE_NAME_list[i].$TABLE_NAME_icon);\n		}\n\n		this.$TABLE_NAME_table_data_source.data = this.$TABLE_NAME_list;\n		//this.snackbar.open(this.i18.select_success, \"x\", {duration: 5 * 1000});\n	} else if (key === \"update_$TABLE_NAME\") {\n		//this.snackbar.open(this.i18.update_success, \"x\", {duration: 5 * 1000});\n	} else if (key === \"delete_$TABLE_NAME\") {\n		//this.snackbar.open(this.i18.delete_success, \"x\", {duration: 5 * 1000});\n	}\n\n";
    
    private static transient String typescript_form_field_control_class_members = "$FIELD_NAME_control: FormControl;"; //$TYPESCRIPT_FORM_FIELD_CONTROL_CLASS_MEMBERS
    private static transient String typescript_form_foreign_field_control_class_members = "$FOREIGN_FIELD_NAME_list_control: FormControl;"; //$TYPESCRIPT_FORM_FOREIGN_FIELD_CONTROL_CLASS_MEMBERS
    private static transient String typescript_form_foreign_field_array_class_members = "$FOREIGN_FIELD_NAME_array_control: FormArray;"; //$TYPESCRIPT_FORM_FOREIGN_FIELD_ARRAY_CLASS_MEMBERS
    private static transient String typescript_form_field_control_definition = "this.$FIELD_NAME_control = new FormControl([]);"; //$TYPESCRIPT_FORM_FIELD_CONTROL_DEFINITION
    private static transient String typescript_form_foreign_field_control_definition = "this.$FOREIGN_FIELD_NAME_list_control = new FormControl([]);"; //$TYPESCRIPT_FORM_FOREIGN_FIELD_CONTROL_DEFINITION
    private static transient String typescript_form_foreign_field_array_definition = "this.$FOREIGN_FIELD_NAME_array_control = new FormArray([]);"; //$TYPESCRIPT_FORM_FOREIGN_FIELD_ARRAY_DEFINITION
    private static transient String typescript_form_field_control = "$FIELD_NAME: this.$FIELD_NAME_control,"; //$TYPESCRIPT_FORM_FIELD_CONTROL
    private static transient String typescript_form_foreign_field_control = "$FOREIGN_FIELD_NAME_check_list: this.$FOREIGN_FIELD_NAME_array_control,"; //$TYPESCRIPT_FORM_FOREIGN_FIELD_CONTROL
    private static transient String form_component_ts = 
"	$TABLE_NAME_form: FormGroup;\n" +
"$TYPESCRIPT_FORM_FIELD_CONTROL_CLASS_MEMBERS\n\n" +
"$TYPESCRIPT_FORM_FOREIGN_FIELD_CONTROL_CLASS_MEMBERS\n\n" +
"$TYPESCRIPT_FORM_FOREIGN_FIELD_ARRAY_CLASS_MEMBERS\n\n" +
"	constructor() {\n\n" +
"$TYPESCRIPT_FORM_FIELD_CONTROL_DEFINITION\n" +
"$TYPESCRIPT_FORM_FOREIGN_FIELD_CONTROL_DEFINITION\n" +
"$TYPESCRIPT_FORM_FOREIGN_FIELD_CONTROL_DEFINITION\n\n" +
"$TYPESCRIPT_FORM_FOREIGN_FIELD_ARRAY_DEFINITION\n\n" +
"\n" +
"	this.$TABLE_NAME_form = form_builder.group({\n" +
"		$TABLE_NAME_group: form_builder.group({\n" +
"$TYPESCRIPT_FORM_FIELD_CONTROL\n" +
"$TYPESCRIPT_FORM_FOREIGN_FIELD_CONTROL\n" +
"		}),\n" +
"	},\n" +
"	//{ validators: passwordMatchValidator, asyncValidators: otherValidator }\n" +
"	);\n" +
"	}\n\n" +
"	setSelected$TABLE_CLASSRecord(selected_$TABLE_NAME_record: $TABLE_CLASS) {\n" +
"		this.selected_$TABLE_NAME_record = selected_$TABLE_NAME_record;\n" +
"	}\n" +
"	\n" +
"	ngOnChanges(changes: SimpleChanges): void {\n" +
"	\n" +
"$TYPESCRIPT_FORM_FOREIGN_FIELD_CHECK_BOX\n" +
"	\n" +
"	}";
    
    private static transient String typescript_form_foreign_field_check_box = 
"		this.$FOREIGN_TABLE_NAME_list.forEach( ($FOREIGN_TABLE_NAME: $FOREIGN_TABLE_CLASS) => {\n" +
"			$FOREIGN_TABLE_NAME.is_checked = false;\n" +
"			let $FOREIGN_TABLE_NAME_control: FormControl = new FormControl($FOREIGN_TABLE_NAME);\n" +
"			$FOREIGN_TABLE_NAME_control.['option_value'] = $FOREIGN_TABLE_NAME;\n" +
"			$FOREIGN_TABLE_NAME_control.setValue($FOREIGN_TABLE_NAME_control['option_value']);\n" +
"			this.$FOREIGN_TABLE_NAME_array_control.push($FOREIGN_TABLE_NAME_control);\n" +
"		});";//$TYPESCRIPT_FORM_FOREIGN_FIELD_CHECK_BOX
    
    private static transient String form_component_html = 
"<div class=\"container\">\n" +
"    <p class=\"card\">{{i18.$TABLE_NAME.title')}}</p>\n" +
"<ng-container *ngIf=\"show_confirmation==false\">\n"+
"<form class=\"form yes-mouse\" [formGroup]=\"$TABLE_NAME_form\" [dir]=\"direction\">\n" +
"	<div role=\"group\" formGroupName=\"$TABLE_NAME_group\" [hidden]=\"false\" [dir]=\"direction\">\n" +
"		<div class=\"form-group\" [dir]=\"direction\">\n" +
"		\n" +
"$TYPESCRIPT_FORM_FIELD_HTML_TEXT_BOX_CONTROL\n" +
"			\n" +
"$TYPESCRIPT_FORM_FIELD_HTML_DATE_BOX_CONTROL\n" +
"		\n" +
"$TYPESCRIPT_FORM_FOREIGN_FIELD_HTML_CHECK_BOX_CONTROL\n" +
"			\n" +
"$TYPESCRIPT_FORM_FOREIGN_FIELD_HTML_LIST_BOX_CONTROL\n" +
"			\n" +
"        </div>\n" +
"	</div>\n" +
"</form>\n" +
"<div class=\"tool-box\" [dir]=\"direction\">{{'Controls - ' + i18.$TABLE_CLASS.title}}</div>\n" +
"<div class=\"form-group yes-mouse\" [dir]=\"direction\">\n" +
"<div class=\"tool-box yes-mouse\" [dir]=\"direction\">\n" +
"	<!--{{'Controls - ' + i18.$TABLE_CLASS.title}}-->\n" +
"	<img *ngIf=\"selected_$TABLE_NAME_record.record_state == null\" src=\"{{ICON.blank_icon}}\" (error)=\"ICON.blank_icon\" class=\"icon\" />\n" +
"	<img *ngIf=\"selected_$TABLE_NAME_record.record_state == INSERT\" src=\"{{ICON.insert_record_icon}}\" (error)=\"ICON.blank_icon\" class=\"icon\" />\n" +
"	<img *ngIf=\"selected_$TABLE_NAME_record.record_state == SELECT\" src=\"{{ICON.true_icon}}\" (error)=\"ICON.blank_icon\" class=\"icon\" />\n" +
"	<img *ngIf=\"selected_$TABLE_NAME_record.record_state == UPDATE\" src=\"{{ICON.update_record_icon}}\" (error)=\"ICON.blank_icon\" class=\"icon\" />\n" +
"	<img *ngIf=\"selected_$TABLE_NAME_record.record_state == DELETE\" src=\"{{ICON.delete_record_icon}}\" (error)=\"ICON.blank_icon\" class=\"icon\" />\n" +
"	<button class=\"tool-box-button-pushable yes-mouse\" matTooltip=\"{{i18.first_record}}\" [disabled]=\"cursor == 0\" (click)=\"firstRecord()\">\n" +
"		<span class=\"tool-box-button-front\" [dir]=\"direction\">\n" +
"			{{ i18.first }}\n" +
"		</span>\n" +
"	</button>\n" +
"	<button class=\"tool-box-button-pushable yes-mouse\" matTooltip=\"{{i18.previous_record}}\" [disabled]=\"cursor == 0\" (click)=\"previousRecord()\">\n" +
"		<span class=\"tool-box-button-front\" [dir]=\"direction\">\n" +
"			{{ i18.previous }}\n" +
"		</span>\n" +
"	</button>\n" +
"	<button class=\"tool-box-button-pushable yes-mouse\" matTooltip=\"{{i18.next_record}}\" [disabled]=\"cursor == this.$TABLE_NAME_list.length-1\" (click)=\"nextRecord()\">\n" +
"		<span class=\"tool-box-button-front\" [dir]=\"direction\">\n" +
"			{{ i18.next }}\n" +
"		</span>\n" +
"	</button>\n" +
"	<button class=\"tool-box-button-pushable yes-mouse\" matTooltip=\"{{i18.last_record}}\" [disabled]=\"cursor == this.$TABLE_NAME_list.length-1\" (click)=\"lastRecord()\">\n" +
"		<span class=\"tool-box-button-front\" [dir]=\"direction\">\n" +
"			{{ i18.last }}\n" +
"		</span>\n" +
"	</button>\n" +
"</div>\n" +
"<div class=\"form-group yes-mouse\" [dir]=\"direction\">\n" +
"	<button class=\"pushable-blue\" (click)=\"submitForm()\">\n" +
"		<span class=\"front-blue\" [dir]=\"direction\">\n" +
"			{{ i18.submit }}\n" +
"		</span>\n" +
"	</button>\n" +
"	<button class=\"pushable-red\" (click)=\"resetForm()\">\n" +
"		<span class=\"front-red\" [dir]=\"direction\">\n" +
"			{{ i18.reset }}\n" +
"		</span>\n" +
"	</button>\n" +
"</div>" +
"</ng-container>\n" +
"    <confirmation-component *ngIf=\"show_confirmation==true\" (onConfirmationAcknowledged)=\"confirmationAcknowledged($event)\"></confirmation-component>\n" +
"</div>\n";
    
    private static transient String typescript_form_field_html_text_box_control =
"			<!-- Text Box $FIELD_NAME -->\n" +
"			<mat-form-field [appearance]=\"outline\" [dir]=\"direction\">\n" +
"				<input formControlName=\"$FIELD_NAME\" matInput type=\"$FIELD_HTML_TYPE\"\n" +
"					placeholder=\"{{i18.$TABLE_NAME.$FIELD_NAME}}\"\n" +
"					required=\"$FIELD_IS_NOT_NULL\" $PRIMARY_KEY_TEXT_BOX \n" +
"					[ngClass]=\"{ 'is-invalid': isValidFieldValue('$TABLE_NAME_group.$FIELD_NAME') === false }\" />\n" +
"				<div *ngIf=\"isModified('$TABLE_NAME_group.$FIELD_NAME') && isValid('$TABLE_NAME_group.$FIELD_NAME') === false\" class=\"alert alert-danger\">\n" +
"					<ng-container *ngFor=\"let field_error of getFieldErrors('$TABLE_NAME_group.$FIELD_NAME')\">\n" +
"						<div class=\"warning\">{{field_error.error_message[lang]}}></div>\n" +
"					<ng-container>\n" +
"				</div>\n" +
"			</mat-form-field>\n"; //$TYPESCRIPT_FORM_FIELD_HTML_TEXT_BOX_CONTROL

    private static transient String typescript_form_field_html_date_box_control = 
"			<!-- Date Box $FIELD_NAME -->\n" +
"			<mat-form-field [appearance]=\"outline\" [dir]=\"direction\">\n" +
"				<mat-label>{{i18.$TABLE_NAME.$FIELD_NAME}}</mat-label>\n" +
"				<input formControlName=\"$FIELD_NAME\" matInput [matDatepicker]=\"datepicker\" placeholder=\"{{ i18.$TABLE_NAME.$FIELD_NAME }}\">\n" +
"				<mat-datepicker-toggle matPrefix [for]=\"datepicker\"></mat-datepicker-toggle>\n" +
"				<mat-datepicker #datepicker></mat-datepicker>\n" +
"			</mat-form-field>\n"; //$TYPESCRIPT_FORM_FIELD_HTML_DATE_BOX_CONTROL


    private static transient String typescript_form_foreign_field_html_check_box_control = 
"			<!-- $FOREIGN_FIELD_NAME_list -->\n" +
"			<ng-container formArrayName=\"$FOREIGN_FIELD_NAME_list\">\n" +
"			  <div>\n" +
"				<span>\n" +
"				  <mat-checkbox [checked]=\"isAllListChecked($FOREIGN_FIELD_NAME_check_list)\" [color]=\"primary\"\n" +
"					[indeterminate]=\"isIndeterminate($FOREIGN_FIELD_NAME_list)\"\n" +
"					(change)=\"selectAllList($event, $FOREIGN_FIELD_NAME_list)\">\n" +
"					<img src=\"{{$FOREIGN_FIELD_NAME_icon}}\" class=\"icon\" />\n" +
"					{{ i18.$FOREIGN_FIELD_NAME }}\n" +
"				  </mat-checkbox>\n" +
"				</span>\n" +
"			  </div>\n" +
"			  <div *ngFor=\"let $FOREIGN_FIELD_NAME_control of $FOREIGN_FIELD_NAME_array_control.controls; let i=index\">\n" +
"				<span>\n" +
"				  <mat-checkbox class=\"margin\" [formControl]=\"$FOREIGN_FIELD_NAME_control\"\n" +
"					[color]=\"primary\" [(ngModel)]=\"$FOREIGN_FIELD_NAME_list[i].is_checked\">\n" +
"					<img class=\"icon\" src=\"{{$FOREIGN_FIELD_NAME_list[i].advertising_method_icon}}\" (error)=\"onImageError($event)\">\n" +
"					{{$FOREIGN_FIELD_NAME_list[i].advertising_method_name_i18[lang]}}\n" +
"				  </mat-checkbox>\n" +
"				</span>\n" +
"			  </div>\n" +
"			</ng-container>\n"; //$TYPESCRIPT_FORM_FOREIGN_FIELD_HTML_CHECK_BOX_CONTROL

    private static transient String typescript_form_foreign_field_html_list_box_control = 
"			<!-- $FOREIGN_FIELD_NAME List Box -->\n" +
"			<mat-form-field [appearance]=\"outline\" [dir]=\"direction\">\n" +
"				<mat-label>{{ i18.$FOREIGN_FIELD_NAME }}</mat-label>\n" +
"					<span matPrefix [style.display]=\"'flex'\">\n" +
"					<img src=\"{{selected_$FOREIGN_TABLE_NAME_list_icon}}\" class=\"icon\" />\n" +
"				</span>\n" +
"				<mat-select formControlName=\"$FOREIGN_FIELD_NAME_list_option\" (selectionChange)=\"onChange$FOREIGN_TABLE_CLASS($event)\">\n" +
"					<mat-option *ngFor=\"let option of $FOREIGN_TABLE_NAME_list\" [value]=\"option\">\n" +
"					<span><img class=\"icon\" src=\"{{option.$FOREIGN_TABLE_NAME_icon}}\"\n" +
"					(error)=\"onImageError($event)\">{{option.$FOREIGN_TABLE_NAME_name_i18[lang]}}</span>\n" +
"					</mat-option>\n" +
"				</mat-select>\n" +
"			</mat-form-field>\n"; //$TYPESCRIPT_FORM_FOREIGN_FIELD_HTML_LIST_BOX_CONTROL

    private static transient String table_component_ts = 
"	$TABLE_NAME_table_data_source: MatTableDataSource<$TABLE_CLASS>;\n" +
"	$TABLE_NAME_table_columns: string[] = [\n" +
"$FIELD_LIST\n" +
"$FOREIGN_FIELD_LIST\n" +
"		\"select_record\",\n" +
"		\"delete_record\""+
"	];\n" +
"	\n" +
"	\n" +
"	constructor() {\n" +
"		this.$TABLE_NAME_list = [];\n" +
"		this.$TABLE_NAME_table_data_source = new MatTableDataSource(this.$TABLE_NAME_list);\n" +
"		this.$TABLE_NAME_table_data_source.data = this.$TABLE_NAME_list;\n" +
"	}\n" +
"	\n" +
"	\n";
    
    private static transient String table_component_html = 
"<mat-table class=\"just-table mat-elevation-z8\" [dataSource]=\"$TABLE_NAME_table_data_source\" [dir]=\"direction\">\n" +
"\n" +
"$TYPESCRIPT_TABLE_FIELD_HTML_PRIMARY_KEY_ICON_CONTROL\n"+
"	\n" +
"$TYPESCRIPT_TABLE_FIELD_HTML_I18_CONTROL\n"+
"\n" +
"$TYPESCRIPT_TABLE_FOREIGN_FIELD_HTML_I18_CONTROL\n" +
"	\n" +
"	<!-- extra controls -->\n" +
"	<ng-container matColumnDef=\"select_record\">\n" +
"		<mat-header-cell *matHeaderCellDef class=\"table-nice-back-blue\">{{i18.select_record}}</mat-header-cell>\n" +
"			<mat-cell *matCellDef=\"let $TABLE_NAME_record;\">\n" +
"				<button mat-raised-button color=\"nice-green\" class=\"yes-mouse\" (click)=\"selectRecord($TABLE_NAME_record)\">\n" +
"					<mat-icon>content_copy</mat-icon>\n" +
"				</button>\n" +
"		</mat-cell>\n" +
"	</ng-container>\n" +
"	<ng-container matColumnDef=\"delete_record\">\n" +
"		<mat-header-cell *matHeaderCellDef class=\"table-nice-back-blue\">{{i18.delete_record}}</mat-header-cell>\n" +
"			<mat-cell *matCellDef=\"let $TABLE_NAME_record;\">\n" +
"				<button mat-raised-button color=\"nice-green\" class=\"yes-mouse\" (click)=\"deleteRecord($TABLE_NAME_record)\">\n" +
"					<mat-icon>delete</mat-icon>\n" +
"				</button>\n" +
"		</mat-cell>\n" +
"	</ng-container>\n" +
"\n" +
"	<mat-header-row *matHeaderRowDef=\"$TABLE_NAME_table_columns\">\n" +
"	</mat-header-row>\n" +
"	<mat-row *matRowDef=\"let $TABLE_NAME_record; columns: $TABLE_NAME_table_columns\"></mat-row>\n" +
"</mat-table>";
    
    private static transient String typescript_table_field_html_primary_key_icon_control = 
"	<!-- Table Field $FIELD_NAME with _name_LANG -->\n" +
"	<ng-container matColumnDef=\"$FIELD_NAME\">\n" +
"		<mat-header-cell *matHeaderCellDef class=\"table-nice-back-blue\">{{i18.$TABLE_CLASS.$FIELD_NAME}}</mat-header-cell>\n" +
"		<mat-cell *matCellDef=\"let $TABLE_NAME_record;\">\n" +
"			<img src=\"{{$TABLE_NAME_record.$TABLE_NAME_icon}}\" class=\"icon\" />\n" +
"			{{$TABLE_NAME_record.$TABLE_NAME_name_i18[lang]}}\n" +
"		</mat-cell>\n" +
"	</ng-container>\n";//$TYPESCRIPT_TABLE_FIELD_HTML_PRIMARY_KEY_ICON_CONTROL
    
    private static transient String typescript_table_field_html_i18_control = 
"	<!-- Table -Field $FIELD_NAME -->\n" +
"	<ng-container matColumnDef=\"$TABLE_NAME_name_i18\">\n" +
"		<mat-header-cell *matHeaderCellDef class=\"table-nice-back-blue\">{{i18.$TABLE_CLASS.$TABLE_NAME_name_i18}}</mat-header-cell>\n" +
"		<mat-cell *matCellDef=\"let $TABLE_NAME_record;\">\n" +
"			{{$TABLE_NAME_record.$TABLE_NAME_name_i18[lang]}}\n" +
"		</mat-cell>\n" +
"	</ng-container>\n";//$TYPESCRIPT_TABLE_FIELD_HTML_I18_CONTROL
    
    private static transient String typescript_table_foreign_field_html_i18_control = 
"	<!-- Table ~Field $FOREIGN_FIELD_NAME with _name_LANG -->\n" +
"	<ng-container matColumnDef=\"$FOREIGN_FIELD_NAME\">\n" +
"		<mat-header-cell *matHeaderCellDef class=\"table-nice-back-blue\">{{i18.$FOREIGN_TABLE_CLASS.$FOREIGN_FIELD_NAME}}</mat-header-cell>\n" +
"		<mat-cell *matCellDef=\"let $TABLE_NAME_record;\">\n" +
"			<img src=\"{{$TABLE_NAME_record.$FOREIGN_TABLE_NAME.$FOREIGN_TABLE_NAME_icon}}\" class=\"icon\" />\n" +
"			{{$TABLE_NAME_record.$FOREIGN_TABLE_NAME.$FOREIGN_TABLE_NAME_name_i18[lang]}}\n" +
"		</mat-cell>\n" +
"	</ng-container>\n";//$TYPESCRIPT_TABLE_FOREIGN_FIELD_HTML_I18_CONTROL
    */
}
