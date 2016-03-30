package ie.dit.d13122842;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@WebServlet("/MainServlet")
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

		try {
			
			// showVars(request); // displays all pixel values
			
			String action = request.getParameter("action");

			if (action == null || !action.equalsIgnoreCase("uploadCleaned")) {
				System.out.println("-> Exiting, action is invalid or missing.");
				return;
			}

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
