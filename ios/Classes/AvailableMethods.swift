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
        
        guard let authStateManager = authStateManager else {
            return
        }
        
        
        let errorInValidatingIDToken : Error? = authStateManager.validateToken(idToken: authStateManager.idToken)
        
        if(errorInValidatingIDToken == nil){
            let viewContdroller: UIViewController =
            (UIApplication.shared.delegate?.window??.rootViewController)!;
            
            self.oktaOidc!.signOutOfOkta(authStateManager, from: viewContdroller, callback: { [weak self] error in
                if(error != nil){
                    callback(error)
                    return
                }
                authStateManager.revoke(authStateManager.accessToken) { response, error in
                    if error != nil {
                        callback(error)
                        return
                    }
                }
                authStateManager.revoke(authStateManager.refreshToken)  { response, error in
                    if error != nil {
                        callback(error)
                        return
                    }
                }
                
                self?.authStateManager = nil
                self?.authStateManager?.clear()
                callback(nil);
                
            })
            
        }
        else{
            self.authStateManager = nil
            self.authStateManager?.clear()
            callback(nil);
        }
    }
    
    func initOkta(configuration: [String:String], callback: ((Error?) -> (Void))) {
        do {
            let oktaConfiguration: OktaOidcConfig = try OktaOidcConfig(with: configuration);
            self.oktaOidc = try OktaOidc(configuration: oktaConfiguration);
        } catch let error {
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
        oktaOidc.signInWithBrowser(from: viewController, callback: { [weak self] authStateManager, error in
            if let error = error {
                self?.authStateManager = nil
                
                callback(nil, error)
                return
            }
            
            self?.authStateManager = authStateManager!
            callback(
                [
                    "accessToken": authStateManager!.accessToken!
                ], nil)
            
        })
    }
    
    
    func getUser(callback: @escaping ((String?, Error?)-> (Void))) {
        
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
                
                self?.authStateManager = nil
                callback(nil, error)
                return
            }
            authStateManager!.writeToSecureStorage()
            
            callback(
                [
                    "accessToken": authStateManager!.accessToken!,
                    "userId":successStatus.user!.id!,
                ], nil)
        })
    }
    
}
