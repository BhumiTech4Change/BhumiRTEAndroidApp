package org.bhumi.bhumisrte.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.bhumi.bhumisrte.R;
import org.bhumi.bhumisrte.config.Endpoint;
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

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A Signup screen, with email, phone, pin
 */
public class SignupActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private static final int REQUEST_READ_CONTACTS = 0;

     private final String TAG = "SIGNUP";
    // UI references.
    AutoCompleteTextView emailView;
    EditText passwordView;
    EditText passwordVerifyView;
    RelativeLayout relativeLayout;
    View progressView;
    View loginFormView;
    EditText phoneView;
    EditText pinCodeView;
    private View focusView;

    private String email;
    private String password;
    private String passwordVerify;
    private String phone;
    private String pinCode;
    private Boolean cancel;
    private String endpoint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        endpoint = Endpoint.getInstance().getEndpoint();

        emailView = findViewById(R.id.email);
        relativeLayout = findViewById(R.id.relativeLayout);
        passwordView = findViewById(R.id.password);
        passwordVerifyView = findViewById(R.id.passwordVerify);
        progressView = findViewById(R.id.signup_progress);
        loginFormView = findViewById(R.id.login_form);
        phoneView = findViewById(R.id.phone);
        pinCodeView = findViewById(R.id.pin);
        // Set up the login form.
        populateAutoComplete();

        passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    try {
                        attemptSignup();
                        return true;
                    }
                    catch (Exception e){
                        Toast.makeText(getApplicationContext(), "Unable to Signup", Toast.LENGTH_SHORT);
                    }
                }
                return false;
            }
        });

        Button signUpButton = (Button) findViewById(R.id.sign_up_button);
        signUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    attemptSignup();
                }
                catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Unable to Signup", Toast.LENGTH_SHORT);
                }
            }
        });
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }
        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(emailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptSignup() throws IOException, JSONException {

        // Reset errors.
        emailView.setError(null);
        passwordView.setError(null);
        passwordVerifyView.setError(null);
        pinCodeView.setError(null);
        phoneView.setError(null);

        // Store values at the time of the login attempt.
        email = emailView.getText().toString();
        password = passwordView.getText().toString();
        passwordVerify = passwordVerifyView.getText().toString();
        phone = phoneView.getText().toString();
        pinCode = pinCodeView.getText().toString();

       cancel = false;
        focusView = null;

        // Check for a valid password, if the user entered one.

        validatePhone();
        validatePin();
        validateEmail();
        validatePassword();

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user signup attempt.
            showProgress(true);
            signUp();
        }
    }

    private void signUp() throws IOException, JSONException {
        String mEmail = URLEncoder.encode(email, "UTF-8").replace("+","%20");
        String mPhone = URLEncoder.encode(phone, "UTF-8").replace("+","%20");
        String mPin = URLEncoder.encode(pinCode, "UTF-8").replace("+","%20");
        String mPassword = URLEncoder.encode(password, "UTF-8").replace("+","%20");
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "email="+mEmail+"&password="+mPassword+"&pin="+mPin+"&phone="+mPhone);
        Request request = new Request.Builder()
                .url(endpoint+"/signup/")
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("cache-control", "no-cache")
                .build();

        final Context context = getApplicationContext();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Failed to signup!", Toast.LENGTH_SHORT).show();
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
                    e.printStackTrace();
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

    private void validatePhone() {
        if (TextUtils.isEmpty(phone)) {
            phoneView.setError(getString(R.string.error_field_required));
            focusView = phoneView;
            cancel = true;
        }
        if (phone.length() != 10) {
            phoneView.setError("Enter 10 digit phone number");
            focusView = phoneView;
            cancel = true;
        }
    }

    private void validatePin() {
        if (TextUtils.isEmpty(pinCode)) {
            pinCodeView.setError(getString(R.string.error_field_required));
            focusView = pinCodeView;
            cancel = true;
        }

        if (pinCode.length() != 6) {
            phoneView.setError("Enter 6 digit pin code");
            focusView = pinCodeView;
            cancel = true;
        }
    }

    private void validateEmail() {
        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
            focusView = emailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            emailView.setError(getString(R.string.error_invalid_email));
            focusView = emailView;
            cancel = true;
        }
    }

    private void validatePassword() {

        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.error_field_required));
            focusView = passwordView;
            cancel = true;
        }
        else if (!isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_short_password));
            focusView = passwordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(passwordVerify)) {
            passwordVerifyView.setError(getString(R.string.error_field_required));
            focusView = passwordVerifyView;
            cancel = true;
        }
        else if (!isPasswordValid(passwordVerify)) {
            passwordView.setError(getString(R.string.error_short_password));
            focusView = passwordView;
            cancel = true;
        }


        if (!password.equals(passwordVerify)) {
            passwordVerifyView.setError(getString(R.string.error_password_doesnt_match));
            focusView = passwordVerifyView;
            cancel = true;
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 7;
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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(SignupActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        emailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }
}

