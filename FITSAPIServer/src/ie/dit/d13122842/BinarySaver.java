package ie.dit.d13122842;

// From Java Network Programming by Elliotte Rusty Harold
import java.io.*;
import java.net.*;

public class BinarySaver {

	public String saveBinaryFile(String fits_fz, String fitsDir)
			throws Exception {

		try {

			URL u = new URL(fits_fz);

			URLConnection uc = u.openConnection();
			String contentType = uc.getContentType();
			int contentLength = uc.getContentLength();
			System.out.println("contentType = " + contentType); // application/octet-stream
			if (contentType.startsWith("text/") || contentLength == -1) {
				throw new IOException("This is not a binary file.");
			}

			InputStream raw = uc.getInputStream();
			InputStream in = new BufferedInputStream(raw);
			byte[] data = new byte[contentLength];
			int bytesRead = 0;
			int offset = 0;
			while (offset < contentLength) {
				bytesRead = in.read(data, offset, data.length - offset);
				if (bytesRead == -1)
					break;
				offset += bytesRead;
			}
			in.close();

			if (offset != contentLength) {
				throw new IOException("Only read " + offset
						+ " bytes; Expected " + contentLength + " bytes");
			}

			String fileName = u.getFile();
			fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
			fileName = fitsDir + "/" + fileName;
			System.out.println("Saving to " + fileName);
			File file = new File(fileName);
			if (file.exists()) {
				file.delete(); //todo: use cached files
			}
			FileOutputStream fout = new FileOutputStream(fileName);
			fout.write(data);
			fout.flush();
			fout.close();

			return fileName;

		} catch (IOException ioe) {
			throw new Exception("Failed to download file.\n" + ioe.getMessage());
		}

	}

}
