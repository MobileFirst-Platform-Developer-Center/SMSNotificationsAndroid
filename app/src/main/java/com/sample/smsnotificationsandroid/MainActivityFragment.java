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

package com.sample.smsnotificationsandroid;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPush;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushException;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushNotificationListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPSimplePushNotification;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.List;

public class MainActivityFragment extends Fragment implements View.OnClickListener, MFPPushNotificationListener {

    private static final String TAG = "MainActivityFragment";

    private Context _this;

    // Button references to enable/disable
    private Button unregisterBtn;

    private EditText editPhoneText;
    private MFPPush push = null;

    public MainActivityFragment() {
        // Mandatory empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _this = getActivity();

        // MFPPush is initialized in SMSNotificationsAndroid.class
        push = MFPPush.getInstance();

        // Option for receiving push notifications
        push.listen(this);

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (push != null) {
            push.hold();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (push != null) {
            push.listen(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        editPhoneText = (EditText) view.findViewById(R.id.txtphoneNumber);

        Button registerBtn = (Button) view.findViewById(R.id.btn_register);
        registerBtn.setOnClickListener(this);

        unregisterBtn = (Button) view.findViewById(R.id.btn_unregister);
        unregisterBtn.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch (id) {

            case R.id.btn_register:
                Log.d(TAG, "phonenumbervalue is " + editPhoneText);
                Log.d(TAG, "phone number text is " + editPhoneText.getText().toString());
                JSONObject optionObject = new JSONObject();
                try {
                    optionObject.put("phoneNumber", editPhoneText.getText().toString());

                    MFPPush.getInstance().registerDevice(optionObject, new MFPPushResponseListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            Log.d(TAG,"Registered successfully");
                            enableButtons();
                            showSnackbar("Registered successfully");
                            //showAlertMsg("Success","Registered Successfully");
                        }

                        @Override
                        public void onFailure(MFPPushException e) {
                            showAlertMsg("Error","Failed to register device");
                            Log.d(TAG,"Failed to register device");
                        }
                    });
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
                break;

            case R.id.btn_unregister:
                push.unregisterDevice(new MFPPushResponseListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        showSnackbar("Unregistered successfully");
                        //showAlertMsg("Success","Unregistered successfully");
                        disableButtons();
                        Log.d(TAG, "Unregistered successfully");

                    }

                    @Override
                    public void onFailure(MFPPushException e) {
                        showAlertMsg("Error","Failed to unregister");
                        Log.d(TAG, "Failed to unregister device with error: " + e.toString());
                    }
                });
                break;
            default:
                throw new RuntimeException("Click not handled!!");
        }
    }

    private void enableButtons() {

        Runnable run = new Runnable() {
            public void run() {
                unregisterBtn.setEnabled(true);
            }
        };
        getActivity().runOnUiThread(run);
    }

    private void disableButtons() {

        Runnable run = new Runnable() {
            public void run() {
                unregisterBtn.setEnabled(false);
            }
        };
        getActivity().runOnUiThread(run);
    }

    private void showSnackbar(String message) {
        //noinspection ConstantConditions
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }

    public void showAlertMsg(final String title, final String msg) {

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

    @Override
    public void onReceive(MFPSimplePushNotification mfpSimplePushNotification) {
        Log.i("Push Notifications", mfpSimplePushNotification.getAlert());

        String alert = "Alert: " + mfpSimplePushNotification.getAlert();
        String alertID = "ID: " + mfpSimplePushNotification.getId();
        String alertPayload = "Payload: " + mfpSimplePushNotification.getPayload();

        // Show the received notification in an AlertDialog
        showAlertMsg("Push Notifications", alert + "\n" + alertID + "\n" + alertPayload);
    }
}
