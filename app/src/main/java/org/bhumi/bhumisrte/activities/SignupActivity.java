package org.bhumi.bhumisrte.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;

import org.bhumi.bhumisrte.R;
import org.bhumi.bhumisrte.API.Endpoint;
import org.bhumi.bhumisrte.API.Validator;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * A Signup screen logic
 */
public class SignupActivity extends AppCompatActivity implements  OnClickListener {

    private static final int REQUEST_READ_CONTACTS = 0;

    // UI references.
    AutoCompleteTextView emailView;
    EditText passwordView;
    EditText passwordVerifyView;
    RelativeLayout relativeLayout;
    View progressView;
    View loginFormView;
    EditText phoneView;
    Button signUpButton;
    EditText pinCodeView;
    private View focusView;

    // Data containers
    private String email;
    private String password;
    private String passwordVerify;
    private String phone;
    private String pinCode;
    private Boolean cancel;
    private String endpoint;
    private Validator validator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        validator = Validator.getInstance(getApplicationContext());
        endpoint = Endpoint.getInstance(getApplicationContext()).getEndpoint();

        // Instantiate the views
        emailView = findViewById(R.id.email);
        relativeLayout = findViewById(R.id.relativeLayout);
        passwordView = findViewById(R.id.password);
        passwordVerifyView = findViewById(R.id.passwordVerify);
        progressView = findViewById(R.id.signup_progress);
        loginFormView = findViewById(R.id.login_form);
        phoneView = findViewById(R.id.phone);
        pinCodeView = findViewById(R.id.pin);
        signUpButton = findViewById(R.id.sign_up_button);


        // Set up the login form.

        signUpButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        try {
            attemptSignup();
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(), "Something went wrong, please report to us", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptSignup() throws IOException, JSONException {

        initLogin();
        extractInputs();
        validateInputs();

        if (validator.isOkay()) {
            showProgress(true);
            signUp();
        }
    }

    private void initLogin() {
        // Reset errors.
        validator.reset();
        emailView.setError(null);
        passwordView.setError(null);
        passwordVerifyView.setError(null);
        pinCodeView.setError(null);
        phoneView.setError(null);
    }

    private void extractInputs() {
        // Store values at the time of the login attempt.
        email = emailView.getText().toString();
        password = passwordView.getText().toString();
        passwordVerify = passwordVerifyView.getText().toString();
        phone = phoneView.getText().toString();
        pinCode = pinCodeView.getText().toString();
    }

    private void validateInputs() {
        // validate all inputs
        validator.validatePhone(phone, phoneView);
        validator.validatePin(pinCode, pinCodeView);
        validator.validateEmail(email, emailView);
        validator.validatePassword(password, passwordView);
        validator.validatePassword(passwordVerify, passwordVerifyView);
        validator.checkPasswordsMatch(password, passwordVerify, passwordVerifyView);
    }

    private void signUp() throws IOException {


        final Context context = getApplicationContext();
        // Construct the post request
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");

        RequestBody body = RequestBody.create(mediaType, "email="+validator.encode(email)+
                "&password="+validator.encode(password)+
                "&pin="+validator.encode(pinCode)+
                "&phone="+validator.encode(phone));

        Request request = new Request.Builder()
                .url(endpoint+"/signup/")
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("cache-control", "no-cache")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Something went wrong, please report this", Toast.LENGTH_SHORT).show();
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
                                Intent intent = new Intent(context, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                                showProgress(false);
                            }
                        });
                    }
                } catch (JSONException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Something went wrong, please report to us!", Toast.LENGTH_LONG).show();
                            showProgress(false);
                        }
                    });
                }
            }
        });
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


}




