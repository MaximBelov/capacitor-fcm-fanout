import { registerPlugin } from '@capacitor/core';

import type { FcmFanoutPlugin } from './definitions';

const FcmFanout = registerPlugin<FcmFanoutPlugin>('FcmFanout', {
  web: () => import('./web').then((m) => new m.FcmFanoutWeb()),
});

export * from './definitions';
export { FcmFanout };
