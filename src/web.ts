import { WebPlugin } from '@capacitor/core';

import type { FanoutStatus, FcmFanoutPlugin } from './definitions';

/** A browser has no Firebase messaging service to route from: the plugin reports that it routes nothing. */
export class FcmFanoutWeb extends WebPlugin implements FcmFanoutPlugin {
  async getStatus(): Promise<FanoutStatus> {
    return { messagingServices: [], active: false, receivers: [] };
  }
}
