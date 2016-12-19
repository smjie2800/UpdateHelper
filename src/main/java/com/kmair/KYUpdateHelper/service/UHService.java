package com.kmair.KYUpdateHelper.service;


import com.kmair.KYUpdateHelper.compile.CompileTools;
import com.kmair.KYUpdateHelper.vcs.svn.SvnTools;
import com.kmair.KYUpdateHelper.file.FileTools;
import com.kmair.KYUpdateHelper.utils.Utils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Service
public class UHService {
    private static final Logger logger = Logger.getLogger(UHService.class);

    public static final String CLASS = ".class";
    public static final String java = ".java";
    public static final String webDir = "/WEB-INF/classes";
    public static final String classDir = "/classes";
    public static final String webInfDir = "/WEB-INF";
    public static final String backupDir = "backup";
    public static final String update = "/update/";

    @Autowired
    private SvnTools svnTools;
    @Autowired
    private FileTools fileTools;
    @Autowired
    private CompileTools compileTools;

    /**
     * 从 SVN 更新改变过的文件到本地源文件目录
     * @param svnDirs
     * @param srcDirs
     * @return
     * @throws Exception
     */
    public String updateFiles(String[] svnDirs, String[] srcDirs) throws Exception {
        String succResult = "<br/><br/><br/><h4>update files:</h4><br/><br/>";
        String failResult = "<br/><br/><br/><h4>fail update files:</h4><br/><br/>";

        for (int i = 0; i < svnDirs.length; i++) {
            svnTools.updateProjectFromSvn(svnDirs[i], srcDirs[i]);

            succResult += svnTools.succFile + "<br/>";
            failResult += svnTools.failFile + "<br/>";
        }

        return succResult + failResult;
    }

    /**
     * 根据从 svn 查询到的更新文件编译到工程目录
     * @param srcDirs
     * @param projectDirs
     * @return
     * @throws Exception
     */
    public String compileFiles(String keywords, String commitTime, String[] svnDirs, String[] srcDirs, String[] projectDirs,
                                String encoding, String compileTool, String classpath) throws Exception {
        String result = "<br/><br/><br/><h4>compile files</h4><br/><br/>";

        for (int i = 0; i < projectDirs.length; i++) {
            result += compileFiles(getFilesPathAndMessage(keywords, commitTime, svnDirs).get(SvnTools.updateFilesPath),
                                   srcDirs, projectDirs[i], encoding, compileTool, classpath);
        }

        return result;
    }

    /**
     * 编译文件到目标文件夹
     * @param filesPath
     * @param srcDirs
     * @param targetDir
     * @param encoding
     * @param compileTool
     * @param classpath
     * @return
     */
    public String compileFiles(ArrayList<String> filesPath, String[] srcDirs, String targetDir, String encoding,
                                String compileTool, String classpath) {
        String result = "";

        for (int i = 0; i < srcDirs.length; i++) {
            if (filesPath != null) {
                for (int j = 0; j < filesPath.size(); j++) {
                    String filePath = filesPath.get(j);
                    String relativeFilePath = getRelativeFilePath(filePath, srcDirs[i]);

                    if (relativeFilePath != null) {
                        String javaFilePath = srcDirs[i] + relativeFilePath;
                        result += javaFilePath + "," +
                                compileTools.compile(srcDirs[i], javaFilePath, targetDir, encoding, compileTool, classpath) + "<br/>";
                    }
                }
            } else {
                result += compileTools.compile(srcDirs[i], null, targetDir, encoding, compileTool, classpath) + "<br/>";
            }
        }

        return result;
    }

    /**
     * 抽取文件
     * @param keywords
     * @param commitTime
     * @param svnDirs
     * @param projectDirs
     * @return
     * @throws Exception
     */
    public String extractFiles(String keywords, String commitTime, String[] svnDirs, String[] projectDirs) throws Exception {
        return extractFiles(getFilesPathAndMessage(keywords, commitTime, svnDirs), projectDirs);
    }

