package org.bhumi.bhumisrte.config;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import org.bhumi.bhumisrte.R;

import java.net.URLEncoder;

public class Validator {
    private static Validator instance = new Validator();
    private static Context context;
    private boolean cancel = true;

    public static Validator getInstance(Context mContext) {
        context = mContext;
        return instance;
    }

    // Validation logic
    public void validatePhone(String phoneNumber, EditText phoneView) {
        if (TextUtils.isEmpty(phoneNumber)) {
            phoneView.setError(context.getString(R.string.error_field_required));
            cancel = false;
        }
        else if (phoneNumber.length() != 10) {
            phoneView.setError(context.getString(R.string.error_small_phone_number));
            cancel = false;
        }
    }

    public void validatePin(String pinCode, EditText pinCodeView) {
        if (TextUtils.isEmpty(pinCode)) {
            pinCodeView.setError(context.getString(R.string.error_field_required));
            cancel = false;
        }
        else if (pinCode.length() != 6) {
            pinCodeView.setError(context.getString(R.string.error_small_pin_code));
            cancel = false;
        }
    }

    public void validateEmail(String email, EditText emailView) {
        if (TextUtils.isEmpty(email)) {
            emailView.setError(context.getString(R.string.error_field_required));
            cancel = false;
        }
        else if (!isEmailInvalid(email)) {
            emailView.setError(context.getString(R.string.error_invalid_email));
            cancel = false;
        }
    }

    public void validatePassword(String password, EditText passwordView) {
        if (TextUtils.isEmpty(password)) {
            passwordView.setError(context.getString(R.string.error_field_required));
            cancel = false;
        }
        else if (isPasswordInvalid(password)) {
            passwordView.setError(context.getString(R.string.error_short_password));
            cancel = false;
        }
    }

    public void checkPasswordsMatch(String password, String passwordVerify, EditText passwordView) {
        if (password == passwordVerify) {
            passwordView.setError(context.getString(R.string.error_password_doesnt_match));
            cancel = false;
        }
    }

    public void validateText(String text, EditText editText) {
        if (TextUtils.isEmpty(text)){
            editText.setError(context.getString(R.string.error_field_required));
            cancel = false;
        }
    }

    public void reset() {
        this.cancel = true;
    }

    public boolean isOkay() {
        return cancel;
    }

    private boolean isPasswordInvalid(String password) {
        return password.length() < 8;
    }

    private boolean isEmailInvalid(String email) {
        return email.contains("@");
    }

    public String encode(String text) {
        return URLEncoder.encode(text).replace("+", "%20");
    }
}
