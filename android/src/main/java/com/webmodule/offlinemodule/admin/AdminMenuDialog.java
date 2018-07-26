package com.webmodule.offlinemodule.admin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.webmodule.offlinemodule.Constants;
import com.webmodule.offlinemodule.R;
import com.webmodule.offlinemodule.activity.FullscreenActivity;
import com.webmodule.offlinemodule.broadcast.FeedBackReceiver;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class AdminMenuDialog extends DialogFragment {
    private TextInputLayout screenIdInput;
    private EditText screenId;
    private Spinner spinner;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.admin_menu_dialog, container);
        screenId = v.findViewById(R.id.slides_id_edit_text);
        screenIdInput = v.findViewById(R.id.slides_id_input);
        spinner = v.findViewById(R.id.printer_connection_spinner);
        v.findViewById(R.id.update_button).setOnClickListener(this::update);
        createSpinner();
        setUpValidators();
        v.findViewById(R.id.confirm_button).setOnClickListener(this::showAdminSettings);
        return v;
    }

    private void createSpinner() {
        String[] array = getDialog().getContext().getResources().getStringArray(R.array.connection_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getDialog().getContext(),
                android.R.layout.simple_spinner_item,
                array);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setPrompt(getString(R.string.connection_type));
        applyPreviousValues(array);
    }

    private void applyPreviousValues(String[] array) {
        try {
            String id = getPreferences().getString(Constants.SCREEN_ID, "");
            screenIdInput.getEditText().setText(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String connection = getPreferences().getString(Constants.PRINT_CONNECTION, "");
            if (connection.equalsIgnoreCase(array[1]))
                spinner.setSelection(1);
            else
                spinner.setSelection(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpValidators() {
        screenId.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override public void afterTextChanged(Editable editable) {
                Observable.just(editable.toString())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(text -> TextUtils.isEmpty(text) ? getString(R.string.empty_new_screen_id) : "")
                        .subscribe(s -> changeInputState(s), throwable -> {}, () -> {});
            }
        });
    }

    public void changeInputState(String message) {
        if (TextUtils.isEmpty(message)) {
            screenIdInput.setError(null);
            screenIdInput.setErrorEnabled(true);
        } else {
            screenIdInput.setError(message);
            screenIdInput.setErrorEnabled(true);
        }
    }

    private void showAdminSettings(View v1) {
        if (!TextUtils.isEmpty(screenIdInput.getError()) && !TextUtils.isEmpty(screenId.getText().toString()))
            screenId.startAnimation(AnimationUtils.loadAnimation(screenId.getContext(), R.anim.shake_view_animation));
        else
            tryLoadNewContent();
    }

    private void tryLoadNewContent() {
        if (isNetworkAvailable()) {
            FullscreenActivity activity = (FullscreenActivity) getActivity();
            if (activity != null)
                ((FullscreenActivity) getActivity()).loadNewContent(screenId.getText().toString(), spinner.getSelectedItemPosition());
            dismiss();
        } else
            Toast.makeText(getContext(), getString(R.string.internet_connection_error), Toast.LENGTH_SHORT).show();
    }

    private void update(View view) {
        if (isNetworkAvailable())
            getDialog().getContext().sendBroadcast(new Intent(FeedBackReceiver.UPDATE_ACTION));
        else
            Toast.makeText(getContext(), getString(R.string.internet_connection_error), Toast.LENGTH_SHORT).show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getDialog().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        } else
            return false;
    }

    @Override public void onStop() {
        saveSelectedValues();
        super.onStop();
    }

    private void saveSelectedValues() {
        try {
            getPreferences().edit().putString(Constants.SCREEN_ID, screenIdInput.getEditText().getText().toString()).apply();
            getPreferences().edit().putString(Constants.PRINT_CONNECTION,
                    getDialog().getContext().getResources().getStringArray(R.array.connection_types)[spinner.getSelectedItemPosition()]).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SharedPreferences getPreferences() {
        return getDialog().getContext().getSharedPreferences(Constants.LOCAL_SETTINGS, Context.MODE_PRIVATE);
    }
}
