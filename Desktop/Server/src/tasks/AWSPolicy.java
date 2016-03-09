// Program to encode and sign a Policy document (policy.txt). 
// Based on https://aws.amazon.com/articles/1434
// The output is then used in FITSAPIServer/WebContent/aws_upload.html

package tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import sun.misc.BASE64Encoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class AWSPolicy {

	public static void main(String[] args) {

		String secretKey = "fQ0IlC0Pc4VfRS9SqpaY1KN/A3QQ5PFWmntE3OR9";

		String policy_document = read("policy.txt");
		System.out.println(policy_document);

		try {
			Mac hmac = Mac.getInstance("HmacSHA1");
			hmac.init(new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA1"));

			String policy = (new BASE64Encoder())
					.encode(policy_document.getBytes("UTF-8"))
					.replaceAll("\n", "").replaceAll("\r", "");
			
			System.out.println("Policy:\n"+policy);

			String signature = (new BASE64Encoder()).encode(
					hmac.doFinal(policy.getBytes("UTF-8")))
					.replaceAll("\n", "");
			
			System.out.println("Signature:\n"+signature);

		} catch (InvalidKeyException | UnsupportedEncodingException
				| NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}

	private static String read(String fileName) {

		String str = "";
		try {
			File file = new File(fileName);
			FileInputStream fis = new FileInputStream(file);
			byte[] data = new byte[(int) file.length()];
			fis.read(data);
			fis.close();
			str = new String(data, "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return str;

	}
}
