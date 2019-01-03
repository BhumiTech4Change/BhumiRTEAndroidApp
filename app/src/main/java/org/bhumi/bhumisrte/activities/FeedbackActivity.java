package org.bhumi.bhumisrte.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.bhumi.bhumisrte.R;
import org.bhumi.bhumisrte.config.Endpoint;
import org.bhumi.bhumisrte.config.User;
import org.bhumi.bhumisrte.config.Validator;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FeedbackActivity extends AppCompatActivity implements View.OnClickListener {

    EditText feedbackTextView;
    Button buttonView;
    ProgressBar progressView;
    View feedbackFormView;

    String endpoint;
    User user;
    Validator validator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        feedbackTextView = findViewById(R.id.feedback_edittext);
        buttonView = findViewById(R.id.feedback_submit_button);
        progressView = findViewById(R.id.progress);
        feedbackFormView = findViewById(R.id.feedback_form);


        endpoint = Endpoint.getInstance(getApplicationContext()).getEndpoint();
        user = User.getCurrentUser(getApplicationContext());
        validator = Validator.getInstance(getApplicationContext());

        buttonView.setOnClickListener(this);

    }

    @Override
    public void onClick(View v){
        final String feedbackText = feedbackTextView.getText().toString();
        validator.reset();
        validator.validateText(feedbackText, feedbackTextView);
        if (validator.isOkay()) {
            showProgress(true);
            String email = user.getEmail();
            final Context context = getApplicationContext();
            try {
                OkHttpClient client = new OkHttpClient();
                String mEmail = validator.encode(email);
                String mFeedback = validator.encode(feedbackText);
                Request request = new Request.Builder()
                        .url(endpoint + "/getFeedback/" + mEmail + "/" + mFeedback)
                        .get().addHeader("Content-Type", "application/json")
                        .addHeader("cache-control", "no-cache")
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Unable to send feedback, try again!", Toast.LENGTH_LONG).show();
                                showProgress(false);
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            final String msg = jsonObject.getString("msg");
                            if (jsonObject.getBoolean("success")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                                        feedbackTextView.setText("");
                                        showProgress(false);
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                                        showProgress(false);
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            Toast.makeText(context, "Unable to send feedback, try again!", Toast.LENGTH_LONG).show();
                            showProgress(false);
                        }
                    }
                });


            } catch (Exception e) {
                Toast.makeText(context, "Unable to send feedback, try again!", Toast.LENGTH_LONG).show();
                showProgress(false);
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "Please fix the errors and try again", Toast.LENGTH_LONG).show();
        }
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

            feedbackFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            feedbackFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    feedbackFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            feedbackFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
