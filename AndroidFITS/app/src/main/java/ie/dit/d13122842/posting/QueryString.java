
// Based on:
// Java Network Programming by Elliote Rusty Harold, Cambridge O'Reilly 1997
// Example 7-9: The QueryString class
// Concatenates that variables to be passed when POSTing a form

package ie.dit.d13122842.posting;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class QueryString {

    private StringBuffer query = new StringBuffer();

    public QueryString() {}

    public QueryString(String name, String value) {
        encode(name, value);
    }

    public synchronized void add(String name, String value) {
        query.append('&');
        encode(name, value);
    }

    private synchronized void encode(String name, String value) {
        try {
            query.append(URLEncoder.encode(name, "UTF-8"));
            query.append('=');
            query.append(URLEncoder.encode(value, "UTF-8"));
        }
        catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Broken VM does not support UTF-8");
        }
    }

    public String getQuery() {
        return query.toString();
    }

    public String toString() {
        return getQuery();
    }

}