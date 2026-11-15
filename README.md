# capacitor-fcm-fanout

One Firebase Cloud Messaging service for Capacitor 8 apps on Android that hands every FCM token and message to
[Airship](https://www.airship.com/) and [Intercom](https://www.intercom.com/), so the two can share one app.

## Why

On Android, Firebase delivers a new token or a message to **one** `FirebaseMessagingService` in the app. The Airship
Capacitor plugin declares one, `@capacitor-firebase/messaging` declares one, and Intercom's documentation tells an app
with its own service to pass tokens and messages on by hand. With several declared, Firebase picks one and the other SDKs
never hear of a token: Intercom (or Airship) silently stops receiving pushes.

This plugin is that one service, written once:

- **a new token** goes to every SDK in the app (`IntercomPushClient.sendTokenToIntercom`,
  `AirshipFirebaseIntegration.processNewToken`);
- **a message** goes to the SDK that sent it, and to no other: Intercom when `IntercomPushClient.isIntercomPush` says so,
  otherwise Airship when `AirshipFirebaseIntegration.isAirshipPush` says so (`processMessageSync`). A message neither
  claims is left alone; a notification message is still shown by Firebase itself while the app is in the background.
- an SDK that throws is logged (tag `FcmFanout`) and does not stop the others.

The plugin ships neither SDK. It is compiled against them and uses each one only when the app has it, so it adds no
second copy of either and works with only one of them installed.

On **iOS** the plugin routes nothing, because nothing needs routing: APNs hands its token to the app delegate, which
every SDK reads (Capacitor apps post it as `capacitorDidRegisterForRemoteNotifications`), and each SDK handles its own
notifications. The iOS side is there so that the plugin registers on both platforms and `getStatus` answers.

## Install

```bash
npm install capacitor-fcm-fanout
npx cap sync
```

Requires Capacitor 8 and Android 8.0 (API 26) or newer; the app has to be set up for Firebase (`google-services.json`).

### Android: remove the other messaging services

The plugin's manifest declares `io.github.maximbelov.fcmfanout.FcmFanoutMessagingService` on
`com.google.firebase.MESSAGING_EVENT`. Remove every other service the app's libraries declare for that action, in the
**app's** `android/app/src/main/AndroidManifest.xml` (add `xmlns:tools="http://schemas.android.com/tools"` to its
`<manifest>`):

```xml
<application>
  <!-- Airship (@ua/capacitor-airship) -->
  <service android:name="com.urbanairship.push.fcm.AirshipFirebaseMessagingService" tools:node="remove" />
  <!-- @capacitor-firebase/messaging, when the app has it -->
  <service android:name="io.capawesome.capacitorjs.plugins.firebase.messaging.MessagingService" tools:node="remove" />
</application>
```

Why the app and not the plugin: the app's manifest outranks every library manifest in the merge, while a library's
`tools:node="remove"` reaches only the libraries merged after it, an order the plugin does not control. Removing another
SDK's service is also a decision an app should see in its own manifest, not one a dependency makes for it.

With `@capacitor-firebase/messaging` removed this way, its `tokenReceived` and `notificationReceived` events no longer
fire; its methods (`getToken`, `requestPermissions`, `subscribeToTopic`, ...) keep working, since they call Firebase
directly.

Firebase's own `com.google.firebase.messaging.FirebaseMessagingService` stays: the firebase-messaging library declares
it at priority `-500` as a fallback for apps with no service of their own, and any other service outranks it.

Check the result with `getStatus()` (below) on a device, or in the merged manifest of a build with
`node node_modules/capacitor-fcm-fanout/scripts/check-messaging-services.mjs <merged AndroidManifest.xml>`, which fails
unless the plugin's service is the only one besides that fallback.

### SDK versions

The routing is compiled against Intercom `18.13.0` and `urbanairship-fcm` `21.0.2` (what `@ua/capacitor-airship` 7
brings) and runs against whatever versions the app links. The app's `variables.gradle` can move the compile-time ones
with `intercomSdkVersion` (which `@capacitor-community/intercom` reads too) and `airshipFcmVersion`, and Firebase
Messaging with `firebaseMessagingVersion` (as `@capacitor-firebase/messaging` reads it). A version whose API no longer
matches is reported in the log rather than crashing the service.

## Use

Nothing to call: Firebase starts the service. To confirm the setup at run time:

```typescript
import { FcmFanout } from 'capacitor-fcm-fanout';

const { active, messagingServices, receivers } = await FcmFanout.getStatus();
// Android, set up: active true, receivers ['intercom', 'airship']
```

## API

<docgen-index>

* [`getStatus()`](#getstatus)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### getStatus()

```typescript
getStatus() => Promise<FanoutStatus>
```

Reports whether the plugin's messaging service is the one Firebase delivers to, and which SDKs it routes to. The
routing itself needs no call: the service runs whenever Firebase has a token or a message for the app. This is for
an app (or a test) that wants to confirm the manifest merge left one service.

**Returns:** <code>Promise&lt;<a href="#fanoutstatus">FanoutStatus</a>&gt;</code>

**Since:** 0.1.0

--------------------


### Interfaces


#### FanoutStatus

What the plugin found in the running app.

| Prop                    | Type                          | Description                                                                                                                                                                                                                                                                                                                                                                                                                 |
| ----------------------- | ----------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`messagingServices`** | <code>string[]</code>         | Every service in this app that receives `com.google.firebase.MESSAGING_EVENT`, by class name, highest priority first. Firebase delivers to one of them only, so a working setup lists the plugin's own and, after it, Firebase's fallback `com.google.firebase.messaging.FirebaseMessagingService` (declared by the firebase-messaging library at priority -500, used only when nothing else is). Empty on iOS and the web. |
| **`active`**            | <code>boolean</code>          | Whether the plugin's service is the only one in `messagingServices` besides Firebase's fallback, so that every token and message reaches it. Always `false` on iOS and the web, where the plugin routes nothing.                                                                                                                                                                                                            |
| **`receivers`**         | <code>FanoutReceiver[]</code> | The SDKs the service hands tokens and messages to, in the order a message is offered to them: `intercom` and `airship`, each listed only when its SDK is in the app. Empty on iOS and the web.                                                                                                                                                                                                                              |


### Type Aliases


#### FanoutReceiver

An SDK the service routes to.

<code>'intercom' | 'airship'</code>

</docgen-api>

## Development

```bash
npm ci
npm run build              # TypeScript, rollup, and the API section of this README
npm test                   # the TypeScript unit tests (Vitest)
npm run test:parity        # the API, both platforms and the README agree
npm run test:android       # the routing, on the JVM with fake receivers
npm run test:ios           # XCTest on an iOS simulator
npm run example:android:build
```

## Licence

MIT