    /**
     * 从工程文件夹拷贝更新文件到更新根目录文件夹
     * @param filesPathAndMessage
     * @param projectDirs
     * @return
     */
    public String extractFiles(HashMap<String, ArrayList<String>> filesPathAndMessage, String[] projectDirs) {
        logger.info("method extractFiles(), , parameters filesPathAndMessage,projectDirs: " + filesPathAndMessage + "," +
                      Utils.getArrayString(projectDirs));

        String updateRootDirName = getUpdateDirName(filesPathAndMessage.get(SvnTools.messages), projectDirs[0]);

        String result = "";
        if (filesPathAndMessage.get(SvnTools.updateFilesPath) != null &&
                filesPathAndMessage.get(SvnTools.updateFilesPath).size() > 0) {
            result = extractFiles(filesPathAndMessage.get(SvnTools.updateFilesPath), projectDirs, updateRootDirName);
        }


        if (filesPathAndMessage.get(SvnTools.deleteFilesPath) != null &&
                filesPathAndMessage.get(SvnTools.deleteFilesPath) != null) {
            result += extractDeleteFilesScript(filesPathAndMessage.get(SvnTools.deleteFilesPath), projectDirs, updateRootDirName);
        }


        ArrayList<String> allFilesPath = new ArrayList<String>();
        if (filesPathAndMessage.get(SvnTools.updateFilesPath) != null) {
            allFilesPath.addAll(filesPathAndMessage.get(SvnTools.updateFilesPath));
        }
        if (filesPathAndMessage.get(SvnTools.deleteFilesPath) != null) {
            allFilesPath.addAll(filesPathAndMessage.get(SvnTools.deleteFilesPath));
        }
        if (allFilesPath.size() > 0) {
            result += extractBackupFilesScript(allFilesPath, projectDirs, updateRootDirName);
        }


        return "<br/><br/><br/><h4>update files root dir: " + updateRootDirName + "</h4><br/><br/>" + result;
    }

    /**
     * 从源文件夹拷贝文件到目标文件夹
     * @param filesPath  文件在 SVN 的路径
     * @param projectDirs 本地工程根目录
     * @param targetDir
     * @return
     */
    public String extractFiles(ArrayList<String> filesPath, String[] projectDirs, String targetDir) {
        String succResult = "<br/><br/><br/><h4>extract files:</h4><br/><br/>";
        String failResult = "<br/><br/><br/><h4>fail extract files:</h4><br/><br/>";

        for (int k = 0; k < projectDirs.length; k++) {
            for (int i = 0; i < filesPath.size(); i++) {

                String filePath = filesPath.get(i);
                if (filePath.contains(java)) {
                    filePath = filePath.replace(java, CLASS);
                }

                String relativeFilePath = getRelativeFilePath(filePath, projectDirs[k]);
                if (relativeFilePath != null) {
                    if (fileTools.isFile(projectDirs[k] + relativeFilePath)) {
                        fileTools.copyFile(projectDirs[k] + relativeFilePath, targetDir + relativeFilePath);

                        if (relativeFilePath.contains(CLASS)) {
                            copyInnerClassFile(projectDirs[k] + relativeFilePath, targetDir + relativeFilePath);
                        }
                    } else {
                        fileTools.createDir(targetDir + relativeFilePath);
                    }

                    succResult += filePath + "<br/>";

                } else{
                    relativeFilePath = getRelativeFilePath(filePath, projectDirs[k] + webDir);
                    if(relativeFilePath != null) {
                        if (fileTools.isFile(projectDirs[k] + webDir + relativeFilePath)){
                            fileTools.copyFile(projectDirs[k] + webDir + relativeFilePath, targetDir + webDir + relativeFilePath);

                            if (relativeFilePath.contains(CLASS)) {
                                copyInnerClassFile(projectDirs[k]  + webDir +  relativeFilePath, targetDir  + webDir +  relativeFilePath);
                            }
                        } else {
                            fileTools.createDir(targetDir + webDir + relativeFilePath);
                        }

                        succResult += filePath + "<br/>";

                    } else {
                        relativeFilePath = getRelativeFilePath(filePath, projectDirs[k] + classDir);
                        if(relativeFilePath != null) {
                            if (fileTools.isFile(projectDirs[k] + classDir + relativeFilePath)){
                                fileTools.copyFile(projectDirs[k] + classDir + relativeFilePath, targetDir + classDir + relativeFilePath);

                                if (relativeFilePath.contains(CLASS)) {
                                    copyInnerClassFile(projectDirs[k] + classDir + relativeFilePath, targetDir + classDir + relativeFilePath);
                                }
                            } else {
                                fileTools.createDir(targetDir + classDir + relativeFilePath);
                            }

                            succResult += filePath + "<br/>";

                        } else {
                            relativeFilePath = getRelativeFilePath(filePath, projectDirs[k] + webInfDir);
                            if(relativeFilePath != null) {
                                if (fileTools.isFile(projectDirs[k] + webInfDir + relativeFilePath)){
                                    fileTools.copyFile(projectDirs[k] + webInfDir + relativeFilePath, targetDir + webInfDir + relativeFilePath);

                                    if (relativeFilePath.contains(CLASS)) {
                                        copyInnerClassFile(projectDirs[k] + webInfDir + relativeFilePath, targetDir + webInfDir + relativeFilePath);
                                    }
                                } else {
                                    fileTools.createDir(targetDir + webInfDir + relativeFilePath);
                                }

                                succResult += filePath + "<br/>";

                            } else {
                                failResult += filePath + "<br/>";
                            }

                        }
                    }

                }
            }
        }

        return succResult + failResult;
    }

