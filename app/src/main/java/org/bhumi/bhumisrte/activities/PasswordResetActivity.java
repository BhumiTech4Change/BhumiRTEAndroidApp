package org.bhumi.bhumisrte.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.database.Cursor;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.net.URLEncoder;

import org.bhumi.bhumisrte.R;
import org.bhumi.bhumisrte.config.Endpoint;
import org.bhumi.bhumisrte.config.Validator;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * A login screen that offers login via email/password.
 */
public class PasswordResetActivity extends AppCompatActivity implements OnClickListener{


    private static final int REQUEST_READ_CONTACTS = 1001;
    // UI references.
    private AutoCompleteTextView emailView;
    private View mProgressView;
    private View mLoginFormView;
    private String endpoint;
    private Validator validator;
    private Button mEmailSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        setupActionBar();

        emailView = findViewById(R.id.email);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        endpoint = Endpoint.getInstance(getApplicationContext()).getEndpoint();
        validator = Validator.getInstance(getApplicationContext());

        mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(this);

    }


    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }


    private void attemptPasswordResetRequest() {

        // Reset errors.
        emailView.setError(null);

        // Store values at the time of the login attempt.
        String email = emailView.getText().toString();

        validator.reset();

        validator.validateEmail(email, emailView);


        if (validator.isOkay()) {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            tryResetPassword(email);
        }
        else {
            Toast.makeText(getApplicationContext(), "Fix the errors and try again", Toast.LENGTH_LONG).show();

        }
    }

    private void tryResetPassword(String email) {

        final Context context = getApplicationContext();
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(endpoint+"/forgotPassword/" + URLEncoder.encode(email))
                    .get().addHeader("Content-Type", "application/json")
                    .addHeader("cache-control", "no-cache")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Unable to request for reset", Toast.LENGTH_LONG).show();
                            showProgress(false);
                        }
                    });
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ResponseBody  body = response.body();
                            try {
                                JSONObject jsonObject = new JSONObject(body.string());
                                String msg = jsonObject.getString("msg");
                                if (jsonObject.getBoolean("success")){
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(PasswordResetActivity.this, LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }
                                else {
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                                    showProgress(false);
                                }
                            } catch (Exception e) {
                                Toast.makeText(context, "Something went wrong, please report to us!", Toast.LENGTH_LONG).show();
                                showProgress(false);
                            }

                        }
                    });
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

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        attemptPasswordResetRequest();
    }
}

