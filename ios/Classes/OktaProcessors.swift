//
//  Utils.swift
//  okta_oidc_flutter
//
//  Created by Sai Gokula Krishnan on 26/07/22.
//

import OktaIdx

class OktaProcessors{
    var credsStorage: Credential?
    
    //MARK: Process Remediation
    func processRemediation(response: Result<Response,InteractionCodeFlowError>,  callback: @escaping (([String:String]?,Error?) -> Void), needResult: Bool = true, getToken: Bool = false)-> Response?{
        var result: Response? = nil;
        switch response {
        case .success(let successResponse):
            result = successResponse
            break
            
        case .failure(let errorResponse):
            callback(nil, errorResponse)
            break
        }
        return result
    }
    
    //MARK: Get Token
    func exchangeTokenFromResponse(response: Response,  callback: @escaping (([String:String]?,Error?) -> Void)){
        guard response.isLoginSuccessful
        else {
            return
        }
        response.exchangeCode { tokenResult in
            switch tokenResult{
            case .success(let tokenResponse):
                let tokens = tokenResponse
                self.saveCreds(token: tokens, callback: callback)
                callback([
                    "accessToken": tokens.accessToken
                ],nil)
            case .failure(let error):
                callback(nil, error)
            }
        }
    }
    
    //MARK: Save Creds
    func saveCreds(token: Token, callback: @escaping (([String:String]?,Error?) -> Void)){
        do{
            try self.credsStorage = Credential.store(token)
        }catch{
            callback(nil, error)
        }
    }
}
