rem
rem Copyright (c) 2014, 2014, Shaunyl. All rights reserved.
rem
rem NAME
rem   install.sql
rem DESCRIPTION
rem   Create tables for Encoding Verifier Utility
rem NOTE
rem   This script must be run while connected as SYS
rem MODIFIED
rem   shaunyl  24/02/14 - fix bug on compare task
rem

rem *****************************************************************
rem  Creates and assigns privileges to ENVER user
rem *****************************************************************

create user enver identified by enver password expire account unlock 
/
WHENEVER SQLERROR CONTINUE

--grant select on sys.obj$ to enver
--/
--grant select on sys.col$ to enver
--/
--grant select on sys.icol$ to enver
--/
--grant select on sys.ind$ to enver
--/
--grant select on sys.cdef$ to enver
--/
--grant select on sys.con$ to enver
--/
--grant select on sys.trigger$ to enver
--/
grant select any table to enver;
/
grant insert any table to enver;
/
grant delete any table to enver;
/
grant select on dba_objects to enver;
/
grant select on dba_tab_columns to enver;
/
grant select on dba_tables to enver;
/
grant create database link to enver;
/
grant connect, resource to enver;
/
alter user enver default tablespace ENVER quota unlimited on ENVER
/

rem *****************************************************************
rem  Creates all required objects
rem *****************************************************************

create table enver.tmp_xml_clob(id number, theclob clob)
/
create sequence enver.tmp_xml_clob_seq
/
create trigger enver.bi_tmp_xml_clob
  before insert on enver.tmp_xml_clob
for each row
begin
  select enver.tmp_xml_clob_seq.nextval
  into :new.id
  from dual;
end;
/

rem *****************************************************************
rem  Creates directory and assigns privileges on them to ENVER
rem *****************************************************************

--!mkdir -p $ORACLE_BASE/enver/xml/download
--!mkdir -p $ORACLE_BASE/enver/xml/upload
--
--declare
--ob varchar2(100) := '';
--begin
--   dbms_system.get_env('ORACLE_BASE', ob);
--   EXECUTE IMMEDIATE 'CREATE OR REPLACE DIRECTORY xupload AS ''' || ob || '/enver/xml/upload''';
--   EXECUTE IMMEDIATE 'CREATE OR REPLACE DIRECTORY xdownload AS ''' || ob || '/enver/xml/download''';
--end;
--/

--grant read, write on directory xupload to enver;
--/
--grant read, write on directory xdownload to enver;
--/

rem *****************************************************************
rem  Creates all required types
rem *****************************************************************

create type enver.t_records is table of varchar2(4000)
/

create type enver.t_column as object
(
    column_name varchar2(100),
    column_length integer,
    a_records t_records
)
/

create type enver.t_columns is table of enver.t_column
/

create type enver.t_table as object
(
    table_name varchar2(100),
    a_columns t_columns
)
/

create type enver.t_tables is table of enver.t_table
/
    
create type enver.t_schema as object
(
    schema_name varchar2(100),
    a_tables t_tables
)
/

create type enver.t_tab_schemas is table of enver.t_schema
/

rem *****************************************************************
rem  Compare schemas objects
rem *****************************************************************

create or replace type enver.t_compare as object
(
    object_name    varchar2(200),
    object_type     varchar2(100),
	object_mis		varchar2(100),
    map member function map return varchar2
)
/

create or replace type body enver.t_compare as
  map member function map
    return varchar2
  is
  begin
    return object_name;
  end;
end;
/

create or replace type enver.t_compares is table of enver.t_compare;
/

create or replace type enver.T_COMPARE_TABLE is table of enver.t_compares;
/

CREATE OR REPLACE TYPE enver.varchar_array IS TABLE OF VARCHAR2(80);
/

CREATE OR REPLACE TYPE enver.clob_array IS TABLE OF CLOB;
/

EXIT;










