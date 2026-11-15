import { describe, expect, it } from 'vitest';

import { FcmFanoutWeb } from '../../src/web';

describe('on the web', () => {
  it('reports that the plugin routes nothing', async () => {
    await expect(new FcmFanoutWeb().getStatus()).resolves.toEqual({
      messagingServices: [],
      active: false,
      receivers: [],
    });
  });
});
