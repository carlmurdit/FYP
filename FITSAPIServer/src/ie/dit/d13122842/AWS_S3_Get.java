package ie.dit.d13122842;

import ie.dit.d13122842.Enums.FITS_Type;

import java.io.InputStream;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class AWS_S3_Get {

	public InputStream getFile (
			FITS_Type fitsType, String filename)
			throws Exception {
		
		String bucket;
		String bucketPrefix;
		String accessKeyId; 
		String secretAccessKey;
		Regions regions;
		
		if (fitsType == FITS_Type.RAW) {
			bucket = Config.aws.raw.BUCKET;
			bucketPrefix = Config.aws.raw.BUCKET_PREFIX;
			accessKeyId = Config.aws.raw.ACCESS_KEY_ID;
			secretAccessKey = Config.aws.raw.SECRET_ACCESS_KEY;
			regions = Config.aws.raw.REGIONS;
		} else {
			bucket = Config.aws.clean.BUCKET;
			bucketPrefix = Config.aws.clean.BUCKET_PREFIX;
			accessKeyId = Config.aws.clean.ACCESS_KEY_ID;
			secretAccessKey = Config.aws.clean.SECRET_ACCESS_KEY;
			regions = Config.aws.clean.REGIONS;
		}

		try {

			BasicAWSCredentials awsCreds = new BasicAWSCredentials(
					accessKeyId, secretAccessKey);

			AmazonS3 s3 = new AmazonS3Client(awsCreds);
			s3.setRegion(Region.getRegion(regions));

			System.out.println("Getting file from bucket ...");
			
			String s3key = bucketPrefix+filename; // e.g. cleaned/0000001_2.fits
			System.out.println("AWS_S3_Get.getFile() s3key: "+s3key);
			
            S3Object object = s3.getObject(new GetObjectRequest(bucket, s3key));
            System.out.println("Content-Type: "  + object.getObjectMetadata().getContentType());
            return object.getObjectContent();

		} catch (AmazonServiceException ase) {
			String err = "AWS_S3.getFile() AmazonServiceException: \n";
			err += "A request was rejected by S3: \n";
			err += "Bucket:           " + bucket + " \n";
			err += "Error Message:    " + ase.getMessage() + " \n";
			err += "HTTP Status Code: " + ase.getStatusCode() + " \n";
			err += "AWS Error Code:   " + ase.getErrorCode() + " \n";
			err += "Error Type:       " + ase.getErrorType() + " \n";
			err += "Request ID:       " + ase.getRequestId() + " \n";
			throw new Exception(err);
		} catch (AmazonClientException ace) {
			String err = "AWS_S3.listBucket() AmazonClientException: \n";
			err += "Bucket: " + bucket + " \n";
			err += "A request did not reach S3: \n" + ace.getMessage();
			throw new Exception(err);
		}
	}

}
