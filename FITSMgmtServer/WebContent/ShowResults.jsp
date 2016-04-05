<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%@ page import="java.util.List"%>
<%@ page import="ie.dit.d13122842.ResultMessage"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link href="css/style.css" rel="stylesheet">
<title>Work Results</title>

<c:if test='${not empty resultMessages}'>
	<script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
	<script type="text/javascript">

	google.charts.load("current", {
		packages : ['table', 'corechart']
	});
	google.charts.setOnLoadCallback(drawChart);
	google.charts.setOnLoadCallback(drawTable);
    
 	function drawChart() {
		
		try {
			
			// Generate Chart
			var cdata = google.visualization.arrayToDataTable([
	                ['Work Unit', 'Processing Time'],
	                <c:forEach items="${resultMessages}" var="resultMessage">
	                    [ '${resultMessage.tooltip}', ${resultMessage.processingTime} ],
	                </c:forEach>
	                ]);
			
			var options = {
				title : 'Histogram of Processing Times',
				legend : {position : 'none'}
			};
				
			var chart = new google.visualization.Histogram(document.getElementById('chart_div'));
			chart.draw(cdata, options);
		
    	} catch(err) {
    	    document.getElementById("err").innerHTML = "Error drawing chart: "+err.message;
    	}
	}

	
    function drawTable() {
    	try {
        var data = new google.visualization.DataTable();
    	data.addColumn('boolean', 'Success');
    	data.addColumn('number', 'Time (ms)');
    	data.addColumn('string', 'Activity');
     	data.addColumn('string', 'Source File');
    	data.addColumn('number', 'Planes');
    	data.addColumn('string', 'Results');
        data.addColumn('string', 'Device');   
        data.addColumn('string', 'Follower'); 
        data.addColumn('string', 'Error');
      	<c:forEach items="${resultMessages}" var="resultMessage">
      		data.addRow([
      		         ${resultMessage.success},
    		         ${resultMessage.processingTime},
      		        '${resultMessage.activity}',
      		      	'${resultMessage.sourceFileName}',
      		      	 ${resultMessage.planes},
    		        '${resultMessage.results}',
    		        '${resultMessage.androidId}',
    		   	 	'${resultMessage.followingJob}',
    		   	 	'${resultMessage.errorMessage}'
    		 ]);
        </c:forEach> 
        
        // todo: use different colours for different activities: 
        // data.setProperty(1, 1, 'style', 'background-color: red;');

        var table = new google.visualization.Table(document.getElementById('table_div'));
        table.draw(data, {showRowNumber: true, width: '100%', height: '100%', allowHtml: true});
    	} catch(err) {
    	    document.getElementById("err").innerHTML = err.message;
    	}
      } 

</script>
</c:if>
</head>
<body>
	<div><%@include file="includes/header.jsp" %></div><br />
	
	<h4>Click Submit to retrieve results
	<form action="MainServlet" method="post">

		    <select name="job_current">
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
		    
		<input type="hidden" name="action" value="get_results" />
		<input type="submit" value="submit" />
	</form>
	</h4>
	
	<c:if test="${resultMessages != null}">
		<c:choose>
			<c:when test="${fn:length(resultMessages) eq 0}">
	   			<p>No messages found.</p>
			</c:when>
			<c:otherwise>				
				<div id="err"></div>
				<div id="chart_div" style="width: 900px; height: 500px;"></div>
				<div id="table_div" style="width: 1100px;"></div>
			</c:otherwise>
		</c:choose>
	</c:if>
	
</body>
</html>