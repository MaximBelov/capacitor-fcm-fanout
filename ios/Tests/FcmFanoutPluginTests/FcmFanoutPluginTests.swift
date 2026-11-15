import XCTest
@testable import FcmFanoutPlugin

final class FcmFanoutPluginTests: XCTestCase {
    func testTheStatusSaysThePluginRoutesNothingOnIOS() {
        let status = FcmFanoutPlugin.status()
        XCTAssertEqual(status["messagingServices"] as? [String], [])
        XCTAssertEqual(status["active"] as? Bool, false)
        XCTAssertEqual(status["receivers"] as? [String], [])
    }

    func testThePluginIsRegisteredUnderTheNameTheJavaScriptUses() {
        let plugin = FcmFanoutPlugin()
        XCTAssertEqual(plugin.jsName, "FcmFanout")
        XCTAssertEqual(plugin.pluginMethods.map { $0.name }, ["getStatus"])
    }
}
