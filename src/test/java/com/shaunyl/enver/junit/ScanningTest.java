package com.shaunyl.enver.junit;

import com.shaunyl.enver.exception.CommandLineException;
import java.io.IOException;
import org.junit.Test;

/**
 *
 * @author Filippo Testino (filippo.testino@gmail.com)
 */
public class ScanningTest {
    
    @Test
    public void testOld() throws IOException, CommandLineException {
//        String[] args = new String[] {
////            "expcsv", "-tables=tables"
//            "montbs", "-undo=n", "-warning=70", "-critical=90"
//        };
//
//        CommandLine shell = new CommandLine();
//        CommandLine.addCommand(null);
//
//        try {
//            shell.run(args);
//        } catch (IOException | CommandLineException | EnverException e) {
//            String message = e.getMessage();
//            System.out.println(message);
//        }
    }
    
//
////    @Test
//    public void testDiskUsage() throws IOException, CommandLineException, EnverException {
//        String[] args = new String[]{
//            "udisks -threshold=80" };
//        new Main().parseCommandLine(args);
//    }
//
////    @Test
//    public void testCharacterSetScanMode() throws IOException, CommandLineException, EnverException {
//        String[] args = new String[]{
//            "csscan", "-catalog=n", "-frep=y", "-samples=y", "-schemas=HR" };
//        new Main().parseCommandLine(args);
//    }
//
////    @Test
//    public void testVersionShell() throws IOException, CommandLineException, EnverException {
//        String[] args = new String[]{
//            "version" };
//        new Main().parseCommandLine(args);
//    }
//
////    @Test
//    public void testHelpShell() throws IOException, CommandLineException, EnverException {
//        String[] args = new String[]{
//            "help", "-expxml" };
//        new Main().parseCommandLine(args);
//    }
//
////    @Test
//    public void testJUniversalMode() throws IOException, CommandLineException, EnverException {
//        String[] args = new String[]{
//            "juniversal", "-filename=/home/sms/prova.txt" };
//        new Main().parseCommandLine(args);
//    }
//
////    @Test
//    public void testImportXML() throws IOException, CommandLineException, EnverException {
//        String[] args = new String[]{
//            "xload", "-directory=/home/sms/enver/xml/upload", "-format=dd-MMM-yyyy HH.mm.ss" };
//        new Main().parseCommandLine(args);
//    }
//
////    @Test
//    public void testExportXML() throws IOException, CommandLineException, EnverException {
//        String[] args = new String[]{
//            "expxml", "-schemas=hr", "-tables=REGIONS, COUNTRIE, LOCATIONS" };
//        new Main().parseCommandLine(args);
//    }
//
//    @Test
//    public void testTablespaceMonitoring() throws IOException, CommandLineException, EnverException {
//        String[] args = new String[]{
//            "montbs", "-directory=tbs", "-filename=montbs", "-exclude=ciao", "-undo=y", "-cthreshold=70", "-wthreshold=40"
//        };
//        new Main().parseCommandLine(args);
//    }    
//    
////    @Test
//    public void testExportTabular() throws IOException, CommandLineException, EnverException {
//        String[] args = new String[]{
//            "exptab", "-directory=tbs", "-parfile=tablespace.par", "-filename=tab"
//        };
//        new Main().parseCommandLine(args);
//    }
//
////    @Test
//    public void testExportCSV() throws IOException, CommandLineException, EnverException {
//        String[] args = new String[]{
//            "expcsv", "-multi=y", "-directory=tbs/billing7aprile", "-parfile=tablespace.par" };
//        new Main().parseCommandLine(args);
//    }
//
////    @Test
//    public void testExportEXCEL() throws IOException, CommandLineException, EnverException {
//        String[] args = new String[]{
//            "expexcel", "-parfile=tablespace.par", "-format=xlsx", "-sheets=Space Usage", "-multi=y", "-directory=tbs/141218"
//        };
//        new Main().parseCommandLine(args);
//    }
//
////    @Test
//    public void testCompareSchemasMode() throws IOException, CommandLineException, EnverException {
//        String[] args = new String[]{
//            "compare", "-mode=structure", "-local=HR", "-remote=ENVER/ENVER", "-tnsname=TESTDB" };
//        new Main().parseCommandLine(args);
//    }
//
////    @Test
//    public void LiquibaseMode() throws IOException, CommandLineException, EnverException {
//        String[] args = new String[]{
//            "liquibase", "-mode=migrate", "-changelog=/home/sms/Desktop/migrate.sql", "-filename=/home/sms/enverLiquibaseDiff", "-format=raw"
//        };
//        new Main().parseCommandLine(args);
//    }
//
////    @Test
//    public void testImportExcel() throws IOException, CommandLineException, EnverException {
//        String[] args = new String[]{
//            "impexcel", "-filename=C:/download/PROVA.DB_VER.xls", "-truncate=y"
//        };
//        new Main().parseCommandLine(args);
//    }
//
////    @Test
//    public void testImportCSV() throws IOException, CommandLineException, EnverException {
//        String[] args = new String[]{
//            "impcsv", "-feedback=100", "-table=dba_tablespace_hist", "-schema=filippot", "-filename=tbs/billing7aprile/*",
//            "-truncate=n", "-format=dd-MMM-yyyy HH:mm:SS"
//        };
//        new Main().parseCommandLine(args);
//    }
////    @Test
////    public void testFuffa() throws IOException, CommandLineException, EnverException {
////        new Main();
////    }
//    //        Runtime run = Runtime.getRuntime();
//////        Process proc = run.exec(new String[]{ "/bin/sh", "-c", "echo 5 | ./prog" });
////        Process p = run.exec(new String[]{ "ssh", "oracle@192.168.1.13", "<<", "EOF",
////        "export ORACLE_HOME=/home/u01/app/oracle/product/11.2.0/db_1",
////        "export ORACLE_SID=etest",
////        "export ORACLE_BASE=/home/u01/app/oracle",
////        "sqlplus / as sysdba @wind.sql", "EOF"});
////        BufferedReader stdin = new BufferedReader(new InputStreamReader(p.getInputStream()));
////        String line = null;
////
////        while ((line = stdin.readLine()) != null) {
////            System.out.println(line);
////        }
//
////    @Test
//    public void testInstallENVER() throws IOException, CommandLineException, EnverException {
//        String[] args = new String[]{
//            "install", "-directory=/home/oracle/enver", "-datafiledir=/home/oracle/oradata/etest/"
//        };
//        new Main().parseCommandLine(args);
//    }
//
////    @Test
//    public void testShell() throws IOException, CommandLineException, EnverException {
//        String[] args = new String[]{
//            "shell"
//        };
//        new Main().parseCommandLine(args);
//    }
//
////    @Test
//    public void testExportGRAPH() throws IOException, CommandLineException, EnverException {
//        String[] args = new String[]{
//            "expgraph", "-query="
//            + "select to_char(last_updated, 'DD') undotbs1, SIZE_BYTES / 1024 / 1024 size_mb, USED_BYTES / 1024 / 1024 used_mb "
//            + "from filippot.dba_tablespace_hist where database_name = 'GENE_PRO' and used_pct > 96 order by used_pct ", "-filename=image2", "-mode=cartesian", "-title=GENE_PRO", "-legend=y", "-ylabel=Space Usage"
//        };
//        new Main().parseCommandLine(args);
//    }
//
////    @Test
//    public void a() {
//        ServiceLoader<ICompareTaskMode> load = ServiceLoader.load(ICompareTaskMode.class); // TEMPME
//
//        Iterator<ICompareTaskMode> iterator = load.iterator();
//        while (iterator.hasNext()) {
//            ICompareTaskMode next = iterator.next();
//
//            boolean isGood = next.identify("structure");
//            System.out.println(isGood);
//        }
//    }
//
////    @Test
//    public void testCompareData() throws IOException, CommandLineException, EnverException {
//        String[] args = new String[]{
//            "compare", "-mode=data", "-comparisonName=comp_prova", "-scanMode=FULL",
//            "-schemas=HR, HR", "-tables=REGIONS, REGIONI", "-converge=y" };
//        new Main().parseCommandLine(args);
//    }
//
//    //   @Test
//    public void testSet() throws IOException, CommandLineException, EnverException {
//        String[] args = new String[]{
//            "set", "log.console=true" };
//        new Main().parseCommandLine(args);
//    }
////    
////    @Test
////    public void testSetHost() throws IOException, CommandLineException, EnverException {
////        String[] args = new String[] {
////            "set"/*, "host=db801tcc.intranet.fw", "port=1521", "shame=DBCC"*/};
////        new Main().parseCommandLine(args);
////    }
//
////    @Test
//    public void connectToServer() throws JSchException, SftpException, IOException {
//        JSch jsch = new JSch();
//
//        String user = "ftestino";
//        try (BufferedReader br = new BufferedReader(new FileReader("config/linuxservers.properties"))) {
//            for (String line; (line = br.readLine()) != null;) {
//                String host = line;
//                Session session = jsch.getSession(user, host, 22);
//
//                session.setPassword("UgoLupo10!!");
//                Properties config = new Properties();
//                config.put("StrictHostKeyChecking", "no");
//                session.setConfig(config);
//                session.connect();
//                ChannelExec channel = (ChannelExec) session.openChannel("exec");
//                BufferedReader in = new BufferedReader(new InputStreamReader(channel.getInputStream()));
//                channel.setCommand("./udisk.sh");
//                channel.connect();
//
//                String msg = null;
//                while ((msg = in.readLine()) != null) {
//                    System.out.println(msg);
//                }
//
//                channel.disconnect();
//                session.disconnect();
//            }
//        }
//    }
//
////    @Test
//    public void connectToServer2() throws JSchException, SftpException, IOException {
//        JSch jsch = new JSch();
//
//        String user = "ftestino";
//        try (BufferedReader br = new BufferedReader(new FileReader("config/linuxservers.properties"))) {
//            for (String line; (line = br.readLine()) != null;) {
//                String host = line;
//                Session session = jsch.getSession(user, host, 22);
//
//                session.setPassword("UgoLupo10!!");
//                Properties config = new Properties();
//                config.put("StrictHostKeyChecking", "no");
//                session.setConfig(config);
//                session.connect();
//
//                final Channel channelsftp = session.openChannel("sftp");
//                channelsftp.connect();
//                final ChannelSftp channelSftp = (ChannelSftp) channelsftp;
//
//                File file2 = new File("config/udisk.sh");
//                channelSftp.put(new FileInputStream(file2), "temp", ChannelSftp.OVERWRITE);
//                channelSftp.disconnect();
//
//                ChannelExec channel = (ChannelExec) session.openChannel("exec");
//                BufferedReader in = new BufferedReader(new InputStreamReader(channel.getInputStream()));
//                channel.setCommand("dos2unix temp > udisk.sh ; chmod u+x udisk.sh ; ./udisk.sh 70");
//
//                channel.connect();
//
//                String msg = null;
//                while ((msg = in.readLine()) != null) {
//                    System.out.println(msg);
//                }
//
//                channel.disconnect();
//                session.disconnect();
//            }
//        }
//    }
}