    /**
     * 生成删除文件 bat
     * @param filesPath  文件在 SVN 的路径
     * @param projectDirs 本地工程根目录
     * @param targetDir
     * @return
     */
    public String extractDeleteFilesScript(ArrayList<String> filesPath, String[] projectDirs, String targetDir) {
        String succResult = "<br/><br/><br/><h4>extract delete files:</h4><br/><br/>";
        String failResult = "<br/><br/><br/><h4>fail extract delete files:</h4><br/><br/>";

        String scriptText = "";

        for (int k = 0; k < projectDirs.length; k++) {
            for (int i = 0; i < filesPath.size(); i++) {

                String filePath = filesPath.get(i);
                if (filePath.contains(java)) {
                    filePath = filePath.replace(java, CLASS);
                }

                // 由于 delete 操作后，文件被删除，只能根据文件所在文件夹获取相对路径
                String dirPath = null;
                String fileName = null;

                if (filePath.lastIndexOf("/") < filePath.lastIndexOf(".")) {   // filePath 为文件路径
                    dirPath = filePath.substring(0, filePath.lastIndexOf("/"));
                    fileName = filePath.substring(filePath.lastIndexOf("/")+1);
                } else {
                    dirPath = filePath;
                }

                String relativeFilePath = getRelativeFilePath(dirPath, projectDirs[k]);
                if (relativeFilePath != null) {
                    if (fileName != null) {
                        relativeFilePath += "/" + fileName;
                        scriptText += "del /f /s /q " + relativeFilePath.replace("/", "\\") + fileTools.lineSeparator;

                        if (relativeFilePath.contains(CLASS)) {
                            scriptText += deleteInnerClassFileScript(projectDirs[k] + relativeFilePath, relativeFilePath) + fileTools.lineSeparator;
                        }

                    } else if (fileTools.isEmptyDir(projectDirs[k] + relativeFilePath)) {
                        scriptText += "rd /q " + relativeFilePath.replace("/", "\\") + fileTools.lineSeparator;
                    }

                    succResult += filePath + "<br/>";

                } else{
                    relativeFilePath = getRelativeFilePath(dirPath, projectDirs[k] + webDir);
                    if(relativeFilePath != null) {
                        if (fileName != null) {
                            relativeFilePath += "/" + fileName;
                            scriptText += "del /f /s /q " + (webDir + relativeFilePath).replace("/", "\\") + fileTools.lineSeparator;

                            if (relativeFilePath.contains(CLASS)) {
                                scriptText += deleteInnerClassFileScript(projectDirs[k] + webDir + relativeFilePath, webDir + relativeFilePath) + fileTools.lineSeparator;
                            }

                        } else if (fileTools.isEmptyDir(projectDirs[k] + webDir + relativeFilePath)) {
                            scriptText += "rd /q " + (webDir + relativeFilePath).replace("/", "\\") + fileTools.lineSeparator;
                        }

                        succResult += filePath + "<br/>";

                    } else {
                        relativeFilePath = getRelativeFilePath(dirPath, projectDirs[k] + classDir);
                        if(relativeFilePath != null) {
                            if (fileName != null) {
                                relativeFilePath += "/" + fileName;
                                scriptText += "del /f /s /q " + (classDir + relativeFilePath).replace("/", "\\") + fileTools.lineSeparator;

                                if (relativeFilePath.contains(CLASS)) {
                                    scriptText += deleteInnerClassFileScript(projectDirs[k] + classDir + relativeFilePath, classDir + relativeFilePath) + fileTools.lineSeparator;
                                }

                            } else  if (fileTools.isEmptyDir(projectDirs[k] + classDir + relativeFilePath)) {
                                scriptText += "rd /q " + (classDir + relativeFilePath).replace("/", "\\") + fileTools.lineSeparator;
                            }

                            succResult += filePath + "<br/>";

                        } else {
                            relativeFilePath = getRelativeFilePath(dirPath, projectDirs[k] + webInfDir);
                            if(relativeFilePath != null) {
                                if (fileName != null) {
                                    relativeFilePath += "/" + fileName;
                                    scriptText += "del /f /s /q " + (webInfDir + relativeFilePath).replace("/", "\\") + fileTools.lineSeparator;

                                    if (relativeFilePath.contains(CLASS)) {
                                        scriptText += deleteInnerClassFileScript(projectDirs[k] + webInfDir + relativeFilePath, webInfDir + relativeFilePath) + fileTools.lineSeparator;
                                    }

                                } else if (fileTools.isEmptyDir(projectDirs[k] + webInfDir + relativeFilePath))  {
                                    scriptText += "rd /q " + (webInfDir + relativeFilePath).replace("/", "\\") + fileTools.lineSeparator;
                                }

                                succResult += filePath + "<br/>";

                            } else {
                                String[] dirs = dirPath.split("/");
                                String dir = "";
                                for (int j = 0; j < dirs.length; j++) {
                                    dir += "/" + dirs[j];

                                    scriptText += "rd /q " + dir.replace("/", "\\") + fileTools.lineSeparator;
                                    scriptText += "rd /q " + (webDir + dir).replace("/", "\\") + fileTools.lineSeparator;
                                    scriptText += "rd /q " + (classDir + dir).replace("/", "\\") + fileTools.lineSeparator;
                                    scriptText += "rd /q " + (webInfDir + dir).replace("/", "\\") + fileTools.lineSeparator;
                                }

                                failResult += filePath + "<br/>";
                            }

                        }
                    }

                }
            }
        }

        fileTools.writeFile(targetDir + "/delete.bat", scriptText);

        return succResult + failResult;
    }

