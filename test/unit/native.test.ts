import { afterAll, beforeAll, describe, expect, it, vi } from 'vitest';

import type { FcmFanoutPlugin } from '../../src';

// What the native side of Capacitor injects into the web view: a bridge object that makes the platform 'android', the
// headers of the installed plugins, and nativePromise, which every promise method of a plugin goes through.
const nativePromise = vi.fn<(...args: unknown[]) => Promise<unknown>>(async () => undefined);
const g = globalThis as Record<string, unknown>;

let FcmFanout: FcmFanoutPlugin;

beforeAll(async () => {
  g.androidBridge = {};
  g.Capacitor = {
    PluginHeaders: [{ name: 'FcmFanout', methods: [{ name: 'getStatus', rtype: 'promise' }] }],
    nativePromise,
  };
  ({ FcmFanout } = await import('../../src'));
});

afterAll(() => {
  delete g.androidBridge;
  delete g.Capacitor;
});

describe('on a native platform', () => {
  it('asks the native plugin named FcmFanout and returns what it reports', async () => {
    const status = {
      messagingServices: ['io.github.maximbelov.fcmfanout.FcmFanoutMessagingService'],
      active: true,
      receivers: ['intercom', 'airship'],
    };
    nativePromise.mockResolvedValueOnce(status);

    await expect(FcmFanout.getStatus()).resolves.toEqual(status);
    expect(nativePromise.mock.lastCall?.slice(0, 2)).toEqual(['FcmFanout', 'getStatus']);
  });
});
