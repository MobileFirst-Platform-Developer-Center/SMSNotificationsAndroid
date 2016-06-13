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

import android.app.Application;
import android.util.Log;

import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPush;
import com.worklight.wlclient.api.WLClient;

public class PushNotificationsApplication extends Application {

    private static final String TAG = "Application";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize the MobileFirst SDK. This needs to happen just once.
        WLClient.createInstance(this);

        // Initialize MobileFirst Push SDK. This needs to happen just once.
        MFPPush.getInstance().initialize(this);

        // Initialize challenge handler
        UserLoginChallengeHandler.createAndRegister();

        Log.i(TAG, "Push has been initialized in the PushNotificationsApplication.class.");

    }
}