    /**
     * 生成备份文件 bat
     * @param filesPath  文件在 SVN 的路径
     * @param projectDirs 本地工程根目录
     * @param targetDir
     * @return
     */
    public String extractBackupFilesScript(ArrayList<String> filesPath, String[] projectDirs, String targetDir) {
        String succResult = "<br/><br/><br/><h4>extract backup files:</h4><br/><br/>";
        String failResult = "<br/><br/><br/><h4>fail extract backup files:</h4><br/><br/>";

        String scriptText = "";

        for (int k = 0; k < projectDirs.length; k++) {
            for (int i = 0; i < filesPath.size(); i++) {

                String filePath = filesPath.get(i);
                if (filePath.contains(java)) {
                    filePath = filePath.replace(java, CLASS);
                }

                String relativeFilePath = getRelativeFilePath(filePath, projectDirs[k]);
                if (relativeFilePath != null) {
                    if (fileTools.isFile(projectDirs[k] + relativeFilePath)) {
                        scriptText += "echo f|xcopy ." + relativeFilePath.replace("/", "\\") + " " + (backupDir + relativeFilePath).replace("/", "\\") + " /s " + fileTools.lineSeparator;

                        if (relativeFilePath.contains(CLASS)) {
                            scriptText += copyInnerClassFileScript(projectDirs[k] + relativeFilePath, relativeFilePath) + fileTools.lineSeparator;
                        }

                    } else if (fileTools.isEmptyDir(projectDirs[k] + relativeFilePath)) {
                        scriptText += "echo d|xcopy ." + relativeFilePath.replace("/", "\\") + " " + (backupDir + relativeFilePath).replace("/", "\\") + " /e " + fileTools.lineSeparator;
                    }

                    succResult += filePath + "<br/>";

                } else{
                    relativeFilePath = getRelativeFilePath(filePath, projectDirs[k] + webDir);
                    if(relativeFilePath != null) {
                        if (fileTools.isFile(projectDirs[k] + webDir + relativeFilePath)) {
                            scriptText += "echo f|xcopy ." + (webDir + relativeFilePath).replace("/", "\\") + " " + (backupDir + webDir + relativeFilePath).replace("/", "\\") + " /s " + fileTools.lineSeparator;

                            if (relativeFilePath.contains(CLASS)) {
                                scriptText += copyInnerClassFileScript(projectDirs[k] + webDir + relativeFilePath, webDir + relativeFilePath) + fileTools.lineSeparator;
                            }

                        } else if (fileTools.isEmptyDir(projectDirs[k] + webDir + relativeFilePath)) {
                            scriptText += "echo d|xcopy ." + (webDir + relativeFilePath).replace("/", "\\") + " " + (backupDir + webDir + relativeFilePath).replace("/", "\\") + " /e " + fileTools.lineSeparator;
                        }

                        succResult += filePath + "<br/>";

                    } else {
                        relativeFilePath = getRelativeFilePath(filePath, projectDirs[k] + classDir);
                        if(relativeFilePath != null) {
                            if (fileTools.isFile(projectDirs[k] + classDir + relativeFilePath)) {
                                scriptText += "echo f|xcopy ." + (classDir + relativeFilePath).replace("/", "\\") + " " + (backupDir + classDir + relativeFilePath).replace("/", "\\") + " /s " + fileTools.lineSeparator;

                                if (relativeFilePath.contains(CLASS)) {
                                    scriptText += copyInnerClassFileScript(projectDirs[k] + classDir + relativeFilePath, classDir + relativeFilePath) + fileTools.lineSeparator;
                                }

                            } else if (fileTools.isEmptyDir(projectDirs[k] + classDir + relativeFilePath)) {
                                scriptText += "echo d|xcopy ." + (classDir + relativeFilePath).replace("/", "\\") + " " + (backupDir + classDir + relativeFilePath).replace("/", "\\") + " /e " + fileTools.lineSeparator;
                            }

                            succResult += filePath + "<br/>";

                        } else {
                            relativeFilePath = getRelativeFilePath(filePath, projectDirs[k] + webInfDir);
                            if(relativeFilePath != null) {
                                if (fileTools.isFile(projectDirs[k] + webInfDir + relativeFilePath)) {
                                    scriptText += "echo f|xcopy ." + (webInfDir + relativeFilePath).replace("/", "\\") + " " + (backupDir + webInfDir + relativeFilePath).replace("/", "\\") + " /s " + fileTools.lineSeparator;

                                    if (relativeFilePath.contains(CLASS)) {
                                        scriptText += copyInnerClassFileScript(projectDirs[k] + webInfDir + relativeFilePath, webInfDir + relativeFilePath) + fileTools.lineSeparator;
                                    }

                                } else if (fileTools.isEmptyDir(projectDirs[k] + webInfDir + relativeFilePath)) {
                                    scriptText += "echo d|xcopy ." + (webInfDir + relativeFilePath).replace("/", "\\") + " " + (backupDir + webInfDir + relativeFilePath).replace("/", "\\") + " /e " + fileTools.lineSeparator;
                                }

                                succResult += filePath + "<br/>";

                            } else {
                                failResult += filePath + "<br/>";
                            }
                        }
                    }
                }
            }
        }

        fileTools.writeFile(targetDir + "/backup.bat", scriptText);

        return succResult + failResult;
    }

