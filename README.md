# enver

A Java 1.6 command line based tool that help you to automate tasks on databases. You can use it for example on Linux systems to perform regular checks against a net of nodes.  

It is very light version of the Enver-0.89.6-SHAPSHOT tool and the shauni tool, but it is based on JCommander (http://jcommander.org/).  
It is almost free of external dependecies.  

**expcsv**: -> export data into CSV files  
**montbs**: -> monitor the space usage of the tablespaces  
**exp**: -> export data in a tabular way  
**monbak**: -> monitor the execution of the backups of a database  
**monbako**: -> retrieve the log of a RMAN task querying the v$rman_output view  
**mondisk**: -> monitor the disk usage of the system using a shell script  
**montbstrend**: -> monitor the growth trend of the tablespaces  
**montbsauto**: -> like montbs but also take in account the autoextension of the datafiles  

An embedded help shows you all the options available for each command.  

Example of montbsauto output:  

```java
Alarm Auto Extend - BTM_PRO

Starting MonTbsAuto on instance BTM_PRO at 01-Oct-15 13:17:39
Threshold used -> 1

Retrieving tablespaces info..

  BTM_PRO   SYSTEM[8.00/8.00]                     AUTOEXTEND[OK]    [4.52/3.48]                  57.00%/57.00%
  BTM_PRO   BTMMESSAGELOGDB[654.00/654.00]        AUTOEXTEND[OK]    [177.96/476.03]              27.00%/27.00%
  BTM_PRO   SYSAUX[5.71/32.00]                    AUTOEXTEND[OK]    [5.44/.28]                   95.00%/17.00%
  BTM_PRO   BTMTRANSACTIONDB[20.00/20.00]         AUTOEXTEND[OK]    [2.88/17.12]                 14.00%/14.00%
  BTM_PRO   BTMSPHEREDB[10.00/10.00]              AUTOEXTEND[OK]    [1.26/8.74]                  13.00%/13.00%
  BTM_PRO   BTMMEASUREMENTDB[51.00/51.00]         AUTOEXTEND[OK]    [6.36/44.64]                 12.00%/12.00%
  BTM_PRO   BTMMONITORGROUPDB[5.00/5.00]          AUTOEXTEND[OK]    [.40/4.60]                    8.00%/8.00%
All datafiles are sub-threshold for tablespace USERS

Job completed successfully 01-Oct-15 13:17:39
```
