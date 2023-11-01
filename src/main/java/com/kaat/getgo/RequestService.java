package com.kaat.getgo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class RequestService {
    private HttpClient httpClient;

    public RequestService() {
        // Initialize the HttpClient
        httpClient = HttpClients.createDefault();
    }

    public String sendGetRequest(String url, String headers) {
        try {
            HttpGet request = new HttpGet(url);
            addHeaders(request, headers);
            HttpResponse response = httpClient.execute(request);
            return processResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    public String sendPostRequest(String url, String headers, String requestBody) {
        try {
            HttpPost request = new HttpPost(url);
            addHeaders(request, headers);
            request.setEntity(new StringEntity(requestBody));
            HttpResponse response = httpClient.execute(request);
            return processResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    public String sendPutRequest(String url, String headers, String requestBody) {
        try {
            HttpPut request = new HttpPut(url);
            addHeaders(request, headers);
            request.setEntity(new StringEntity(requestBody));
            HttpResponse response = httpClient.execute(request);
            return processResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    public String sendDeleteRequest(String url, String headers) {
        try {
            HttpDelete request = new HttpDelete(url);
            addHeaders(request, headers);
            HttpResponse response = httpClient.execute(request);
            return processResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    private void addHeaders(HttpRequestBase request, String headers) {
        // Parse and add headers to the request
        // You can customize this method to handle headers as needed.
    }

    private String processResponse(HttpResponse response) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            String responseBody = EntityUtils.toString(entity);
            return "Status Code: " + statusCode + "\n" + responseBody;
        } else {
            return "Status Code: " + statusCode;
        }
    }
}
