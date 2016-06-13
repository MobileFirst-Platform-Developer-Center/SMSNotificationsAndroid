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
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
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

    private BroadcastReceiver loginSuccessReceiver, loginRequiredReceiver, loginFailureReceiver;

    private Context _this;

    // Button references to enable/disable
    private Button subscribeBtn;
    private Button getSubscriptionBtn;
    private Button unsubscribeBtn;
    private Button unregisterBtn;
    private Text txtPhoneNumber;

    private String[] tags;
    private String phoneNumber;

    private EditText editPhoneText;
    private MFPPush push = null;

    public MainActivityFragment() {
        // Mandatory empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _this = getActivity();

        // MFPPush is initialized in PushNotificationsApplication.class
        push = MFPPush.getInstance();


        // Option for receiving push notifications
        push.listen(this);

/*
        // Option for receiving push notification
        push.listen(new MFPPushNotificationListener() {
            @Override
            public void onReceive(MFPSimplePushNotification mfpSimplePushNotification) {
                showSnackbar(mfpSimplePushNotification.getAlert());
            }
        });
*/

        //Handle auto-login success
        loginSuccessReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Go back to main area
                Intent relaunchMain = new Intent(_this, MainActivity.class);
                getActivity().finish();
                _this.startActivity(relaunchMain);
            }
        };

        // Handle challenge broadcast
        loginRequiredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Open login screen
                Intent loginIntent = new Intent(_this, LoginActivity.class);
                _this.startActivity(loginIntent);
            }
        };

        // Handle challenge broadcast
        loginFailureReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                showAlertMsg("Error", intent.getStringExtra("errorMsg"));
            }
        };

    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(_this).registerReceiver(loginSuccessReceiver, new IntentFilter(Constants.ACTION_LOGIN_SUCCESS));
        LocalBroadcastManager.getInstance(_this).registerReceiver(loginRequiredReceiver, new IntentFilter(Constants.ACTION_LOGIN_REQUIRED));
        LocalBroadcastManager.getInstance(_this).registerReceiver(loginFailureReceiver, new IntentFilter(Constants.ACTION_LOGIN_FAILURE));
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(_this).unregisterReceiver(loginSuccessReceiver);
        LocalBroadcastManager.getInstance(_this).unregisterReceiver(loginRequiredReceiver);
        LocalBroadcastManager.getInstance(_this).unregisterReceiver(loginFailureReceiver);
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

        Button pushSupportedBtn = (Button) view.findViewById(R.id.btn_push_supported);
        pushSupportedBtn.setOnClickListener(this);

        editPhoneText = (EditText) view.findViewById(R.id.txtphoneNumber);

        Button registerBtn = (Button) view.findViewById(R.id.btn_register);
        registerBtn.setOnClickListener(this);

       Button getTagsBtn = (Button) view.findViewById(R.id.btn_get_tags);
        getTagsBtn.setOnClickListener(this);

        subscribeBtn = (Button) view.findViewById(R.id.btn_subscribe);
        subscribeBtn.setOnClickListener(this);

        getSubscriptionBtn = (Button) view.findViewById(R.id.btn_get_subscriptions);
        getSubscriptionBtn.setOnClickListener(this);

        unsubscribeBtn = (Button) view.findViewById(R.id.btn_unsubscribe);
        unsubscribeBtn.setOnClickListener(this);

        unregisterBtn = (Button) view.findViewById(R.id.btn_unregister);
        unregisterBtn.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch (id) {
            case R.id.btn_push_supported:
                if (push.isPushSupported()) {
                    showSnackbar("Push is supported");
                } else {
                    showSnackbar("Push is not supported");
                }
                break;
            case R.id.btn_register:
                Log.d(TAG, "phonenumbervalue is " + editPhoneText);
                Log.d(TAG, "phone number text is " + editPhoneText.getText().toString());
                JSONObject optionObject = new JSONObject();
                try {
                    optionObject.put("phoneNumber", editPhoneText.getText().toString());
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
                push.registerDevice(new MFPPushResponseListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                enableButtons();
                                showSnackbar("Registered Successfully");
                            }
                        });
                    }

                    @Override
                    public void onFailure(final MFPPushException e) {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                showSnackbar("Failed to register device");
                                Log.d(TAG, "Failed to register device with error: " + e.toString());
                            }
                        });

                    }
                }, optionObject);
                break;
            case R.id.btn_get_tags:
                push.getTags(new MFPPushResponseListener<List<String>>() {
                    @Override
                    public void onSuccess(List<String> strings) {

                        if (strings.isEmpty()) {
                            tags = new String[0];
                            showAlertMsg("Tags", "There are no available tags");
                        } else {
                            tags = new String[strings.size()];

                            for (int i = 0; i < strings.size(); i++) {
                                tags[i] = strings.get(i);
                            }

                            showAlertMsg("Tags", strings.toString());
                        }
                    }

                    @Override
                    public void onFailure(MFPPushException e) {
                        showSnackbar("Error: " + e.getErrorMessage());
                        Log.d(TAG, "Error: " + e + " Doc URL: " + e.getDocUrl() + " Error code: " + e.getErrorCode());
                    }
                });
                break;
            case R.id.btn_subscribe:
                if (tags != null && tags.length > 0) {
                    if (!tags[0].equals("Push.ALL")) {
                        push.subscribe(tags, new MFPPushResponseListener<String[]>() {
                            @Override
                            public void onSuccess(String[] strings) {
                                showSnackbar("Subscribed successfully");
                            }

                            @Override
                            public void onFailure(MFPPushException e) {
                                showSnackbar("Failed to subscribe");
                                Log.d(TAG, "Failed to subscribe with error: " + e.toString());
                            }
                        });
                    } else {
                        showAlertMsg("Push Notifications", "There are no tags to subscribe to \n\n Try clicking on the \"Get Tags\" button");
                    }
                } else {
                    showAlertMsg("Push Notifications", "There are no tags to subscribe to \n\n Try clicking on the \"Get Tags\" button");
                }
                break;
            case R.id.btn_get_subscriptions:
                push.getSubscriptions(new MFPPushResponseListener<List<String>>() {
                    @Override
                    public void onSuccess(List<String> strings) {
                        tags = new String[strings.size()];

                        for (int i = 0; i < strings.size(); i++) {
                            tags[i] = strings.get(i);
                        }
                        showAlertMsg("Push Notification", strings.toString());
                    }

                    @Override
                    public void onFailure(MFPPushException e) {
                        showAlertMsg("Push Notification", e.getErrorMessage());
                        Log.d(TAG, "Failed to subscribe with error: " + e.toString());
                    }
                });
                break;
            case R.id.btn_unsubscribe:
                push.unsubscribe(tags, new MFPPushResponseListener<String[]>() {
                    @Override
                    public void onSuccess(String[] strings) {
                        updateTags(null);
                        showSnackbar("Unsubscribed successfully");
                    }

                    @Override
                    public void onFailure(MFPPushException e) {
                        showSnackbar("Failed to unsubscribe");
                        Log.d(TAG, "Failed to unsubscribe with error: " + e.toString());
                    }
                });
                break;
            case R.id.btn_unregister:
                push.unregisterDevice(new MFPPushResponseListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        disableButtons();
                        showSnackbar("Unregistered successfully");
                    }

                    @Override
                    public void onFailure(MFPPushException e) {
                        showSnackbar("Failed to unregister");
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
                subscribeBtn.setEnabled(true);
                getSubscriptionBtn.setEnabled(true);
                unsubscribeBtn.setEnabled(true);
                unregisterBtn.setEnabled(true);
            }
        };
        getActivity().runOnUiThread(run);
    }

    private void disableButtons() {

        Runnable run = new Runnable() {
            public void run() {
                subscribeBtn.setEnabled(false);
                getSubscriptionBtn.setEnabled(false);
                unsubscribeBtn.setEnabled(false);
                unregisterBtn.setEnabled(false);
            }
        };
        getActivity().runOnUiThread(run);
    }

    private void showSnackbar(String message) {
        //noinspection ConstantConditions
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }

    private void updateTags(List<String> strings) {
        if (strings != null && strings.size() > 0) {
            tags = new String[strings.size()];

            for (int i = 0; i < strings.size(); i++) {
                tags[i] = strings.get(i);
            }
        } else {
            tags = new String[0];
        }
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
