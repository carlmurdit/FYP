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
	
	public AWS_S3_Uploader(String uploadFileName, String s3key, boolean deleteAfterUpload) {
		this.uploadFileName = uploadFileName;
		this.s3key = s3key; // the path and name within the bucket
		this.deleteAfterUpload = deleteAfterUpload;
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
	    }
	}

}