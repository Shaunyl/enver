create or replace PACKAGE enver.encoding_verifier IS

    PROCEDURE extract_chars (p_owner IN VARCHAR2, p_table IN VARCHAR2, p_array IN OUT t_schema);

    PROCEDURE character_set_scan (p_owner IN VARCHAR2, p_result_array OUT t_tab_schemas);

    FUNCTION getxml_table(p_owner IN VARCHAR2, p_table IN VARCHAR2, p_xml OUT CLOB) RETURN NUMBER;

    FUNCTION  loadxml_table (p_owner IN VARCHAR2, p_tablename IN VARCHAR2, p_format IN VARCHAR2, p_tmpClob IN NUMBER) RETURN number;

    PROCEDURE compare_db_objects (local_schema_name IN VARCHAR2, remote_schema_name IN VARCHAR2, remote_schema_password IN VARCHAR2, tnsname IN VARCHAR2,
	diff  OUT t_compares);

	PROCEDURE export_table_xml (p_owner IN VARCHAR2, p_varchar_array IN varchar_array, v_clob_array OUT clob_array);
	
END;