    /**
     * 获取文件相对于目录 srcDir 的路径
     * @param relativeFilePath
     * @param srcDir
     * @return
     */
    public String getRelativeFilePath(String relativeFilePath, String srcDir) {
        if (new File(srcDir + relativeFilePath).exists()) {
            return relativeFilePath;

        } else {
            if (relativeFilePath.indexOf("/") == 0) {
                relativeFilePath = relativeFilePath.substring(1);

                if (relativeFilePath.indexOf("/") != -1) {
                    return getRelativeFilePath(relativeFilePath.substring(relativeFilePath.indexOf("/")), srcDir);
                }
            }

            return null;
        }
    }

    /**
     * 获取更新文件路径及备注
     * @param keywords
     * @param commitTime
     * @param svnDirs
     * @return
     * @throws Exception
     */
    public HashMap<String, ArrayList<String>> getFilesPathAndMessage(String keywords, String commitTime, String[] svnDirs) throws Exception {
        logger.info("method getFilesPathAndMessage(), parameters keywords,commitTime,svnDir: " + keywords + "," + commitTime + "," + Utils.getArrayString(svnDirs));

        HashMap<String, ArrayList<String>> filesPathAndMessage = new HashMap<String, ArrayList<String>>();

        if (keywords != null && !keywords.trim().equals("")) {
            for (int i = 0; i < svnDirs.length; i++) {
                HashMap<String, ArrayList<String>> tempFPM = svnTools.getFilesPathAndMessageByKeyWord(keywords, svnDirs[i]);

                 if (i == 0) {
                     filesPathAndMessage = tempFPM;

                 } else {
                     if (filesPathAndMessage.get(SvnTools.messages) != null && tempFPM.get(SvnTools.messages) !=null) {
                         filesPathAndMessage.get(SvnTools.messages).addAll(tempFPM.get(SvnTools.messages));
                     }
                     if (filesPathAndMessage.get(SvnTools.messages) == null && tempFPM.get(SvnTools.messages) !=null) {
                         filesPathAndMessage.put(SvnTools.messages, tempFPM.get(SvnTools.messages));
                     }

                     if (filesPathAndMessage.get(SvnTools.updateFilesPath) != null && tempFPM.get(SvnTools.updateFilesPath) !=null) {
                         filesPathAndMessage.get(SvnTools.updateFilesPath).addAll(tempFPM.get(SvnTools.updateFilesPath));
                     }
                     if (filesPathAndMessage.get(SvnTools.updateFilesPath) == null && tempFPM.get(SvnTools.updateFilesPath) !=null) {
                         filesPathAndMessage.put(SvnTools.updateFilesPath, tempFPM.get(SvnTools.updateFilesPath));
                     }

                     if (filesPathAndMessage.get(SvnTools.deleteFilesPath) != null && tempFPM.get(SvnTools.deleteFilesPath) !=null) {
                         filesPathAndMessage.get(SvnTools.deleteFilesPath).addAll(tempFPM.get(SvnTools.deleteFilesPath));
                     }
                     if (filesPathAndMessage.get(SvnTools.deleteFilesPath) == null && tempFPM.get(SvnTools.deleteFilesPath) !=null) {
                         filesPathAndMessage.put(SvnTools.deleteFilesPath, tempFPM.get(SvnTools.deleteFilesPath));
                     }

                 }
            }
        }


        if (commitTime != null && !commitTime.trim().equals("")) {
            HashMap<String, ArrayList<String>> ctFilesPathAndMessage = new HashMap<String, ArrayList<String>>();

            for (int i = 0; i < svnDirs.length; i++) {
                HashMap<String, ArrayList<String>> tempFPM = svnTools.getFilesPathAndMessageByCommitDate(new SimpleDateFormat("yyyy-MM-dd").parse(commitTime), svnDirs[i]);

                if (i == 0) {
                    ctFilesPathAndMessage = tempFPM;

                } else {
                    if (ctFilesPathAndMessage.get(SvnTools.messages) != null && tempFPM.get(SvnTools.messages) !=null) {
                        ctFilesPathAndMessage.get(SvnTools.messages).addAll(tempFPM.get(SvnTools.messages));
                    }
                    if (ctFilesPathAndMessage.get(SvnTools.messages) == null && tempFPM.get(SvnTools.messages) !=null) {
                        ctFilesPathAndMessage.put(SvnTools.messages, tempFPM.get(SvnTools.messages));
                    }

                    if (ctFilesPathAndMessage.get(SvnTools.updateFilesPath) != null && tempFPM.get(SvnTools.updateFilesPath) !=null) {
                        ctFilesPathAndMessage.get(SvnTools.updateFilesPath).addAll(tempFPM.get(SvnTools.updateFilesPath));
                    }
                    if (ctFilesPathAndMessage.get(SvnTools.updateFilesPath) == null && tempFPM.get(SvnTools.updateFilesPath) !=null) {
                        ctFilesPathAndMessage.put(SvnTools.updateFilesPath, tempFPM.get(SvnTools.updateFilesPath));
                    }

                    if (ctFilesPathAndMessage.get(SvnTools.deleteFilesPath) != null && tempFPM.get(SvnTools.deleteFilesPath) !=null) {
                        ctFilesPathAndMessage.get(SvnTools.deleteFilesPath).addAll(tempFPM.get(SvnTools.deleteFilesPath));
                    }
                    if (ctFilesPathAndMessage.get(SvnTools.deleteFilesPath) == null && tempFPM.get(SvnTools.deleteFilesPath) !=null) {
                        ctFilesPathAndMessage.put(SvnTools.deleteFilesPath, tempFPM.get(SvnTools.deleteFilesPath));
                    }
                }
            }


            if (filesPathAndMessage.get(SvnTools.updateFilesPath) != null && ctFilesPathAndMessage.get(SvnTools.updateFilesPath) != null) {
                filesPathAndMessage.get(SvnTools.updateFilesPath).addAll(ctFilesPathAndMessage.get(SvnTools.updateFilesPath));
            }
            if (filesPathAndMessage.get(SvnTools.updateFilesPath) == null && ctFilesPathAndMessage.get(SvnTools.updateFilesPath) != null) {
                filesPathAndMessage.put(SvnTools.updateFilesPath, ctFilesPathAndMessage.get(SvnTools.updateFilesPath));
            }

            if (filesPathAndMessage.get(SvnTools.deleteFilesPath) != null && ctFilesPathAndMessage.get(SvnTools.deleteFilesPath) != null) {
                filesPathAndMessage.get(SvnTools.deleteFilesPath).addAll(ctFilesPathAndMessage.get(SvnTools.deleteFilesPath));
            }
            if (filesPathAndMessage.get(SvnTools.deleteFilesPath) == null && ctFilesPathAndMessage.get(SvnTools.deleteFilesPath) != null) {
                filesPathAndMessage.put(SvnTools.deleteFilesPath, ctFilesPathAndMessage.get(SvnTools.deleteFilesPath));
            }
        }

        logger.info("更新文件总数:" +
                ((filesPathAndMessage.get(SvnTools.updateFilesPath) == null ? 0 : filesPathAndMessage.get(SvnTools.updateFilesPath).size()) +
                 (filesPathAndMessage.get(SvnTools.deleteFilesPath) == null ? 0 : filesPathAndMessage.get(SvnTools.deleteFilesPath).size()))) ;

        return filesPathAndMessage;
    }

