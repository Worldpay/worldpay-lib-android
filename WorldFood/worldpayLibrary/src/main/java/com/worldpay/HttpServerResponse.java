package com.worldpay;

import org.apache.http.HttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HttpServerResponse {

    private int statusCode;
    private String response;

    protected HttpServerResponse(HttpResponse response) throws IllegalStateException, IOException {
        BufferedReader in = null;
        try {
            statusCode = response.getStatusLine().getStatusCode();
            in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null)
                sb.append(line + NL);
            in.close();

            this.response = sb.toString();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponse() {
        return response;
    }

}
