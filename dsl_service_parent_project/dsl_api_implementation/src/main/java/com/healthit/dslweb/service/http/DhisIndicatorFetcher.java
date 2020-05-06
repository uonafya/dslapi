package com.healthit.dslweb.service.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import org.apache.http.ssl.SSLContextBuilder;

/**
 *
 * @author duncan
 */
public class DhisIndicatorFetcher {

    public static void main(String args[]) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        String host = "http://127.0.0.1:8090/indicators";

        String url = "http://www.google.com/search?q=httpClient";
        
//        HttpClient client = httpClientBuilder.setDefaultCredentialsProvider(provider).build();
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(host);

        // add request header
        //request.addHeader("User-Agent", USER_AGENT);
        HttpResponse response = client.execute(request);

        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        System.out.println("THe line ----: " + result.toString());

    }

}
