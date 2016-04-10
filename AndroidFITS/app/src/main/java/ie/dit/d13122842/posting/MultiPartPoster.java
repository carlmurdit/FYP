// based on https://books.google.ie/books?id=vhWMAgAAQBAJ&pg=PA250&lpg=PA250&dq=%22multipart/form-data;boundary%22+setRequestProperty&source=bl&ots=0ohv9ptt5B&sig=2Lkphs5zxTGGrSAmSyul5K2d8kE&hl=en&sa=X&ved=0ahUKEwijpsTOkejLAhVE7A4KHfZ-DiQ4ChDoAQg3MAQ#v=onepage&q=%22multipart%2Fform-data%3Bboundary%22&f=false
// Learning Android: Develop Mobile Apps Using Java and Eclipse
// by Marko Gargenta, Masumi Nakamura

package ie.dit.d13122842.posting;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MultiPartPoster {

    public String upload(String serverURL,
                         String action,
                         String fileName,
                         double[][][] cleanPixels,
                         String starNum,
                         int planeCount,
                         String followingJob,
                         int boxWidth) throws Exception {

        final String boundary = "*****"; // any string can be used to identify the boundary
        final String crlf = "\r\n";
        final String twoHyphens = "--";

        URL url = new URL(serverURL);
        HttpURLConnection urlConnection = null;

        try {

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            urlConnection.setRequestProperty("starNum", starNum);
            urlConnection.setRequestProperty("planeCount", String.valueOf(planeCount));
            urlConnection.setRequestProperty("followingJob", followingJob);
            urlConnection.setRequestProperty("action", action);
            urlConnection.setRequestProperty("boxWidth", String.valueOf(boxWidth));

            DataOutputStream request = new DataOutputStream(urlConnection.getOutputStream());

            request.writeBytes(twoHyphens + boundary + crlf);
            request.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\""+fileName +"\""+crlf);

            request.writeBytes(crlf);
            for (int p = 0; p < planeCount; p++) {
                for (int x = 0; x < boxWidth; x++) {
                    for (int y = 0; y < boxWidth; y++) {
                        request.writeDouble(cleanPixels[p][x][y]);
                    }
                }
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

            return sb.toString().trim();

        } catch (Exception e) {
            throw new IOException("Error uploading file. "+e.getMessage(),e);
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }

    }
}
