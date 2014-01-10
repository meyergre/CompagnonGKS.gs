package fr.lepetitpingouin.android.gks;

/**
 * Created by gregory on 23/11/2013.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SuperGKSHttpBrowser {CookieStore cookieStore;

    SharedPreferences prefs;

    String encoding = "UTF-8";

    String username, password, url, errorMessage = "", fadeMessage = "";

    Integer responseCode = 0;

    List<NameValuePair> data = new ArrayList<NameValuePair>(9);

    public SuperGKSHttpBrowser(Context context) {
        Log.d("SuperT411HttpBrowser", "constructor");
        cookieStore = new BasicCookieStore();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        //new LoginTask().execute(Default.URL_LOGIN);
    }

    public SuperGKSHttpBrowser connect(String mUrl) {
        Log.d("SuperT411HttpBrowser", "connect");
        this.url = mUrl;
        return this;
    }

    public SuperGKSHttpBrowser login(String username, String password) {
        Log.d("SuperT411HttpBrowser", "login");
        this.username = username;
        this.password = password;
        return this;
    }

    public SuperGKSHttpBrowser setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public String execute() {
        Log.d("SuperT411HttpBrowser", "execute");
        String value = "";
        try {
            value = new LoginTask(username, password).execute(url).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return value;
    }

    public SuperGKSHttpBrowser addData(String key, String value) {
        this.data.add(new BasicNameValuePair(key, value));
        return this;
    }

    public String executeLoginForMessage() {
        Log.d("SuperT411HttpBrowser", "loginForMessage");
        HttpContext clientcontext;
        clientcontext = new BasicHttpContext();
        clientcontext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        AndroidHttpClient httpclient = AndroidHttpClient.newInstance(prefs.getString("User-Agent", Default.USER_AGENT));

        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

        HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), Integer.valueOf(prefs.getString("timeout", Default.timeout))*1000);
        HttpConnectionParams.setSoTimeout(httpclient.getParams(), Integer.valueOf(prefs.getString("timeout", Default.timeout))*1000);

        HttpPost httppost = new HttpPost(Default.URL_LOGIN);

        HttpResponse response;
        String responseString = null;
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
        //nameValuePairs.add(new BasicNameValuePair("remember", "0"));
        nameValuePairs.add(new BasicNameValuePair("username", username));
        nameValuePairs.add(new BasicNameValuePair("password", password));

        HttpEntity e = null;

        try {
            e = new UrlEncodedFormEntity(nameValuePairs);

            httppost.setEntity(e);

            response = httpclient.execute(httppost, clientcontext);
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (Exception ex) {
            //TODO Handle problems..
        }
        httpclient.close();
        if(responseString == null)
            responseString = "OK";
        return responseString;
    }

    class LoginTask extends AsyncTask<String, String, String> {

        String username, password, url;

        public LoginTask() {}

        public LoginTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected String doInBackground(String... uri) {
            this.url = uri[0];
            return executeInAsyncTask();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public String executeInAsyncTask() {
        Log.d("SuperT411HttpBrowser", "executeAsync");
        HttpContext clientcontext;
        clientcontext = new BasicHttpContext();
        clientcontext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        AndroidHttpClient httpclient = AndroidHttpClient.newInstance(prefs.getString("User-Agent", Default.USER_AGENT));

        HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), Integer.valueOf(prefs.getString("timeout", Default.timeout)) * 1000);
        HttpConnectionParams.setSoTimeout(httpclient.getParams(), Integer.valueOf(prefs.getString("timeout", Default.timeout))*1000);


        //HttpGet httppost = new HttpGet(Default.URL_LOGIN);
        HttpPost httppost = new HttpPost(Default.URL_LOGIN);

        Log.e("URL", Default.URL_LOGIN);
        httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");

        HttpResponse response;
        String responseString = null;
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
        nameValuePairs.add(new BasicNameValuePair("connect", ""));
        nameValuePairs.add(new BasicNameValuePair("login", " Connexion "));
        nameValuePairs.add(new BasicNameValuePair("username", username));
        nameValuePairs.add(new BasicNameValuePair("password", password));
        Log.e("username", username);
        Log.e("password", password);

        HttpEntity e = null;

        try {
            e = new UrlEncodedFormEntity(nameValuePairs);

            httppost.setEntity(e);

            response = httpclient.execute(httppost, clientcontext);
            StatusLine statusLine = response.getStatusLine();
            responseCode = statusLine.getStatusCode();
            if(responseCode == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
                //Log.e("HTML", responseString);
                Log.e("HTTP STATUS", "OK");
            } else{
                //Closes the connection.
                Log.e("HTTP STATUS", "...");
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }

        } catch (Exception ex) {
            //TODO Handle problems..
        }
        //return responseString;

        //httppost = new HttpGet(url);
        httppost = new HttpPost(url);
        Log.e("URL", url);

        try {

            e = new UrlEncodedFormEntity(data);

            httppost.setEntity(e);

            response = httpclient.execute(httppost, clientcontext);
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                Log.e("HTTP STATUS", "OK");
                //responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
                responseString = EntityUtils.toString(response.getEntity(), encoding);
            } else{
                Log.e("HTTP STATUS", "...");
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }

            try {
                String conError = Jsoup.parse(responseString).select("div.fade").first().text();
                if (!conError.equals("")) {
                    fadeMessage = conError;
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }

        } catch (Exception ex) {
            //TODO Handle problems..
            ex.printStackTrace();
        }

        httpclient.close();
        String retValue = "";

        try {
            retValue = new String(responseString.getBytes());
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return retValue;
    }

}
