// Java Network Programming by Elliote Rusty Harold, Cambridge O'Reilly 1997
// Example 15-8: Posting a form

package ie.dit.d13122842.main;

import android.app.Activity;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

// Params, the type of the parameters sent to the task upon execution.
// Progress, the type of the progress units published during the background computation.
// Result, the type of the result of the background computation.
public class FormPoster extends AsyncTask<Void, Integer, AsyncTaskResult<byte[]>> {

    private URL url;
    private String description; //e.g. "Config download"
    private QueryString query = new QueryString();
    private Activity parent;

    public FormPoster(String webService, Activity parent, String description) {

        try {
            this.url = new URL(webService);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(
                    webService+" is not a valid URL.");
        }
        this.parent = parent;
        this.description = description;

        if (!url.getProtocol().toLowerCase().startsWith("http")) {
            throw new IllegalArgumentException(
                    "Posting only works for http URLs");
        }
    }

    public void add(String name, String value) {
        query.add(name, value);
    }

    public URL getURL() {
        return this.url;
    }

    @Override
    protected AsyncTaskResult<byte[]> doInBackground(final Void... params) {

        // open the connection and prepare it to POST
        URLConnection uc;
        OutputStreamWriter out;
        try {
            uc = url.openConnection();
            uc.setDoOutput(true);
            out = new OutputStreamWriter(uc.getOutputStream(), "ASCII");
            // The POST line, the Content-type header,
            // and the Content-length headers are sent by the URLConnection.
            // We just need to send the data
            out.write(query.toString());
            // out.write("\r\n"); //todo should this be here? Got appended to the parameter.
            out.flush();
            out.close();
        } catch (IOException ioe) {
            return new AsyncTaskResult<byte[]>(ioe, "Error opening or writing to URL Connection. "+url.toString());
        }

        String contentType = ((uc.getContentType()==null) ? "" : uc.getContentType());
        int contentLength = uc.getContentLength();
        if (contentType.startsWith("text/") || contentLength <= 0) {
            return new AsyncTaskResult<byte[]>(new Exception(), url.getPath() + " did not supply a binary file.");
        }

        byte[] buffer;
        try {
            InputStream raw = uc.getInputStream();
            InputStream in = new BufferedInputStream(raw);
            buffer = new byte[contentLength];
            int bytesRead = 0;
            int offset = 0;
            while (offset < contentLength) {
                bytesRead = in.read(buffer, offset, buffer.length - offset);
                if (bytesRead == -1) break;
                offset += bytesRead;
//                float fProgress = (float) offset * 100 / (float) contentLength;
//                publishProgress((int) fProgress);
            }
            in.close();
            if (offset != contentLength) {
                throw new IOException("Only read " + offset
                        + " bytes; Expected " + contentLength + " bytes");
            }
        } catch (IOException ioe) {
            return new AsyncTaskResult<byte[]>(ioe, this.description+" failed.");
        }

        return new AsyncTaskResult<byte[]>(buffer, this.description+" succeeded.");
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
//        pbar.setProgress(values[0]);
    }

}


