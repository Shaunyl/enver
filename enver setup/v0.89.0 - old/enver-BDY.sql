create or replace 
PACKAGE BODY enver.encoding_verifier IS

PROCEDURE extract_chars(p_owner IN VARCHAR2, p_table IN VARCHAR2, p_array IN OUT t_schema)
AS

    tcursor       	INTEGER;
    l_status      	INTEGER;
    cols_count    	NUMBER := 0;
    col_type      	NUMBER(9, 0) := -1;
	  j               INTEGER := 1;

    tab           	dbms_sql.desc_tab;

    l_columnValue 	VARCHAR2(4000);
    v_query       	VARCHAR2(1000) := 'SELECT ';
    v_ext_name		  VARCHAR2(100) := p_owner || '.' || p_table;
    sql_stmt      	VARCHAR2(1000) DEFAULT 'SELECT column_name FROM all_tab_columns WHERE table_name = '''
                        || p_table || '''' || ' AND owner = ''' || p_owner || ''' AND data_type IN (''VARCHAR2'',''CHAR'')';

    TYPE c_all_tab_columns_type IS REF CURSOR;
    c_all_tab_columns c_all_tab_columns_type;

    column_name all_tab_columns.column_name%TYPE;

    e_char_cols  EXCEPTION;

BEGIN

    open c_all_tab_columns for
        'SELECT column_name FROM all_tab_columns WHERE table_name = ''' || p_table || '''' || ' AND owner = ''' || p_owner || ''' AND data_type IN (''VARCHAR2'',''CHAR'')';
    loop
      fetch c_all_tab_columns into column_name;

      exit when c_all_tab_columns%notfound;
          v_query := v_query || '"' || column_name || '", ';
    end loop;

    close c_all_tab_columns;

    IF v_query = 'SELECT ' THEN
      RAISE e_char_cols;
    ELSE
      p_array.schema_name := p_owner;
      p_array.a_tables.extend;
      p_array.a_tables(1) := t_table(p_table, null);
    END IF;

    v_query := substr(v_query, 1, length(v_query) - 2) || ' FROM ' || v_ext_name;

    tcursor := dbms_sql.open_cursor;

    dbms_sql.parse( tcursor, v_query, dbms_sql.native );
    dbms_sql.describe_columns( tcursor, cols_count, tab );

    p_array.a_tables(1).a_columns := t_columns();
    p_array.a_tables(1).a_columns.extend(cols_count);

    FOR i IN 1 .. cols_count LOOP

      dbms_sql.define_column( tcursor, i, l_columnValue, 4000 );
      col_type   := tab(i).col_type;

      IF col_type = 1 OR col_type = 9 OR col_type = 96 THEN --FIXME: forse è superfluo questo controllo.

        p_array.a_tables(1).a_columns(i) := t_column(tab(i).col_name, tab(i).col_max_len, null);
        l_status := dbms_sql.execute(tcursor);

        p_array.a_tables(1).a_columns(i).a_records := t_records();

        WHILE ( dbms_sql.fetch_rows(tcursor) > 0 ) LOOP
          dbms_sql.column_value( tcursor, i, l_columnValue );
          if l_columnValue is not null and ascii(l_columnValue) <> 32 then
            p_array.a_tables(1).a_columns(i).a_records.extend;
            p_array.a_tables(1).a_columns(i).a_records(j) := l_columnValue;
            j := j + 1;
          end if;
        END LOOP;
        j := 1;

      ELSE
        CONTINUE;
      END IF;
    END LOOP;

    dbms_sql.close_cursor(tcursor);

  EXCEPTION
    WHEN e_char_cols THEN
        dbms_output.put_line('No character data types found in table: ' || v_ext_name);
    WHEN OTHERS THEN
        dbms_sql.close_cursor(tcursor);
    IF SQLCODE = -932 THEN
        raise_application_error(-20002, dbms_utility.format_error_backtrace() || chr(10) || 'ACTION: To resolve this error, you need to ignore BLOB datatype columns.');
    ELSE
        raise_application_error(-20001, 'An error was env_countered: ' || SQLERRM || '. ' || chr(10) || dbms_utility.format_error_backtrace() || chr(10) || 'Query: ' || v_query || chr(10));
    END IF;

END;

PROCEDURE character_set_scan (p_owner IN VARCHAR2, p_result_array OUT t_tab_schemas)
AS
    counter number := 1;
    tables_table t_tables;
    table_object t_schema;
BEGIN
    p_result_array := t_tab_schemas();
    FOR i IN (SELECT table_name FROM all_tables WHERE owner = upper(p_owner)) LOOP
      p_result_array.extend;
      tables_table := t_tables();
      table_object := t_schema(null, tables_table);
      extract_chars(p_owner, i.table_name, table_object);
      p_result_array(counter) := table_object;
      counter := counter + 1;
    END LOOP;

    EXCEPTION
        WHEN OTHERS THEN
          raise_application_error(-20001, 'An error was encountered: ' || SQLERRM || '. ' || chr(10) || dbms_utility.format_error_backtrace());
END;

PROCEDURE export_table_xml (p_owner IN VARCHAR2, p_varchar_array IN varchar_array, v_clob_array OUT clob_array)
AS
	v_rows integer;
	v_xml_clob clob;
	
BEGIN
	v_clob_array := clob_array();
	FOR i IN p_varchar_array.first .. p_varchar_array.last LOOP
		v_rows := getxml_table(p_owner, p_varchar_array(i), v_xml_clob);
		v_clob_array.extend;
		v_clob_array(i) := v_xml_clob;
		exception
			when others then
				dbms_output.put_line(substr(SQLERRM, 1, 255));
    END LOOP;
END;

FUNCTION getxml_table(p_owner IN VARCHAR2, p_table IN VARCHAR2, p_xml OUT CLOB) RETURN NUMBER
IS

  v_ctx   DBMS_XMLGEN.ctxHandle;
  v_rows       INTEGER;

BEGIN

  -- Get the query context;
  v_ctx := DBMS_XMLGEN.newContext('SELECT * FROM ' || upper(p_owner || '.' || p_table));

  p_xml := DBMS_XMLGEN.getxml(v_ctx);
  v_rows := DBMS_XMLGEN.getNumRowsProcessed(v_ctx);
  DBMS_XMLGEN.closecontext(v_ctx);

  return v_rows;

  exception
	WHEN NO_DATA_FOUND THEN
	  dbms_output.put_line('The table ' || p_table || ' is empty');
    when others then
      dbms_output.put_line(substr(SQLERRM, 1, 255));

END getxml_table;

FUNCTION loadxml_table (p_owner IN VARCHAR2, p_tablename IN VARCHAR2, p_format IN VARCHAR2, p_tmpClob IN NUMBER) RETURN number
IS

  v_ctx dbms_xmlsave.ctxType;
  v_rows number := -1;

BEGIN

  for c1 in (select theclob
               from enver.tmp_xml_clob
              where id = p_tmpClob) loop

    v_ctx := dbms_xmlsave.newContext(upper(p_owner || '.' || p_tableName));
    --dbms_xmlsave.setDateFormat(v_ctx, 'dd-MMM-yyyy HH.mm.ss');
    dbms_xmlsave.setDateFormat(v_ctx, p_format);
    dbms_output.put_line(c1.theclob);
    v_rows := dbms_xmlsave.insertxml(v_ctx, c1.theclob);
    dbms_xmlsave.closeContext(v_ctx);

    delete from enver.tmp_xml_clob
     where id = p_tmpclob;
  end loop;

return v_rows;

END loadxml_table;

PROCEDURE compare_db_objects (
    local_schema_name IN VARCHAR2
  , remote_schema_name IN VARCHAR2
  , remote_schema_password IN VARCHAR2
  , tnsname IN VARCHAR2
  --, remote_missing char default 'LOCAL'
  , diff  OUT t_compares) 
AS

  j                             pls_integer := 1;
  t_cs                          t_compares    := t_compares();
  t_cs_remote                   t_compares    := t_compares();
  v_local_schema                VARCHAR2(100) DEFAULT upper(local_schema_name);
  --v_sql_create_link             VARCHAR2(150) := 'CREATE DATABASE LINK remote_schema_link CONNECT TO ' || remote_schema_name || ' IDENTIFIED BY ' || remote_schema_password || ' USING ''' || tnsname || '''';
  --v_sql_drop_link               VARCHAR2(50) := 'DROP DATABASE LINK remote_schema_link';

BEGIN

--EXECUTE IMMEDIATE v_sql_create_link;

for r in (
(
select object_type, object_name, 'Local'      mis from dba_objects@remote_schema_link where owner = v_local_schema --order by object_name, object_type
MINUS
select object_type, object_name, 'Local'      mis from dba_objects where owner = v_local_schema --order by object_name, object_type
) UNION ALL
(
select object_type, object_name, 'Remote'      mis from dba_objects where owner = v_local_schema --order by object_name, object_type
MINUS
select object_type, object_name, 'Remote'      mis from dba_objects@remote_schema_link where owner = v_local_schema --order by object_name, object_type
) order by 1, 2

) loop
  t_cs.extend;
  t_cs(j) := t_compare(r.object_name, r.object_type, r.mis);
  j := j + 1;

end loop;
/*
j := 1;

for s in (select object_type, object_name from dba_objects@remote_schema_link where owner = v_local_schema order by object_name, object_type) loop
  t_cs_remote.extend;
  t_cs_remote(j) := t_compare(s.object_name, s.object_type, 'Remote');
  j := j + 1;
end loop;*/

--if upper(remote_missing) = 'LOCAL' then
  diff := t_cs;--t_cs multiset except t_cs_remote;
--elsif upper(remote_missing) = 'REMOTE' then
  --diff := t_cs_remote multiset except t_cs;
--else
  --raise_application_error(-20010, 'Value ''' || remote_missing || ''' for command SCOPE is not allowed.');
--end if;

--EXECUTE IMMEDIATE v_sql_drop_link;

j := 1;

/*for i in (

(SELECT table_name
    , column_name
    , 'Local'      mis
  FROM dba_tab_columns@remote_schema_link
  WHERE owner = v_local_schema and table_name not like 'BIN$%'
  MINUS
  SELECT
      table_name
    , column_name
    , 'Local'     mis
  FROM   dba_tab_columns WHERE owner = v_local_schema  and table_name not like 'BIN$%')
  
  UNION ALL
(
  SELECT
      table_name
    , column_name
    , 'Remote'  mis
  FROM   dba_tab_columns
  WHERE owner = v_local_schema  and table_name not like 'BIN$%'
  MINUS
  SELECT
      table_name
    , column_name
    , 'Remote'   mis
  FROM   dba_tab_columns@remote_schema_link WHERE owner = v_local_schema and table_name not like 'BIN$%'
)
ORDER BY 1, 2

  
  ) loop
  
    t_cs.extend;
    t_cs(j) := t_compare(i.table_name, i.column_name, i.mis);
    j := j + 1;
  
end loop;

diff := t_cs;*/

END;

END;
