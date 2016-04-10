package ie.dit.d13122842;

// From Java Network Programming by Elliotte Rusty Harold
import java.io.*;
import java.net.*;

public class BinarySaver {

	public String saveBinaryFile(String remotePathFilename, String localPath)
			throws Exception {

		try {

			URL u = new URL(remotePathFilename);

			// get file metadata
			URLConnection uc = u.openConnection();
			String contentType = uc.getContentType();
			int contentLength = uc.getContentLength();
			System.out.println("contentType = " + contentType); // application/octet-stream
			if (contentType.startsWith("text/") || contentLength == -1) {
				throw new IOException("saveBinaryFile(): This is not a binary file.\n"+
						"remotePathFilename: "+remotePathFilename+"\n"+
						"localPath: "+localPath);
			}

			// get the file contents
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

			// check that all was read
			if (offset != contentLength) {
				throw new IOException("Only read " + offset
						+ " bytes; Expected " + contentLength + " bytes");
			}

			// prepare to create a local file with the same name
			String newFilename = u.getFile();
			newFilename = newFilename.substring(newFilename.lastIndexOf('/') + 1);
			newFilename = localPath + "/" + newFilename;
			System.out.println("Saving to " + newFilename);
			File file = new File(newFilename);
			if (file.exists()) {
				file.delete(); //todo: use cached files
			}
			
			// write the downloaded data to the local file 
			FileOutputStream fout = new FileOutputStream(newFilename);
			fout.write(data);
			fout.flush();
			fout.close();

			return newFilename;

		} catch (IOException ioe) {
			throw new Exception("Failed to download file.\n" + ioe.getMessage());
		}

	}

}
