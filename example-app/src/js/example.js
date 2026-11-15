import { FcmFanout } from 'capacitor-fcm-fanout';

// Every call is written into #log and kept in window.fcmFanoutLog, so a person can read it and a script can poll it.
window.fcmFanoutLog = [];
const log = (line) => {
  window.fcmFanoutLog.push(line);
  document.getElementById('log').textContent = window.fcmFanoutLog.join('\n');
};

document.getElementById('status').addEventListener('click', async () => {
  try {
    log(`getStatus: ${JSON.stringify(await FcmFanout.getStatus())}`);
  } catch (error) {
    log(`getStatus: FAIL ${error.code ?? ''} ${error.message}`);
  }
});
