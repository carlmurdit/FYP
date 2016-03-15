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
		        	value="job_photometry" 
		        	${job_current=="job_photometry" ? 'selected="selected"' : ''}>
		        	FITS Star Photometry
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
						FITS Cleaning Job Parameters:<br />
					</h4>
					<table>
					<tr>
						<td align="right">Description:</td>
						<td align="right"><input name="description" size=50 type="text" value="FITS Cleaning" /></td>
					</tr>
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
						<td align="right">First FITS Number:</td>				   			
						<td><input type="number" min="1" max="5000" step="1" name="fits_num_start" size=5 type="text" value="1" required/></td>
					</tr>
					<tr>
						<td align="right">Last FITS Number:</td>
						<td><input type="number" min="1" max="5000" step="1" name="fits_num_end" size=5 type="text" value="1" required/></td>
					</tr>
					<tr>
						<td align="right">Planes per FITS:</td>
						<td><input type="number" min="1" max="20" step="1" name="planes_per_fits" size=1 type="text" value="2" required/></td>
					</tr>
					<tr><td/><td><input type="submit" value="submit" /></td></tr>
					</table>
				</c:when>
				<c:when test='${job_current=="job_photometry"}'>
					<h4>
						FITS Star Photometry Job Parameters:<br />
					</h4>
					<table>
					<tr>
						<td align="right">Description:</td>
						<td align="right"><input name="description" size=50 type="text" value="FITS Cleaning" /></td>
					</tr>
					<tr>
						<td align="right">Work Queue URL:</td>
						<td><input name="work_queue_url" size=50 type="text" value="amqp://test:test@192.168.3.21:5672" /></td>
					</tr>
					<tr>
						<td align="right">Work Queue Name:</td>
						<td><input name="work_queue_name" size=50 type="text" value="work_queue" /></td>
					</tr>					
					<!-- <tr><td/><td><input type="submit" value="submit" /></td></tr>	 -->
					</table>
					<h4>
					FITS Star Photometry Job is not yet implemented.
					</h4>
				</c:when>
		   		<c:otherwise>
		   			Form controls for the default...
				</c:otherwise>
			</c:choose>
		
		
		</table>

	</form>

</body>
</html>