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
			
			System.out.println("In processRequest with an action...");
			
			try {
				receiveMultipart(request, response);
			} catch (IOException e) {
				System.out.println("ERROR! "+e.getMessage());
				e.printStackTrace();
			}
			
			if (true) return;
		
			// the clients upload the filename, the star, and the data.
			// data is a series of planes, all bound by the same box
			String fitsFilename = request.getParameter("fitsFilename");
			String starNum = request.getParameter("starNum");
			String planeCount = request.getParameter("planeCount");
			String images = request.getParameter("images");
			
			// check parameters are set		
			if (fitsFilename == null || starNum == null || planeCount == null || images == null) {
				System.out.println("-> Exiting. Post must contain "
						+ "fitsFilename, starNum, planeCount and images.");
				return;
			}
			
			// save to a file on the server
			String pathFilename = new FITSCreator().saveResult(fitsFilename, starNum, images);
			
			// Forward the saved file to S3. Put into "cleaned" subfolder with the same filename.
			String s3key = Config.AWS_Cleaned.BUCKET_PREFIX+pathFilename.substring(pathFilename.lastIndexOf('/'));
			boolean deleteLocalCopy = true;
			boolean postAsMagnitudeJob = true; // post AFTER successful upload to s3
			AWS_S3_Uploader s3 = new AWS_S3_Uploader(pathFilename, s3key, deleteLocalCopy, postAsMagnitudeJob);
			// ToDo: enable s3.start();  
			
			String bucketURL = Config.AWS_Cleaned.ENDPOINT+Config.AWS_Cleaned.BUCKET+"/"+s3key;

			// return AWS file's public URL
			// e.g. https://s3-us-west-2.amazonaws.com/cleanedfits/cleaned/0000001_1.fits		
			response.getOutputStream().write(bucketURL.getBytes());
			
		} catch (Exception ex) {
			System.out.println("--> Exception in processRequest(): "
					+ ex.getMessage());
		}
	}
	
	static final int BUFFER_SIZE = 4096;
	
	protected void doFileUpload(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		System.out.println("In doFileUpload...");
		
		// This works, but the file contents includes the boundaries and "Content-Disposition:..."

		// Gets file name for HTTP header
		String fileName = request.getHeader("fileName");
		System.out.println("fileName: "+fileName);
		File saveFile = new File(Config.RESULTSDIR + fileName);

		// prints out all header values
		System.out.println("===== Begin headers =====");
		Enumeration<String> names = request.getHeaderNames();
		while (names.hasMoreElements()) {
			String headerName = names.nextElement();
			System.out.println(headerName + " = "
					+ request.getHeader(headerName));
		}
		System.out.println("===== End headers =====\n");

		// opens input stream of the request for reading data
		InputStream inputStream = request.getInputStream();

		// opens an output stream for writing file
		FileOutputStream outputStream = new FileOutputStream(saveFile);

		byte[] buffer = new byte[BUFFER_SIZE];
		int bytesRead = -1;
		System.out.println("Receiving data...");
	
		while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
		}

		System.out.println("Data received.");
		outputStream.close();
		inputStream.close();

		System.out.println("File written to: " + saveFile.getAbsolutePath());

		// sends response to client
		response.getWriter().print("UPLOAD DONE");
	}
	
	protected void receiveMultipart(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		// Based on https://docs.oracle.com/javaee/7/tutorial/servlets016.htm#BABDGFJJ
		
		System.out.println("In doFileUploadJEE...");
		
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

		final Part filePart = request.getPart("file");
		if (filePart == null) {
			String msg = "Error: The post request did not include a \"file\" part.";
			System.out.println(msg);
			writer.println(msg);
			return;
		}		
		final String fileName = getFileName(filePart);
		
		OutputStream out = null;
		InputStream filecontent = null;

		try {
			out = new FileOutputStream(new File(Config.RESULTSDIR + fileName));
			filecontent = filePart.getInputStream();

			int read = 0;
			final byte[] bytes = new byte[1024];

			while ((read = filecontent.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}

			String msg = "New file " + fileName + " created at " + Config.RESULTSDIR;
			writer.println(msg);
			System.out.println(msg);
			
		} catch (FileNotFoundException fne) {
			String msg = "You either did not specify a file to upload or are "
					+ "trying to upload a file to a protected or nonexistent "
					+ "location." + fne.getMessage();
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
