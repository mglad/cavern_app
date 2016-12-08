package com.example.mglad.cavern;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class PaymentActivity extends AppCompatActivity {
    private View mPaymentFormView;
    private View mProgressView;
    private JSONObject order;
    private JSONObject user;

    private TimePicker selectTime;
    private PlaceOrderTask mPlaceOrderTask = null;
    private Calendar myCalendar = Calendar.getInstance();
    private TextView selectDatePreview;
    private RadioGroup selectScheduleType;
    private DatePickerDialog.OnDateSetListener myDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0,
                                      int year, int monthOfYear, int dayOfMonth) {
                    myCalendar.set(Calendar.YEAR, year);
                    myCalendar.set(Calendar.MONTH, monthOfYear);
                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateLabel();
                }
            };

    public PaymentActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPaymentFormView = findViewById(R.id.payment_form);
        mProgressView = findViewById(R.id.payment_progress);
        selectDatePreview = (TextView) findViewById(R.id.date_preview);

        try {
            user = new JSONObject(getIntent().getStringExtra("user"));
            order = new JSONObject(getIntent().getStringExtra("order"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Button confirmPaymentButton = (Button) findViewById(R.id.confirm_payment_button);
        confirmPaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String scheduleType = ((RadioButton) findViewById(selectScheduleType.getCheckedRadioButtonId())).getText().toString();
                    if(scheduleType.equals("Later")) {
                        order.put("pickUpNow", false);
                        order.put("date", selectDatePreview.getText().toString());
                        order.put("time", getTime());
                    } else {
                        order.put("pickUpNow", true);
                    }
                    confirmOrder();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Button selectDateButton = (Button) findViewById(R.id.select_date_button);
        selectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(PaymentActivity.this, myDateListener, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        selectTime = (TimePicker) findViewById(R.id.select_time);

        selectScheduleType = (RadioGroup) findViewById(R.id.schedule_type);

        selectScheduleType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) findViewById(checkedId);
                String text = rb.getText().toString();

                View selectDate = findViewById(R.id.select_date);

                if (text.equals("Now")) {
                    selectDate.setVisibility(View.GONE);
                    selectTime.setVisibility(View.GONE);
                } else {

                    selectDate.setVisibility(View.VISIBLE);
                    selectTime.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private String getTime() {
        int hour = selectTime.getCurrentHour();
        int min = selectTime.getCurrentMinute();
        String format;

        if (hour == 0) {
            hour += 12;
            format = "AM";
        } else if (hour == 12) {
            format = "PM";
        } else if (hour > 12) {
            hour -= 12;
            format = "PM";
        } else {
            format = "AM";
        }

       return new StringBuilder().append(hour).append(":").append(min).append(":00")
                .append(" ").append(format).toString();
    }

    private void confirmOrder() throws JSONException {
        showProgress(true);
        mPlaceOrderTask = new PlaceOrderTask();
        mPlaceOrderTask.execute((Void) null);
    }


    private void updateLabel() {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        selectDatePreview.setText(sdf.format(myCalendar.getTime()));
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
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

            mPaymentFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mPaymentFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mPaymentFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mPaymentFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class PlaceOrderTask extends AsyncTask<Void, Void, String> {

        private final String urlString = getString(R.string.server_url) + "order";

        PlaceOrderTask() {
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

                root.put("order", order);
                root.put("user", user);

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
            mPlaceOrderTask = null;
            showProgress(false);
            try {
                JSONObject result = new JSONObject(response);
                if (result.getBoolean("success")) {
                    Intent intent = new Intent();
                    if (getParent() == null) {
                        setResult(Activity.RESULT_OK, intent);
                    } else {
                        getParent().setResult(Activity.RESULT_OK, intent);
                    }
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), result.getString("error"),
                            Toast.LENGTH_LONG)
                            .show();
                }
            } catch (Exception e) {

            }
        }

        @Override
        protected void onCancelled() {
            mPlaceOrderTask = null;
            showProgress(false);
        }
    }
}
