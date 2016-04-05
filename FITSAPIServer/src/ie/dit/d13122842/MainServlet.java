package ie.dit.d13122842;

import ie.dit.d13122842.Enums.FITS_Type;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;

@WebServlet("/MainServlet")
public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public MainServlet() {
		super();
	}

	public void init() throws ServletException {}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	@Override
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

			if (action.equalsIgnoreCase("getFile")) {
				
				processGetFile(request, response);
				
			} else if (action.equalsIgnoreCase("getBox")) {
				
				processGetBox(request, response);
				
			} else if (action.equalsIgnoreCase("getClean")) {
				
				processGetClean(request, response);

			} else {

				System.out.println("Invalid action: '" + action + "'");
			}

		} catch (Exception ex) {
			System.out.println("--> Exception in processRequest(): "+ex.getMessage());
		}

	}
	
	private void processGetFile(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		System.out.println("in processGetFile()");

		String filename = request.getParameter("filename");
		if (filename == null) {
			System.out.println("-> Exiting, no filename specified for GETFILE.");
			return;
		}
	
		serveFile(response, Config.FITSDIR+filename, "text/plain");
	
	}
	
	private void processGetBox(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		System.out.println("in processGetBox()");

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
		String app = Config.app.FITS_SUBSET;
		String param1 = Config.FITSDIR + filename + box;
		String param2 = plane;
		System.out.println("Calling\n\t"+app+"\n\t"+param1+"\n\t"+param2);
		String[] cmdArray = new String[] { app, param1, param2 };
		
		// call routine that calls the program and send its output as the http response
		runCommand(cmdArray, response);
	
		return;
	}
	
	private void processGetClean(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		System.out.println("in processGetClean()");

		String filename = request.getParameter("filename"); // e.g. 0000005_1.fits
		if (filename == null ) {
			System.out.println("-> Exiting. Filename missing for getClean.");
			return;
		}
		
		AWS_S3_Get s3 = new AWS_S3_Get();
		InputStream fileIn;
		try {
			fileIn = s3.getFile(FITS_Type.CLEAN, filename);
		} catch (Exception e) {
			System.out.println("Failed to get file '"+filename+"'. "+e.getMessage());
			return;
		}
		
		ServletOutputStream stream = response.getOutputStream();
		
		BufferedInputStream buf = new BufferedInputStream(fileIn);
		int readBytes = 0;
		// read the output from the command, write to the ServletOutputStream
		while ((readBytes = buf.read()) != -1) {
			stream.write(readBytes);
		}
		
/*		BufferedReader reader = new BufferedReader(new InputStreamReader(fileIn));
        while (true) {
            String line = reader.readLine();
            if (line == null) break;
            System.out.println("    " + line);
        }*/
		
	}
	
	private void runCommand(String cmdarray[], HttpServletResponse response) throws Exception {		

		try {
			
			ServletOutputStream stream = response.getOutputStream();
			
			Process p = Runtime.getRuntime().exec(cmdarray);

			// Prepare to read stdOut and stdError of the called program 
			BufferedReader stdError = 
					new BufferedReader(new InputStreamReader(p.getErrorStream()));			
			BufferedInputStream buf = new BufferedInputStream(p.getInputStream());
			
			int readBytes = 0;
			// read the output from the command, write to the ServletOutputStream
			while ((readBytes = buf.read()) != -1) {
				stream.write(readBytes);
			}
				
			// read any errors from the attempted command
			String errLine = null, err = null;
			while ((errLine = stdError.readLine()) != null) {
				System.out.println(errLine);
				err += errLine;
			}
			if (err != null) {
				throw new Exception("Error returned from called program: "+err);
			}

		} catch (IOException e) {
			throw new Exception("Error in runCommand(): " + e.getMessage());
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
			String remotePathFile_Fz = Config.aws.raw.URL() + filename + ".fz";
			
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
