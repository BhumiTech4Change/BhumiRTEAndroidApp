package org.bhumi.bhumisrte.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.bhumi.bhumisrte.R;
import org.bhumi.bhumisrte.API.User;
import org.bhumi.bhumisrte.API.Validator;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/*
 * Home Activity
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    // UI references
    private ProgressBar progressView;
    private EditText parentNameView;
    private EditText phoneView;
    private EditText childNameView;
    private EditText pinCodeView;
    private EditText dateOfBirthView;
    private EditText commentView;
    private Button submit;
    private Toolbar toolbar;
    private CheckBox incomeView, communityView, birthView, addressView;
    private RelativeLayout relativeLayout;
    private View loginFormView;
    private Calendar myCalendar;

    // Data fields
    private String parentName, phone, childName, pinCode;
    private String dateOfBirth, comment, email, token, certificates;
    private String endpoint;
    private Validator validator;
    private User user;

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Quit?").setMessage("Do you really want to quit ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Instantiate UI References
        toolbar = findViewById(R.id.toolbar);
        parentNameView = findViewById(R.id.parentname);
        phoneView = findViewById(R.id.phone);
        loginFormView = findViewById(R.id.form);
        progressView = findViewById(R.id.progress);
        childNameView = findViewById(R.id.childname);
        pinCodeView = findViewById(R.id.pincode);
        dateOfBirthView = findViewById(R.id.dateofbirth);
        commentView = findViewById(R.id.comment);
        submit = findViewById(R.id.submit_button);
        relativeLayout = findViewById(R.id.relativeLayout);
        incomeView = findViewById(R.id.incomeCert);
        communityView = findViewById(R.id.communityCert);
        birthView = findViewById(R.id.birthCert);
        addressView = findViewById(R.id.addressCert);
        user = User.getCurrentUser(getApplicationContext());

        // Intialize the data
        endpoint = getString(R.string.rte_api_url);
        validator = Validator.getInstance(getApplicationContext());
        myCalendar = Calendar.getInstance();

        setSupportActionBar(toolbar);

        // Check user loggedIn
        if (!user.isLoggedIn()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            finish();
            startActivity(intent);
        }

        // DOB field handler
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };
        dateOfBirthView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(MainActivity.this, date, myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        submit.setOnClickListener(this);
    }

    /*
     * Menubar menu options
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /*
    Menubar click handler
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.signout:
                signout(); break;
            case R.id.credit:
                startActivity(new Intent(this, CreditsActivity.class));
                break;
            case R.id.aboutRTE:
                startActivity(new Intent(this, AboutRteActivity.class));
                break;
            case R.id.feedback:
                startActivity(new Intent(this, FeedbackActivity.class));
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    /*
    * Displays a alert to ask if the user really wants to logout
     */
    private void signout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Logout").setMessage("Do you really want to logout ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        user.logout();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        finish();
                        startActivity(intent);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /*
     * Updates the input field based on the selected date from calendar
     */
    private void updateLabel() {
        String myFormat = "dd-MM-yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        dateOfBirthView.setText(sdf.format(myCalendar.getTime()));
    }

    /*
     * Extract data, validate and submit data
     */
    private void sendData() throws IOException{
        OkHttpClient client = new OkHttpClient();

        // Encode the input fields
        String mEmail = validator.encode(email);
        String mPhone = validator.encode(phone);
        String mParentName = validator.encode(parentName);
        String mChildName = validator.encode(childName);
        String mDateOfBirth = validator.encode(dateOfBirth);
        String mComment = validator.encode(comment);
        String mPinCode = validator.encode(pinCode);
        String mToken = validator.encode(token);
        String mCertificates = validator.encode(certificates);

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        showProgress(true);
        RequestBody body = RequestBody.create(mediaType, "email="+mEmail+
                "&phone="+mPhone+"&parentName="+mParentName+"&childName="+mChildName+"+" +
                "&dateOfBirth="+mDateOfBirth+"&pin="+mPinCode+"&certificate="+mCertificates+
                "&comment="+mComment);

        Request request = new Request.Builder()
                .url(endpoint+"/form")
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Authorization", "Bearer "+mToken)
                .addHeader("cache-control", "no-cache")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar snackbar = Snackbar.make(relativeLayout,
                                "Failed to submit !", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        showProgress(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody responseBody = response.body();
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(responseBody.string());
                    final String msg = jsonObject.getString("msg");

                    if (jsonObject.getBoolean("success")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showProgress(false);
                                clearFields();
                                Snackbar snackbar = Snackbar.make(relativeLayout, msg, Snackbar.LENGTH_SHORT);
                                snackbar.show();
                            }
                        });

                    }else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showProgress(false);
                                Snackbar snackbar = Snackbar.make(relativeLayout, msg, Snackbar.LENGTH_SHORT);
                                snackbar.show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Something went wrong, please report to us!", Toast.LENGTH_LONG).show();
                            showProgress(false);
                        }
                    });

                }
            }
        });

    }

    /*
    * Clears the input filed once the data is submitted
     */
    private void clearFields() {
        childNameView.setText("");
        parentNameView.setText("");
        phoneView.setText("");
        dateOfBirthView.setText("");
        pinCodeView.setText("");
        commentView.setText("");
        addressView.setChecked(false);
        incomeView.setChecked(false);
        communityView.setChecked(false);
        birthView.setChecked(false);
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

            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            loginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /*
    * Extract the data from the input fields
     */
    private void extractValues() {
        parentName = parentNameView.getText().toString();
        childName = childNameView.getText().toString();
        phone = phoneView.getText().toString();
        pinCode = pinCodeView.getText().toString();
        dateOfBirth = dateOfBirthView.getText().toString();
        comment = commentView.getText().toString();

        email = user.getEmail();
        token = user.getToken();
        certificates = "";

        if (incomeView.isChecked()) certificates +="Income Certificate, ";
        if (communityView.isChecked()) certificates += "Community Certificate, ";
        if (birthView.isChecked()) certificates += "Birth Certificate, ";
        if (addressView.isChecked()) certificates += "Address Proof";


    }

    /*
    * Validate the inputs
     */
    private void validateInputFields() {
        validator.validateText(parentName, parentNameView);
        validator.validateText(childName, childNameView);
        validator.validateText(dateOfBirth, dateOfBirthView);
        validator.validatePhone(phone, phoneView);
        validator.validatePin(pinCode, pinCodeView);
    }

    /*
     * OnClick handler to submit data
     */
    @Override
    public void onClick(View v) {
        validator.reset();

        extractValues();
        validateInputFields();
        if (validator.isOkay()) {
            try {
                sendData();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(),"Unable to submit data", Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "Fix the errors and try again!", Toast.LENGTH_LONG).show();
        }
    }
}
