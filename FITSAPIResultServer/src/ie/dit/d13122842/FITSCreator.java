package ie.dit.d13122842;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FITSCreator {
	
	public void saveResult(String fitsFilename, String starNum, String imgs) throws IOException {
		
		// Suffix the filename with the star number e.g. 0000999.fits > 0000999_1.fits
		String filename = Config.RESULTSDIR;
		filename += fitsFilename.substring(0,  fitsFilename.lastIndexOf('.'));
		filename += "_"+starNum;
		filename += fitsFilename.substring(fitsFilename.lastIndexOf('.'));
		
		BufferedOutputStream bos = null;
		
		try {	
			bos = new BufferedOutputStream(new FileOutputStream(filename));
			bos.write(imgs.getBytes());
			bos.flush();
			bos.close();	
			System.out.println("Images written to "+filename);
		} catch (IOException e) {
			throw new IOException("Error in saveResult: "+e.getMessage());
		}
			
	}

}
