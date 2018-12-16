package org.bhumi.bhumisrte.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.bhumi.bhumisrte.R;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ProgressBar progressView;
    private EditText parentNameView;
    private EditText phoneView;
    private EditText childNameView;
    private EditText pinCodeView;
    private  EditText dateOfBirthView;
    private EditText commentView;
    private  Button submit;
    private String parentName, phone, childName, pinCode, dateOfBirth, comment, email, token, certificates;

    private CheckBox incomeView, communityView, birthView, addressView;

    private Boolean cancel = false;
    private View focusView;
    private String SHARED_PREFS_NAME = "user";
    private RelativeLayout relativeLayout;
    private View loginFormView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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


        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(this, LoginActivity.class));
        }
        submit.setOnClickListener(this);
    }

    private void sendData() throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();
        String mEmail = URLEncoder.encode(email, "UTF-8");
        String mPhone = URLEncoder.encode(phone, "UTF-8");
        String mParentName = URLEncoder.encode(parentName, "UTF-8");
        String mChildName = URLEncoder.encode(childName, "UTF-8");
        String mDateOfBirth = URLEncoder.encode(dateOfBirth, "UTF-8");
        String mComment = URLEncoder.encode(comment, "UTF-8");
        String mPinCode = URLEncoder.encode(pinCode, "UTF-8");
        String mToken = URLEncoder.encode(token, "UTF-8");
        String mCertificates = URLEncoder.encode(certificates, "UTF-8");
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        showProgress(true);
        RequestBody body = RequestBody.create(mediaType, "email="+mEmail+
                "&phone="+mPhone+"&parentName="+mParentName+"&childName="+mChildName+"+" +
                "&dateOfBirth="+mDateOfBirth+"&pinCode="+mPinCode+"&certificate="+mCertificates+"&comment="+mComment);

        Request request = new Request.Builder()
                .url("https://bhumirte.herokuapp.com/form")
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
                                "Failed to submit !", Snackbar.LENGTH_INDEFINITE);
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


                    if (jsonObject.getBoolean("success")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showProgress(false);
                                clearFields();
                                Snackbar snackbar = Snackbar.make(relativeLayout, "Succuessfully Submitted!", Snackbar.LENGTH_INDEFINITE);
                                snackbar.show();
                            }
                        });

                    }else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showProgress(false);
                                Snackbar snackbar = Snackbar.make(relativeLayout, "Failed to submit!", Snackbar.LENGTH_INDEFINITE);
                                snackbar.show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

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

    private void extractValues() {
        parentName = parentNameView.getText().toString();
        childName = childNameView.getText().toString();
        phone = phoneView.getText().toString();
        pinCode = pinCodeView.getText().toString();
        dateOfBirth = dateOfBirthView.getText().toString();
        comment = commentView.getText().toString();
        certificates = "";
        if (incomeView.isChecked()) certificates +="Income Certificate, ";
        if (communityView.isChecked()) certificates += "Community Certificate, ";
        if (birthView.isChecked()) certificates += "Birth Certificate, ";
        if (addressView.isChecked()) certificates += "Address Proof.";

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME,Context.MODE_PRIVATE);
        email = sharedPreferences.getString("email", null);
        token = sharedPreferences.getString("token", null);
    }
    private void validateInputFields() {
        validateInputField(parentName, parentNameView);
        validateInputField(childName, childNameView);
        validateInputField(phone, phoneView);
        validateInputField(pinCode, pinCodeView);
        validateInputField(dateOfBirth, dateOfBirthView);
        // CommentView is optional
    }



    private void validateInputField(String value, View editTextView) {
        if (TextUtils.isEmpty(value)){
            parentNameView.setError(getString(R.string.error_field_required));
            focusView = editTextView;
            cancel = true;
        }
    }


    @Override
    public void onClick(View v) {
        extractValues();
        validateInputFields();
        if (cancel) {

        }
        else {
            try {
                sendData();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}
