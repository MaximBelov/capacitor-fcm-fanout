/** What the plugin found in the running app. */
export interface FanoutStatus {
  /**
   * Every service in this app that receives `com.google.firebase.MESSAGING_EVENT`, by class name, highest priority
   * first. Firebase delivers to one of them only, so a working setup lists the plugin's own and, after it, Firebase's
   * fallback `com.google.firebase.messaging.FirebaseMessagingService` (declared by the firebase-messaging library at
   * priority -500, used only when nothing else is). Empty on iOS and the web.
   */
  messagingServices: string[];
  /**
   * Whether the plugin's service is the only one in `messagingServices` besides Firebase's fallback, so that every
   * token and message reaches it.
   * Always `false` on iOS and the web, where the plugin routes nothing.
   */
  active: boolean;
  /**
   * The SDKs the service hands tokens and messages to, in the order a message is offered to them: `intercom` and
   * `airship`, each listed only when its SDK is in the app. Empty on iOS and the web.
   */
  receivers: FanoutReceiver[];
}

/** An SDK the service routes to. */
export type FanoutReceiver = 'intercom' | 'airship';

export interface FcmFanoutPlugin {
  /**
   * Reports whether the plugin's messaging service is the one Firebase delivers to, and which SDKs it routes to. The
   * routing itself needs no call: the service runs whenever Firebase has a token or a message for the app. This is for
   * an app (or a test) that wants to confirm the manifest merge left one service.
   *
   * @since 0.1.0
   */
  getStatus(): Promise<FanoutStatus>;
}
