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
import UIKit
import AuthenticationServices



@available(iOS 13.0, *)
class AvailableMethods{
    var oktaOidc: OktaOidc?
    var authStateManager: OktaOidcStateManager?
    private weak var presentationContext: ASWebAuthenticationPresentationContextProviding?
    var idxFlow: InteractionCodeFlow?
    private var webAuthSession: ASWebAuthenticationSession?
    var credsStorage: Credential?
    
    
    
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
        guard let credsStorage = credsStorage else {
            return
        }
        do{
          let revoke =  Credential.revoke(credsStorage)
            revoke(Token.RevokeType.accessToken, {_ in
                revoke(Token.RevokeType.refreshToken, { _ in
                    callback(nil)
                })
                
            })
        }
      
        
    }
    
    func initOkta(configuration: [String:String], callback: ((Error?) -> (Void))) {
        do {
            let oktaConfiguration: OktaOidcConfig = try OktaOidcConfig(with: configuration);
            let idx: InteractionCodeFlow = InteractionCodeFlow(issuer: URL(string: oktaConfiguration.issuer)!, clientId: oktaConfiguration.clientId, scopes: oktaConfiguration.scopes, redirectUri: oktaConfiguration.redirectUri)
            self.idxFlow = idx
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
        print("inside")
        guard oktaOidc != nil else {
            return
        }
        
        let flow = InteractionCodeFlow(
            issuer: URL(string: oktaOidc!.configuration.issuer)!,
            clientId: oktaOidc!.configuration.clientId,
            scopes: oktaOidc!.configuration.scopes,
            redirectUri: oktaOidc!.configuration.redirectUri)
        
        
        flow.start { result in
            switch result {
            case .success(let response):
                guard let remediation = response.remediations[.identify],
                      let usernameField = remediation["identifier"],
                      let rememberMeField = remediation["rememberMe"]
                else {
                    return
                }
                usernameField.value = Username
                rememberMeField.value = false
                
                remediation.proceed { remediationResponse in
                    switch remediationResponse {
                    case .success(let successResponse):
                        guard let remediation = successResponse.remediations[.challengeAuthenticator],
                              let passwordField = remediation["credentials.passcode"]
                        else {
                            return
                        }
                        
                        passwordField.value = Password
                        
                        remediation.proceed { passwordResponse in
                            switch passwordResponse {
                            case .success(let successPasswordResponse):
                                print(successPasswordResponse)
                                guard successPasswordResponse.isLoginSuccessful
                                else {
                                    return
                                }
                                successPasswordResponse.exchangeCode { tokenResult in
                                    switch tokenResult{
                                    case .success(let tokenResponse):
                                        let tokens = tokenResponse
                                        
                                        self.saveCreds(token: tokens, callback: callback)
                                        callback([
                                            "accessToken": tokens.accessToken,
                                            "userId":tokens.id
                                        ],nil)
                                    case .failure(let error):
                                        callback(nil, error)
                                    }
                                }
                            case .failure(let failurePasswordResponse):
                                print(failurePasswordResponse)
                                
                                
                            }
                        }
                        
                        
                        
                    case .failure(let failureResponse):
                        callback(nil, failureResponse)
                    }
                }
                
            case .failure(let error):
                callback(nil, error)
                
            }
        }
    }
    
    func saveCreds(token: Token, callback: @escaping (([String:String]?,Error?) -> Void)){
        do{
            try self.credsStorage = Credential.store(token)
        }catch{
            callback(nil, error)
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
                self.handleRegistrationSuccess(username: Username, password: Password, response: response, callback: callback)
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
    
    
    func signInWithBrowser(callback: @escaping (([String:String]?,Error?) -> Void), idp: String, from presentationContext: ASWebAuthenticationPresentationContextProviding? = nil) {
        guard let idxFlow = idxFlow else {
            callback(nil, nil)
            return
        }
        
        self.presentationContext = presentationContext
        
        
        idxFlow.start { result in
            switch result {
            case .success(let response):
                var socialRemedation: Remediation?
                
                response.remediations.forEach { item in
                    if(item.capabilities.isEmpty){
                        return
                    }
                    if(item.socialIdp?.id == idp){
                        socialRemedation = item
                    }
                }
                
                guard let remediation = socialRemedation else {
                    return
                }
                let  socialCapabilites = remediation.socialIdp
                
                DispatchQueue.main.async {
                    self.webLogin(idxflow: idxFlow , url: socialCapabilites!.redirectUrl, callback: callback)
                }
                
                
                
            case .failure(let error):
                callback(nil, error)
                
            }
        }
        
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
    
    
    func handleRegistrationSuccess(username:String, password: String, response: Response,callback: @escaping (( [String:String]?,Error?) -> Void))  {
        if(response.isLoginSuccessful){
            response.cancel()
            return
        }
        guard let remediation = response.remediations[.selectEnrollProfile] else {
            return
        }
        remediation.proceed { remediationResponse in
            switch remediationResponse {
            case .success(let successResponse):
                guard let remediation = successResponse.remediations[.enrollProfile],
                      
                        let emailField = remediation["userProfile.email"],
                      let selfRoleField = remediation["userProfile.magnifi_self_role"]
                else {
                    successResponse.cancel()
                    return;
                }
                
                emailField.value = username
                selfRoleField.value = "Individual Investor"
                
                
                remediation.proceed { secondResult in
                    switch secondResult{
                    case .success(let secondResponse):
                        guard let remediation = secondResponse.remediations[.selectAuthenticatorEnroll],
                              let authenticatorField = remediation["authenticator"],
                              let authenticationOption = authenticatorField.options?.first(where: { option in
                                  option.label == "Password"
                              })
                        else{
                            successResponse.cancel()
                            return
                            
                        }
                        authenticatorField.selectedOption = authenticationOption
                        remediation.proceed { authOptionResult in
                            switch authOptionResult{
                            case .success(let authOptionResponse):
                                
                                guard let remediation = authOptionResponse.remediations[.enrollAuthenticator],
                                      let passcode = remediation["credentials.passcode"]
                                else{
                                    authOptionResponse.cancel()
                                    return}
                                passcode.value = password
                                remediation.proceed { passcodeResult in
                                    switch passcodeResult{
                                    case .success(let passcodeResponse):
                                        guard let remediation = passcodeResponse.remediations[.skip]
                                        else{
                                            passcodeResponse.cancel()
                                            return}
                                        remediation.proceed { skipResponse in
                                            switch skipResponse{
                                            case .success(let finalResponse):
                                                guard finalResponse.isLoginSuccessful
                                                else {
                                                    finalResponse.cancel()
                                                    return
                                                }
                                                finalResponse.exchangeCode { tokenResult in
                                                    switch tokenResult{
                                                    case .success(let tokenResponse):
                                                        let tokens = tokenResponse
                                                        callback([
                                                            "accessToken": tokens.accessToken,
                                                            "userId":tokens.id
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
                        successResponse.cancel()
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
    
    func webLogin(idxflow: InteractionCodeFlow , url : URL, callback: @escaping (([String:String]?,Error?) -> Void)) {
        
        
        let session = ASWebAuthenticationSession(url: url,
                                                 callbackURLScheme: idxflow.redirectUri.scheme)
        { [weak self] (returnUrl, error) in
            guard error == nil
            else {
                return
            }
            
            let result = idxflow.redirectResult(for: returnUrl!)
            switch result {
            case .authenticated:
                idxflow.exchangeCode(redirect: returnUrl!) { token in
                    idxflow.resume { resumeResponse in
                        switch resumeResponse{
                        case .success(let resume):
                            resume.exchangeCode { tokenResult in
                                switch tokenResult{
                                case .success(let tokens):
                                    let token = tokens
                                    self!.saveCreds(token: tokens, callback: callback)
                                    print([
                                        "accessToken": token.accessToken,
                                        "userId":token.id
                                    ])
                                    callback([
                                        "accessToken": token.accessToken,
                                        "userId":token.id
                                    ],nil)

                                case .failure(let error):
                                    callback(nil, error)
                                }
                            }
                            break
                            
                        case .failure(let error):
                            callback(nil, error)
                            break
                        }
                    }
                }
                
                
            case .invalidContext:
                print("Invalid context")
                break
            case .remediationRequired:
                print("Remediation required")
                break
                
            case .invalidRedirectUrl:
                print("Invalid redirect URL")
                break
            }
        }
        
        
        session.presentationContextProvider = presentationContext
        session.prefersEphemeralWebBrowserSession = true
        session.start()
        self.webAuthSession = session
        
        
    }
    
    
}


