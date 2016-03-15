package ie.dit.d13122842;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/MainServlet")
public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String JOB_CHANGE = "job_change";
	private static final String JOB_SUBMIT = "job_submit";

	public MainServlet() {
		super();
	}

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

			if (action == null) {
				System.out.println("-> Exiting, no action specified.");
				return;
			}

			if (action.equalsIgnoreCase(JOB_CHANGE)) {

				String job = request.getParameter("job_current");
				if (job != null) {
					request.getSession().setAttribute("job_current", job);
				}

				forwardToPage(request, response, "/CreateJob.jsp");

			} else if (action.equalsIgnoreCase(JOB_SUBMIT)) {

				String job = null;
				if (request.getSession().getAttribute("job_current") != null) {
					job = (String) request.getSession().getAttribute(
							"job_current");
				}
				
				System.out.println("job="+job);

				if (job == null) {
					System.out.println
						("-> No job_current set for action job_submit, exiting.");
				} else if (job.compareTo("job_magnitude")==0) {
					System.out.println("job_magnitude not implemented.");
				} else if (job.compareTo("job_clean")==0) {
					
					CleaningJob cleaningJob = new CleaningJob("1",
							request.getParameter("description"),
							request.getParameter("work_queue_url"),
							request.getParameter("work_queue_name"),
							request.getParameter("result_queue_url"),
							request.getParameter("result_queue_name"),
							request.getParameter("api_server_url"),
							request.getParameter("result_server_url"),
							request.getParameter("flat_filename"),
							request.getParameter("bias_filename"),
							request.getParameter("config_filename"),
							request.getParameter("fits_num_start"),
							request.getParameter("fits_num_end"),
							request.getParameter("planes_per_fits"));
					
					// get listing of FITS files from AWS
					AWS_S3 aws = new AWS_S3();
										
					cleaningJob.setFITS_Filenames(aws.listBucket(cleaningJob.getFits_num_start(), cleaningJob.getFits_num_end()));
					
					MessageQueueManager mqm = new MessageQueueManager();
					mqm.postCleaningJob(cleaningJob);
					
					// put listing into the session
					request.getSession().setAttribute("cleaningjob", cleaningJob);
					
					// Show Confirm page
					forwardToPage(request, response, "/ConfirmJob.jsp");
					return;
				}

	
				forwardToPage(request, response, "/CreateJob.jsp");

			}

		} catch (Exception ex) {
			System.out.println("--> Exception in processRequest(): "
					+ ex.getMessage());
		}

	}

	private void forwardToPage(HttpServletRequest request,
			HttpServletResponse response, String page) {

		// Get the request dispatcher object and forward the request to the
		// appropriate JSP page...
		RequestDispatcher dispatcher = getServletContext()
				.getRequestDispatcher(page);

		try {
			dispatcher.forward(request, response);
		} catch (Exception e) {
			System.out.println("Error in forwardToPage: " + e.getMessage());
			e.printStackTrace();
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
