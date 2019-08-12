package com.updateHelper.utils;

import org.apache.log4j.Logger;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utils {
    private static final Logger logger = Logger.getLogger(Utils.class);

    public static final String execSucc = "execute cmd succ";
    public static final String execFail = "execute cmd fail";

    public static String getArrayString(String[] strArray) {
        String strs = null;
        for (int i = 0; i < strArray.length; i++) {
             strs += strArray[i] + System.getProperty("line.separator");
        }
        return strs;
    }

    public static  String URLGet(String url){
        logger.info("url:" + url);

        String  code = "";
        HttpURLConnection con = null;
        try{
            URL urlObj = new URL(url);
            con = (HttpURLConnection) urlObj.openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(3000);
            code = new Integer(con.getResponseCode()).toString();
            con.disconnect();

        } catch (Exception e) {
            try {
                code = con.getResponseCode() + e.getMessage();

            } catch (Exception e1) {
                e1.printStackTrace();
                code = "Cann't get response code," + e.getMessage();
            }

            e.printStackTrace();
        }

        logger.info("get Response Code:"+ code);

        return code ;
    }

    /**
     * 执行命令
     * @param cmd
     * @return
     * 解决 Runtime.exec() 方法阻塞及死锁问题。
       发现程序被阻塞了，什么原因呢？JDK's Javadoc文档解释说：
       Because some native platforms only provide limited buffer size for standard input and output streams,
       failure to promptly write the input stream or read the output stream of the subprocess may cause the
       subprocess to block, and even deadlock.
      翻译：
      一些平台只为标准输入输出提供有限的缓存。错误的写子进程的输入流或者错误的输出子进程的输出流都有可能造成子进程的阻塞，甚至是死锁
      解决上面问题的办法就是程序中将子进程的输出流和错误流都输出到标准输出中。在程序中最好是能将子进程的错误流和输出流都能输出并清空。
     */
    public static String exeCmd(String cmd) {
        logger.info("cmd:" + cmd);

        String result = execSucc;

        // 返回与当前 Java 应用程序相关的运行时对象
        Runtime run = Runtime.getRuntime();

        try {
            // 启动另一个进程来执行命令
            Process p = run.exec(cmd);

            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
            // kick off stderr
            errorGobbler.start();

            StreamGobbler outGobbler = new StreamGobbler(p.getInputStream(), "STDOUT");
            // kick off stdout
            outGobbler.start();

            //检查命令是否执行失败
            if (p.waitFor() != 0) {
                //0表示正常结束，1：非正常结束
                result = String.valueOf(p.exitValue());
                if (result.equals("1")) {
                    System.err.println("命令执行失败!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = execFail;
        }
        logger.info("exec result:" + result);

        return result;
    }

    /**
     * 执行多个命令
     * @param filePath
     * @param cmds
     */
    public static String exeCmds(String filePath, String cmds) {
        String result = "";

        ProcessBuilder processBuilder = new ProcessBuilder(cmds);
        processBuilder.directory(new File(filePath));

        try {
            Process process = processBuilder.start();

            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "ERROR");
            // kick off stderr
            errorGobbler.start();

            StreamGobbler outGobbler = new StreamGobbler(process.getInputStream(), "STDOUT");
            // kick off stdout
            outGobbler.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

/*    public static void main(String[] args ) {
        exeCmd("\"C:/Program Files/Java/jdk1.5.0_17/bin/javac\" -encoding \"GBK\" -sourcepath \"D:\\kyebBackend\\src\" -extdirs \"D:\\kyebBackend\\WEB-INF\\lib\" -d \"D:\\kyebBackend\\WEB-INF\\classes\" \"D:\\kyebBackend\\src\\com\\iss\\szair\\b2a\\orderInfo\\action\\OrderInfoCtrl.java\"");
        FileTools fileTools = new FileTools();
        fileTools.writeFile("d:/a.bat", "D:" + System.getProperty("line.separator") + "cd D:/KyEcDataRead" + System.getProperty("line.separator") + "D:/maven-3.2.1/bin/mvn.bat clean & D:/maven-3.2.1/bin/mvn.bat compile");
        exeCmds("d:/a.bat", "D:/KyEcDataRead");
    }*/
}