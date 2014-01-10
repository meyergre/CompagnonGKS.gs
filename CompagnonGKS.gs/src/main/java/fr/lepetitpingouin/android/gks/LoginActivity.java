package fr.lepetitpingouin.android.gks;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Map;

public class LoginActivity extends Activity {

    EditText username, password;
    SharedPreferences prefs;
    SharedPreferences.Editor edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        edit = prefs.edit();

        username = (EditText)findViewById(R.id.login_username);
        username.setText(prefs.getString("account_username", ""));
        password = (EditText)findViewById(R.id.login_password);
        password.setText(prefs.getString("account_password", ""));
    }

    public void onLogin(View v) {
        Toast.makeText(getApplicationContext(), "Test de connexion ("+username.getText().toString()+")...", Toast.LENGTH_SHORT).show();
        new asyncLoginTester().execute();
    }

    private class asyncLoginTester extends AsyncTask<Integer, Integer[], Integer> {

        Document doc;
        Connection.Response res;

        @Override
        protected Integer doInBackground(Integer... params) {

            try {
                Map<String, String> cookies = Jsoup.connect(Default.URL_LOGIN)
                        .data("username", username.getText().toString(), "password", password.getText().toString())
                        .header("Content-Type","application/x-www-form-urlencoded;charset=UTF-8")
                        .followRedirects(true)
                        .referrer(Default.URL_LOGIN)
                        .userAgent("Mozilla")
                        .method(Connection.Method.POST)
                        .execute().cookies();

                res = Jsoup.connect(Default.URL_PROFILE)
                        .followRedirects(true)
                        .cookies(cookies)
                        .execute();

                doc = res.parse();

                if(doc.title().equals("GKS Connexion"))
                    return 2;
                else
                    return 1;
            } catch (Exception e) {
                return 0;
            }
        }

        @Override
        protected void onPostExecute(Integer value) {
            prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            edit = prefs.edit();
            switch(value) {
                case 0: // erreur connexion
                    Toast.makeText(getApplicationContext(), "Erreur lors de la connexion au serveur.", Toast.LENGTH_SHORT).show();
                    break;
                case 1: // ok
                    edit.putString("account_username", username.getText().toString());
                    edit.putString("account_password", password.getText().toString());
                    edit.commit();
                    Toast.makeText(getApplicationContext(), "Connexion réussie!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    startService(new Intent(getApplicationContext(), gksUpdater.class));
                    break;
                case 2: // nok
                    Toast.makeText(getApplicationContext(), "Login ou Password incorrect.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
