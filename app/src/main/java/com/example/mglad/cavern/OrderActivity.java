package com.example.mglad.cavern;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class OrderActivity extends AppCompatActivity {
    private View mOrderFormView;
    private View mProgressView;
    private Spinner sandwichSpinner;
    private Spinner wrapSpinner;
    private Spinner saladSpinner;
    private TextView selectChoiceTextView;
    private JSONObject user;

    private GetMenuTask mMenuTask = null;
    private final int REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            user = new JSONObject(getIntent().getStringExtra("user"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mOrderFormView = findViewById(R.id.order_form);
        mProgressView = findViewById(R.id.order_progress);

        sandwichSpinner = (Spinner) findViewById(R.id.sandwich_spinner);
        wrapSpinner = (Spinner) findViewById(R.id.wrap_spinner);
        saladSpinner = (Spinner) findViewById(R.id.salad_spinner);
        selectChoiceTextView = (TextView) findViewById(R.id.select_choice);
        getMenu();

        Button placeOrderButton = (Button) findViewById(R.id.place_order_button);
        placeOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    placeOrder();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.main_course_type);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) findViewById(checkedId);
                String text = rb.getText().toString();

                selectChoiceTextView.setVisibility(View.VISIBLE);

                sandwichSpinner.setVisibility(View.GONE);
                wrapSpinner.setVisibility(View.GONE);
                saladSpinner.setVisibility(View.GONE);

                System.out.println(text);
                if (text.equals("Sandwich")) {
                    sandwichSpinner.setVisibility(View.VISIBLE);
                } else if (text.equals("Wrap")) {
                    wrapSpinner.setVisibility(View.VISIBLE);
                } else if (text.equals("Salad")) {
                    saladSpinner.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void placeOrder() throws JSONException {
        // Reset errors.
        RadioGroup selectType = (RadioGroup) findViewById(R.id.main_course_type);
        RadioButton typeButton = (RadioButton) findViewById(selectType.getCheckedRadioButtonId());

        if (typeButton == null) {
            showError("You must select a type.");
            return;
        }

        String type = typeButton.getText().toString();

        Spinner selectedSpinner;
        if (type.equals("Sandwich"))
            selectedSpinner = sandwichSpinner;
        else if (type.equals("Wrap"))
            selectedSpinner = wrapSpinner;
        else
            selectedSpinner = saladSpinner;

        String mainCourse = selectedSpinner.getSelectedItem().toString();
        if (mainCourse.equals("Select")) {
            showError("You must select a main course.");
            return;
        }


        Spinner side1Spinner = (Spinner) findViewById(R.id.side_spinner);
        String side1 = side1Spinner.getSelectedItem().toString();

        Spinner side2Spinner = (Spinner) findViewById(R.id.side_spinner2);
        String side2 = side2Spinner.getSelectedItem().toString();

        if (side1.equals("Select") || side2.equals("Select")) {
            showError("You must select 2 sides.");
            return;
        }

        String beverage = ((Spinner) findViewById(R.id.beverage_spinner)).getSelectedItem().toString();
        if (beverage.equals("Select")) {
            showError("You must select a beverage.");
            return;
        }
//
        JSONObject order = new JSONObject();
        order.put("type", type);
        order.put("mainCourse", mainCourse);
        order.put("side1", side1);
        order.put("side2", side2);
        order.put("beverage", beverage);

        goToPayment(order);
    }

    private void goToPayment(JSONObject order) {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("order", order.toString());
        intent.putExtra("user", user.toString());
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        }
    }

    private void showError(String error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(error)
                .setTitle("Error").setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

            }
        });

// 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void getMenu() {
        showProgress(true);
        mMenuTask = new GetMenuTask();
        mMenuTask.execute((Void) null);
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

            mOrderFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mOrderFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mOrderFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mOrderFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class GetMenuTask extends AsyncTask<Void, Void, String> {

        private final String urlString = "http://192.168.2.17:3000/menu";

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

    private void setMenu(JSONObject result) throws JSONException {
        ArrayList<String> sandwiches = new ArrayList();
        sandwiches.add("Select");
        ArrayList<String> wraps = new ArrayList();
        wraps.add("Select");
        ArrayList<String> salads = new ArrayList();
        salads.add("Select");

        JSONArray mainCourses = result.getJSONArray("mainCourses");

        for (int i = 0; i < mainCourses.length(); i++) {
            JSONObject mainCourse = mainCourses.getJSONObject(i);
            String type = mainCourse.getString("type");
            if (mainCourse.getBoolean("available"))
                if (type.equals("sandwich")) {
                    sandwiches.add(mainCourse.getString("name"));
                } else if (type.equals("wrap")) {
                    wraps.add(mainCourse.getString("name"));
                } else if (type.equals("salad")) {
                    salads.add(mainCourse.getString("name"));
                }
        }

        setSpinner(R.id.sandwich_spinner, sandwiches);
        setSpinner(R.id.wrap_spinner, wraps);
        setSpinner(R.id.salad_spinner, salads);

        ArrayList<String> sideList = new ArrayList();
        sideList.add("Select");
        JSONArray sides = result.getJSONArray("sides");
        for (int i = 0; i < sides.length(); i++) {
            JSONObject side = sides.getJSONObject(i);
            if (side.getBoolean("available"))
                sideList.add(side.getString("name"));
        }

        setSpinner(R.id.side_spinner, sideList);
        setSpinner(R.id.side_spinner2, sideList);

        ArrayList<String> beverageList = new ArrayList<>();
        beverageList.add("Select");
        JSONArray beverages = result.getJSONArray("beverages");
        for (int i = 0; i < beverages.length(); i++) {
            JSONObject beverage = beverages.getJSONObject(i);
            if (beverage.getBoolean("available"))
                beverageList.add(beverage.getString("name"));
        }

        setSpinner(R.id.beverage_spinner, beverageList);
    }

    private void setSpinner(int resource, ArrayList<String> arr) {
        Spinner spinner = (Spinner) findViewById(resource);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arr);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinnerAdapter.notifyDataSetChanged();
    }


}
