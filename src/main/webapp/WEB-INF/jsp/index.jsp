<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
    <title>update helper</title>
    <link rel="stylesheet" type="text/css"  href="/res/css/jQuery-ui.css" />
    <script type="text/javascript" src="/res/js/jQuery-1.7.2.min.js"></script>
    <script type="text/javascript" src="/res/js/jQuery-ui.js"></script>
    <script type="text/javascript">
        $(function(){
            $("#commitTime").datepicker();
        });
    </script>
</head>
<body  onkeydown="submitForm(event);" autocomplete="off">
<a href="<%=request.getContextPath()%>/">产生更新文件</a>&nbsp;&nbsp;<a href="<%=request.getContextPath()%>/statusCheckPage">系统 HTTP 状态检查</a><br/><br/><br/>
<h3>在输入框输入更新信息，产生更新文件</h3><br/><br/>
<form action="<%=request.getContextPath()%>/extractFiles" method="post" id="gfForm">
    系统：<select>
    <option onclick="setFfp();">ffp</option>
</select>
    备注关键字：<input type="text" name="keywords"  autocomplete="off" value="${keywords}" />&nbsp;&nbsp;或&nbsp;&nbsp;
    提交时间：<input type="text" id="commitTime" name="commitTime" value="${commitTime}" /><br/>
    SVN 目录:<textarea id="svnDirs" name="svnDirs" rows="2" cols="70">https://10.1.11.111/svn/myPro</textarea><br/>
    本地源文件目录:<textarea id="srcDirs" name="srcDirs" rows="2" cols="70">D:/myPro</textarea><br/>
    本地工程目录:<textarea id="projectDirs" name="projectDirs" rows="2" cols="70">D:/myPro/WebContent</textarea><br/>
    编译工具:<select id="compileTool" name="compileTool" >
                <option value="C:/Program Files/Java/jdk1.6.0_27/bin/javac.exe">jdk1.6.0_27</option>
                <option value="D:/maven-3.2.1/bin/mvn.bat">maven-3.2.1</option>
                <option value="C:/Program Files/Java/jdk1.5.0_17/bin/javac.exe">jdk1.5.0_17</option>
                <option value="C:/Program Files/Java/j2sdk1.4.2_02/bin/javac.exe">j2sdk1.4.2_02</option>
                <option value="C:/Program Files/Java/jdk1.7.0_13/bin/javac.exe">jdk1.7.0_13</option>
            </select><br/>
    源文件编码:<select id="encoding" name="encoding" >
                    <option value="UTF-8">UTF-8</option>
                    <option value="GBK">GBK</option>
                </select><br/>
    编译时所用到类所在路径:<textarea id="classpath" name="classpath" rows="2" cols="70">D:/myPro/WebContent/WEB-INF/lib</textarea><br/><br/><br/>
    <button type="submit" onclick="updateFiles();">更新文件</button>
    <button type="submit" onclick="compileFiles();">编译文件</button>
    <button type="submit">提取文件</button>
</form>
<p>${updateRootDirName}</p>
</body>
<script type="text/javascript">
    function updateFiles() {
        document.getElementById("gfForm").action = "<%=request.getContextPath()%>/updateFiles";
        document.getElementById("gfForm").submit();
    }

    function compileFiles() {
        document.getElementById("gfForm").action = "<%=request.getContextPath()%>/compileFiles";
        document.getElementById("gfForm").submit();
    }

    function setFfp() {
        document.getElementById("svnDirs").value = "https://10.1.11.111/svn/ffp";
        document.getElementById("projectDirs").value = "D:/FFP/WebRoot";
    }

    function submitForm(e) {
        if (e.keyCode == 13 || e.which == 13) {
            document.getElementById("gfForm").submit();
        }
    }
</script>
</html>