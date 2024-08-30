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

import static net.reyadeyat.api.relational.database.SqlParser.sqlConvert;
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
public class SqlParser2 {

    final static public ArrayList<String> kw = new ArrayList<String>(Arrays
            .asList(new String[]{"select", "insert", "update", "delete",
        "distinct", "as", "if", "from", "inner", "outer", "left",
        "right", "join", "on", "where", "order", "by", "group",
        "having", "asc", "desc", "limit", "skip", "first",
        "offset", "unique", "between", "in", "like", "and", "or",
        "order"}));

    final static public String symbol = "./*-+()[]|<>,;:\\'\"";

    final static public HashMap<String, String> imtf = new HashMap<String, String>() {
        {
            put("TO_CHAR", "DATE_FORMAT");
            put("TO_DATE", "DATE_FORMAT");
            put("NVL", "IFNULL");
        }
    };

    final static public HashMap<String, String> imtkw = new HashMap<String, String>() {
        {
            put("UNIQUE", "DISTINCT");
            put("TRUNC", "TRUNCATE");
            put("INTEGER", "SIGNED");
            put("NUMERIC", "SIGNED INTEGER");
            put("TEMP", "TEMPORARY");
        }
    };

    final static public StringBuffer informixt_to_mysql_parser(String ii)
            throws Exception {
        String symbol = "/*-+()'[]|<>,;:\\\r\n\t ";
        String ignore = "\r\n\t ";
        Character sc = '\'';
        Character esc = '\"';
        Character escr = null;
        ArrayList<String> mm;
        StringBuffer fisk, ms = new StringBuffer(), b = new StringBuffer();
        mm = new ArrayList<String>();
        boolean s = false, p = false;
        char c, pc, nc;
        c = pc = nc = '\0';
        int l = ii.length();
        for (int x = 0; x < l; x++) {
            c = ii.charAt(x);
            nc = x == l - 1 ? '\0' : ii.charAt(x + 1);
            if (s == false && (symbol.indexOf(c) > -1)) {
                if (b.length() > 0) {
                    mm.add(b.toString());
                    b.delete(0, b.length());
                }
                if (ignore.indexOf(c) > -1) {
                    b.setLength(0);
                    continue;
                }
                if (symbol.indexOf(c) > -1) {
                    if (b.length() > 0) {
                        mm.add(b.toString());
                        b.setLength(0);
                    }
                    mm.add(String.valueOf(c));
                }
                if (s == false && c == sc) {
                    s = true;
                }
            } else if (s == true && c == esc && nc == sc) {
                if (escr == null) {
                    b.append(esc).append(sc);
                } else {
                    b.append(escr);
                }
                x++;
            } else if (s == true && c == sc) {
                if (b.length() > 0) {
                    mm.add(b.toString());
                    b.setLength(0);
                }
                mm.add(String.valueOf(c));
                s = false;
            } else {
                b.append(c);
            }
            pc = c;
        }
        if (b.length() > 0) {
            mm.add(b.toString());
            b.delete(0, b.length());
        }
        b.setLength(0);

        int ml = mm.size();
        Boolean st = false;
        Boolean concatenate = false;
        String sm0 = "", sm = "", sm1 = mm.get(0);
        fisk = b.append(" ");
        for (int i = 0; i < ml; i++) {
            sm = sm1;
            sm1 = (i + 1 == ml ? "" : mm.get(i + 1));
            if (sm.equalsIgnoreCase("'") == true) {
                ms.append(sm);
                ms.append((st = !st) ? "" : " ");
            } else if (st == true) {
                ms.append(sm);
            } else if (sm.equalsIgnoreCase("extend") == true) {
                ++i;
                ms.append(mm.get(++i));
                while (mm.get(++i).equalsIgnoreCase(")") == false)
					;
                sm1 = (i + 1 == ml ? "" : mm.get(i + 1));
            } else if (imtkw.containsKey(sm.toUpperCase()) == true) {
                ms.append(imtkw.get(sm.toUpperCase())).append(
                        "(,".indexOf(sm) > -1 ? "" : " ");
            } else if (imtf.containsKey(sm.toUpperCase()) == true
                    && sm1.equalsIgnoreCase("(")) {
                ms.append(imtf.get(sm.toUpperCase()));

            } else if (sm.equalsIgnoreCase("cast") == true) {
                ms.append(sm);
                while ((sm = mm.get(++i)).equalsIgnoreCase("AS") == false) {
                    ms.append(sm).append(" ");
                }
                ms.append(sm).append(" ");
                sm = mm.get(++i);
                if (imtkw.containsKey(sm.toUpperCase()) == true) {
                    ms.append(imtkw.get(sm.toUpperCase()));
                } else if (sm.equalsIgnoreCase("DATETIME") == true) {
                    ms.append("DATETIME) ");
                    while (p && mm.get(++i).equalsIgnoreCase(")") == false)
						;
                } else if (sm.equalsIgnoreCase("INTEGER") == true) {
                    ms.append("SIGNED) ");
                    while (p && mm.get(++i).equalsIgnoreCase(")") == false)
						;
                } else {
                    throw new Exception("CAST AS '" + sm + "' is not defined");
                }
                sm1 = (i + 1 == ml ? "" : mm.get(i + 1));
            } else if (sm1.equalsIgnoreCase("[") == true) {
                ++i;
                int f = Integer.parseInt(mm.get(++i).toString());
                ++i;
                int t = Integer.parseInt(mm.get(++i).toString()) - f;
                ms.append("SUBSTRING(").append(sm).append(", ").append(f)
                        .append(", ").append(t).append(")");
                ++i;
                sm1 = (i + 1 == ml ? "" : mm.get(i + 1));
            } else if (sm.equalsIgnoreCase(":") == true
                    && sm1.equalsIgnoreCase(":") == true) {
                ms.delete(ms.length() - sm0.length() - 1, ms.length());
                ++i;
                sm = mm.get(++i);
                ms.append("CAST(").append(sm0).append(" AS ").append(
                        imtkw.get(sm.toUpperCase())).append(")");
                sm1 = (i + 1 == ml ? "" : mm.get(i + 1));
            } else if (sm.equalsIgnoreCase("skip") == true) {
                fisk.append("LIMIT ").append(
                        Integer.parseInt(mm.get(i + 3).toString())).append(" ");
                fisk.append("OFFSET ").append(
                        Integer.parseInt(mm.get(i + 1).toString()));
                i += 3;
                sm1 = (i + 1 == ml ? "" : mm.get(i + 1));
                sm = "";
            } else if (sm.equalsIgnoreCase("first") == true) {
                fisk.append("LIMIT ").append(
                        Integer.parseInt(mm.get(++i).toString())).append(" ");
                sm1 = (i + 1 == ml ? "" : mm.get(i + 1));
                sm = "";
            } else if (sm.equalsIgnoreCase("AS") || sm.equalsIgnoreCase(")")
                    || sm.equalsIgnoreCase(",")) {
                if (concatenate == true) {
                    ms.append(") ");
                }
                ms.append(sm).append(" ");
            } else if (sm.equalsIgnoreCase("|") == true
                    && sm1.equalsIgnoreCase("|")) {
                if (concatenate == false) {
                    ms.insert(ms.length() - sm0.length() - 1, "CONCAT(");
                    concatenate = true;
                }
                ms.append(", ");
                i++;
                sm = (i == ml ? "" : mm.get(i));
                sm1 = (i + 1 == ml ? "" : mm.get(i + 1));
                /*
				 * while ((sm = mm.get(++i)).equalsIgnoreCase(",") == false) {
				 * if (sm.equalsIgnoreCase("|") == true) { i++; ms.append(","); }
				 * else { ms.append(sm); } } ms.append("),");
                 */
                // sm1 = (i + 1 == ml ? "" : mm.get(i + 1));
            } else if (sm.equalsIgnoreCase("CASE") == true) {
                ms.append(sm);
                Boolean str = false;
                while ((sm = mm.get(++i)).equalsIgnoreCase("end") == false) {
                    if (sm.equalsIgnoreCase("|") == true) {
                        i++;
                        ms.append(", ");
                    } else {
                        if (sm.equals("'")) {
                            str = !str;
                        }
                        ms.append(sm).append(str == true ? "" : " ");

                    }
                }
                ms.append(sm).append(" ");
                sm1 = (i + 1 == ml ? "" : mm.get(i + 1));
            } else {
                // ms.append(sm).append("'(.".indexOf(sm) > -1 ||
                // "'.,()".indexOf(sm1) > -1 ? "" : "<>".indexOf(sm) > -1 &&
                // ">=".indexOf(sm1) > -1 ? "" : " ");
                ms.append(sm).append(
                        "(.".indexOf(sm) > -1 || ".,(".indexOf(sm1) > -1 ? ""
                        : "<>".indexOf(sm) > -1
                        && ">=".indexOf(sm1) > -1 ? "" : " ");
            }
            sm0 = sm;
        }

        return ms.append(fisk);
    }

