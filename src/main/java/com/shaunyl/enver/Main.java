package com.shaunyl.enver;

import com.shaunyl.enver.commandline.CommandLine;
import java.util.Locale;

/**
 *
 * @author Filippo Testino (filippo.testino@gmail.com)
 */
public class Main {

    public static void main(String[] args) throws Exception {
        
        /*
        TEST:
        */
        
//        TablespaceMonitor tbsm = new TablespaceMonitor(new FileWriter("./prova.txt"), 90, 95, "n");
//        
//        List<String[]> data = new ArrayList();
//        data.add(new String[] {"BAC_PRO", "SYSTEM", "1000000", "200000", "800000", "99"});
//        data.add(new String[] {"BAC_PRO", "SYSAUX", "2000000", "400000", "1600000", "91"});
//        tbsm.writeAll(data);
//        tbsm.close();

        Locale.setDefault(Locale.ENGLISH);


//        String[] argv = {
//            "expcsv",
//            "-userid", "sys",
//            "-queries", "select tablespace_name from sys.dba_tablespaces order by 1"
            //            "-queries", "SELECT * FROM regions;SELECT * FROM hr.departments",
//        };

//        String[] argv = {
//            "montbs",
//            "-undo", "y",
//            "-multi", "n",
//            "-exclude", "OSBBE_IAS_OPSS",
//            "-filename", "montbs.txt",
//            "-warning", "10", "-critical", "95", "-directory", "snapshot",
//            "-parfile", "./snapshot/montbs.par"
//        };
        
//        String[] monbako = {
//            "monbak",
//            "-deep", "30",
//            "-all", "n",
//            "-directory", "./backup",
//            "-multi", "y",
//            "-parfile", "./backup.par"
//        }; // retention of backup (2), only errors (not all)
        
//        String[] argv = {
//              "exp",
//              "-userid", "sys",
//              "-schema", "TESTINF",
//              "-tables", "test,test2,test3,test4,test4",
//              "-queries", "select * from gv$instance",
//              "-Cinst_id=10", "-Csql_text=60",
//                ,"-Ctablespace_name=20",
//              "-Cjob_title=33", "-Cjob_id=10", "-Cmin_salary=10", "-Cmax_salary=10",
//              "-multi=n"
//        };

        CommandLine cli = new CommandLine() {
        };

//        String[] mondisk = { // bug togliere pass e userid paramaters perchp non servono
//            "mondisk",
//            "-threshold", "1",
//            "-multi", "y",
//            "-include", "ora,app,u01,ind,swap"
//        };
        
//        String[] montbstrend = {
//            "montbstrend",
//            "-deep", "3",
//            "-threshold", "5",
//            "-directory", "./trend",
//            "-multi", "y"
//        }; // retention of backup (2), only errors (not all)
        
//        String[] montbsauto = {
//            "montbsauto",
//            "-multi", "y",
//            "-threshold", "10",
//            "-directory", "./tablespace"
//        };

        cli.run(args);
    }
}
