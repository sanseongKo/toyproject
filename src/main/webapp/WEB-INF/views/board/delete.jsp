<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>삭제</title>
</head>
<body>
	<form name="deleteForm" action="<c:url value="/delete/${cid}" />" method="post">
		<input size="1" name="cid" value="${cid}"/>번 글을 삭제하시겠습니까?<br>
		<!-- 비밀번호 : <input name="password" /> -->
		<input type="submit" value="삭제"><a href="<c:url value="/contentRead/${cid}" />">취소</a>
	</form>
	
	<div>${msg}</div>
</body>
</html>