package com.example.mglad.cavern;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

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
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class AdminActivity extends AppCompatActivity {
    private View mAdminView;
    private View mProgressView;
    private ListView mListView;
    private OrderAdapter adapter;
    private GetOrdersTask mOrdersTask = null;
    private UpdateOrderTask mUpdateOrderTask = null;
    private static final HashMap<String, String> ORDER_STATUS = new HashMap<String, String>() {{
        put("Placed", "PLACED");
        put("In Progress", "IN_PROGRESS");
        put("Finished", "FINISHED");
    }};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        mAdminView = findViewById(R.id.admin_layout);
        mProgressView = findViewById(R.id.admin_progress);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Refreshing...", Snackbar.LENGTH_LONG).setDuration(500).show();
                getOrders();
            }
        });
        getOrders();
    }

    private void getOrders() {
        showProgress(true);
        mOrdersTask = new GetOrdersTask();
        mOrdersTask.execute((Void) null);
    }

    private void updateOrder(JSONObject order, String status) {
        showProgress(true);
        mUpdateOrderTask = new UpdateOrderTask(order, status);
        mUpdateOrderTask.execute((Void) null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_admin; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_set_menu:
                Intent menuIntent = new Intent(this, AdminMenuActivity.class);
                startActivity(menuIntent);
                return true;
            case R.id.action_set_hours:
                Intent hourIntent = new Intent(this, AdminHourActivity.class);
                startActivity(hourIntent);
                return true;
            case R.id.action_edit_users:
                Intent editUserIntent = new Intent(this, AdminUserActivity.class);
                startActivity(editUserIntent);
                return true;
            case R.id.action_sign_out:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    public class GetOrdersTask extends AsyncTask<Void, Void, String> {

        private final String urlString = getString(R.string.server_url) + "order";

        GetOrdersTask() {
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
            mOrdersTask = null;
            showProgress(false);
            JSONObject result = null;
            try {
                result = new JSONObject(response);
                setOrders(result.getJSONArray("orders"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            mOrdersTask = null;
            showProgress(false);
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        System.out.println("called");
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Set Status");
        menu.add("Placed");
        menu.add("In Progress");
        menu.add("Finished");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String status = item.getTitle().toString();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        JSONObject obj = null;
        try {
            obj = new JSONObject(adapter.getItem(info.position).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        updateOrder(obj, ORDER_STATUS.get(status));
        return true;
    }

    private void setOrders(JSONArray orders) throws JSONException {
        mListView = (ListView) findViewById(R.id.order_list_view);

        final ArrayList<JSONObject> orderList = new ArrayList<>();
        for (int i = 0; i < orders.length(); i++) {
            orderList.add(orders.getJSONObject(i));
        }

        adapter = new OrderAdapter(this, orderList);


        mListView.setAdapter(adapter);
        registerForContextMenu(mListView);
    }

    public class UpdateOrderTask extends AsyncTask<Void, Void, String> {

        private final String urlString = getString(R.string.server_url) + "order";
        private final JSONObject mOrder;
        private final String mStatus;

        UpdateOrderTask(JSONObject order, String status) {
            mOrder = order;
            mStatus = status;
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
                url = new URL(requestURL + "/" + mOrder.getInt("id"));

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("PUT");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.setRequestProperty("Content-Type", "application/json");

                JSONObject root = new JSONObject();

                root.put("status", mStatus);

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
            mUpdateOrderTask = null;
            showProgress(false);
            System.out.println(response);
            try {
                JSONObject result = new JSONObject(response);
                setOrders(result.getJSONArray("orders"));
            } catch (Exception e) {

            }
        }

        @Override
        protected void onCancelled() {
            mUpdateOrderTask = null;
            showProgress(false);
        }
    }
}
