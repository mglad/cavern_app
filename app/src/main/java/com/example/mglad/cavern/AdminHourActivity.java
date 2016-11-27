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
import android.widget.Spinner;
import android.widget.TimePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class AdminHourActivity extends AppCompatActivity {

    private View mAdminView;
    private View mProgressView;
    private GetHoursTask mGetHoursTask = null;
    private UpdateHoursTask mUpdateHoursTask = null;
    private JSONArray mHours;
    private Spinner daySpinner;
    private TimePicker openPicker;
    private TimePicker closePicker;
    private CheckBox closedCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_hour);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAdminView = findViewById(R.id.admin_hour_form);
        mProgressView = findViewById(R.id.admin_progress);
        daySpinner = (Spinner) findViewById(R.id.days_of_week_spinner);
        openPicker = (TimePicker) findViewById(R.id.select_open_time);
        closePicker = (TimePicker) findViewById(R.id.select_close_time);
        closedCheckBox = (CheckBox) findViewById(R.id.is_closed);

        daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                try {
                    setTimePickerText(position - 1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        getHours();

        Button setHoursButton = (Button) findViewById(R.id.set_hours_button);
        setHoursButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setHours();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    private void setHours() {
        int day = daySpinner.getSelectedItemPosition();
        if (closedCheckBox.isChecked()) {
            showProgress(true);
            mUpdateHoursTask = new UpdateHoursTask(day, null, null);
            mUpdateHoursTask.execute((Void) null);
        } else {
            String openTime = formatTime(openPicker.getCurrentHour(), openPicker.getCurrentMinute());
            String closeTime = formatTime(closePicker.getCurrentHour(), closePicker.getCurrentMinute());
            showProgress(true);
            mUpdateHoursTask = new UpdateHoursTask(day, openTime, closeTime);
            mUpdateHoursTask.execute((Void) null);
        }
    }

    private String formatTime(Integer currentHour, Integer currentMinute) {
        String time = currentHour + ":" + currentMinute;
        DateFormat outputFormat = new SimpleDateFormat("KK:mm:ss a", Locale.US);
        SimpleDateFormat parseFormat = new SimpleDateFormat("k:m", Locale.US);
        try {
            Date dt = parseFormat.parse(time);
            return outputFormat.format(dt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void setTimePickerText(int day) throws JSONException {
        JSONObject hoursForDay = new JSONObject(mHours.get(day).toString());
        String openHours = hoursForDay.getString("open");
        String closeHours = hoursForDay.getString("close");
        System.out.println(openHours);
        if (!openHours.equals("null")) {
            openPicker.setCurrentHour(getHours(openHours));
            openPicker.setCurrentMinute(getMinutes(openHours));
            closePicker.setCurrentHour(getHours(closeHours));
            closePicker.setCurrentMinute(getMinutes(closeHours));
        } else {
            openPicker.setCurrentHour(0);
            openPicker.setCurrentMinute(0);
            closePicker.setCurrentHour(0);
            closePicker.setCurrentMinute(0 );
        }
        closedCheckBox.setChecked(openHours.equals("null"));
    }

    private int getHours(String time) {
        DateFormat outputFormat = new SimpleDateFormat("k", Locale.US);
        SimpleDateFormat parseFormat = new SimpleDateFormat("KK:mm:ss a", Locale.US);
        try {
            Date dt = parseFormat.parse(time);
            return Integer.parseInt(outputFormat.format(dt));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getMinutes(String time) {
        DateFormat outputFormat = new SimpleDateFormat("m", Locale.US);
        SimpleDateFormat parseFormat = new SimpleDateFormat("KK:mm:ss a", Locale.US);
        try {
            Date dt = parseFormat.parse(time);
            return Integer.parseInt(outputFormat.format(dt));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void getHours() {
        showProgress(true);
        mGetHoursTask = new GetHoursTask();
        mGetHoursTask.execute((Void) null);
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

    public class GetHoursTask extends AsyncTask<Void, Void, String> {

        private final String urlString = "http://192.168.2.17:3000/hours";

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
                mHours = result.getJSONArray("hoursOfOperation");
            } catch (Exception e) {

            }
        }

        @Override
        protected void onCancelled() {
            mGetHoursTask = null;
            showProgress(false);
        }

    }

    public class UpdateHoursTask extends AsyncTask<Void, Void, String> {

        private final String urlString = "http://192.168.2.17:3000/hours";
        private final int mDay;
        private final String mOpenTime;
        private final String mCloseTime;

        UpdateHoursTask(int day, String openTime, String closeTime) {
            mDay = day;
            mOpenTime = openTime;
            mCloseTime = closeTime;
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
                url = new URL(requestURL + "/" + mDay);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("PUT");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.setRequestProperty("Content-Type", "application/json");

                JSONObject root = new JSONObject();

                root.put("open", mOpenTime);
                root.put("close", mCloseTime);

                String str = root.toString();

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
            mUpdateHoursTask = null;
            showProgress(false);
            System.out.println(response);
            try {
                JSONObject result = new JSONObject(response);
                mHours = result.getJSONArray("hoursOfOperation");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            mUpdateHoursTask = null;
            showProgress(false);
        }
    }
}
