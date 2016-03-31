package ie.dit.d13122842;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;


@WebServlet("/MainServlet")
@MultipartConfig
public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	private void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException {
		
		System.out.println("In processRequest...");
		
		try {
			
			// showVars(request); // displays all pixel values
			
			String action = request.getHeader("action");

			if (action == null || !action.equalsIgnoreCase("uploadCleaned")) {
				System.out.println("-> Exiting, action is invalid or missing.");
				return;
			}
			
			receiveMultipart(request, response);

		} catch (Exception ex) {
			System.out.println("--> Exception in processRequest(): "
					+ ex.getMessage());
		}
	}
	
	protected void receiveMultipart(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		// Based on https://docs.oracle.com/javaee/7/tutorial/servlets016.htm#BABDGFJJ
		
		System.out.println("In receiveMultipart()...");
		
		// prints out all header values
		System.out.println("===== Begin headers =====");
		Enumeration<String> names = request.getHeaderNames();
		while (names.hasMoreElements()) {
			String headerName = names.nextElement();
			System.out.println(headerName + " = "
					+ request.getHeader(headerName));
		}
		System.out.println("===== End headers =====");
		
		response.setContentType("text/html;charset=UTF-8");
		
		final PrintWriter writer = response.getWriter();
		
		// clients upload the file (handled below), the star and the number of planes
		// file contents is a series of planes, all bound by the same box
		String starNum = request.getHeader("starNum");
		String planeCount = request.getHeader("planeCount");
		
		// check parameters are set		
		if (starNum == null || planeCount == null) {
			String msg = "Error. Post must contain "
					+ "starNum and planeCount.";
			System.out.println(msg);
			writer.println(msg);
			return;
		}
		
		// get the filename from the file part of the POST
		final Part filePart = request.getPart("file");
		if (filePart == null) {
			String msg = "Error: The post request did not include a \"file\" part.";
			System.out.println(msg);
			writer.println(msg);
			return;
		}	
		final String fitsFilename = getFileName(filePart);
		
		// Prefix the filename with the directory and
		// suffix it with the star number (e.g. 0000999.fits > 0000999_1.fits)
		String pathFilename = Config.RESULTSDIR;
		pathFilename += fitsFilename.substring(0,  fitsFilename.lastIndexOf('.'));
		pathFilename += "_"+starNum;
		pathFilename += fitsFilename.substring(fitsFilename.lastIndexOf('.'));
		
		OutputStream out = null;
		InputStream filecontent = null;

		try {
			out = new FileOutputStream(new File(pathFilename));
			filecontent = filePart.getInputStream();

			int read = 0;
			final byte[] bytes = new byte[1024];

			while ((read = filecontent.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			
			System.out.println("Uploaded data saved to " + pathFilename + ".");
			
			// Forward the saved file to S3. Put into "cleaned" subFolder with the same filename.
			String s3key = Config.AWS_Cleaned.BUCKET_PREFIX+pathFilename.substring(pathFilename.lastIndexOf('/'));
			boolean deleteLocalCopy = true;
			boolean postAsMagnitudeJob = true; // post AFTER successful upload to s3
			AWS_S3_Uploader s3 = new AWS_S3_Uploader(pathFilename, s3key, deleteLocalCopy, postAsMagnitudeJob);
			s3.start();  
			
			String bucketURL = Config.AWS_Cleaned.ENDPOINT+Config.AWS_Cleaned.BUCKET+"/"+s3key;

			// return AWS file's public URL
			// e.g. https://s3-us-west-2.amazonaws.com/cleanedfits/cleaned/0000001_1.fits		
			writer.println(bucketURL);
			
		} catch (FileNotFoundException fne) {
			String msg = "Error. Either no file included with POST or attempt "
					+ " to upload to an invalid location." + fne.getMessage();
			writer.println(msg);
			System.out.println(msg);
		} finally {
			if (out != null) out.close();
			if (filecontent != null) filecontent.close();
			if (writer != null) writer.close();
		}
	}

	private String getFileName(final Part part) {	
		final String partHeader = part.getHeader("content-disposition");
		System.out.println("Part Header = " + partHeader);
		for (String content : part.getHeader("content-disposition").split(";")) {
			if (content.trim().startsWith("filename")) {
				return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
			}
		}
		return null;
	}
	
	private void showVars(HttpServletRequest request) {

		Date now = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss");
		System.out.println("--- " + ft.format(now) + '\n');

		// debug info
		HttpSession session = request.getSession();
		Enumeration<String> e = session.getAttributeNames();
		while (e.hasMoreElements()) {
			String name = (String) e.nextElement();
			String val = session.getAttribute(name).toString();
			System.out.println("--- Session: '" + name + "' = '" + val + "'");
		}

		// more debug info
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String paramName = parameterNames.nextElement();
			System.out.println("--- Parameter: '" + paramName + "'");
			String[] paramValues = request.getParameterValues(paramName);
			for (int i = 0; i < paramValues.length; i++) {
				String paramValue = paramValues[i];
				System.out.println("\t'" + paramValue + "'");
			}
		}
	}
	
}
