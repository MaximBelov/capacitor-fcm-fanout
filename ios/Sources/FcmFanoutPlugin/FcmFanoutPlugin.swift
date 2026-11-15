import Foundation
import Capacitor

/// The iOS side of the plugin, which routes nothing. On Android Firebase delivers a token or a message to exactly one
/// service, so one service has to hand it to every SDK. On iOS the APNs token reaches the app delegate, which posts it
/// for every SDK to read, and each SDK handles its own notifications, so there is nothing to share out. `getStatus`
/// says so.
@objc(FcmFanoutPlugin)
public class FcmFanoutPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "FcmFanoutPlugin"
    public let jsName = "FcmFanout"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "getStatus", returnType: CAPPluginReturnPromise)
    ]

    /// What `getStatus` resolves with on iOS: no messaging service, not active, no receivers.
    static func status() -> [String: Any] {
        return ["messagingServices": [String](), "active": false, "receivers": [String]()]
    }

    @objc func getStatus(_ call: CAPPluginCall) {
        call.resolve(FcmFanoutPlugin.status())
    }
}
