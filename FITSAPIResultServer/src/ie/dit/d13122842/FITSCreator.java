package ie.dit.d13122842;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class FITSCreator {
	
	public void saveResult(String fitsFilename, String starNum, ArrayList<String> imgs) throws IOException {
		
		String filename = Config.RESULTSDIR + fitsFilename + "_" + starNum;
		BufferedOutputStream bos = null;
		
		try {
			for (String img : imgs) {
				bos = new BufferedOutputStream(new FileOutputStream(filename));
				bos.write("\n----------------\n".getBytes());
				bos.write(img.getBytes());
				bos.flush();
				bos.close();		
			}			
		} catch (IOException e) {
			throw new IOException("Error in saveResult: "+e.getMessage());
		}
			
	}
	
	public void saveResult(String fitsFilename, String starNum, String imgs) throws IOException {
		
		String filename = Config.RESULTSDIR + fitsFilename + "_" + starNum;
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
