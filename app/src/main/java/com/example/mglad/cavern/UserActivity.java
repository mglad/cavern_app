package com.example.mglad.cavern;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class UserActivity extends AppCompatActivity {
    private JSONObject user;
    private View mUserButtons;
    private View mProgressView;
    private GetHoursTask mGetHoursTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Button placeOrderButton = (Button) findViewById(R.id.place_order_button);
        Button trackingButton = (Button) findViewById(R.id.track_order_button);
        Button signOutButton = (Button) findViewById(R.id.sign_out_button);
        mUserButtons = findViewById(R.id.user_buttons);
        mProgressView = findViewById(R.id.hours_progress);

        placeOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToOrder();
            }
        });
        trackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToTracking();
            }
        });
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);

                builder.setMessage("Are you sure you want to sign out?")
                        .setTitle("Sign Out").setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

                AlertDialog dialog = builder.create();

                dialog.show();
            }
        });
        try {
            user = new JSONObject(getIntent().getStringExtra("user"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getHours();
    }

    private void getHours() {
        showProgress(true);
        mGetHoursTask = new GetHoursTask();
        mGetHoursTask.execute((Void) null);
    }

    private void goToOrder() {
        Intent intent = new Intent(this, OrderActivity.class);
        intent.putExtra("user", user.toString());
        startActivity(intent);
    }

    private void goToTracking() {
        Intent intent = new Intent(this, TrackingActivity.class);
        intent.putExtra("user", user.toString());
        startActivity(intent);
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

            mUserButtons.setVisibility(show ? View.GONE : View.VISIBLE);
            mUserButtons.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mUserButtons.setVisibility(show ? View.GONE : View.VISIBLE);
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
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mUserButtons.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class GetHoursTask extends AsyncTask<Void, Void, String> {

        private final String urlString = getString(R.string.server_url) + "hours";

        GetHoursTask() {
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
            mGetHoursTask = null;
            showProgress(false);
            try {
                JSONObject result = new JSONObject(response);
                setHours(result.getJSONArray("hoursOfOperation"));
            } catch (Exception e) {

            }
        }

        @Override
        protected void onCancelled() {
            mGetHoursTask = null;
            showProgress(false);
        }
    }

    private void setHours(JSONArray hours) {
        TextView sundayTextView = (TextView) findViewById(R.id.hours_sunday);
        TextView mondayTextView = (TextView) findViewById(R.id.hours_monday);
        TextView tuesdayTextView = (TextView) findViewById(R.id.hours_tuesday);
        TextView wednesdayTextView = (TextView) findViewById(R.id.hours_wednesday);
        TextView thursdayTextView = (TextView) findViewById(R.id.hours_thursday);
        TextView fridayTextView = (TextView) findViewById(R.id.hours_friday);
        TextView saturdayTextView = (TextView) findViewById(R.id.hours_saturday);


        try {
            sundayTextView.setText(formatHours(hours.getJSONObject(0)));
            mondayTextView.setText(formatHours(hours.getJSONObject(1)));
            tuesdayTextView.setText(formatHours(hours.getJSONObject(2)));
            wednesdayTextView.setText(formatHours(hours.getJSONObject(3)));
            thursdayTextView.setText(formatHours(hours.getJSONObject(4)));
            fridayTextView.setText(formatHours(hours.getJSONObject(5)));
            saturdayTextView.setText(formatHours(hours.getJSONObject(6)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String formatHours(JSONObject day) throws JSONException {
        String open = day.getString("open");
        String close = day.getString("close");
        System.out.println(open);
        return open != "null" ? formatDate(open) + " to " + formatDate(close) : "Closed";
    }

    private String formatDate(String date) {
        DateFormat outputFormat = new SimpleDateFormat("h:mm a", Locale.US);
        SimpleDateFormat parseFormat = new SimpleDateFormat("KK:mm:ss a", Locale.US);
        try {
            Date dt = parseFormat.parse(date);
            return outputFormat.format(dt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }
}
