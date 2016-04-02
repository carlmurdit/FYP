package ie.dit.d13122842;

import java.io.File;

public class Extractor {
	
	public String extractFitsFz(String fileNameFitsFz) throws Exception {
		
		ShellExecute se = new ShellExecute();

		String[] cmdArray = new String[] {
				Config.app.FUNPACK,
				"-v", fileNameFitsFz};

		String r = se.executeCommand(cmdArray);
		System.out.println("Extractor: funpack returned: " + r + "\n");
		
		// extracted file is same except without .fz extension
		String fileNameFits = fileNameFitsFz.substring(0, fileNameFitsFz.lastIndexOf("."));
		
		File fileFits = new File(fileNameFits);
		if (!fileFits.exists()) {
			throw new Exception("Extracted Fits file "+fileNameFits+" was not found on the server.");
		}		
		if (fileFits.length()==0) {
			throw new Exception("Extracted Fits file "+fileNameFits+" is empty.");
		}
		
		// return filename - 
		return fileNameFits;
	}

}
