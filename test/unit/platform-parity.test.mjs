// Every method of the TypeScript API has its native method on both platforms and its section in the README, and neither
// platform has a method the API does not declare. A miss on either side compiles fine and fails only on a phone. Plain
// Node, no install.
import { readFileSync } from 'node:fs';
import { test } from 'node:test';
import assert from 'node:assert/strict';

const read = (path) => readFileSync(new URL(`../../${path}`, import.meta.url), 'utf8');

const definitions = read('src/definitions.ts');
const api = [...definitions.matchAll(/^\s+(\w+)\([^)]*\):\s*Promise</gm)].map((m) => m[1]).sort();
const swift = read('ios/Sources/FcmFanoutPlugin/FcmFanoutPlugin.swift');
const kotlin = read('android/src/main/kotlin/io/github/maximbelov/fcmfanout/FcmFanoutPlugin.kt');
const catalogue = read('android/src/main/kotlin/io/github/maximbelov/fcmfanout/ReceiverCatalogue.kt');
const ios = [...swift.matchAll(/CAPPluginMethod\(name:\s*"(\w+)"/g)].map((m) => m[1]).sort();
const iosFunctions = [...swift.matchAll(/@objc (?:override )?(?:public )?func (\w+)\(_ call: CAPPluginCall\)/g)]
  .map((m) => m[1])
  .sort();
const android = [...kotlin.matchAll(/@PluginMethod\s+(?:override\s+)?fun (\w+)\(/g)].map((m) => m[1]).sort();

test('the API declares its methods', () => {
  assert.deepEqual(api, ['getStatus']);
});

test('iOS registers exactly the methods of the API', () => {
  assert.deepEqual(ios, api);
});

test('iOS has an @objc function for every registered method', () => {
  assert.deepEqual(iosFunctions, api);
});

test('Android annotates exactly the methods of the API', () => {
  assert.deepEqual(android, api);
});

test('the plugin has the same name in JavaScript, Swift and Kotlin', () => {
  assert.match(read('src/index.ts'), /registerPlugin<FcmFanoutPlugin>\('FcmFanout'/);
  assert.match(swift, /jsName = "FcmFanout"/);
  assert.match(kotlin, /name = "FcmFanout"/);
});

test('both platforms report the fields the API declares', () => {
  for (const field of ['messagingServices', 'active', 'receivers']) {
    assert.match(definitions, new RegExp(`^\\s+${field}:`, 'm'));
    assert.match(swift, new RegExp(`"${field}":`), `iOS sends no ${field}`);
    assert.match(kotlin, new RegExp(`"${field}"`), `Android sends no ${field}`);
  }
});

test('the receiver names Android reports are the ones the API declares', () => {
  const declared = [...definitions.matchAll(/FanoutReceiver = (.+);/g)].flatMap((m) =>
    [...m[1].matchAll(/'(\w+)'/g)].map((n) => n[1]),
  );
  const kotlinNames = [...catalogue.matchAll(/const val \w+ = "(\w+)"/g)].map((m) => m[1]);
  assert.deepEqual(kotlinNames.sort(), declared.sort());
});

test('the README documents every method', () => {
  const readme = read('README.md');
  for (const method of api) {
    assert.match(readme, new RegExp(`### ${method}\\(`), `README.md has no section for ${method}()`);
  }
});
