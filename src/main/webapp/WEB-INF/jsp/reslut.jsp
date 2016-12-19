<%--
  Created by IntelliJ IDEA.
  User: mzj
  Date: 16-1-29
  Time: 上午10:44
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title></title>
</head>
<body>
<a href="<%=request.getContextPath()%>/">产生更新文件</a>&nbsp;&nbsp;<a href="<%=request.getContextPath()%>/statusCheckPage">系统 HTTP 状态检查</a><br/><br/><br/>
       <form method="post" action="<%=request.getContextPath()%>/statusCheck">
           系统名称:<select name="systemName">
           <option value="b2a"onclick="setFFPUrls();">FFP</option>
            </select>

           <textarea id="urls" name="urls" rows="3" cols="40">http://10.2.22.112/</textarea>
           <input type="submit" value="验证">
       </form>
<p>${result}</p>
</body>
<script type="text/javascript">
     function setB2AUrls(){
         document.getElementById("urls").value = "http://10.2.22.112/";
     } 

</script>
</html>