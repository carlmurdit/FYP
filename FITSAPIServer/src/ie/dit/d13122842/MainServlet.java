package ie.dit.d13122842;

import java.beans.XMLEncoder;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
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
	private static String fitsDir;

	public MainServlet() {
		super();
	}

	public void init() throws ServletException {
		fitsDir = "/Users/carl/Dropbox/FYP/Eclipse/APIServer/APIServer/WebContent/fitsdir";
		// if (...) throw new UnavailableException("Error");
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		System.out.println("into doGet()");
		processRequest(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		System.out.println("into doPost()");
		processRequest(request, response);
	}

	private void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException {

		try {

			System.out.println("into processRequest()");

			Date now = new Date();
			SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss");
			System.out.println("--- " + ft.format(now) + '\n');

			// debug info
			HttpSession session = request.getSession();
			Enumeration<String> e = session.getAttributeNames();
			while (e.hasMoreElements()) {
				String name = (String) e.nextElement();
				String val = session.getAttribute(name).toString();
				System.out.println("--- '" + name + "' = '" + val + "'");
			}

			// more debug info
			Enumeration<String> parameterNames = request.getParameterNames();
			while (parameterNames.hasMoreElements()) {
				String paramName = parameterNames.nextElement();
				System.out.println("Parameter: '" + paramName + "'");
				String[] paramValues = request.getParameterValues(paramName);
				for (int i = 0; i < paramValues.length; i++) {
					String paramValue = paramValues[i];
					System.out.println("\t'" + paramValue + "'");
				}
			}

			String action = request.getParameter("action");

			if (action == null) {
				System.out.println("-> Exiting, no action specified.");
				return;
			}

			if (action.equalsIgnoreCase(GETFILE)) {

				System.out.println("in GETFILE");

				String filename = request.getParameter("filename");
				// filename = "config";
				if (filename == null) {
					System.out
							.println("-> Exiting, no filename specified for GETFILE.");
					return;
				}
				forwardToPage(request, response, "/fitsdir/" + filename);

			} else if (action.equalsIgnoreCase(GETBOX)) {

				System.out.println("in GETBOX");

				String filename = request.getParameter("filename"); // e.g.
																	// Final-MasterFlat.fits
				String box = request.getParameter("box"); // e.g. [220:3,300:83]
				String plane = request.getParameter("plane"); // e.g. 1 (1st)

				// validate
				if (filename == null || box == null || plane == null) {
					System.out
							.println("-> Exiting, filename, box or plane missing for GETBOX.");
					return;
				}
				
				//
				// File resultFile = new
				// File("/WEB-INF/configuration.properties");
				// ServletContext context = getServletContext();
				// InputStream is = context.getResourceAsStream(filename);
				//

				String resultFileName = "/Users/carl/Documents/git/fyp/FITSAPIServer/WebContent/my_ShowData_2.result";
				File resultFile = new File(getServletContext().getRealPath(
						resultFileName));
				if (resultFile.exists())
					resultFile.delete();

	
				// Prepare to call external program
				String app = "/Users/carl/Documents/git/fyp/FITS_C/src/my_ShowData_2";
				String fitsDir = "/Users/carl/Documents/git/fyp/FITS_C/src/fits/";
				String param1 = fitsDir + filename + box;
				String param2 = plane;
				String param3 = resultFileName;
				System.out.println("Calling\n\t" + app + "\n\t" + param1
						+ "\n\t" + param2 + "\n\t" + param3);
				String[] cmdArray = new String[] { app, param1, param2, param3 };

				ShellExecute se = new ShellExecute();
				String resp = se.executeCommand(cmdArray);
				System.out.println("my_ShowData_2 returned: \n" + resp + "\n");
				
				serveFile(response, resultFileName, "text/plain");
				
				return;

				// forwardToPage(request, response,
				// "/results/my_ShowData_2.result");

				// response.setStatus(HttpServletResponse.SC_OK);
				// PrintWriter ow = response.getWriter();
				// ow.write(resp);
				// ow.flush();
				// //
				// response.setContentType("text/html");

				// InputStream is =
				// getServletContext().getResourceAsStream(resultFileName);
				// if (is != null) {
				// InputStreamReader isr = new InputStreamReader(is);
				// BufferedReader reader = new BufferedReader(isr);
				// PrintWriter writer = response.getWriter();
				// String text = "";
				//
				// while ((text = reader.readLine()) != null) {
				// System.out.println(text);
				// writer.println(text);
				// }
				// } else {
				// System.out.println("Error getting resource "+resultFileName);
				// }

				// String mimeType = "text/plain";
				// String contentDisposition =
				// String.format("attachment; filename=%s",
				// resultFile.getName());
				// int fileSize = Long.valueOf(resultFile.length()).intValue();
				//
				// response.setContentType(mimeType);
				// response.setHeader("Content-Disposition",
				// contentDisposition);
				// response.setContentLength(fileSize);
				//
				// try (OutputStream out = response.getOutputStream()) {
				// Path path = resultFile.toPath();
				// Files.copy(path, out);
				// out.flush();
				// } catch (IOException ioe) {
				// System.out.println(ioe.toString());
				// System.out.println(String.format("Error outputting %1s: ",
				// resultFile.getCanonicalPath())+ioe.getMessage());
				// }
				


//				try {
//					FileInputStream inputStream = new FileInputStream(
//							resultFileName);
//					String disposition = "attachment; fileName=resultFile.txt";
//					response.setContentType("text/plain");
//					response.setHeader("Content-Disposition", disposition);
//					response.setHeader(
//							"content-Length",
//							String.valueOf(stream(inputStream,
//									response.getOutputStream())));
//
//				} catch (IOException ioe) {
//					System.out
//							.println("Error occurred while downloading file: "
//									+ ioe.getMessage());
//				}


			} else {

				System.out.println("Invalid action: '" + action + "'");
			}

		} catch (Exception ex) {
			System.out.println("--> Exception in processRequest(): "
					+ ex.getMessage());
		}

	}
//
//	private long stream(InputStream input, OutputStream output)
//			throws IOException {
//
//		ReadableByteChannel inputChannel = Channels.newChannel(input);
//		WritableByteChannel outputChannel = Channels.newChannel(output);
//		ByteBuffer buffer = ByteBuffer.allocate(4096);
//		long size = 0;
//
//		while (inputChannel.read(buffer) != -1) {
//			buffer.flip();
//			size += outputChannel.write(buffer);
//			buffer.clear();
//		}
//		return size;
//
//	}

	private void processRequest_GetFile(HttpServletRequest request,
			HttpServletResponse response) throws ServletException {

		System.out.println("into processRequest_GetFile()");

		try {

			/*
			 * // to do: set fitsDir in web.xml fitsDir =
			 * getServletContext().getInitParameter("fits-Dir"); if (fitsDir ==
			 * null || fitsDir.equals("")) throw new ServletException(
			 * "Invalid or non-existent 'fits-Dir' context-param.");
			 */

			// Get FITS number from POST or GET parameter
			String fitsNum = request.getParameter("fitsNum");
			if (fitsNum == null)
				fitsNum = "0000803"; // for testing
			System.out.println("fitsNum param = " + fitsNum);

			// Download compressed FITS.fz file
			String remoteFitsFz = "https://s3-eu-west-1.amazonaws.com/"
					+ "astronomydata/AstronomyData/compressedRAW/" + fitsNum
					+ ".fits.fz";

			BinarySaver bs = new BinarySaver();
			String fileNameFitsFz = bs.saveBinaryFile(remoteFitsFz, fitsDir);
			// String fileNameFitsFz = fitsDir+"/"+fitsNum + ".fits.fz";

			System.out.println("Downloaded fileNameFitsFz: " + fileNameFitsFz);

			// Extract n.fits.fz to n.fits
			Extractor extractor = new Extractor();
			String fileNameFits = extractor.extractFitsFz(fileNameFitsFz);

			System.out.println("Extracted fileNameFits: " + fileNameFits);

			// Serve the file to the client
			serveFile(response, fileNameFits, "text/plain");

			System.out.println("Served fileNameFits: " + fileNameFits);

		} catch (Exception e) {
			System.out.println("Exception in processRequest(): "
					+ e.getMessage());
			// throw new ServletException(e.getMessage());
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

	private void send_xml(HttpServletResponse response, Object data) {
		try {
			XMLEncoder enc = new XMLEncoder(response.getOutputStream());
			enc.writeObject(data); // data.toSring() in book
			enc.close();
		} catch (IOException e) {
			throw new HTTPException(
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private void sendhtml(HttpServletResponse response, Object data) {
		String html_start = "<html><head><title>send html response</title></head><body><div>";
		String html_end = "</div></body></html>";
		String html_doc = html_start + data.toString() + html_end;
		send_plain(response, html_doc);
	}

	private void send_plain(HttpServletResponse response, Object data) {
		try {
			OutputStream out = response.getOutputStream();
			out.write(data.toString().getBytes()); // offset param?
			out.flush();
		} catch (IOException e) {
			throw new HTTPException(
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private void forwardToPage(HttpServletRequest request,
			HttpServletResponse response, String page) {

		System.out.println("forwardToPage: " + page);

		// File file = new File (page); // need to verify a relative path
		// if (!file.exists() || file.isDirectory()) {
		// System.out.println("Invalid forwardToPage: "+page);
		// }

		// Get the request dispatcher object and forward the request
		RequestDispatcher dispatcher = getServletContext()
				.getRequestDispatcher(page);

		try {
			dispatcher.forward(request, response);
		} catch (Exception e) {
			System.out.println("Error in forwardToPage: " + e.getMessage());
		}

	}

}
