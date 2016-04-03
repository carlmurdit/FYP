package ie.dit.d13122842;

import org.json.simple.JSONObject;

public class ActivationMessage {

	private static class Fields {
		public static final String ACT_ID = "Act ID";
		public static final String DESC = "Desc";
		public static final String WORK_Q_URL = "Work Q URL";
		public static final String WORK_Q_NAME = "Work Q Name";
		public static final String RESULT_Q_URL = "Result Q URL";
		public static final String RESULT_Q_NAME = "Result Q Name";
		public static final String API_SERVER_URL = "API Server URL";
		public static final String RESULT_SERVER_URL = "Result Server URL";
		public static final String FLAT_FILENAME = "Flat Filename";
		public static final String BIAS_FILENAME = "Bias Filename";
		public static final String CONFIG_FILENAME = "Config Filename";
		public static final String FOLLOW_ON_JOB = "Follow On Job";
	}

	private String actId;
	private String desc;
	private String workQUrl;
	private String workQName;
	private String resultQUrl;
	private String resultQName;
	private String apiServerUrl;
	private String resultServerUrl;
	private String flatFilename;
	private String biasFilename;
	private String configFilename;
	private String followOnJob;
	
	
	public ActivationMessage(
		String actId, 
		String desc, 
		String workQUrl, 
		String workQName, 
		String resultQUrl, 
		String resultQName, 
		String apiServerUrl, 
		String resultServerUrl, 
		String flatFilename, 
		String biasFilename, 
		String configFilename, 
		String followOnJob) throws Exception {
		
		this.actId = actId;
		this.desc = desc;
		this.workQUrl = workQUrl;
		this.workQName = workQName;
		this.resultQUrl = resultQUrl;
		this.resultQName = resultQName;
		this.apiServerUrl = apiServerUrl;
		this.resultServerUrl = resultServerUrl;
		this.flatFilename = flatFilename;
		this.biasFilename = biasFilename;
		this.configFilename = configFilename;
		this.followOnJob = followOnJob;
		
		if (this.actId == null ||
				this.desc == null ||
				this.workQUrl == null ||
				this.workQName == null ||
				this.resultQUrl == null ||
				this.resultQName == null ||
				this.apiServerUrl == null ||
				this.resultServerUrl == null ||
				this.flatFilename == null ||
				this.biasFilename == null ||
				this.configFilename == null ||
				this.followOnJob == null) {
			throw new Exception("The Activation Message's constructor "
					+ "has been called with a null parameter.");
		}
			
	}

	@SuppressWarnings("unchecked")
	public String toJSON() {
		JSONObject obj = new JSONObject();	
		obj.put(Fields.ACT_ID, actId);
		obj.put(Fields.DESC, desc);
		obj.put(Fields.WORK_Q_URL, workQUrl);
		obj.put(Fields.WORK_Q_NAME, workQName);
		obj.put(Fields.RESULT_Q_URL, resultQUrl);
		obj.put(Fields.RESULT_Q_NAME, resultQName);
		obj.put(Fields.API_SERVER_URL, apiServerUrl);
		obj.put(Fields.RESULT_SERVER_URL, resultServerUrl);
		obj.put(Fields.FLAT_FILENAME, flatFilename);
		obj.put(Fields.BIAS_FILENAME, biasFilename);
		obj.put(Fields.CONFIG_FILENAME, configFilename);
		obj.put(Fields.FOLLOW_ON_JOB, followOnJob);
		System.out.print(obj);
		return obj.toString();
	}

	public String getActId() {
		return actId;
	}

	public String getDesc() {
		return desc;
	}

	public String getWorkQUrl() {
		return workQUrl;
	}

	public String getWorkQName() {
		return workQName;
	}

	public String getResultQUrl() {
		return resultQUrl;
	}

	public String getResultQName() {
		return resultQName;
	}

	public String getApiServerUrl() {
		return apiServerUrl;
	}

	public String getResultServerUrl() {
		return resultServerUrl;
	}

	public String getFlatFilename() {
		return flatFilename;
	}

	public String getBiasFilename() {
		return biasFilename;
	}

	public String getConfigFilename() {
		return configFilename;
	}

	public String getFollowOnJob() {
		return followOnJob;
	}

}
