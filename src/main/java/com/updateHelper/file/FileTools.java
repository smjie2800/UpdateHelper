package com.updateHelper.file;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/*
 * The methods: createFile(), createDir() from http://blog.csdn.net/qiaqia609/article/details/11048463
 * The method: coypeFileContent() from http://lyklove123.blog.163.com/blog/static/199371378201212892849589
 */

@Repository
public class FileTools {
    private static final Logger logger = Logger.getLogger(FileTools.class);

    public static final int fileNameLength = 250;

    public static final String lineSeparator = System.getProperty("line.separator");


    /**
     * 创建文件
     * @param targetFile
     * @return
     */
    public File createFile(String targetFile) {
        logger.info("创建文件:" + targetFile);

        File file = new File(targetFile);
        if(file.exists()) {
            logger.info("文件：" + targetFile + "，已存在！删除该文件重新创建");
            file.delete();
        }
        if (targetFile.endsWith(File.separator)) {
            logger.info("不能创建文件：" + targetFile + "，该文件不能为目录！");
            return null;
        }

        //判断该文件所在的目录是否存在
        if(!file.getParentFile().exists()) {
            //如果该文件所在的目录不存在，则创建父目录
            logger.info("该文件所在目录不存在，准备创建它！");
            if(!file.getParentFile().mkdirs()) {
                logger.info("创建该文件所在目录失败！");
                return null;
            }
        }
        //创建该文件
        try {
            if (file.createNewFile()) {
                logger.info("创建文件" + targetFile + "成功！");
                return file;
            } else {
                logger.info("创建文件" + targetFile + "失败！");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("创建文件" + targetFile + "失败！" + e.getMessage());
            return null;
        }
    }

    /**
     * 创建目录
     * @param targetDir
     * @return
     */
    public File createDir(String targetDir) {
        logger.info("创建目录:" + targetDir);

        File dir = new File(targetDir);
        if (dir.exists()) {
            logger.info("创建目录" + targetDir + "失败，该目录已经存在");
            return dir;
        }
        if (!targetDir.endsWith(File.separator)) {
            targetDir = targetDir + File.separator;
        }
        //创建目录
        if (dir.mkdirs()) {
            logger.info("创建目录" + targetDir + "成功！");
            return dir;
        } else {
            logger.info("创建目录" + targetDir + "失败！");
            return null;
        }
    }

    /**
     * 将文本写入到文件
     * @param filePath
     * @param text
     *
     */
    public void writeFile(String filePath, String text) {
        logger.info("将文本:" + text + "写入到文件:" + filePath);
        if (new File(filePath).exists()) {
            new File(filePath).delete();
        }

        try {
            FileOutputStream outs = new FileOutputStream(createFile(filePath));
            outs.write(text.getBytes());
            outs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 多次写入文本到文件
     * @param filePath
     * @param text
     * @param append 是否写文本到文件末尾
     *
     */
    public void multipleWriteFile(String filePath, String text, boolean append) {
        logger.info("将文本:" + text + "写入到文件:" + filePath);
        if (!new File(filePath).exists()) {
            try {
                new File(filePath).createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            FileOutputStream outs = new FileOutputStream(filePath, append);
            outs.write(text.getBytes());
            outs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 清空文件内容
     * @param filePath
     */
    public void clearFileContent(String filePath) {
        multipleWriteFile(filePath, "", false);
    }

    /**
     * 读取源文件内容到目标文件
     * @param srcFile
     * @param targetFile
     */
    public void coypeFileContent(String srcFile,String targetFile){
        logger.info("读取文件:" + srcFile + "内容到文件:" + targetFile);

        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(srcFile);
            //判断文件是否存在，如果文件存在则实现该文件向新文件的复制
            if(oldfile.exists()){
                //读取原文件
                InputStream ins = new FileInputStream(srcFile);
                //创建文件输出流，写入文件
                FileOutputStream outs = new FileOutputStream(targetFile);
                //创建缓冲区，大小为1024字节
                byte[] buffer = new byte[1024];
                //每次从文件流中读取500字节数据，计算当前为止读取的数据总数
                while((byteread = ins.read(buffer)) != -1){
                    bytesum += byteread;
                    //把当前缓冲区中的数据写入新文件
                    outs.write(buffer,0,byteread);
                }
                logger.info("读取文件总字节数:" + bytesum);
                ins.close();
                outs.close();
            }
            else  //如果原文件不存在，则扔出异常
                throw new Exception();
        }catch(Exception ex){
            System.out.print("原文件不存在！");
            ex.printStackTrace();
        }
    }

    /**
     * 拷贝文件
     * @param srcFile
     * @param targetFile
     */
    public void copyFile(String srcFile,String targetFile) {
        logger.info("拷贝文件:" + srcFile + "到:" + targetFile);
        createFile(targetFile);
        coypeFileContent(srcFile, targetFile);
    }

    /**
     *
     * @param filePath
     * @return
     */
    public boolean isFile(String filePath) {
        if (new File(filePath).isFile()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param filePath
     * @return
     */
    public boolean isDir(String filePath) {
        if (new File(filePath).isDirectory()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 得到 path 路径下指定文件类型的文件
     * @param path
     * @param fileType
     * @return
     */
    public ArrayList<File> getFiles(String path, String fileType) {
        ArrayList<File> files = new ArrayList<File>();

        File[] allFiles = new File(path).listFiles();
        for (int i = 0; i < allFiles.length; i++) {
            if (allFiles[i].isFile() &&
                    (allFiles[i].getName().lastIndexOf(fileType) == allFiles[i].getName().length() - fileType.length())) {
                files.add(allFiles[i]);
            }
        }

        return files;
    }

    /**
     * 得到 path 路径下的名称以 perfix 开头的文件
     * @param path
     * @param perfix
     * @return
     */
    public ArrayList<File> getFilesStartByPerfix(String path, String perfix) {
        ArrayList<File> files = new ArrayList<File>();

        File[] fileAndDirs = new File(path).listFiles();
        if (fileAndDirs != null) {
            for (int i = 0; i < fileAndDirs.length; i++) {
                if (fileAndDirs[i].isFile() && fileAndDirs[i].getName().startsWith(perfix)) {
                    files.add(fileAndDirs[i]);
                }
            }
        }

        return files;
    }

    /**
     * 得到 path 路径下的文件夹
     * @param path
     * @return
     */
    public ArrayList<File> getDirsInPath(String path) {
        ArrayList<File> dirPaths = new ArrayList<File>();

        File[] files = new File(path).listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                dirPaths.add(files[i]);
            }
        }

        return dirPaths;
    }

    /**
     *
     * @param dirPath
     * @return
     */
    public boolean isEmptyDir(String dirPath) {
        File dir = new File(dirPath);
        if (dir.isDirectory() && dir.listFiles().length == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *  读取匹配 regEx 正则表达式的第一那行
     * @param file
     * @param regex
     * @return
     * @throws IOException
     */
    public String readFileLines(File file, String regex) {
        String line = "";

        try{
            InputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            while ((line = reader.readLine()) != null) {
                if (Pattern.compile(regex).matcher(line).find()) {
                   break;
                }
            }

            reader.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return line;
    }

    // 过滤特殊字符
    public  String stringFilter(String str)   throws PatternSyntaxException {
        /*
         在 java 字符串中，符号 \ 需要用 \\ 来表示，
         而正则表达式中，符号 \ 也需要用 \\ 来表示，
         所以在 java 字符串的正则表达式中，表示 \ 需要 \\\\ 来表示
         */

        String regex="[`~!@#$%^&:*?<>\\\\/\"|()+={}',.~！\\[\\]|\\s]";
        Pattern p   =   Pattern.compile(regex);
        Matcher m   =   p.matcher(str);
        return  m.replaceAll("").trim();
    }
}