    public String getUpdateDirName(ArrayList<String> message, String srcDir) {
        String updateDir = "";

        if (message != null) {
            for (int i = 0; i < message.size(); i++) {
                updateDir += message.get(i).toString() + ";";
            }
            updateDir = fileTools.stringFilter(updateDir);
            if (updateDir.length() > FileTools.fileNameLength) {
                updateDir = updateDir.substring(0, FileTools.fileNameLength);
            }
        }

        return (srcDir + update + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + " " + updateDir).trim();
    }

    public void copyInnerClassFile(String srcFilePath, String targetFilePath) {
        String targetDir = targetFilePath.substring(0, targetFilePath.lastIndexOf("/"));

        ArrayList<File> files = getInnerClassFiles(srcFilePath);
        for (int i = 0; i < files.size(); i++) {
                fileTools.copyFile(files.get(i).getPath(), targetDir + "/" + files.get(i).getName());
        }
    }

    public String deleteInnerClassFileScript(String projectFilePath, String targetRelativeFilePath) {
        String scriptText = "";

        String dir =  targetRelativeFilePath.substring(0, targetRelativeFilePath.lastIndexOf("/")+1);
        String className = projectFilePath.substring(projectFilePath.lastIndexOf("/")+1).replace(CLASS, "");

        scriptText += "del /f /s /q " + (dir + className + "$*.class").replace("/", "\\") + fileTools.lineSeparator;

        return scriptText;
    }

