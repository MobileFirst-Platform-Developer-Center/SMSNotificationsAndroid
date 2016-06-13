/*
 * Copyright 2016 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sample.pushnotificationsandroid;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivityFragment extends Fragment implements OnClickListener {

    private static final String TAG = "LoginActivityFragment";

    private EditText userNameET;
    private EditText passwordET;
    private TextView errorLabelTV;
    private TextView remainingAttemptsTV;

    private String error;
    private Integer remainingAttempts;

    private BroadcastReceiver loginErrorReceiver, loginRequiredReceiver, loginSuccessReceiver;

    private FragmentActivity _this;

    private final String DEBUG_NAME = "LoginActivityFragment";



    public LoginActivityFragment() {
        // Mandatory empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _this = getActivity();
        remainingAttempts = 3;

    }

    @Override
    public void onStart() {
        Log.d(DEBUG_NAME, "onStart");
        super.onStart();
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(loginRequiredReceiver, new IntentFilter(Constants.ACTION_LOGIN_REQUIRED));
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(loginErrorReceiver, new IntentFilter(Constants.ACTION_LOGIN_FAILURE));
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(loginSuccessReceiver, new IntentFilter(Constants.ACTION_LOGIN_SUCCESS));
    }

    @Override
    public void onStop() {
        Log.d(DEBUG_NAME,"onStop");
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(loginErrorReceiver);
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(loginRequiredReceiver);
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(loginSuccessReceiver);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (remainingAttempts != null) {
            remainingAttemptsTV.setText(String.valueOf(remainingAttempts));
        }

        if (error != null) {
            errorLabelTV.setText(error);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        userNameET = (EditText) view.findViewById(R.id.et_username);
        passwordET = (EditText) view.findViewById(R.id.et_password);
        errorLabelTV = (TextView) view.findViewById(R.id.tv_error_message);
        remainingAttemptsTV = (TextView) view.findViewById(R.id.tv_remaining_attempts);

        Button loginBtn = (Button) view.findViewById(R.id.btn_login);
        loginBtn.setOnClickListener(this);

        Button cancelBtn = (Button) view.findViewById(R.id.btn_cancel);
        cancelBtn.setOnClickListener(this);

        //Login error receiver
        loginErrorReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(DEBUG_NAME, "loginErrorReceiver");
                errorLabelTV.setText("");
                remainingAttemptsTV.setText("");
                showAlertMsg("Error", intent.getStringExtra("errorMsg"),true);
            }
        };

        //Login required
        loginRequiredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                Log.d(DEBUG_NAME, "loginRequiredReceiver");
                Runnable run = new Runnable() {
                    public void run() {
                        //Set error message:
                        errorLabelTV.setText(intent.getStringExtra("errorMsg"));

                        //Display remaining attempts
                        if(intent.getIntExtra("remainingAttempts",-1) > -1) {
                            remainingAttemptsTV.setText(Integer.toString(intent.getIntExtra("remainingAttempts",-1)));
                        }
                    }
                };
                _this.runOnUiThread(run);


            }
        };

        //Login success
        loginSuccessReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Go back to main screen
                getActivity().finish();
            }
        };

        return view;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            // Login button pressed
            case R.id.btn_login:
                String username = String.valueOf(userNameET.getText());
                String password = String.valueOf(passwordET.getText());

                if (username.isEmpty() || password.isEmpty()) {
                    showAlertMsg("Error", "Username and password are required",false);
                } else {
                    JSONObject credentials = new JSONObject();
                    try {
                        credentials.put("username", username);
                        credentials.put("password", password);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent();
                    intent.setAction(Constants.ACTION_LOGIN);
                    intent.putExtra("credentials", credentials.toString());
                    LocalBroadcastManager.getInstance(_this).sendBroadcast(intent);
                }
                break;
            case R.id.btn_cancel:
                Intent intent = new Intent();
                intent.setAction(Constants.ACTION_LOGIN_CANCEL);
                LocalBroadcastManager.getInstance(_this).sendBroadcast(intent);
                getActivity().finish();
                break;
            default:
                Log.d(TAG, "OnClick not handled");
        }
    }

    public void showAlertMsg(final String title, final String msg, final boolean finish) {

        Runnable run = new Runnable() {
            @Override
            public void run() {
                // Create an AlertDialog Builder, and configure alert
                AlertDialog.Builder builder = new AlertDialog.Builder(_this);
                builder.setTitle(title)
                        .setMessage(msg)
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i(TAG, "Okay was pressed");
                                if(finish){
                                    getActivity().finish();
                                }
                            }
                        });

                // Create the AlertDialog
                AlertDialog dialog = builder.create();

                // Display the dialog
                dialog.show();
            }
        };

        getActivity().runOnUiThread(run);
    }

}
