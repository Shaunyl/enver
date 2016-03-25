-- -----------------------------------------------------------------------------------
-- File Name    : pre-install.sql
-- Author       : Filippo Testino
-- Description  : Pre-installs some objects required by Encoding Verifier Utility to work properly.
-- Call Syntax  : @pre-install (folder-that-contains-datafile)
-- Last Modified: */02/2014
-- -----------------------------------------------------------------------------------

SET LINESIZE 255
SET PAGESIZE 1000

begin
   EXECUTE IMMEDIATE 'CREATE TABLESPACE enver DATAFILE ''&1/enver.dbf'' SIZE 50M EXTENT MANAGEMENT LOCAL AUTOALLOCATE';
end;
/

SET PAGESIZE 14


