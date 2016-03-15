package ie.dit.d13122842;

import java.beans.XMLEncoder;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import javax.xml.ws.http.HTTPException;

@WebServlet("/MainServlet")
public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String GETFILE = "GetFile";
	private static final String GETBOX = "GetBox";
	
	public MainServlet() {
		super();
	}

	public void init() throws ServletException {}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	private void processRequest(
			HttpServletRequest request,
			HttpServletResponse response) throws ServletException {

		try {

			showVars(request); // debug request parameters 

			String action = request.getParameter("action");

			if (action == null) {
				System.out.println("-> Exiting, no action specified.");
				return;
			}

			if (action.equalsIgnoreCase(GETFILE)) {

				System.out.println("in GETFILE");

				String filename = request.getParameter("filename");
				if (filename == null) {
					System.out.println("-> Exiting, no filename specified for GETFILE.");
					return;
				}
			
				serveFile(response, Config.FITSDIR+filename, "text/plain");
				
			} else if (action.equalsIgnoreCase(GETBOX)) {
				
				System.out.println("in GETBOX");

				String filename = request.getParameter("filename"); // e.g. Final-MasterFlat.fits
				String box = request.getParameter("box"); // e.g. [220:3,300:83]
				String plane = request.getParameter("plane"); // e.g. 1 (1st)

				// validate
				if (filename == null || box == null || plane == null) {
					System.out.println("-> Exiting. Filename, box or plane missing for GETBOX.");
					return;
				}
				
				// The FITS filenames originate from the compressed versions on AWS S3.
				// But we deal with extracted versions so remove the ".fz" extension 
				if (filename.endsWith(".fz")) {
					filename = filename.substring(0,filename.lastIndexOf('.'));
				}
				
				// download the file from AWS if necessary
				checkDownloaded(filename);
				
				// Prepare to call external program
				String app = Config.BINDIR+"my_ShowData";
				String param1 = Config.FITSDIR + filename + box;
				String param2 = plane;
				System.out.println("Calling\n\t"+app+"\n\t"+param1+"\n\t"+param2);
				String[] cmdArray = new String[] { app, param1, param2 };
				
				// call routine that calls the program and send its output as the http response
				runCommand(cmdArray, response);
			
				return;

			} else {

				System.out.println("Invalid action: '" + action + "'");
			}

		} catch (Exception ex) {
			System.out.println("--> Exception in processRequest(): "+ex.getMessage());
		}

	}
	
	private void runCommand(String cmdarray[], HttpServletResponse response) {
		
		ServletOutputStream stream = null;
		String s = null;
		
		try {

			Process p = Runtime.getRuntime().exec(cmdarray);

			BufferedReader stdError = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));
			
			BufferedInputStream buf = new BufferedInputStream(p.getInputStream());
			int readBytes = 0;
			stream = response.getOutputStream();
			// read the output from the command, write to the ServletOutputStream
			while ((readBytes = buf.read()) != -1) {
				stream.write(readBytes);
			}
				
			// read any errors from the attempted command
			while ((s = stdError.readLine()) != null) {
				System.out.println(s);
			}

		} catch (IOException e) {
			System.out.println("Error in runCommand(): " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void checkDownloaded(String filename) {
		// takes a filename and checks it that it exists in the FITS directory
		// otherwise it downloads it from AWS S3 and extracts it.
		
		try {

			// Check if file already exists in the fits directory
			if (new File(Config.FITSDIR+filename).exists()) {
				System.out.println(filename +" is cached.");
				return;
			} else {
				System.out.println("Downloading "+filename +" from S3...");
			}
			
			if (!filename.endsWith(".fits")) {
				// this routine is only for downloading fits files
				throw new Exception("Invalid file type for download, expecting .fits: "+filename);
			}

			// Identify the compressed version (.fz file) on S3
			String remotePathFile_Fz = Config.RAWFITS_URL + filename + ".fz";
					
			BinarySaver bs = new BinarySaver();
			String pathFile_Fz = bs.saveBinaryFile(remotePathFile_Fz, Config.FITSDIR);

			System.out.println("Downloaded fileNameFitsFz: " + pathFile_Fz);

			// Extract n.fits.fz to n.fits
			Extractor extractor = new Extractor();
			String pathFile = extractor.extractFitsFz(pathFile_Fz);

			System.out.println("Extracted fileNameFits: " + pathFile);

		} catch (Exception e) {
			System.out.println("Exception in verifyFITSFile(): "
					+ e.getMessage());
		}

	}

	private void serveFile(HttpServletResponse response, String fileName, String contentType)
			throws Exception {

		ServletOutputStream stream = null;
		BufferedInputStream buf = null;

		try {

			stream = response.getOutputStream();

			File fitsFile = new File(fileName);
			
			System.out.println(String.format(
					"Served file "+fileName+", Size = ",+fitsFile.length()));

			// set response headers
			response.setContentType(contentType); // e.g "application/octet-stream" or "text/plain" 
			response.addHeader("Content-Disposition", "attachment; filename="
					+ fitsFile.getName());
			response.setContentLength((int) fitsFile.length());

			FileInputStream input = new FileInputStream(fitsFile);
			buf = new BufferedInputStream(input);
			int readBytes = 0;

			// read from the file; write to the ServletOutputStream
			while ((readBytes = buf.read()) != -1)
				stream.write(readBytes);

		} catch (IOException ioe) {

			throw new Exception("Exception in serveFile(): Error serving file "
					+ fileName + ": " + ioe.getMessage());

		} finally {

			try {
				// close the input/output streams
				if (stream != null)
					stream.close();
				if (buf != null)
					buf.close();
			} catch (IOException ioe) {
				throw new Exception(
						"Exception in serveFile(): Error closing stream/buffer: "
								+ fileName + ": " + ioe.getMessage());
			}

		}

	}

	// routine to log time, form parameters and session variables
	private void showVars(HttpServletRequest request) {

		Date now = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss");
		System.out.println("--- " + ft.format(now) + '\n');

		// Session variables
		HttpSession session = request.getSession();
		Enumeration<String> e = session.getAttributeNames();
		while (e.hasMoreElements()) {
			String name = (String) e.nextElement();
			String val = session.getAttribute(name).toString();
			System.out.println("--- Session: '" + name + "' = '" + val + "'");
		}

		// Variables submitted with GET / POST
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