    public String copyInnerClassFileScript(String projectFilePath, String targetRelativeFilePath) {
        String scriptText = "";

        String dir =  targetRelativeFilePath.substring(0, targetRelativeFilePath.lastIndexOf("/")+1);

        ArrayList<File> files = getInnerClassFiles(projectFilePath);
        for (int i = 0; i < files.size(); i++) {
            scriptText += "echo f|xcopy ." + (dir + files.get(i).getName()).replace("/", "\\") + " " + (backupDir + dir + files.get(i).getName()).replace("/", "\\") + " /s " + fileTools.lineSeparator;
        }

        return scriptText;
    }

    public ArrayList<File> getInnerClassFiles(String projectFilePath) {
        int pos = projectFilePath.lastIndexOf("/");
        String dir =  projectFilePath.substring(0, pos);
        String fileName = projectFilePath.substring(pos+1);
        String className = fileName.replace(CLASS, "");

        ArrayList<File> files = fileTools.getFilesStartByPerfix(dir, className);
        ArrayList<File> innerClassFiles = new ArrayList<File>();
        for (int i = 0; i < files.size(); i++) {
            if (!files.get(i).getName().equals(fileName)) {
                innerClassFiles.add(files.get(i));
            }
        }

        return innerClassFiles;
    }

}
