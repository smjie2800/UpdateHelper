<!DOCTYPE html>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html lang="en">
<% try {%>
<body>
<c:url value="/res/text.txt" var="url"/>
<spring:url value="/res/text.txt" htmlEscape="true" var="springUrl" />
<a href="/res/text.txt">text.txt</a>
Spring URL: ${springUrl} at ${time}
<br>
JSTL URL: ${url}
<br>
Message: ${message}
</body>
<% } catch (Exception e) { e.printStackTrace();}%>
</html>