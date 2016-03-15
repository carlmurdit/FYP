package ie.dit.d13122842;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class AWS_S3 {

	public ArrayList<String> listBucket(int minNum, int maxNum)
			throws Exception {

		try {

			System.out.println("listBucket()...");

			ArrayList<String> fileNames = new ArrayList<String>();

			// 'carl' user, created by Paul
			//  Access Key ID and Secret Access Key
			BasicAWSCredentials awsCreds = new BasicAWSCredentials(
					Config.AWS.ACCESS_KEY_ID, 
					Config.AWS.SECRET_ACCESS_KEY);

			AmazonS3 s3 = new AmazonS3Client(awsCreds);
			Region usWest2 = Region.getRegion(Regions.EU_WEST_1);
			s3.setRegion(usWest2);

			/*
			 * List objects in your bucket by prefix - There are many options
			 * for listing the objects in your bucket. Keep in mind that buckets
			 * with many objects might truncate their results when listing their
			 * objects, so be sure to check if the returned object listing is
			 * truncated, and use the AmazonS3.listNextBatchOfObjects(...)
			 * operation to retrieve additional results.
			 */
			System.out.println("Listing objects");

			ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
					.withBucketName(Config.AWS.SOURCE_BUCKET).withPrefix(Config.AWS.SOURCE_BUCKET_PREFIX);
			ObjectListing objectListing;
			
			// Define capturing groups for filename (1) and file number (2)
			// from "AstronomyData/compressedRAW/0003676.fits.fz"
			Pattern ptn = Pattern.compile("/((\\d+).fits.fz)");
			Matcher mat;
			
			do {
				objectListing = s3.listObjects(listObjectsRequest);
				for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
					
					// extract filename and file number
					mat = ptn.matcher(objectSummary.getKey());
					
					if (mat.find()) {
						if (Integer.parseInt(mat.group(2))>=minNum 
								&& Integer.parseInt(mat.group(2))<=maxNum) {
							// System.out.println("filename: "+mat.group(1)+"\t"+mat.group(2));
							fileNames.add(mat.group(1));
						}				
					}
				
				}
				listObjectsRequest.setMarker(objectListing.getNextMarker());
			} while (objectListing.isTruncated());

			System.out.println();
			System.out.println("All done.");
			return fileNames;

		} catch (AmazonServiceException ase) {
			String err = "AWS_S3.listBucket() AmazonServiceException: \n";
			err += "A request was rejected by S3: \n";
			err += "Error Message:    " + ase.getMessage() + " \n";
			err += "HTTP Status Code: " + ase.getStatusCode() + " \n";
			err += "AWS Error Code:   " + ase.getErrorCode() + " \n";
			err += "Error Type:       " + ase.getErrorType() + " \n";
			err += "Request ID:       " + ase.getRequestId() + " \n";
			throw new Exception(err);
		} catch (AmazonClientException ace) {
			String err = "AWS_S3.listBucket() AmazonClientException: \n";
			err += "A request did not reach S3: \n" + ace.getMessage();
			throw new Exception(err);
		}
	}

}
