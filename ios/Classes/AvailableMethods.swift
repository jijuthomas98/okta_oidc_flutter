//
//  available_methods.swift
//  okta_oidc_flutter
//
//  Created by Sai Gokula Krishnan on 09/07/22.
//

import Foundation
import OktaOidc
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
    let oktaProcessors: OktaProcessors = OktaProcessors()
    
    // MARK: Initialize Okta
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
    
    
    // MARK: Logout from OKTA
    func logOut( callback: @escaping ((Error?) -> (Void))){
        guard let idxFlow = self.idxFlow,
              let   credsStorage = oktaProcessors.credsStorage else {
            callback(nil)
            return
        }
        
        do{
            idxFlow.start { result in
                switch result{
                case .success(let successResponse):
                    if(successResponse.isLoginSuccessful){
                        guard let remediation = successResponse.remediations[.cancel] else{
                            callback("error" as? Error)
                            return
                        }
                        
                        remediation.proceed(completion: { finalResult in
                            switch finalResult {
                            case .success(_):
                                let revoke =  Credential.revoke(credsStorage)
                                revoke(Token.RevokeType.accessToken, {_ in
                                    revoke(Token.RevokeType.refreshToken, { _ in
                                        callback(nil)
                                    })
                                    
                                })
                            case .failure(let error):
                                callback(error)
                            }
                        })
                    }else{
                        callback("error" as? Error)
                    }
                case .failure(let error):
                    callback(error)
                }
            }
        }
        
        
    }
    
    
    //MARK: Login with Creds
    func signInWithCreds(Username: String!, Password: String!,NewPassword: String?, callback: @escaping (([String:String]?,Error?) -> Void)){
        
        guard let idx = idxFlow else {
            return
        }
        
        idx.start { result in
            let idxResult : Response? = self.oktaProcessors.processRemediation(response: result, callback: callback)
            if(idxResult != nil){
                guard let remediation = idxResult!.remediations[.identify],
                      let usernameField = remediation["identifier"],
                      let rememberMeField = remediation["rememberMe"]
                else {
                    return
                }
                usernameField.value = Username
                rememberMeField.value = false
                
                remediation.proceed { remediationResponse in
                    let remediationResult : Response? =      self.oktaProcessors.processRemediation(response: remediationResponse, callback: callback)
                    if(remediationResult != nil){
                        guard let remediation = remediationResult!.remediations[.challengeAuthenticator],
                              let passwordField = remediation["credentials.passcode"]
                        else {
                            return
                        }
                        
                        passwordField.value = Password
                        
                        remediation.proceed { passwordResponse in
                            let passwordResult : Response? = self.oktaProcessors.processRemediation(response: passwordResponse, callback: callback)
                            if(passwordResult != nil){
                                let isPasswordReEnroll : Bool = passwordResult!.remediations[.reenrollAuthenticator] != nil
                                if(isPasswordReEnroll && NewPassword == nil){
                                    callback(["reEnroll": "true"], nil)
                                    return
                                }
                                if(isPasswordReEnroll && NewPassword != nil){
                                    guard let remediation = passwordResult!.remediations[.reenrollAuthenticator],
                                          let passwordField = remediation["credentials.passcode"]
                                    else {
                                        return
                                    }
                                    passwordField.value = NewPassword
                                    remediation.proceed { passwordReEnrollResponse in
                                        let passwordReEnrollSuccess : Response? = self.oktaProcessors.processRemediation(response: passwordReEnrollResponse, callback: callback)
                                        if(passwordReEnrollSuccess != nil){
                                            self.oktaProcessors.exchangeTokenFromResponse(response: passwordReEnrollSuccess!, callback: callback)
                                        }
                                    }
                                }else{
                                    self.oktaProcessors.exchangeTokenFromResponse(response: passwordResult!, callback: callback)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    
    
    //MARK: Register with Creds
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
            let idxResult : Response? = self.oktaProcessors.processRemediation(response: result, callback: callback)
            if(idxResult != nil){
                self.handleRegistrationSuccess(username: Username, password: Password, response: idxResult!, callback: callback)
            }
        }
        
    }
    
    
    
    //MARK: Browser Sign in
    func signInWithBrowser(callback: @escaping (([String:String]?,Error?) -> Void), idp: String, from presentationContext: ASWebAuthenticationPresentationContextProviding? = nil) {
        guard let idxFlow = idxFlow else {
            callback(nil, nil)
            return
        }
        
        self.presentationContext = presentationContext
        
        
        idxFlow.start { result in
            let idxResult : Response? = self.oktaProcessors.processRemediation(response: result, callback: callback)
            if(idxResult != nil){
                var socialRemedation: Remediation?
                
                idxResult!.remediations.forEach { item in
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
            }
        }
        
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
            let remediationResponseSuccess : Response? = self.oktaProcessors.processRemediation(response: remediationResponse, callback: callback)
            if(remediationResponseSuccess != nil){
                guard let remediation = remediationResponseSuccess!.remediations[.enrollProfile],
                      
                        let emailField = remediation["userProfile.email"],
                      let selfRoleField = remediation["userProfile.magnifi_self_role"]
                else {
                    remediationResponseSuccess!.cancel()
                    return;
                }
                emailField.value = username
                selfRoleField.value = "Individual Investor"
                
                remediation.proceed { secondResult in
                    let secondResultSuccess : Response? = self.oktaProcessors.processRemediation(response: secondResult, callback: callback)
                    if(secondResultSuccess != nil){
                        guard let remediation = secondResultSuccess!.remediations[.selectAuthenticatorEnroll],
                              let authenticatorField = remediation["authenticator"],
                              let authenticationOption = authenticatorField.options?.first(where: { option in
                                  option.label == "Password"
                              })
                        else{
                            remediationResponseSuccess!.cancel()
                            return
                            
                        }
                        authenticatorField.selectedOption = authenticationOption
                        remediation.proceed { authOptionResult in
                            let authOptionResultSuccess : Response? = self.oktaProcessors.processRemediation(response: authOptionResult, callback: callback)
                            if(authOptionResultSuccess != nil){
                                guard let remediation = authOptionResultSuccess!.remediations[.enrollAuthenticator],
                                      let passcode = remediation["credentials.passcode"]
                                else{
                                    authOptionResultSuccess!.cancel()
                                    return}
                                passcode.value = password
                                remediation.proceed { passcodeResult in
                                    let passcodeResultSuccess : Response? = self.oktaProcessors.processRemediation(response: passcodeResult, callback: callback)
                                    if(passcodeResultSuccess != nil){
                                        guard let remediation = passcodeResultSuccess!.remediations[.skip]
                                        else{
                                            passcodeResultSuccess!.cancel()
                                            return
                                            
                                        }
                                        remediation.proceed { skipResponse in
                                            let skipResponseSuccess : Response? = self.oktaProcessors.processRemediation(response: skipResponse, callback: callback)
                                            if(skipResponseSuccess != nil){
                                                self.oktaProcessors.exchangeTokenFromResponse(response: skipResponseSuccess!, callback: callback)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
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
                            self!.oktaProcessors.exchangeTokenFromResponse(response: resume, callback: callback)
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


