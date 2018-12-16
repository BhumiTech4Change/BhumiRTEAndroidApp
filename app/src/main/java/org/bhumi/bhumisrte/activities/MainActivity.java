package org.bhumi.bhumisrte.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import org.bhumi.bhumisrte.R;

import butterknife.BindView;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.parentname) private EditText parentNameView;
    @BindView(R.id.phone) private EditText phoneView;
    @BindView(R.id.childname) private EditText childNameView;
    @BindView(R.id.pincode) private EditText pinCodeView;
    @BindView(R.id.dateofbirth) private EditText dateOfBirthView;
    @BindView(R.id.comment) private EditText commentView;


    private String parentName, phone, childName, pinCode, dateOfBirth, comment;

    private Boolean cancel;
    private View focusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        extractValues();
        validateInputFields();
        sendData();
    }

    private void sendData() {
        // Send the data
    }

    private void extractValues() {
        parentName = parentNameView.getText().toString();
        childName = childNameView.getText().toString();
        phone = phoneView.getText().toString();
        pinCode = pinCodeView.getText().toString();
        dateOfBirth = dateOfBirthView.getText().toString();
        comment = commentView.getText().toString();
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



}
