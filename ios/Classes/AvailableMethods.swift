//
//  available_methods.swift
//  okta_oidc_flutter
//
//  Created by Sai Gokula Krishnan on 09/07/22.
//

import Foundation
import OktaOidc
import OktaAuthSdk

class AvailableMethods{
    var oktaOidc: OktaOidc?
    var authStateManager: OktaOidcStateManager?
    
    
     func isAuthenticated(callback: ((Bool) -> (Void))?) {
         if  let oktaOidc = oktaOidc,
           let _ = OktaOidcStateManager.readFromSecureStorage(for: oktaOidc.configuration)?.accessToken {
           self.authStateManager = OktaOidcStateManager.readFromSecureStorage(for: oktaOidc.configuration)
           callback?(true)
           return
         }
         callback?(false)
       }
     
     
      func logOut( callback: @escaping ((Error?) -> (Void))){
          let viewController: UIViewController =
                         (UIApplication.shared.delegate?.window??.rootViewController)!;
          guard let oktaOidc = oktaOidc else {
              return
          }
          guard let authStateManager = authStateManager else {
              return
          }
          do {
              try authStateManager.removeFromSecureStorage()
              authStateManager.revoke(authStateManager.refreshToken) { response, error in
                           if error != nil {
                               print(error!)
                               callback(error)
                               return
                           }
                       }
                   }
          catch let error {
                       print("Logging out failed \(error)");
                       callback(error)
                       return
                     }
          
          do{

              oktaOidc.signOutOfOkta(authStateManager, from: viewController, callback: { error in
                  if(error != nil){
                      callback(error)
                   
                  }
                  callback(nil);
              })
         }
          
          self.authStateManager = nil
     }
     
      func initOkta(configuration: [String:String], callback: ((Error?) -> (Void))) {
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
     
      func signInWithCreds(Username: String!, Password: String!, callback: @escaping (([String:String]?,Error?) -> Void)){
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
     
      func forgotPassword(Username: String!, callback: @escaping (([String:String]?,Error?) -> Void)){
         guard let oktaOidc = oktaOidc else {
             return
         }
         let domainUrl: URL = URL(string: oktaOidc.configuration.issuer)!
         
         OktaAuthSdk.recoverPassword(with: domainUrl, username: Username, factorType: OktaRecoveryFactors.email) { authStatus in
             let successStatus: String = authStatus.factorResult!.rawValue
             print("successState: ......\(successStatus)")
             callback([
                 "status":  successStatus], nil)
         } onError: { error in
             callback(nil, error)
         }
     }
     
     
      func signInWithBrowser(callback: @escaping (([String:String]?,Error?) -> Void)) {
         let viewController: UIViewController =
                     (UIApplication.shared.delegate?.window??.rootViewController)!;
         guard let oktaOidc = oktaOidc else {
             return
         }
        self.authStateManager = nil
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


      func getUser(callback: @escaping ((String?, Error?)-> (Void))) {
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
     
     func getAccessToken(callback: ((String?) -> (Void))? ) {
         if let accessToken = authStateManager?.accessToken {
           callback?(accessToken)
         }
         else { callback?(nil) }
       }
     
      func handleStatus(status: OktaAuthStatus, callback: @escaping (([String:String]?,Error?) -> Void)) {
            let currentStatus = status
             
             switch status.statusType {
                 case .success:
                 self.handleSuccessStatus(status: status, callback: callback)
                 
             case .passwordWarning,.passwordExpired,.MFARequired,.MFAEnroll,.MFAEnrollActivate,.recoveryChallenge, .recovery,.passwordReset, .lockedOut,.unauthenticated,.unknown(_),
                     .MFAChallenge:
                     print("current Status: \(currentStatus)")
                 
         }

    }
     
       func handleSuccessStatus(status: OktaAuthStatus,callback: @escaping (( [String:String]?,Error?) -> Void)) {
         let successStatus: OktaAuthStatusSuccess = status as! OktaAuthStatusSuccess
         let oidcClient = oktaOidc
         // Okta OIDC
         oidcClient!.authenticate(withSessionToken: successStatus.sessionToken!,
                                  callback: { [weak self] authStateManager, error in
                                     if let error = error {
                                         print("error")
                                         self?.authStateManager = nil
                                         callback(nil, error)
                                         return
                                  }
             authStateManager!.writeToSecureStorage()
             print("id: \(String(describing: successStatus.user!.id!))")
             callback(
                 [
                 "access_token": authStateManager!.accessToken!,
                 "id":successStatus.user!.id!,
             ], nil)
         })
     }

}
