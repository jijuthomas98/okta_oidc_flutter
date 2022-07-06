import Flutter
import UIKit
import OktaOidc
import OktaAuthSdk


public class SwiftOktaOidcFlutterPlugin: NSObject, FlutterPlugin {
    var oktaOidc: OktaOidc?
    var authStateManager: OktaOidcStateManager?
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "okta_oidc_flutter", binaryMessenger: registrar.messenger())
    let instance = SwiftOktaOidcFlutterPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
      switch call.method {
      case "CREATE_CONFIG":
          print("Init")
          guard let oktaInfo: Dictionary = call.arguments as? [String: Any] else {
              print("else")
                          result(-1);
                          return;
          }
          let clientId: String = oktaInfo["clientId"] as! String;
          let issuer: String? = oktaInfo["issuer"] as? String;
          let endSessionRedirectUri: String? = oktaInfo["endSessionRedirectUri"] as? String;
          let redirectUrl: String = oktaInfo["redirectUrl"] as! String;
          let idp: String? = oktaInfo["idp"] as? String ?? "idp";
          let scopeArray: [String] = oktaInfo["scopes"] as! [String];
          let scopes = scopeArray.joined(separator: " ");
          let oktaConfigMap : [String:String] = (idp != nil) ? [
            "clientId": clientId,
            "logoutRedirectUri": endSessionRedirectUri!,
            "issuer": issuer!,
            "idp": idp!,
            "scopes": scopes,
            "redirectUri": redirectUrl,
          ] :  [
            "clientId": clientId,
            "issuer": issuer!,
            "logoutRedirectU'ri": endSessionRedirectUri!,
            "scopes": scopes,
            "redirectUri": redirectUrl,
          ] ;
         return initOkta(configuration: oktaConfigMap, callback: { error in
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
          
          signInWithCreds(Username: username, Password: password,callback: {token,error  in
              if(error != nil) {
                  let flutterError: FlutterError = FlutterError(code: "Sign_In_Error", message: error?.localizedDescription, details: error.debugDescription);
                result(flutterError);
                return
              }
              result(token);
            });
          break
      case "WEB_SIGN_IN":
          signInWithBrowser(callback: {token,error  in
              if(error != nil) {
                  let flutterError: FlutterError = FlutterError(code: "Web_In_Error", message: error?.localizedDescription, details: error.debugDescription);
                result(flutterError);
                return
              }
              result(token);
            });
          break
    
      case "SIGN_OUT":
          logOut( callback: { error in
              if(error != nil) {
                  let flutterError: FlutterError = FlutterError(code: "Sign_Out_Error", message: error?.localizedDescription, details: error.debugDescription);
                result(flutterError);
                return
              }
              result(true);
            })
          break
        
      default:
          result("iOS " + UIDevice.current.systemVersion)
          break
      }
  }
    
   private func isAuthenticated(callback: ((Bool) -> (Void))?) {
        if  let oktaOidc = oktaOidc,
          let _ = OktaOidcStateManager.readFromSecureStorage(for: oktaOidc.configuration)?.accessToken {
          self.authStateManager = OktaOidcStateManager.readFromSecureStorage(for: oktaOidc.configuration)
          callback?(true)
          return
        }
        callback?(false)
      }
    
    
    private func logOut( callback: @escaping ((Error?) -> (Void))){
        do {
            try self.authStateManager?.removeFromSecureStorage()
            self.authStateManager?.revoke(authStateManager?.refreshToken) { response, error in
                if error != nil {
                    print(error!)
                    callback(error)
                    return
                }
            }
        }catch let error {
            print("Logging out failed \(error)");
            callback(error)
            return
          }

        
    }
    
    private func initOkta(configuration: [String:String], callback: ((Error?) -> (Void))) {
           do {
                let oktaConfiguration: OktaOidcConfig = try OktaOidcConfig(with: configuration);
                self.oktaOidc = try OktaOidc(configuration: oktaConfiguration);
              } catch let error {
                print("okta object creation error \(error)");
                callback(error)
                return
              }
        if let oktaOidc = oktaOidc,
                 let _ = OktaOidcStateManager.readFromSecureStorage(for: oktaOidc.configuration)?.refreshToken {
                self.authStateManager = OktaOidcStateManager.readFromSecureStorage(for: oktaOidc.configuration)
              }
        callback(nil)
       }
    
    private func signInWithCreds(Username: String!, Password: String!, callback: @escaping (([String:String]?,Error?) -> Void)){
        guard let oktaOidc = oktaOidc else {
            return
        }
        do{
            self.authStateManager = OktaOidcStateManager.readFromSecureStorage(for: oktaOidc.configuration)
            let domainUrl: URL = URL(string: oktaOidc.configuration.issuer)!
            //Okta Auth SDK - Session Id
           OktaAuthSdk.authenticate(with: domainUrl, username: Username, password: Password,  onStatusChange: { authStatus in
               self.handleStatus(status: authStatus, callback:  callback)
            },
            onError: { error in
               print("Logging out failed \(error)");
               callback(nil,error)
           });
            
           
        }
    }
    
    private func signInWithBrowser(callback: @escaping (([String:String]?,Error?) -> Void)) {
        let viewController: UIViewController =
                    (UIApplication.shared.delegate?.window??.rootViewController)!;
        guard let oktaOidc = oktaOidc else {
            return
        }
        self.authStateManager = OktaOidcStateManager.readFromSecureStorage(for: oktaOidc.configuration)
        oktaOidc.signInWithBrowser(from: viewController, callback: { [weak self] authStateManager, error in
          if let error = error {
            self?.authStateManager = nil
            print("Signin Error: \(error)");
              callback(nil, error)
            return
          }
            print("token:  \(String(describing: authStateManager!.accessToken!))")
            callback(
                [
                "access_token": authStateManager!.accessToken!
            ], nil)

        })
      }


  private  func getUser(callback: @escaping ((String?, Error?)-> (Void))) {
      print("INside")
        authStateManager?.getUser { response, error in
          guard let response = response else {
            let alert = UIAlertController(title: "Error", message: error?.localizedDescription, preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
            callback(nil, error)
            return
          }
          if let jsonData = try? JSONSerialization.data(withJSONObject: response, options: .prettyPrinted) {
          let jsonString = String(data: jsonData, encoding: .ascii)
            callback(jsonString, nil)
          } else {
            callback(nil, error)
          }
        }
      }
    
private func getAccessToken(callback: ((String?) -> (Void))? ) {
        if let accessToken = authStateManager?.accessToken {
          callback?(accessToken)
        }
        else { callback?(nil) }
      }
    
    private func handleStatus(status: OktaAuthStatus, callback: @escaping (([String:String]?,Error?) -> Void)) {
           let currentStatus = status
            
            switch status.statusType {
                case .success:
                self.handleSuccessStatus(status: status, callback: callback)
                
            case .passwordWarning,.passwordExpired,.MFARequired,.MFAEnroll,.MFAEnrollActivate,.recoveryChallenge, .recovery,.passwordReset, .lockedOut,.unauthenticated,.unknown(_),
                    .MFAChallenge:
                    print("current Status: \(currentStatus)")
                
        }

}
    
    private  func handleSuccessStatus(status: OktaAuthStatus,callback: @escaping (( [String:String]?,Error?) -> Void)) {
        let successStatus: OktaAuthStatusSuccess = status as! OktaAuthStatusSuccess
        let oidcClient = oktaOidc
        // Okta OIDC
        oidcClient!.authenticate(withSessionToken: successStatus.sessionToken!,
                                 callback: { [weak self] authStateManager, error in
                                    if let error = error {
                                        print("error")
                                        self?.authStateManager = nil
                                        callback(nil, error)
                                        self?.handleError(error: error as! OktaError)
                                        return
                                 }
            authStateManager!.writeToSecureStorage()
//            print("AccessToken: \(String(describing: authStateManager!.accessToken))")
            print("id: \(String(describing: successStatus.user!.id!))")
            callback(
                [
                "access_token": authStateManager!.accessToken!,
                "id":successStatus.user!.id!,
            ], nil)
        })
    }
    
    
    private func handleError(error: OktaError) {
        switch error {
            case .serverRespondedWithError(let errorResponse):
                print("Error: \(errorResponse.errorSummary ?? "server error")")
            default:
                print("Error: \(error.description)")
        }
    }
}
