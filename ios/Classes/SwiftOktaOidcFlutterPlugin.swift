import Flutter
import UIKit
import OktaOidc
import OktaAuthSdk


public class SwiftOktaOidcFlutterPlugin: NSObject, FlutterPlugin {
    let availableMethods: AvailableMethods = AvailableMethods()
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "okta_oidc_flutter", binaryMessenger: registrar.messenger())
        let instance = SwiftOktaOidcFlutterPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
        case "CREATE_CONFIG":
            guard let oktaInfo: Dictionary = call.arguments as? [String: Any] else {
                result(-1);
                return;
            }
            let clientId: String = oktaInfo["clientId"] as! String;
            let issuer: String = oktaInfo["issuer"] as! String;
            let endSessionRedirectUri: String? = oktaInfo["endSessionRedirectUri"] as? String;
            let redirectUrl: String = oktaInfo["redirectUri"] as! String;
            let idp: String? = oktaInfo["idp"] as? String;
            let scopeArray: [String] = oktaInfo["scopes"] as! [String];
            let scopes = scopeArray.joined(separator: " ");
            let oktaConfigMap : [String:String] = (idp != nil) ? [
                "clientId": clientId,
                "logoutRedirectUri": endSessionRedirectUri!,
                "issuer": issuer,
                "idp": idp!,
                "scopes": scopes,
                "redirectUri": redirectUrl,
            ] :  [
                "clientId": clientId,
                "issuer": issuer,
                "logoutRedirectUri": endSessionRedirectUri!,
                "scopes": scopes,
                "redirectUri": redirectUrl,
            ] ;
            
            return availableMethods.initOkta(configuration: oktaConfigMap, callback: { error in
                if(error != nil) {
                    result(error);
                    return
                }
                result(true);
            });
            
        case "SIGN_IN_WITH_CREDENTIAL":
            guard let creds: Dictionary = call.arguments as? [String: String] else {
                result(-1);
                return;
            }
            let username: String = creds["username"]! as String;
            let password: String = creds["password"]! as String;
            
            availableMethods.signInWithCreds(Username: username, Password: password,callback: {token,error  in
                if(error != nil) {
                    let flutterError: FlutterError = FlutterError(code: "Sign_In_Error", message: error?.localizedDescription, details: error.debugDescription);
                    result(flutterError);
                    return
                }
                result(token);
            });
            break
        case "WEB_SIGN_IN":
            availableMethods.signInWithBrowser(callback: {token,error  in
                if(error != nil) {
                    let flutterError: FlutterError = FlutterError(code: "Web_In_Error", message: error?.localizedDescription, details: error.debugDescription);
                    result(flutterError);
                    return
                }
                result(token);
            });
            break
        case "FORGOT_PASSWORD":
            guard let creds: Dictionary = call.arguments as? [String: String] else {
                result(-1);
                return;
            }
            let username: String = creds["username"]! as String;
            
            availableMethods.forgotPassword(Username: username,callback: {status,error  in
                if(error != nil) {
                    let flutterError: FlutterError = FlutterError(code: "Forgot_Password_Error", message: error?.localizedDescription, details: error.debugDescription);
                    result(flutterError);
                    return
                }
                result(status);
            });
            break
            
        case "SIGN_OUT":
            availableMethods.logOut( callback: { error in
                if(error != nil) {
                    let flutterError: FlutterError = FlutterError(code: "Sign_Out_Error", message: error?.localizedDescription, details: error.debugDescription);
                    result(flutterError);
                    return
                }
                result(true);
            })
            break
            
        default:
            let flutterError: FlutterError = FlutterError(code: "NOT_IMPLEMENTED", message: "This method is not implemented", details: "This method is not implemented, Check the invokation method name");
            result(flutterError);
            break
        }
    }
}
