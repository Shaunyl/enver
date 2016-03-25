-- -----------------------------------------------------------------------------------
-- File Name    : uninstall.sql
-- Author       : Filippo Testino
-- Description  : Uninstalls Encoding Verifier Utility.
-- Call Syntax  : @pre-install (folder-that-contains-datafile)
-- Last Modified: */02/2014
-- -----------------------------------------------------------------------------------
DROP USER enver CASCADE;
DROP DIRECTORY XUPLOAD;
DROP DIRECTORY XDOWNLOAD;

ALTER TABLESPACE enver OFFLINE;
DROP TABLESPACE enver INCLUDING CONTENTS AND DATAFILES;