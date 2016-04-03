<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="job_current" value="${sessionScope.job_current}" />

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link href="css/style.css" rel="stylesheet">
	<title>Create Processing Job</title>
</head>
<body>

	<%@include file="includes/header.jsp" %>

	<h2>Create Processing Job</h2>
 
 	<table class="table1"><tr><td align="right">Job Type:</td><td>
		
		<form action="MainServlet" method="post">
			<input type="hidden" name="action" value="job_change" />
		    <select name="job_current" onchange="this.form.submit()">
		    	<option 
		        	value=""
		        	${job_current=="" ? 'selected="selected"' : ''}>
	        	</option>
		        <option 
		        	value="job_clean"
		        	${job_current=="job_clean" ? 'selected="selected"' : ''}>
		        	FITS Cleaning
	        	</option>
		        <option 
		        	value="job_magnitude" 
		        	${job_current=="job_magnitude" ? 'selected="selected"' : ''}>
		        	FITS Star Magnitude
	       		</option>
		    </select>
		</form>
	
	</td></tr></table>
	

	
	<form action="MainServlet" method="post">
		<input type="hidden" name="action" value="job_submit" />
		
			<c:choose>
				<c:when test='${empty job_current}'>
					<h4>Select a Job Type</h4>
				</c:when>
				<c:when test='${job_current=="job_clean"}'>
					<h4>
						FITS Cleaning Job Parameters:
					</h4>
					<table class="table1">
					<tr>
						<td align="right">Description:</td>
						<td align="right"><input name="description" size=50 type="text" value="FITS Cleaning" /></td>
					</tr>
					<tr>
						<td align="right">Work Queue URL:</td>
						<td><input name="work_queue_url" size=50 type="text" value="amqp://test:test@<c:out value="${mq_host}"/>:5672" /></td>
					</tr>
					<tr>
						<td align="right">Work Queue Name:</td>
						<td><input name="work_queue_name" size=50 type="text" value="<c:out value="${work_queue}"/>" /></td>
					</tr>
					<tr>
						<td align="right">Result Queue URL:</td>
						<td><input name="result_queue_url" size=50 type="text" value="amqp://test:test@<c:out value="${mq_host}"/>:5672" /></td>
					</tr>
					<tr>
						<td align="right">Result Queue Name:</td>
						<td><input name="result_queue_name" size=50 type="text" value="<c:out value="${result_queue}"/>" /></td>
					</tr>
					<tr>
						<td align="right">API Server URL:</td>
						<td><input name="api_server_url" size=50 type="text" value="http://<c:out value="${api_host}"/>:8080/FITSAPIServer/MainServlet" /></td>
					</tr>
					<tr>
						<td align="right">Result Server URL:</td>
						<td><input name="result_server_url" size=50 type="text" value="http://<c:out value="${result_host}"/>:8080/FITSAPIResultServer/MainServlet" /></td>
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
						<td align="right">Source Bucket</td>
						<td><input name="source_bucket" size=50 type="text" value="<c:out value="${source_bucket}"/>" /> </td>
					</tr>
					<tr>
						<td align="right">Source Bucket Prefix</td>
						<td><input name="source_bucket_prefix" size=50 type="text" value="<c:out value="${source_bucket_prefix}"/>" /></td>
					</tr>								
					<tr>			
						<td align="right">First FITS Number:</td>				   			
						<td><input type="number" min="1" max="5000" step="1" name="fits_num_start" size=5 type="text" value="1" required/></td>
					</tr>
					<tr>
						<td align="right">Last FITS Number:</td>
						<td><input type="number" min="1" max="5000" step="1" name="fits_num_end" size=5 type="text" value="2" required/></td>
					</tr>
					<tr>
						<td align="right">Planes per FITS:</td>
						<td><input type="number" min="1" max="20" step="1" name="planes_per_fits" size=5 type="text" value="1" required/></td>
					</tr>
					<tr>
						<td align="right">Activity to process results:</td>
						<td>						
							<select name="following_job">
						    	<option value="0" selected="selected">
						        	None
					        	</option>
					        	<c:choose>
					        		<c:when test='${job_current=="job_magnitude"}'>
								        <option value="1">
								        	FITS Cleaning
							        	</option>
							        </c:when>
					        		<c:when test='${job_current=="job_clean"}'>
								        <option value="2">
								        	FITS Star Magnitude
							       		</option>
					       			</c:when>
					       		</c:choose>
						    </select>
						</td>
					</tr>
					<tr><td/><td><input type="submit" value="submit" /></td></tr>
					</table>
				</c:when>
				<c:when test='${job_current=="job_magnitude"}'>
					<h4>
						FITS Star Magnitude Job Parameters:
					</h4>
					<table class="table1">
					<tr>
						<td align="right">Description:</td>
						<td align="right"><input name="description" size=50 type="text" value="<c:out value="${description}"/>" /></td>
					</tr>
					<tr>
						<td align="right">Work Queue URL:</td>
						<td><input name="work_queue_url" size=50 type="text" value="amqp://test:test@<c:out value="${mq_host}"/>:5672" /></td>
					</tr>
					<tr>
						<td align="right">Work Queue Name:</td>
						<td><input name="work_queue_name" size=50 type="text" value="<c:out value="${work_queue}"/>" /></td>
					</tr>
					<tr>
						<td align="right">Result Queue URL:</td>
						<td><input name="result_queue_url" size=50 type="text" value="amqp://test:test@<c:out value="${mq_host}"/>:5672" /></td>
					</tr>
					<tr>
						<td align="right">Result Queue Name:</td>
						<td><input name="result_queue_name" size=50 type="text" value="<c:out value="${result_queue}"/>" /></td>
					</tr>
					<tr>
						<td align="right">API Server URL:</td>
						<td><input name="api_server_url" size=50 type="text" value="http://<c:out value="${api_host}"/>:8080/FITSAPIServer/MainServlet" /></td>
					</tr>
					<tr>
						<td align="right">Config Filename:</td>
						<td><input name="config_filename" size=50 type="text" value="config" /></td>
					</tr>
					<tr>
						<td align="right">Source Bucket</td>
						<td><input name="source_bucket" size=50 type="text" value="<c:out value="${source_bucket}"/>" /> </td>
					</tr>
					<tr>
						<td align="right">Source Bucket Prefix</td>
						<td><input name="source_bucket_prefix" size=50 type="text" value="<c:out value="${source_bucket_prefix}"/>" /></td>
					</tr>
					<tr>			
						<td align="right">First FITS Number:</td>				   			
						<td><input type="number" min="1" max="5000" step="1" name="fits_num_start" size=5 type="text" value="1" required/></td>
					</tr>
					<tr>
						<td align="right">Last FITS Number:</td>
						<td><input type="number" min="1" max="5000" step="1" name="fits_num_end" size=5 type="text" value="2" required/></td>
					</tr>
					<tr>
						<td align="right">Planes per FITS:</td>
						<td><input type="number" min="1" max="20" step="1" name="planes_per_fits" size=5 type="text" value="1" required/></td>
					</tr>
					<tr><td/><td><input type="submit" value="submit" /></td></tr>	
					</table>
					<input type="hidden" name="result_server_url" value="" />
					<input type="hidden" name="flat_filename" value="" />
					<input type="hidden" name="bias_filename" value="" />
					<input type="hidden" name="following_job" value="0" />
				</c:when>
		   		<c:otherwise>
		   			Form controls for the default...
				</c:otherwise>
			</c:choose>	
		
		</table>

	</form>

</body>
</html>