package ie.dit.d13122842;

import java.util.ArrayList;

public class CleaningJob {


	    private String actID;
	    private String Desc;
	    private String Work_Q_URL;
	    private String Work_Q_Name;
	    private String Result_Q_URL;
	    private String Result_Q_Name;
	    private String API_Server_URL;
	    private String Result_Server_URL;
	    private String Flat_Filename;
	    private String Bias_Filename;
	    private String Config_Filename;
	    private int fits_num_start;
	    private int fits_num_end;
	    private int planes_per_fits;
	    private String following_Job; // 0 (none), 1 (clean) or 2 (magnitude) should follow this
	    
	    private ArrayList<String> FITS_Filenames;

	    public CleaningJob(String actID, String Desc, String Work_Q_URL,
	                          String Work_Q_Name, String Result_Q_URL, String Result_Q_Name,
	                          String API_Server_URL, String Result_Server_URL, String Flat_Filename, 
	                          String Bias_Filename, String Config_Filename, String fits_num_start, 
	                          String fits_num_end, String planes_per_fits, String following_Job) {
	    	
	    	FITS_Filenames = new ArrayList<String>();

	        this.actID = actID;
	        this.Desc = Desc;
	        this.Work_Q_URL = Work_Q_URL;
	        this.Work_Q_Name = Work_Q_Name;
	        this.Result_Q_URL = Result_Q_URL;
	        this.Result_Q_Name = Result_Q_Name;
	        this.API_Server_URL = API_Server_URL;
	        this.Result_Server_URL = Result_Server_URL;
	        this.Flat_Filename = Flat_Filename;
	        this.Bias_Filename = Bias_Filename;
	        this.Config_Filename = Config_Filename;
	        try {
	        	 this.fits_num_start = Integer.parseInt(fits_num_start);
	        } catch (NumberFormatException e) {
	        	throw new NumberFormatException(
	        		"First FITS Number '"+fits_num_start+"' is invalid. "+e.getMessage());
	        }
	        try {
	        	 this.fits_num_end = Integer.parseInt(fits_num_end);
	        } catch (NumberFormatException e) {
	        	throw new NumberFormatException(
	        		"Last FITS Number '"+fits_num_end+"' is invalid. "+e.getMessage());
	        }
	        try {
	        	 this.planes_per_fits = Integer.parseInt(planes_per_fits);
	        } catch (NumberFormatException e) {
	        	throw new NumberFormatException(
	        		"Planes per FITS '"+planes_per_fits+"' is invalid. "+e.getMessage());
	        }
	        this.following_Job = following_Job;
	       
	    }

	    public String getActID() {
	        return actID;
	    }

	    public void setActID(String cID) {
	        actID = cID;
	    }

	    public String getDesc() {
	        return Desc;
	    }

	    public void setDesc(String desc) {
	        Desc = desc;
	    }

	    public String getWork_Q_URL() {
	        return Work_Q_URL;
	    }

	    public void setWork_Q_URL(String work_Q_URL) {
	        Work_Q_URL = work_Q_URL;
	    }

	    public String getWork_Q_Name() {
	        return Work_Q_Name;
	    }

	    public void setWork_Q_Name(String work_Q_Name) {
	        Work_Q_Name = work_Q_Name;
	    }

	    public String getResult_Q_URL() {
	        return Result_Q_URL;
	    }

	    public void setResult_Q_URL(String result_Q_URL) {
	        Result_Q_URL = result_Q_URL;
	    }

	    public String getResult_Q_Name() {
	        return Result_Q_Name;
	    }

	    public void setResult_Q_Name(String result_Q_Name) {
	        Result_Q_Name = result_Q_Name;
	    }

	    public String getAPI_Server_URL() {
	        return API_Server_URL;
	    }

	    public void setAPI_Server_URL(String aPI_Server_URL) {
	        API_Server_URL = aPI_Server_URL;
	    }

	    public String getResult_Server_URL() {
			return Result_Server_URL;
		}

		public void setResult_Server_URL(String result_Server_URL) {
			Result_Server_URL = result_Server_URL;
		}

		public String getFlat_Filename() {
	        return Flat_Filename;
	    }

	    public void setFlat_Filename(String flat_Filename) {
	        Flat_Filename = flat_Filename;
	    }

	    public String getBias_Filename() {
	        return Bias_Filename;
	    }

	    public void setBias_Filename(String bias_Filename) {
	        Bias_Filename = bias_Filename;
	    }

	    public String getConfig_Filename() {
	        return Config_Filename;
	    }

	    public void setConfig_Filename(String config_Filename) {
	        Config_Filename = config_Filename;
	    }

		public int getFits_num_start() {
			return fits_num_start;
		}

		public void setFits_num_start(int fits_num_start) {
			this.fits_num_start = fits_num_start;
		}

		public int getFits_num_end() {
			return fits_num_end;
		}

		public void setFits_num_end(int fits_num_end) {
			this.fits_num_end = fits_num_end;
		}

		public int getPlanes_per_fits() {
			return planes_per_fits;
		}

		public void setPlanes_per_fits(int planes_per_fits) {
			this.planes_per_fits = planes_per_fits;
		}
		
		public ArrayList<String> getFITS_Filenames() {
			return FITS_Filenames;
		}
		
		public void setFITS_Filenames(ArrayList<String> fITS_Filenames) {
			FITS_Filenames = fITS_Filenames;
		}
		
	    public String getFollowingJob() {
			return following_Job;
		}

		public void setFollowingJob(String following_Job) {
			this.following_Job = following_Job;
		}

		public String toString() {
	        return String.format("CID:%s\nDesc:%s\nWork_Q_URL:%s\n"
	                        + "Work_Q_Name:%s\nResult_Q_URL:%s\nResult_Q_Name:%s\n"
	                        + "API_Server_URL:%s\nResult_Server_URL:%s\nFlat_Filename:%s\nBias_Filename:%s\n"
	                        + "Config_Filename:%s\nfits_num_start=%d\nfits_num_end=%d\n"
	                        + "planes_per_fits=%d\nfollowing_Job=%s\nFITS_Filenames.size=%d",
	                actID, Desc, Work_Q_URL, Work_Q_Name, Result_Q_URL, Result_Q_Name,
	                API_Server_URL, Result_Server_URL, Flat_Filename, Bias_Filename,
	                Config_Filename, fits_num_start, fits_num_end, 
	                planes_per_fits, following_Job, FITS_Filenames.size());
	    }

	}
