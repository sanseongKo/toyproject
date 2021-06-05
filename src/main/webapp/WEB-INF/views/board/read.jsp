<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%> 
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>${contentVO.title}&nbsp내용</title>
<link rel="stylesheet"
	href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css">
<!-- 달력 flatpickr -->
<script src="https://cdn.jsdelivr.net/npm/flatpickr"></script>

<!--  step1 - 라이브러리 추가  -->
<!--  제이쿼리  -->
<script type="text/javascript"
	src="https://code.jquery.com/jquery-1.12.4.min.js"></script>
</head>

<body>

	<div style="float: right;">

		<c:choose>

			<c:when test="${contentVO.on_off == 1}">
				<%@ include file="/WEB-INF/views/board/onPay.jsp"%>

			</c:when>

			<c:when test="${contentVO.on_off == 2}">

				<%@ include file="/WEB-INF/views/board/offPay.jsp"%>

			</c:when>



			<c:otherwise>
						잘못된 정보입니다. 
					</c:otherwise>

		</c:choose>


	</div>



	<div>
		<div>${contentVO.title}&nbsp &nbsp &nbsp &nbsp &nbsp 판매자: &nbsp
			${VendorNickname} &nbsp &nbsp &nbsp &nbsp &nbsp 작성일: &nbsp
			${contentVO.condate}</div>
	</div>




	<div>
		<table border="1">
			<tr>
				<th>상품 설명</th>
				<td>${contentVO.content}
					<c:forEach var="name" items="${images}" varStatus="status">
					<div>
						<p><img
							src="<spring:url value="/image/${name}"/>" style="width: 300px; height: 100px" /> </p>
						<br>	
						
					</div>
					</c:forEach>
				</td>
				
			</tr>
		</table>
	</div>





	<div>
		<c:choose>
			<c:when test="${contentVO.on_off == 1}">
				<%@ include file="/WEB-INF/views/board/onRead.jsp"%>
			</c:when>

			<c:when test="${contentVO.on_off == 2}">
				<%@ include file="/WEB-INF/views/board/offRead.jsp"%>
			</c:when>

			<c:otherwise>
				<tr>
					<th>무슨 클래스게~?</th>
				</tr>
			</c:otherwise>
		</c:choose>
	</div>

	<div>
		<c:choose>
			<c:when test="${memberVO.verification == 3}">
				<!-- 관리자일 경우	:	글 수정/삭제 버튼 + 목록 버튼 -->
				<a href="<c:url value="/edit/${contentVO.cid}"/>">수정</a>
				<a href="<c:url value="/delete/${contentVO.cid}"/>">삭제</a>
				<a href="<c:url value="/"/>">목록</a>
			</c:when>
			<c:otherwise>
				<!-- 판매자 및 구매자일 경우		:	 목록 버튼만 -->
				<button
					onClick="location.href='<c:url value="/edit/${contentVO.cid}"/>'">수정</button>
				<button
					onClick="location.href='<c:url value="/delete/${contentVO.cid}"/>'">삭제</button>
				<button onClick="history.go(-1)">이전 목록</button>
			</c:otherwise>
		</c:choose>
	</div>

	<div>
		<form:form commandName="replyVO" method="POST">
			<%@ include file="/WEB-INF/views/reply/repWrite.jsp"%>
		</form:form>
	</div>

	<div>
		<%@ include file="/WEB-INF/views/reply/repList.jsp"%>
	</div>





</body>
</html>