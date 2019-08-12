package com.updateHelper.compile;

import com.updateHelper.file.FileTools;
import com.updateHelper.utils.Utils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.ArrayList;

@Repository
public class CompileTools {
    private static final Logger logger = Logger.getLogger(CompileTools.class);

    @Autowired
    private FileTools fileTools;

    public static final String javaSuffixName = ".java";
    public static final String classSuffixName = ".class";
    public static final String packageSymbol = "package";

    public static final String packageRegex = "(\\s*)" + packageSymbol + "(\\s+)(\\w+)(\\s*)(((.*)(\\s*)(\\w+)(\\s*))*)(\\s*);(\\s*)";

    public static final String javaFilesList = CompileTools.class.getResource("/").getPath() + "javaFilesList";

    /**
     * 编译 srcDir 文件夹里的 java 文件,生成对应 class 文件到 targetDir 文件夹
     * @param srcDir 源文件所在文件夹,源文件主要是 java 文件，及其他文件，如 xml proerties 文件等
     * @param srcFilePaths 将要编译的源文件路径
     * @param targetDir class 文件所在文件夹
     * @param encoding java 源文件编码, 如：UTF-8,GBK
     * @param compileTool 编译工具,如:C:/Program Files/Java/jdk1.6.0_27/bin/javac.exe,或者D:/maven-3.2.1/bin/mvn.bat
     * @param classpath 编译时所用到类所在路径，web 工程通常是 /工程根目录/WEB-INF/lib
     * @return
     */
    public String compile(String srcDir, String srcFilePaths, String targetDir, String encoding, String compileTool, String classpath) {
        String result = "";
        long compileStartTime = System.currentTimeMillis();

        if (compileTool.contains("javac")) {
            // web 工程，类文件输出到 targetDir/WEB-INF/classes 目录
            if (fileTools.isDir(targetDir + "/WEB-INF/classes")) {
                targetDir += "/WEB-INF/classes";
            }

            if (srcFilePaths.contains(javaSuffixName)) {
                String javaSrcDir = getJavaSrcDir(srcDir, srcFilePaths);

                fileTools.createDir(targetDir);
                String compileCmd = "\"" + compileTool + "\" -J-Xms256M -J-Xmx256M -encoding \"" + encoding + "\"  -sourcepath \"" + javaSrcDir +"\" " +
                        "-extdirs \"" + classpath + "\" -d \"" + targetDir + "\" ";

                logger.info("compile  cmd:" + compileCmd + "\n  compile dir:" + srcDir);

                if (srcFilePaths != null && !srcFilePaths.trim().equals("")) {
                    result = Utils.exeCmd(compileCmd + srcFilePaths);
                } else {
                    result = compile(javaSrcDir, compileCmd, targetDir);
                }

            } else {
                String relativePath = getRelativePath(srcFilePaths, srcDir);
                String targetFile = targetDir + relativePath;
                fileTools.copyFile(srcFilePaths, targetFile);
                result = "copy file :" + srcFilePaths + " to :" + targetFile;
            }

        } else if (compileTool.contains("maven")) {
            result = mavenCompile(srcDir, compileTool);
        }

        logger.info("project compile result:" + result +
                     ", compile time:" + ((System.currentTimeMillis() - compileStartTime)/1000) + " S");

        return result;
    }

    /**
     * 使用 compileCmd 编译 srcDir 文件夹下的 java 文件
     * @param srcDir java 文件所在文件夹
     * @param compileCmd java 编译命令
     * @param targetDir class 文件所在文件夹
     * @return
     */
    public String compile(String srcDir, String compileCmd, String targetDir) {
        logger.info("compile srcDir:" + srcDir);
        long compileStartTime = System.currentTimeMillis();

        ArrayList<File> javaFiles = fileTools.getFiles(srcDir, javaSuffixName);

        ArrayList<File> notCompiledJavaFiles = new ArrayList<File>();
        String packagePath = "";

        if (javaFiles.size() != 0) {
            packagePath = getPackagePath(javaFiles.get(0));

            String notCompiledJavaFilesStr = " ";
            for (int i = 0; i < javaFiles.size(); i++) {

                if (!fileTools.isFile(getClassFilePath(javaFiles.get(i), targetDir,  packagePath))) {
                    notCompiledJavaFilesStr += javaFiles.get(i) + " ";
                    notCompiledJavaFiles.add(javaFiles.get(i));
                }
            }

            String result = Utils.exeCmd(compileCmd + notCompiledJavaFilesStr);

            if (!result.contains(Utils.execSucc)) {
                logger.info("one time compile result:" + result);

                return result;
            }
        }


        ArrayList<File> dirs = fileTools.getDirsInPath(srcDir);
        for (int i = 0; i < dirs.size(); i++) {
            waitCompileComplete(notCompiledJavaFiles, targetDir, packagePath);

            String result = compile(dirs.get(i).getPath(), compileCmd, targetDir);

            //编译失败，就退出，不再编译其他文件
            if (!result.contains(Utils.execSucc)) {
                logger.info("one time compile result:" + result);

                return result;
            }
        }

        logger.info("one time compile result:" + Utils.execSucc +
                     ", compile time:" + ((System.currentTimeMillis() - compileStartTime)/1000) + " S");

        return Utils.execSucc;
    }

