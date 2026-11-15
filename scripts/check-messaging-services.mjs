// Reads a merged AndroidManifest.xml and checks that the plugin's service is the only one on Firebase's messaging action,
// apart from Firebase's own fallback (com.google.firebase.messaging.FirebaseMessagingService, declared by the
// firebase-messaging library at priority -500, which is used only when no other service is declared). Exits 1 otherwise.
//
//   node scripts/check-messaging-services.mjs <path to merged AndroidManifest.xml>
import { readFileSync } from 'node:fs';

const FALLBACK = 'com.google.firebase.messaging.FirebaseMessagingService';
const SERVICE = 'io.github.maximbelov.fcmfanout.FcmFanoutMessagingService';

export function messagingServices(manifest) {
  const services = [...manifest.matchAll(/<service\b[\s\S]*?(?:\/>|<\/service>)/g)].map((m) => m[0]);
  return services
    .filter((block) => block.includes('com.google.firebase.MESSAGING_EVENT'))
    .map((block) => /android:name="([^"]+)"/.exec(block)?.[1])
    .filter((name) => name !== undefined && name !== FALLBACK);
}

if (process.argv[1] && import.meta.url.endsWith(process.argv[1].split('/').pop())) {
  const path = process.argv[2];
  const found = messagingServices(readFileSync(path, 'utf8'));
  console.log(`messaging services besides Firebase's fallback: ${JSON.stringify(found)}`);
  if (found.length !== 1 || found[0] !== SERVICE) {
    console.error(`expected exactly ${SERVICE}`);
    process.exit(1);
  }
}
