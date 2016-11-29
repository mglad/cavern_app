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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mglad.cavern.Adapters.MenuItemAdapter;
import com.example.mglad.cavern.Adapters.OrderAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class AdminMenuActivity extends AppCompatActivity {
    private View mAdminView;
    private View mProgressView;
    private GetMenuTask mMenuTask = null;
    private SetMenuTask mSetMenuTask = null;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAdminView = findViewById(R.id.admin_layout);
        mProgressView = findViewById(R.id.admin_progress);
        mListView = (ListView) findViewById(R.id.menu_list_view);

        mListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox checkBox = (CheckBox) view.findViewById(R.id.menu_item_checkbox);
                checkBox.toggle();
                JSONObject item = new JSONObject();
                try {
                    item.put("name", ((TextView) view.findViewById(R.id.menu_item_text)).getText().toString());
                    item.put("category", ((TextView) view.findViewById(R.id.menu_category_text)).getText().toString());
                    item.put("type", ((TextView) view.findViewById(R.id.menu_type_text)).getText().toString());
                    item.put("available", ((CheckBox) view.findViewById(R.id.menu_item_checkbox)).isChecked());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                showProgress(true);
                mSetMenuTask = new SetMenuTask(item);
                mSetMenuTask.execute((Void) null);
            }
        });
        getMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    private void getMenu() {
        showProgress(true);
        mMenuTask = new GetMenuTask();
        mMenuTask.execute((Void) null);
    }

    private void setMenu(JSONObject result) throws JSONException {
        JSONArray mainCourses = result.getJSONArray("mainCourses");
        JSONArray sides = result.getJSONArray("sides");
        JSONArray beverages = result.getJSONArray("beverages");

        final ArrayList<JSONObject> menuList = new ArrayList<>();
        for (int i = 0; i < mainCourses.length(); i++) {
            JSONObject item = mainCourses.getJSONObject(i);
            item.put("category", "Main Course");
            menuList.add(item);
        }

        for (int i = 0; i < sides.length(); i++) {
            JSONObject item = sides.getJSONObject(i);
            item.put("category", "Side");
            menuList.add(item);
        }

        for (int i = 0; i < beverages.length(); i++) {
            JSONObject item = beverages.getJSONObject(i);
            item.put("category", "Beverage");
            menuList.add(item);
        }

        MenuItemAdapter adapter = new MenuItemAdapter(this, menuList);
        mListView.setAdapter(adapter);
    }

    public class GetMenuTask extends AsyncTask<Void, Void, String> {

        private final String urlString = "https://dry-bayou-35727.herokuapp.com/menu";

        GetMenuTask() {
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
            mMenuTask = null;
            System.out.println(response);
            showProgress(false);
            try {
                JSONObject result = new JSONObject(response);
                setMenu(result);
            } catch (Exception e) {

            }
        }

        @Override
        protected void onCancelled() {
            mMenuTask = null;
            showProgress(false);
        }
    }

    public class SetMenuTask extends AsyncTask<Void, Void, String> {

        private final String urlString = "https://dry-bayou-35727.herokuapp.com/menu";
        private final JSONObject mMenuItem;

        SetMenuTask(JSONObject menuItem) {
            mMenuItem = menuItem;
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

                root.put("menuItem", mMenuItem);

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
            mSetMenuTask = null;
            showProgress(false);
            try {
                JSONObject result = new JSONObject(response);
                if (result.getBoolean("success")) {
                    Toast.makeText(getApplicationContext(), "Menu Updated",
                            Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error updating menu.",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            } catch (Exception e) {

            }
        }

        @Override
        protected void onCancelled() {
            mSetMenuTask = null;
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
