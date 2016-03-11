<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="job_current" value="${sessionScope.job_current}" />

<html>
	<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Create Processing Job</title>
</head>
<body>

	<h2>Create Processing Job</h2>
 
 	<table><tr><td>Job Type:</td><td>
		
		<form action="MainServlet" method="post">
			<input type="hidden" name="action" value="job_change" />
		    <select name="job_selection" onchange="this.form.submit()">
		        <option 
		        	value="job_clean"
		        	${job_current=="job_clean" ? 'selected="selected"' : ''}>
		        	FITS Cleaning
	        	</option>
		        <option 
		        	value="job_photometry" 
		        	${job_current=="job_photometry" ? 'selected="selected"' : ''}>
		        	FITS Star Photometry
	       		</option>
		        <option 
		        	value="other"
		        	${job_current=="other" ? 'selected="other"' : ''}>
		        	Other
	        	</option>
		    </select>
		</form>
	
	</td></tr></table>
	
	<c:if test='${not empty job_current}'>
		<h4>
			Required information for job '<c:out value="${job_current}" />':<br />
		</h4>
	</c:if>
	
	<form action="MainServlet" method="post">
		<input type="hidden" name="action" value="job_submit" />
		<table>
			<tr>
				<td align="right">Description:</td>
				<td align="right"><input name="description" size=50 type="text" value="FITS Cleaning" /></td>
			</tr>
		
			<c:choose>
				<c:when test='${job_current=="job_clean"}'>
					Form controls for job_clean...
					<tr>
						<td align="right">Work Queue URL:</td>
						<td><input name="work_queue_url" size=50 type="text" value="amqp://test:test@192.168.3.21:5672" /></td>
					</tr>
					<tr>
						<td align="right">Work Queue Name:</td>
						<td><input name="work_queue_name" size=50 type="text" value="work_queue" /></td>
					</tr>
					<tr>
						<td align="right">Result Queue URL:</td>
						<td><input name="result_queue_url" size=50 type="text" value="amqp://test:test@192.168.3.21:5672" /></td>
					</tr>
					<tr>
						<td align="right">Result Queue Name:</td>
						<td><input name="result_queue_name" size=50 type="text" value="result_queue" /></td>
					</tr>
					<tr>
						<td align="right">API Server URL:</td>
						<td><input name="api_server_url" size=50 type="text" value="http://192.168.3.13:8080/FITSAPIServer/MainServlet" /></td>
					</tr>
					<tr>
						<td align="right">Flat Filename:</td>
						<td><input name="flat_filename" size=50 type="text" value="Final-MasterFlat.fits" /></td>
					</tr>
					<tr>
						<td align="right">Bias Filename:</td>
						<td><input name="bias_filename" size=50 type="text" value="Final-MasterBias-subrect.fits" /></td>
					</tr>
					<tr>
						<td align="right">Config Filename:</td>
						<td><input name="config_filename" size=50 type="text" value="config" /></td>
					</tr>
					<tr>
						<td align="right">No. of FITS Files:</td>
						<td><input name="fits_file_count" size=5 type="text" value="20" /></td>
					</tr>
					<tr>
						<td align="right">Planes per FITS:</td>
						<td><input name="planes_per_fits" size=5 type="text" value="2" /></td>
					</tr>			
				</c:when>
				<c:when test='${job_current=="job_photometry"}'>
					Form controls for job_photometry...
				</c:when>
		   		<c:otherwise>
		   			Form controls for the default...
				</c:otherwise>
			</c:choose>
		
		<tr><td/><td><input type="submit" value="submit" /></td></tr>
		</table>

	</form>

</body>
</html>