    private static String sqlParserInformixToMySql(String strSql) {
        String newSql = "";
        try {
            newSql = informixt_to_mysql_parser(strSql.replace("\n", ""))
                    .toString();
        } catch (Exception e) {
            String errMsg = "Error in Sql : " + strSql + " >> Exception "
                    + e.getMessage();
            //String fileName = "SqlParserLog";
            //Logger.writeLog(errMsg, "SqlParser", fileName);
            return (strSql);
        }
        return (newSql);
    }

    public static void main(String[] args) throws Exception {
        System.err.println(sqlConvert("select 'test' || case (mast.customer_type)     when 1 then '???? ?????'     when 2 then '?????'     when 3 then '????'      when 4 then ' ????'      when 5 then '???'     when 6 then '?????'    when 7 then '????'    else ''     end as Customertype      from trn_inv_add_mast mast   inner join trn_inv_add_det det   		 on mast.add_serial = det.add_serial   inner join cde_inv_item_code ic   		 on ic.item_code = det.item_code   inner join	trn_inv_op_balance_det opdet   		 on det.item_code = opdet.item_code   inner join	trn_inv_op_balance_mast opmast on opmast.year_code = mast.year_code  and opmast.store_code = mast.store_code and opmast.branch_code = mast.branch_code  and opmast.balance_serial = opdet.balance_serial  inner join cde_inv_stores st   		 on st.store_code = mast.store_code   inner join cde_inv_stores_type sty   		 on sty.store_type_code = st.store_type_code   inner join cde_inv_finance_year fy  		 on fy.year_code = mast.year_code   inner join department br  		 on br.department_id = mast.branch_code   inner join cde_inv_item_status items   		 on items.item_status_code = det.item_status_code  inner join cde_inv_item_unit iu  		on iu.item_unit_code = ic.item_unit_code   left outer join cde_inv_store_strg_places issp   		 on mast.store_code = issp.store_code where mast.doc_date between '2019/01/01' and '2019/12/31' order by mast.doc_no, det.item_code"));
        System.err.println(sqlConvert("select '2019/01/01'"));
        System.err.println(sqlConvert("select (mast.customer_type::NUMERIC) as customer_type from appmenu"));
        System.err.println(sqlConvert("select max(cast(mnuid as Integer)) as CODE from appmenu"));
        System.err.println(sqlConvert("create table temp(x integer);"));
        System.err.println(sqlConvert("select skip 0 first 50  cde_hr_emp_data.*  from cde_hr_emp_data"));
        System.err.println(sqlConvert("select sum(nvl(credits,0)) , sum(nvl(entry_credits,0)) ,sum(nvl(remaining_value,0)) from fy_accounts_costs_credits_values "));
        System.err.println(sqlConvert("select REPLACE(cde_hr_job.job_name_ar, '\', ' ') from cde_hr_job"));

        System.err.println(sqlConvert("select prl_instalments.*  from prl_instalments where TO_DATE(year_code || '/' || month_code || '/' || '01', '%Y/%m/%d') <= TO_DATE('2020/10/01', '%Y/%m/%d')  "));
	System.err.println(sqlConvert("select max(cast(mnuid as Integer)) as CODE from appmenu"));
	System.err.println(sqlConvert("create table temp(x integer);"));
	System.err.println(sqlConvert("select  case (mast.customer_type)     when 1 then '???? ?????'     when 2 then '?????'     when 3 then '????'      when 4 then ' ????'      when 5 then '???'     when 6 then '?????'    when 7 then '????'    else ''     end as Customertype      from trn_inv_add_mast mast   inner join trn_inv_add_det det   		 on mast.add_serial = det.add_serial   inner join cde_inv_item_code ic   		 on ic.item_code = det.item_code   inner join	trn_inv_op_balance_det opdet   		 on det.item_code = opdet.item_code   inner join	trn_inv_op_balance_mast opmast on opmast.year_code = mast.year_code  and opmast.store_code = mast.store_code and opmast.branch_code = mast.branch_code  and opmast.balance_serial = opdet.balance_serial  inner join cde_inv_stores st   		 on st.store_code = mast.store_code   inner join cde_inv_stores_type sty   		 on sty.store_type_code = st.store_type_code   inner join cde_inv_finance_year fy  		 on fy.year_code = mast.year_code   inner join department br  		 on br.department_id = mast.branch_code   inner join cde_inv_item_status items   		 on items.item_status_code = det.item_status_code  inner join cde_inv_item_unit iu  		on iu.item_unit_code = ic.item_unit_code   left outer join cde_inv_store_strg_places issp   		 on mast.store_code = issp.store_code  where mast.doc_date between '2019/01/01' and '2019/12/31'  order by mast.doc_no, det.item_code"));
	System.err.println(sqlConvert("select skip 0 first 50  cde_hr_emp_data.*  from cde_hr_emp_data"));
	System.err.println(sqlConvert("select sum(nvl(credits,0)) , sum(nvl(entry_credits,0)) ,sum(nvl(remaining_value,0)) from fy_accounts_costs_credits_values "));
	System.err.println(sqlConvert("select REPLACE(cde_hr_job.job_name_ar, '\', ' ') from cde_hr_job"));
    }

    public static String sqlConvert(String strSql) {
        try {
            //String targetDataBase = Config.getString("TargetDataBase");
            String targetDataBase = "mysql";
            if (targetDataBase.equalsIgnoreCase("mysql")) {
                String strSqlNew = sqlParserInformixToMySql(strSql);
                return (strSqlNew);
            }
        } catch (Exception e) {
            System.err.println("Error sqlConvert >> " + e.getMessage());
        }
        return (strSql);
    }
}
