package fr.lepetitpingouin.android.gks;

/**
 * Created by gregory on 23/11/2013.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.StrictMode;
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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class SuperGKSHttpBrowserNoApache {

    CookieStore cookieStore;
    SharedPreferences prefs;
    SharedPreferences.Editor edit;
    String encoding = "UTF-8";
    String method = "POST";
    String username, password;
    Integer responseCode = 0;
    String query = "";
    List<String> cookies;

    String _cookies;

    URL url;
    URLConnection connection = null;

    List<NameValuePair> data = new ArrayList<NameValuePair>(9);

    public SuperGKSHttpBrowserNoApache(Context context) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Log.d("SuperT411HttpBrowser", "constructor");
        cookieStore = new BasicCookieStore();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        edit = prefs.edit();
    }

    public SuperGKSHttpBrowserNoApache connect(String mUrl) throws MalformedURLException {
        Log.d("SuperT411HttpBrowser", "connect");
        this.url = new URL(mUrl);
        return this;
    }

    public SuperGKSHttpBrowserNoApache login(String username, String password) {
        Log.d("SuperT411HttpBrowser", "login");
        this.username = username;
        this.password = password;
        return this;
    }

    public SuperGKSHttpBrowserNoApache setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public String execute() {
        Log.d("SuperGKSHttpBrowser", "execute");
        String value = "";
        try {
            value = new LoginTask(username, password).execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return value;
    }

    public SuperGKSHttpBrowserNoApache addData(String key, String value) {
        this.data.add(new BasicNameValuePair(key, value));
        this.query += ("".length() > 0) ? "&" : "?";
        this.query += key + "=" + value;

        Log.d("http POST query", this.query);
        return this;
    }

    public String executeLoginForMessage() {
        Log.d("SuperT411HttpBrowser", "loginForMessage");
        HttpContext clientcontext;
        clientcontext = new BasicHttpContext();
        clientcontext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        AndroidHttpClient httpclient = AndroidHttpClient.newInstance(prefs.getString("User-Agent", Default.USER_AGENT));

        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

        HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), Integer.valueOf(prefs.getString("timeout", Default.timeout)) * 1000);
        HttpConnectionParams.setSoTimeout(httpclient.getParams(), Integer.valueOf(prefs.getString("timeout", Default.timeout)) * 1000);

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
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (Exception ex) {
            //TODO Handle problems..
        }
        httpclient.close();
        if (responseString == null)
            responseString = "OK";
        return responseString;
    }

    class LoginTask extends AsyncTask<String, String, String> {

        String username, password, url;

        public LoginTask() {
        }

        public LoginTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected String doInBackground(String... uri) {
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

    public String executeLogin() {
        try {
            Log.e("executeLogin", "executeLogin");
            connection = new URL("https://gks.gs/login").openConnection();
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);
            //connection.setDoInput(true);

            connection.setConnectTimeout(2000);
            connection.setRequestProperty("User-Agent", "runscope/0.1");
            connection.setRequestProperty("Referer", "https://gks.gs/login");
            connection.setRequestProperty("Origin", "https://gks.gs");

            String urlParameters = "username=" + URLEncoder.encode(username, encoding) + "&password=" + URLEncoder.encode(password, encoding) + "&login=+Connexion+";
            Log.e("urlParameters", String.valueOf(urlParameters.getBytes().length));

            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            Log.e("outputStream", "ok");
            Log.e("url", connection.getURL().toString());


            /*InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();*/

            //Log.e("response", response.toString());

            HttpsURLConnection conn = (HttpsURLConnection) connection;
            Log.e("returnCode", conn.getResponseCode() + "");
            Log.e("returnCode", conn.getResponseMessage());


            try {
                cookies = connection.getHeaderFields().get("Set-Cookie");
                Log.e("Cookies size", cookies.size() + "");
            } catch (Exception e) {
                e.printStackTrace();
            }

            String mCookies = "";
            try {
                for (String cookie : cookies) {
                    mCookies += cookie.split(";", 1)[0] + ";";
                }
                saveCookies(mCookies);
                Log.d("mCookies", mCookies);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return null;
    }

    private void saveCookies(String cookies) {
        edit.putString("cookies_value", cookies);
        edit.putLong("cookies_time", new Date().getTime());
        edit.commit();
    }

    private void loadCookies() {
        String cookies = prefs.getString("cookies_value", "");
        Long cookies_time = prefs.getLong("cookies_time", 0);

        // Si cookie vide ou plus vieux que 24h, on le recharge.
        if (cookies == "" || cookies_time < (new Date().getTime() - 86400000) /*24h*/) {
            executeLogin();
            //loadCookies();
        } else {
            this._cookies = cookies;
        }
    }

    public String executeInAsyncTask() {
        Log.d("SuperGKSHttpBrowser", "executeAsync");

        executeLogin();

        String value = "erreur";

        try {
            connection = url.openConnection();
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            connection.addRequestProperty("Cookie", _cookies);
            String urlParameters = query;

            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();

            //save cookies : //cookies = connection.getHeaderFields().get("Set-Cookie");

            value = response.toString();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return value;
    }
}
