package kanjikyoushi.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class FeedHandler {

    private static Logger logger = Logger.getLogger(FeedHandler.class);

    private Map<String, List<String>> postData;
    private URL url;
    private String userPassword = null;
    private boolean verbose = false;

    public FeedHandler(String _url) throws MalformedURLException {

        this.url = new URL(_url);
        this.postData = new HashMap<String, List<String>>();

    }

    public FeedHandler(String _url, String user, String password)
            throws MalformedURLException {

        this.url = new URL(_url);
        this.postData = new HashMap<String, List<String>>();
        this.userPassword = user + ":" + password;

    }

    public void addData(String key, Collection<String> data) {
        addData(key, data.toArray(new String[] {}));
    }

    public void addData(String key, String... data) {

        if (!postData.containsKey(key)) {
            postData.put(key, new ArrayList<String>());
        }
        postData.get(key).addAll(Arrays.asList(data));

    }

    public void clearData() {

        postData.clear();

    }

    public void doGet() throws IOException {

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (userPassword != null) {
            String encoding = new sun.misc.BASE64Encoder().encode(userPassword
                    .getBytes());
            conn.setRequestProperty("Authorization", "Basic " + encoding);
        }
        conn.setDoOutput(true);

        getResponse(conn);

    }

    public void doPost() throws IOException {

        // Create Post String
        StringBuffer dataBuffer = new StringBuffer();

        String ampersand = "";
        for (String key : postData.keySet()) {
            for (String data : postData.get(key)) {
                dataBuffer.append(ampersand + URLEncoder.encode(key, "UTF-8")
                        + "=" + URLEncoder.encode(data, "UTF-8"));
                ampersand = "&";
            }
        }

        if (verbose) {
            logger.debug("posting data to " + url + ": "
                    + dataBuffer.toString());
        }

        // Send Data To Page
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (userPassword != null) {
            String encoding = new sun.misc.BASE64Encoder().encode(userPassword
                    .getBytes());
            conn.setRequestProperty("Authorization", "Basic " + encoding);
        }
        conn.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(dataBuffer.toString());
        wr.flush();

        getResponse(conn);

    }

    private void getResponse(HttpURLConnection conn) throws IOException {

        int responseCode = conn.getResponseCode();

        if (!verbose) {
            return;
        }

        logger.info("response code: " + responseCode);

        BufferedReader rd;
        if (responseCode == 200) {
            rd = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream()));
        }

        String line;
        while ((line = rd.readLine()) != null) {
            logger.info(line);
        }

        rd.close();

    }

    public boolean isVerbose() {

        return verbose;

    }

    public void setVerbose(boolean verbose) {

        this.verbose = verbose;

    }
}
