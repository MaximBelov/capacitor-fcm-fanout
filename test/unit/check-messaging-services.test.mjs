// The manifest check CI runs on the example app, against hand-written merged manifests. Plain Node, no install.
import { test } from 'node:test';
import assert from 'node:assert/strict';

import { messagingServices } from '../../scripts/check-messaging-services.mjs';

const service = (name, priority = '') => `
        <service android:name="${name}" android:exported="false">
            <intent-filter${priority}>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>`;

test("Firebase's own fallback is left out, the plugin's service is found", () => {
  const manifest = service('io.github.maximbelov.fcmfanout.FcmFanoutMessagingService') +
    service('com.google.firebase.messaging.FirebaseMessagingService', ' android:priority="-500"');
  assert.deepEqual(messagingServices(manifest), ['io.github.maximbelov.fcmfanout.FcmFanoutMessagingService']);
});

test('a second service on the action is reported', () => {
  const manifest = service('io.github.maximbelov.fcmfanout.FcmFanoutMessagingService') +
    service('com.urbanairship.push.fcm.AirshipFirebaseMessagingService');
  assert.equal(messagingServices(manifest).length, 2);
});

test('services on other actions, and self-closing ones, are ignored', () => {
  const manifest = `<service android:name="com.example.Other" />
        <service android:name="com.example.Sync"><intent-filter><action android:name="android.content.SyncAdapter" /></intent-filter></service>`;
  assert.deepEqual(messagingServices(manifest), []);
});