    /**
     * 取得 java 文件所在根目录
     * @param javaFilePath
     * @return
     */
    public String getJavaSrcDir(String javaFilePath) {
        String javaSrcDir = javaFilePath.substring(0,javaFilePath.lastIndexOf("/")).replace(getPackagePath(new File(javaFilePath)), "");
        return (javaSrcDir.lastIndexOf("/") != (javaSrcDir.length()-1)) ? (javaSrcDir + "/") : javaSrcDir;
    }

    /**
     * 得到包路径
     * @param javaFile
     * @return
     */
    public String getPackagePath(File javaFile) {
        String packageStr = fileTools.readFileLines(javaFile, packageRegex);
        return packageStr.substring(packageStr.indexOf(packageSymbol)+packageSymbol.length(), packageStr.indexOf(";")).trim().replace(".", "/");
    }

    /**
     * 查询 java 文件所在根目录
     * @param srcDir 源文件所在根目录
     * @return
     */
    public String searchJavaSrcDir(String srcDir) {
        ArrayList<File> javaFiles = fileTools.getFiles(srcDir, javaSuffixName);
        if (javaFiles.size() > 0) {
            String javaFilePath = javaFiles.get(0).getPath().replaceAll("\\\\", "/");
            return getJavaSrcDir(javaFilePath);

        } else {
            ArrayList<File> dirs = fileTools.getDirsInPath(srcDir);
            if (dirs.size() > 0) {
                for (int i = 0; i < dirs.size(); i++) {
                    String javaSrcDir = searchJavaSrcDir(dirs.get(i).getPath());

                    if (javaSrcDir != null) {
                        return javaSrcDir;
                    }
                }
            }

            return null;
        }
    }

    /**
     *  获取 java 文件根目录
     * @param srcDir
     * @param javaFilePaths
     * @return
     */
    public String getJavaSrcDir(String srcDir, String javaFilePaths) {
        String javaSrcDir = null;
        if (javaFilePaths != null) {
            javaSrcDir = getJavaSrcDir(javaFilePaths.split(" ")[0]);
        } else {
            javaSrcDir = searchJavaSrcDir(srcDir);
        }

        return javaSrcDir;
    }

    /**
     * 获取相对于源文件根目录的相对路径
     * @param srcFilePaths
     * @param srcDir
     * @return
     */
    public String getRelativePath(String srcFilePaths, String srcDir) {
        String relativePath = srcFilePaths.replace(getJavaSrcDir(srcDir, null), "");
        return (relativePath.indexOf("/") == 0) ? relativePath : ("/" + relativePath);
    }

    /**
     * 得到类文件路径
     * @param javaFile
     * @param targetDir
     * @param packagePath
     * @return
     */
    public String getClassFilePath(File javaFile, String targetDir, String packagePath) {
        return targetDir + "/" + packagePath  + "/" + javaFile.getName().replace(javaSuffixName, classSuffixName);
    }

    /**
     *  等待 javac 编译完成该文件夹下边的文件
     * @param javaFiles
     * @param targetDir
     * @param packagePath
     */
    public void waitCompileComplete(ArrayList<File> javaFiles, String targetDir, String packagePath) {
        while (!isClassesCompiled(javaFiles, targetDir, packagePath)) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    /**
     * 类文件存在，表示编译完成
     * @param javaFiles
     * @param targetDir
     * @param packagePath
     * @return
     */
    public boolean isClassesCompiled(ArrayList<File> javaFiles, String targetDir, String packagePath) {
        boolean isCompiled = true;

        for (int i = 0; i < javaFiles.size(); i++) {
            isCompiled &= fileTools.isFile(getClassFilePath(javaFiles.get(i), targetDir, packagePath));
        }

        return isCompiled;
    }

    /**
     * 使用 maven 编译
     * @param srcDir java文件所在文件夹
     * @param mavenCmd maven命令路径，如：D:/maven-3.2.1/bin/mvn.bat
     * @return
     */
    public String mavenCompile(String srcDir, String mavenCmd) {
        String  mavenCompileCmd = srcDir + "/mavenCompile.bat";
        fileTools.writeFile(mavenCompileCmd, srcDir.split(":")[0]+":" + fileTools.lineSeparator +
                                              "cd " + srcDir + fileTools.lineSeparator +
                                              mavenCmd + " clean & " + mavenCmd +" compile");
        return Utils.exeCmds(srcDir, mavenCompileCmd);
    }

/*    public static void main(String[] args) {
        CompileTools compileTools = new CompileTools();
        //compileTools.compile("D:/kyebBackend/src", null, "D:/kyebBackend/WEB-INF/classes", "GBK", "C:/Program Files/Java/jdk1.5.0_17/bin/javac.exe", "D:/kyebBackend/WEB-INF/lib");
        //compileTools.mavenCompile("D:/KyEcDataRead", "D:/maven-3.2.1/bin/mvn.bat");
        System.out.print("java src dir:" + compileTools.searchJavaSrcDir("D:/IBE_API/branches/IBEAPI_SpringBoot"));
    }*/
}