// based on http://docs.aws.amazon.com/AmazonS3/latest/dev/UploadObjSingleOpJava.html

package ie.dit.d13122842;

import java.io.File;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;

public class AWS_S3_Uploader extends Thread {
	private String uploadFileName;
	private String s3key; // filename in AWS S3
	private boolean deleteAfterUpload;
	private String followingJob;
	private int planeCount;
	
	public AWS_S3_Uploader(String uploadFileName, String s3key, boolean deleteAfterUpload, String followingJob, int planeCount) {
		this.uploadFileName = uploadFileName;
		this.s3key = s3key; // the path and name within the bucket
		this.deleteAfterUpload = deleteAfterUpload;
		this.followingJob = followingJob;
		this.planeCount = planeCount;
	}
	
	@Override
	public void run() {
	
		//  Access Key ID and Secret Access Key for 'fitsuser' user, created by Carl
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(
				Config.AWS_Cleaned.ACCESS_KEY_ID, 
				Config.AWS_Cleaned.SECRET_ACCESS_KEY);
		
	    AmazonS3 s3client = new AmazonS3Client(awsCreds);
	    try {
	        System.out.println("Uploading a new object to S3 from a file\n");
	        File file = new File(uploadFileName);
	        s3client.putObject(new PutObjectRequest(
	        		Config.AWS_Cleaned.BUCKET, s3key, file));
	        System.out.println("AWS: "+uploadFileName+" was uploaded as "+s3key);
	        
	        if (deleteAfterUpload) {
	        	file.delete();
	        	System.out.println(uploadFileName + " was deleted.");
	        }
	
	     } catch (AmazonServiceException ase) {
	    	 String err = "Error. AmazonServiceException caught, which " +
	        		"means the request made it " +
	                "to Amazon S3, but was rejected\n";
	        err+="Error Message:    " + ase.getMessage();
	        err+="HTTP Status Code: " + ase.getStatusCode();
	        err+="AWS Error Code:   " + ase.getErrorCode();
	        err+="Error Type:       " + ase.getErrorType();
	        err+="Request ID:       " + ase.getRequestId();
	        System.out.println(err);
	        
	    } catch (AmazonClientException ace) {
	    	String err = "Error. AmazonClientException caught while trying to " +
	                "communicate with S3. " +  ace.getMessage();
	    	System.out.println(err);
	    	return;
	    }
	    
	    if (followingJob.compareTo("0")!=0) {
		    if (followingJob.compareTo("2")==0) {
		    	try {
		    		// A Magnitude Job should be created
					// Post the uploaded info into a queue to be processed as a new job
					MessageQueueManager mqm = new MessageQueueManager();
					String Desc = "Magnitude";
					// Config.AWS_Cleaned.ENDPOINT+Config.AWS_Cleaned.BUCKET+"/"+s3key;
					Thread.sleep(5000);
					mqm.postMagnitudeJob(followingJob, Desc, s3key, planeCount);  
		    	} catch (Exception e) {
			    	String err = "Error. " +  e.getMessage();
			    	System.out.println(err);
			    	return;
		    	}
		    } else {
		    	System.out.println("Follow-on job "+followingJob+" is not supported.");
		    }
	    }
	}

}
