<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ page import="java.util.ArrayList"%>
<%@ page import="ie.dit.d13122842.CleaningJob"%>



<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Job Submitted</title>
	<script>
		function goBack() {
		    window.history.back();
		}
	</script>
</head>
<body>

<h4>The files below have been queued for processing.</h4>

<button onclick="goBack()">Close</button>

<table class="table1"> 

	<!-- Table Header -->
	<thead>
		<tr>
			<th>Name</th>
		</tr>
	</thead>
	
	<!-- Table Body -->
	<tbody>
		<c:forEach items="${cleaningjob.FITS_Filenames}" var="current">
		<tr>
			<td><c:out value="${current}" /></td>
		</tr>
		</c:forEach>
	</tbody>

</table>


</body>
</html>