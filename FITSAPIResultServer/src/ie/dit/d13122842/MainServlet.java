package ie.dit.d13122842;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
			
			showVars(request);
			
			String action = request.getParameter("action");

			if (action == null || !action.equalsIgnoreCase("upload")) {
				System.out.println("-> Exiting, action is invalid or missing.");
				return;
			}

			// the clients upload the filename, the star, and the data.
			// data is a series of planes, all bound by the same box
			String fitsFilename = request.getParameter("fitsFilename");
			String starNum = request.getParameter("starNum");
			String planeCount = request.getParameter("planeCount");
			ArrayList<String> imgs = new ArrayList<String>();
			
			// check parameters are set
			if (fitsFilename == null || starNum == null || planeCount == null) {
				System.out.println("-> Exiting. fitsFilename, starNum or planeCount missing for upload.");
				return;
			}
			
			// Store the images together
			int iPlaneCount = Integer.parseInt(planeCount);
			for (int i=1; i<=iPlaneCount; i++) {
				imgs.add(request.getParameter("img_"+i));
			}
			
			// Check there are one or more images
			if (imgs.size() == 0) {
				System.out.println("-> Exiting. No images found in parameters.");
				return;
			}
			
			// save it to file
			new FITSCreator().saveResult(fitsFilename, starNum, imgs);
			
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
