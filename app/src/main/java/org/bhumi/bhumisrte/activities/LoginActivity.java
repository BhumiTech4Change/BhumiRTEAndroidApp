package org.bhumi.bhumisrte.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.bhumi.bhumisrte.R;
import org.bhumi.bhumisrte.API.User;
import org.bhumi.bhumisrte.API.Validator;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements OnClickListener{

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    // UI references.
    View loginFormView;
    AutoCompleteTextView emailView;
    EditText passwordView;
    View progressView;
    Button signUpButton;
    TextView forgotPasswordButton;
    Button emailSignInButton;
    TextView websiteView;

    // Data variables
    private String email;
    private String password;
    private RelativeLayout relativeLayout;
    private String endpoint;
    private Validator validator;
    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //initialize data
        endpoint = getString(R.string.api_url);

        // Instantiate ui references
        loginFormView = findViewById(R.id.login_form);
        emailView = findViewById(R.id.email);
        signUpButton  = findViewById(R.id.sign_up_button);
        relativeLayout = findViewById(R.id.relativeLayout);
        passwordView = findViewById(R.id.password);
        progressView = findViewById(R.id.login_progress);
        forgotPasswordButton = findViewById(R.id.forgot_password_button);
        emailSignInButton = findViewById(R.id.email_sign_in_button);
        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);
        websiteView = findViewById(R.id.websiteTextView);
        passwordView = findViewById(R.id.password);
        validator = Validator.getInstance(getApplicationContext());
        user = User.getCurrentUser(getApplicationContext());
        signUpButton.setOnClickListener(this);
        websiteView.setOnClickListener(this);

        emailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    attemptLogin();
                }
                catch (Exception ex){
                    Toast.makeText(getApplicationContext(), "Unable to signin", Toast.LENGTH_SHORT).show();
                }
            }
        });
        forgotPasswordButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.websiteTextView:
                String url = "http://rte25.bhumi.ngo/";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
                 break;
            case R.id.sign_up_button:
                startActivity(new Intent(getApplicationContext(), SignupActivity.class));
                break;
            case R.id.forgot_password_button:
                startActivity(new Intent(getApplicationContext(), PasswordResetActivity.class));
                break;
        }
    }



    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() throws IOException, JSONException{


        // Reset errors.
        emailView.setError(null);
        passwordView.setError(null);
        validator.reset();

        // Store values at the time of the login attempt.
        email = emailView.getText().toString();
        password = passwordView.getText().toString();

        validator.validatePassword(password, passwordView);
        validator.validateEmail(email, emailView);

        if (validator.isOkay()) {
            showProgress(true);
            signIn();
        }
    }

    private void signIn() throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();
        final Context context = getApplicationContext();

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "email="+validator.encode(email)+
                "&password="+validator.encode(password));
        Request request = new Request.Builder()
                .url(endpoint+"/signin/")
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
                        Toast.makeText(context, "Failed to signin!", Toast.LENGTH_LONG).show();
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
                        // Successfully logged in
                        String token = jsonObject.getString("token");
                        user.login(email, token);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(context, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                                showProgress(false);
                            }
                        });
                    }
                } catch (JSONException e) {
                    Toast.makeText(context, "Something went wrong, please report to us", Toast.LENGTH_LONG).show();
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

