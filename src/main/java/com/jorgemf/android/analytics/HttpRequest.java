package com.jorgemf.android.analytics;

import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HttpRequest {

    protected static boolean postData(String appKey, String json) {
        boolean ok = false;
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(Settings.HTTP_URL);

        try {
            List<NameValuePair> nameValuePairs = new ArrayList<>(2);
            nameValuePairs.add(new BasicNameValuePair(Settings.Json.APP_KEY, appKey));
            nameValuePairs.add(new BasicNameValuePair(Settings.Json.DATA, json));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse httpResponse = httpclient.execute(httppost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK) {
                Log.e(Settings.LOG_TAG, "Failed to send data. Response code: " + statusCode);
            } else {
                ok = true;
            }
        } catch (IOException e) {
            Log.e(Settings.LOG_TAG, "Failed to send data: " + e.getMessage());
        }
        return ok;
    }
}
