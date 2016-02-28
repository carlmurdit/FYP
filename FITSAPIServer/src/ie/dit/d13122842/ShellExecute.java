package ie.dit.d13122842;

import java.io.*;

public class ShellExecute {

	public String executeCommand(String[] cmdArray) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(cmdArray);
			int rCode = p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}
			if (rCode != 0) {
				output.append(String.format("executeCommand Return Code was %d.\n", rCode));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();

	}
}
