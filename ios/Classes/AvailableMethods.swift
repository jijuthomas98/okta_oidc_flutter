//
//  available_methods.swift
//  okta_oidc_flutter
//
//  Created by Sai Gokula Krishnan on 09/07/22.
//

import Foundation
import OktaOidc
import OktaAuthSdk
import OktaIdx

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
            if #available(iOS 13,*) {
                self.oktaOidc!.configuration.noSSO = true
            }
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
    
    func registerWithCreds(Username: String!, Password: String!, callback: @escaping (([String:String]?,Error?) -> Void)){
        guard let oktaOidc = oktaOidc else {
            return
        }
        let flow = InteractionCodeFlow(
            issuer: URL(string: oktaOidc.configuration.issuer)!,
            clientId: oktaOidc.configuration.clientId,
            scopes: oktaOidc.configuration.scopes,
            redirectUri: oktaOidc.configuration.redirectUri)
        
        flow.start { result in
            switch result {
            case .success(let response):
                print(response)
                self.handleRegistrationSuccess(userName: Username, password: Password, response: response, callback: callback)
            case .failure(let error):
                callback(nil, error)
                
            }
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
        if #available(iOS 13,*) {
            oktaOidc.configuration.noSSO = true
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
                    "sessionToken": successStatus.sessionToken!
                ], nil)
        })
    }
    
    
    func handleRegistrationSuccess(userName:String, password: String, response: Response,callback: @escaping (( [String:String]?,Error?) -> Void))  {
        print(response)
        guard let remediation = response.remediations[.selectEnrollProfile] else {
            return
        }
        remediation.proceed { remediationResponse in
            switch remediationResponse {
            case .success(let successResponse):
                print(successResponse)
                guard let remediation = successResponse.remediations[.enrollProfile],
                      
                        let emailField = remediation["userProfile.email"],
                      let selfRoleField = remediation["userProfile.magnifi_self_role"]
                else {
                    return;
                }
                
                emailField.value = userName
                selfRoleField.value = "Individual Investor"
                
                
                remediation.proceed { secondResult in
                    switch secondResult{
                    case .success(let secondResponse):
                        guard let remediation = secondResponse.remediations[.selectAuthenticatorEnroll],
                              let authenticatorField = remediation["authenticator"],
                              let authenticationOption = authenticatorField.options?.first(where: { option in
                                  option.label == "Password"
                              })
                        else{return}
                        
                        authenticatorField.selectedOption = authenticationOption
                        
                        remediation.proceed { authOptionResult in
                            switch authOptionResult{
                            case .success(let authOptionResponse):
                                print(authOptionResponse)
                                guard let remediation = authOptionResponse.remediations[.enrollAuthenticator],
                                      let passcode = remediation["credentials.passcode"]
                                else{return}
                                
                                passcode.value = password
                                
                                remediation.proceed { passcodeResult in
                                    switch passcodeResult{
                                        
                                    case .success(let passcodeResponse):
                                        
                                        guard let remediation = passcodeResponse.remediations[.skip]
                                        else{return}
                                        remediation.proceed { skipResponse in
                                            switch skipResponse{
                                            case .success(let finalResponse):
                                                guard finalResponse.isLoginSuccessful
                                                else {
                                                    return
                                                }
                                                finalResponse.exchangeCode { tokenResult in
                                                    switch tokenResult{
                                                    case .success(let tokenResponse):
                                                        let tokens = tokenResponse
                                                        callback([
                                                            "accessToken": tokens.accessToken
                                                        ],nil)
                                                    case .failure(let error):
                                                        callback(nil, error)
                                                    }
                                                }
                                            case .failure(let error):
                                                callback(nil, error)
                                            }
                                            
                                        }
                                        
                                    case .failure(let error):
                                        callback(nil, error)
                                        return
                                    }
                                }
                            case .failure(let error):
                                callback(nil, error)
                                return
                            }
                        }
                        
                        
                        
                    case .failure(let error):
                        callback(nil, error)
                        return
                    }
                    
                }
            case .failure(let error):
                callback(nil, error)
                return
                
            }
        }
        
    }
}
