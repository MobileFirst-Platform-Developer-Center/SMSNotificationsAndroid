IBM MobileFirst Platform Foundation
===
## PushNotificationsAndroid
A sample application demonstrating use of push notifications in Android applications.

### Tutorials
https://mobilefirstplatform.ibmcloud.com/tutorials/en/foundation/8.0/notifications/

### Usage

1. Import the project to Android Studio.
2. From a **Command-line** window, navigate to the project's root folder and run the command: `mfpdev app register`.
3. In the MobileFirst console:
	* Under **Applications** → **PushNotificationsAndroid** → **Security** → **Map scope elements to security checks**, add a mapping for `push.mobileclient`.
	* Under **Applications** → **PushNotificationsAndroid** → **Push** → **Push Settings**, add Google Clout Messaging (GMC) Server API Key and Sender ID.
5. Run the app by clicking the **Run** button.

**[Sending a notification](https://mobilefirstplatform.ibmcloud.com/tutorials/en/foundation/8.0/notifications/sending-push-notifications):**

* Tag notification
    * Use the **MobileFirst Operations Console → [your application] → Push → Send Push tab**.
* Authenticated notification:
    * Deploy the [**UserLogin** sample Security Check](https://mobilefirstplatform.ibmcloud.com/tutorials/en/foundation/8.0/authentication-and-security/user-authentication/security-check).
    * In **MobileFirst Operations Console → [your application] → Security tab**, map the **push.mobileclient** scope to the **UserLogin** Security Check.
    * Follow the instructions for [REST APIs](https://mobilefirstplatform.ibmcloud.com/tutorials/en/foundation/8.0/notifications/sending-push-notifications#rest-apis) to send the notification.

**Notes:**

* The GCM Server Key and senderId values must be configured via the MobileFirst Operations Console.

### Supported Levels
IBM MobileFirst Platform Foundation 8.0

### License
Copyright 2015 IBM Corp.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
att
http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
