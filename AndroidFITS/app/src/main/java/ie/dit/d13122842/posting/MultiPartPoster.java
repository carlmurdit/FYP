// based on https://books.google.ie/books?id=vhWMAgAAQBAJ&pg=PA250&lpg=PA250&dq=%22multipart/form-data;boundary%22+setRequestProperty&source=bl&ots=0ohv9ptt5B&sig=2Lkphs5zxTGGrSAmSyul5K2d8kE&hl=en&sa=X&ved=0ahUKEwijpsTOkejLAhVE7A4KHfZ-DiQ4ChDoAQg3MAQ#v=onepage&q=%22multipart%2Fform-data%3Bboundary%22&f=false
// Learning Android: Develop Mobile Apps Using Java and Eclipse
// by Marko Gargenta, Masumi Nakamura

package ie.dit.d13122842.posting;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class MultiPartPoster {

    public String upload(String serverURL,
                         String action,
                         String fileName,
                         String fileContents,
                         String starNum,
                         String planeCount) throws Exception {

        String boundary = "*****";
        String crlf = "\r\n";
        String twoHyphens = "--";

        URL url = new URL(serverURL);
        HttpURLConnection urlConnection = null;

        try {

            // Convert the data string to an InputStream
            InputStream fileContentsStream = new ByteArrayInputStream(fileContents.getBytes(Charset.forName("UTF-8")));

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            urlConnection.setRequestProperty("starNum", starNum);
            urlConnection.setRequestProperty("planeCount", planeCount);
            urlConnection.setRequestProperty("action", action);

            DataOutputStream request = new DataOutputStream(urlConnection.getOutputStream());

            request.writeBytes(twoHyphens + boundary + crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\""+fileName +"\""+crlf);

            request.writeBytes(crlf);

            // create a buffer of maximum size
            int bytesAvailable = fileContentsStream.available();
            int maxBufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[bufferSize];

            // read file and write it into form...
            int bytesRead = fileContentsStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                request.write(buffer, 0, bufferSize);
                bytesAvailable = fileContentsStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileContentsStream.read(buffer, 0, bufferSize);
            }

            request.writeBytes(crlf);
            request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
            request.flush();
            request.close();

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            int c;
            StringBuilder sb = new StringBuilder();
            while((c = in.read()) != -1) {
                sb.append((char) c);
            }
            in.close();

            return sb.toString();

        } catch (Exception e) {
            throw new IOException("Error uploading file. "+e.getMessage(),e);
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }

    }
}
