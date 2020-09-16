<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.web.CallMessage" %>
<html>
<head>
    <title>index</title>
</head>
<body>
<h2>Hello World!</h2>
 <div>
   <%=new CallMessage().Show()%>
 </div>
</body>
</html>
