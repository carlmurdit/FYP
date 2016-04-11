package ie.dit.d13122842;

import ie.dit.d13122842.Enums.FITS_Type;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

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
	private static final String GET_RESULTS = "get_results";

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
					request.getSession().setAttribute("mq_host", Config.MQ.HOST);
					if (job.equals("job_clean")) {
						request.getSession().setAttribute("description", "FITS Cleaning");
						request.getSession().setAttribute("work_queue", Config.MQ.CLEANING_WORK_QUEUE);
						request.getSession().setAttribute("result_queue", Config.MQ.CLEANING_RESULT_QUEUE);
						request.getSession().setAttribute("source_bucket", Config.aws.raw.BUCKET);
						request.getSession().setAttribute("source_bucket_prefix", Config.aws.raw.BUCKET_PREFIX);
					} else if (job.equals("job_magnitude")) {
						request.getSession().setAttribute("description", "Magnitude");
						request.getSession().setAttribute("work_queue", Config.MQ.MAGNITUDE_WORK_QUEUE);
						request.getSession().setAttribute("result_queue", Config.MQ.MAGNITUDE_RESULT_QUEUE);
						request.getSession().setAttribute("source_bucket", Config.aws.clean.BUCKET);
						request.getSession().setAttribute("source_bucket_prefix", Config.aws.clean.BUCKET_PREFIX);
					}
					request.getSession().setAttribute("api_host", Config.API.HOST);
					request.getSession().setAttribute("result_host", Config.Result.HOST);
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
				} else if (job.equalsIgnoreCase("job_clean") || job.equalsIgnoreCase("job_magnitude")) {
					
					FITS_Type fitsType;
					String actID;
					if (job.equalsIgnoreCase("job_clean")) {
						fitsType = FITS_Type.RAW;
						actID = Enums.Activities.CLEANING;
					} else {
						fitsType = FITS_Type.CLEAN;
						actID = Enums.Activities.MAGNITUDE;
					}
									
					Activity activity = new Activity(actID,
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
							request.getParameter("planes_per_fits"),
							request.getParameter("following_job"));
					
					// get listing of FITS files from AWS
					AWS_S3_List aws = new AWS_S3_List();
					
					try {
						
						request.getSession().removeAttribute("cleaningjob");
						request.getSession().removeAttribute("error");
						
						// get the FITS file names to be retrieved from S3
						ArrayList<String> filenames = aws.listBucket(
								fitsType,
								activity.getFits_num_start(), 
								activity.getFits_num_end());
						if (filenames.size() ==0) {
							System.out.println("No files were found to populate Work Unit Messages");
							return;
						}
						
						// save them with the other message components
						activity.setFITS_Filenames(filenames);
						
						MessageQueueManager mqm = new MessageQueueManager();
						mqm.postJob(activity);
						
						// put the list of selected FITS files into the session
						request.getSession().setAttribute("cleaningjob", activity);
						
					} catch (Exception e) {
						request.getSession().setAttribute("error", e.getMessage());
					}
					
					// Show Confirm page
					forwardToPage(request, response, "/ConfirmJob.jsp");
					return;
				}

				forwardToPage(request, response, "/CreateJob.jsp");

			} else if (action.equalsIgnoreCase(GET_RESULTS)) {
				
				// User can enter a string to filter the devices included in the results
				String deviceFilter = request.getParameter("deviceFilter");
				if (deviceFilter != null) {
					// apply choice when page refreshed
					request.getSession().setAttribute("deviceFilter", deviceFilter);
				}
				if (deviceFilter.trim().equalsIgnoreCase("")) deviceFilter = null;
				
				String job = request.getParameter("job_current");
				if (job != null) {
					// apply choice when page refreshed
					request.getSession().setAttribute("job_current", job);
				}
			
				// Get messages from Result Queue, filtering by Device ID
				MessageQueueManager mqm = new MessageQueueManager();
				List<ResultMessage> resultMessages = null;
				if (job.equals("job_clean")) {
					resultMessages = mqm.getResultMessages(Config.MQ.CLEANING_RESULT_QUEUE, deviceFilter);
				} else if (job.equals("job_magnitude")) {
					resultMessages = mqm.getResultMessages(Config.MQ.MAGNITUDE_RESULT_QUEUE, deviceFilter);
				}
				
				// Pass the messages to the Results page
				request.setAttribute("resultMessages", resultMessages);
				forwardToPage(request, response, "/ShowResults.jsp");
				
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
