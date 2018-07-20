package com.webmodule.offlinemodule.admin;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import com.webmodule.offlinemodule.Constants;
import com.webmodule.offlinemodule.R;
import com.webmodule.offlinemodule.activity.FullscreenActivity;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class AdminAuthDialog extends DialogFragment {
    private TextInputLayout passwordInput;
    private EditText password;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.auth_dialog, container);
        password = v.findViewById(R.id.password_edit_text_login);
        passwordInput = v.findViewById(R.id.password_input_login);
        v.findViewById(R.id.login_button).setOnClickListener(this::showAdminSettings);
        setUpValidator();
        return v;
    }

    private void showAdminSettings(View v1) {
        if (TextUtils.isEmpty(passwordInput.getError()) && !TextUtils.isEmpty(password.getText().toString())) {
            if (password.getText().toString().equals(Constants.DEV_ADMIN_PASSWORD)) {
                ((FullscreenActivity) getActivity()).showAdminMenu();
                dismiss();
            } else {
                changeInputState(getString(R.string.incorrect_password));
                showAdminSettings(v1);
            }
        } else
            password.startAnimation(AnimationUtils.loadAnimation(password.getContext(), R.anim.shake_view_animation));
    }

    private void setUpValidator() {
        password.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override public void afterTextChanged(Editable editable) {
                validate(editable);
            }
        });
    }

    private void validate(Editable editable) {
        Observable.just(editable.toString())
                .observeOn(AndroidSchedulers.mainThread())
                .map(text -> TextUtils.isEmpty(text) ? getString(R.string.empty_password)
                        : (text.length() <= 5) ? getString(R.string.no_fill_password) : "")
                .subscribe(this::changeInputState, throwable -> {}, () -> {});
    }

    public void changeInputState(String message) {
        if (TextUtils.isEmpty(message)) {
            passwordInput.setError(null);
            passwordInput.setErrorEnabled(true);
        } else {
            passwordInput.setError(message);
            passwordInput.setErrorEnabled(true);
        }
    }
}
