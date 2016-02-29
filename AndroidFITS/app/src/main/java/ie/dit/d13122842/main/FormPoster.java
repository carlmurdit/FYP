package ie.dit.d13122842.main;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class FormPoster {

    private URL url;
    // based on http://www.cafeaulait.org/books/jnp3/examples/15/FormPoster.java
    private QueryString query = new QueryString();

    public FormPoster (String webService) {

        try {
            this.url = new URL(webService);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(
                    webService+" is not a valid URL.");
        }

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

    public String post() throws IOException {

        URLConnection uc;
        try {
            // open the connection and prepare it to POST
            uc = url.openConnection();
            uc.setDoOutput(true);
            OutputStreamWriter out
                    = new OutputStreamWriter(uc.getOutputStream(), "ASCII");

            // The POST request, the Content-type and Content-length headers are sent
            // by the URLConnection, we just send the parameters.
            // e.g. &action=getbox&filename=Final-MasterFlat.fits&box=%5B79%3A64%2C159%3A144%5D
            out.write(query.toString());
            // out.write("\r\n");  //todo should this be here? Got included in last parameter.
            out.flush();
            out.close();

        } catch (IOException e) {
            throw new IOException("Error opening or writing to URL Connection. \n"+
                    url.toString()+"\n " + query.toString()+"\n "+e.getMessage(), e);
        }

        try {
            InputStream in = uc.getInputStream();

            // Read the response
            InputStreamReader r = new InputStreamReader(in);
            int c;
            StringBuilder sb = new StringBuilder();
            while((c = r.read()) != -1) {
                sb.append((char) c);
            }
            in.close();
            // Log.d("fyp", "-> StringBuilder = "+sb.toString());
            return sb.toString();
        }
        catch (IOException e) {
            throw new IOException("Error reading POST response string.\n" +
                    url.toString() + "\n "+query.toString()+"\n"+e.getMessage(), e);
        }

    }

}
