package ie.dit.d13122842;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FITSCreator {
	
	public String saveResult(String fitsFilename, String starNum, String imgs) throws IOException {
		
		// Suffix the filename with the star number e.g. 0000999.fits > 0000999_1.fits
		String pathFilename = Config.RESULTSDIR;
		pathFilename += fitsFilename.substring(0,  fitsFilename.lastIndexOf('.'));
		pathFilename += "_"+starNum;
		pathFilename += fitsFilename.substring(fitsFilename.lastIndexOf('.'));
		
		BufferedOutputStream bos = null;
		
		try {	
			bos = new BufferedOutputStream(new FileOutputStream(pathFilename));
			bos.write(imgs.getBytes());
			bos.flush();
			bos.close();	
			System.out.println("Images written to "+pathFilename);
		} catch (IOException e) {
			throw new IOException("Error in saveResult: "+e.getMessage());
		}
		
		return pathFilename; 
				
	}

}
