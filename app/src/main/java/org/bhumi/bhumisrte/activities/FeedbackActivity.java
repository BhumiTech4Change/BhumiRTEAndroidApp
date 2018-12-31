package org.bhumi.bhumisrte.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.bhumi.bhumisrte.R;
import org.bhumi.bhumisrte.config.Endpoint;
import org.json.JSONException;
import org.json.JSONObject;;
import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FeedbackActivity extends AppCompatActivity implements View.OnClickListener {

    EditText feedbackTextVeiw;
    Button buttonView;
    private String SHARED_PREFS_NAME = "user";
    String endpoint;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        feedbackTextVeiw = findViewById(R.id.feedback_edittext);
        buttonView = findViewById(R.id.feedback_submit_button);
        progressBar = findViewById(R.id.progress);
        endpoint = Endpoint.getInstance().getEndpoint();
        buttonView.setOnClickListener(this);

    }

    @Override
    public void onClick(View v){
        final String feedbackText = feedbackTextVeiw.getText().toString();

        if (TextUtils.isEmpty(feedbackText)) {
            feedbackTextVeiw.setError("Please type something to send");
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        String email = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE).getString("email","");
        final Context context = getApplicationContext();
        buttonView.setEnabled(false);
        try {
        OkHttpClient client = new OkHttpClient();
        String mEmail = URLEncoder.encode(email,"UTF-8").replace("+","%20");
        String mFeedback = URLEncoder.encode(feedbackText,"UTF-8").replace("+","%20");
        Request request = new Request.Builder()
                .url(endpoint+"/getFeedback/" + mEmail +"/"+mFeedback)
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
                        progressBar.setVisibility(View.GONE);
                        buttonView.setEnabled(true);
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
                                feedbackTextVeiw.setText("");
                                progressBar.setVisibility(View.GONE);
                                buttonView.setEnabled(true);
                            }
                        });
                    }
                    else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                                buttonView.setEnabled(true);
                            }
                        });
                    }
                } catch (JSONException e) {
                    Toast.makeText(context, "Unable to send feedback, try again!", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    buttonView.setEnabled(true);
                }
            }
        });

    }
    catch (Exception e) {
            Toast.makeText(context, "Unable to send feedback, try again!", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            buttonView.setEnabled(true);
    }
}}
