package ie.dit.d13122842;

import ie.dit.d13122842.Enums.FITS_Type;

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
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class AWS_S3_List {

	public ArrayList<String> listBucket(
			FITS_Type fitsType, int minNum, int maxNum)
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

			ArrayList<String> fileNames = new ArrayList<String>();

			//  Access Key ID and Secret Access Key
			BasicAWSCredentials awsCreds = new BasicAWSCredentials(
					accessKeyId, secretAccessKey);

			AmazonS3 s3 = new AmazonS3Client(awsCreds);
			s3.setRegion(Region.getRegion(regions));

			System.out.println("Listing bucket contents...");

			ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
					.withBucketName(bucket).withPrefix(bucketPrefix);
			ObjectListing objectListing;
			
			Pattern ptn;
			// Define capturing groups for filename (1) and file number (2)
			if (fitsType == FITS_Type.RAW) {
				// e.g. "AstronomyData/compressedRAW/0003676.fits.fz"
				ptn = Pattern.compile("/((\\d+).fits.fz)");
			} else {
				// e.g. "cleanedfits/cleaned/0000001_2.fits"
				ptn = Pattern.compile("/((\\d+)_\\d+.fits)");
			}
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
				// isTruncated() tells us when not all objects are returned 
				// and we need to continue listing from the last marker
				listObjectsRequest.setMarker(objectListing.getNextMarker());
			} while (objectListing.isTruncated());

			System.out.println("List complete.");
			return fileNames;

		} catch (AmazonServiceException ase) {
			String err = "AWS_S3.listBucket() AmazonServiceException: \n";
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
