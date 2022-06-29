package com.jiju.thomas.okta_oidc_flutter.entities;

import com.okta.oidc.OIDCConfig;
import com.okta.oidc.clients.AuthClient;
import com.okta.oidc.clients.web.WebAuthClient;

public class OktaClient {
    private OIDCConfig config;
    private WebAuthClient webAuthClient;
    private AuthClient authClient;
    private boolean isInitialized = false;

    public void init(OIDCConfig config, WebAuthClient webAuthClient, AuthClient authClient){
        this.config = config;
        this.webAuthClient = webAuthClient;
        this.authClient = authClient;
        isInitialized = true;
    }

    public OIDCConfig getConfig(){
        if(!isInitialized) throw new IllegalStateException(Errors.notConfigured);
        return  this.config;
    }

    public WebAuthClient getWebClient(){
        if(!isInitialized) throw new IllegalStateException(Errors.notConfigured);
        return  this.webAuthClient;
    }

    public AuthClient getAuthClient(){
        if(!isInitialized) throw new IllegalStateException(Errors.notConfigured);
        return  this.authClient;
    }
}
