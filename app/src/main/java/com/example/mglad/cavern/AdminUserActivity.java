package com.example.mglad.cavern;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mglad.cavern.Adapters.UserAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class AdminUserActivity extends AppCompatActivity {
    private View mAdminView;
    private View mProgressView;
    private GetUsersTask mGetUsersTask = null;
    private SetUserTask mSetUserTask = null;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAdminView = findViewById(R.id.admin_layout);
        mProgressView = findViewById(R.id.admin_progress);
        mListView = (ListView) findViewById(R.id.user_list_view);

        mListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox checkBox = (CheckBox) view.findViewById(R.id.user_checkbox);
                checkBox.toggle();
                JSONObject item = new JSONObject();
                try {
                    item.put("username", ((TextView) view.findViewById(R.id.user_item_text)).getText().toString());
                    item.put("blacklisted", !checkBox.isChecked());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                showProgress(true);
                mSetUserTask = new SetUserTask(item);
                mSetUserTask.execute((Void) null);
            }
        });
        getUsers();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    private void getUsers() {
        showProgress(true);
        mGetUsersTask = new GetUsersTask();
        mGetUsersTask.execute((Void) null);
    }

    private void setUsers(JSONObject result) throws JSONException {
        JSONArray users = result.getJSONArray("users");
        ArrayList<JSONObject> userList = new ArrayList<>();

        for(int i=0; i<users.length(); i++) {
            userList.add(users.getJSONObject(i));
        }

        UserAdapter adapter = new UserAdapter(this, userList);
        mListView.setAdapter(adapter);
    }

    public class GetUsersTask extends AsyncTask<Void, Void, String> {

        private final String urlString = getString(R.string.server_url) + "users";

        GetUsersTask() {
        }

        @Override
        protected String doInBackground(Void... params) {

            String response;

            try {
                response = performGetCall(urlString);
                return response;
            } catch (Exception e) {

            }

            return "";
        }


        public String performGetCall(String requestURL) {

            URL url;
            String response = "";
            try {
                url = new URL(requestURL);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(10000);

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                } else {
                    response = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(final String response) {
            mGetUsersTask = null;
            System.out.println(response);
            showProgress(false);
            try {
                JSONObject result = new JSONObject(response);
                setUsers(result);
            } catch (Exception e) {

            }
        }

        @Override
        protected void onCancelled() {
            mGetUsersTask = null;
            showProgress(false);
        }
    }

    public class SetUserTask extends AsyncTask<Void, Void, String> {

        private final String urlString = getString(R.string.server_url) + "users";
        private final JSONObject mUser;

        SetUserTask(JSONObject user) {
            mUser = user;
        }

        @Override
        protected String doInBackground(Void... params) {

            String response;

            try {
                response = performPostCall(urlString);
                return response;
            } catch (Exception e) {

            }


            return "";
        }


        public String performPostCall(String requestURL) {

            URL url;
            String response = "";
            try {
                url = new URL(requestURL);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.setRequestProperty("Content-Type", "application/json");

                JSONObject root = new JSONObject();

                root.put("user", mUser);

                String str = root.toString();
                System.out.println(str);
                byte[] outputBytes = str.getBytes("UTF-8");
                OutputStream os = conn.getOutputStream();
                os.write(outputBytes);

                int responseCode = conn.getResponseCode();
                System.out.println(responseCode);

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                } else {
                    response = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(final String response) {
            mSetUserTask = null;
            showProgress(false);
            try {
                JSONObject result = new JSONObject(response);
                if (result.getBoolean("success")) {
                    Toast.makeText(getApplicationContext(), "Users Updated",
                            Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error updating users.",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            } catch (Exception e) {

            }
        }

        @Override
        protected void onCancelled() {
            mSetUserTask = null;
            showProgress(false);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mAdminView.setVisibility(show ? View.GONE : View.VISIBLE);
            mAdminView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mAdminView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mAdminView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
