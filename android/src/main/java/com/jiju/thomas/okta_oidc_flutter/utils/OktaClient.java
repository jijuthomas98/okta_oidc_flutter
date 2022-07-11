package com.jiju.thomas.okta_oidc_flutter.utils;

import com.okta.oidc.OIDCConfig;
import com.okta.oidc.clients.AuthClient;
import com.okta.oidc.clients.web.WebAuthClient;

public class OktaClient {
    private final OIDCConfig config;
    private WebAuthClient webAuthClient = null;
    private AuthClient authClient = null;
    private static OktaClient oktaInstance = null;

    private OktaClient(OIDCConfig config, WebAuthClient webAuthClient,AuthClient authClient){
        this.config = config;
        this.webAuthClient = webAuthClient;
        this.authClient = authClient;
    }

    public static OktaClient getInstance(){
        if(oktaInstance == null){
            return null;
        }
        return oktaInstance;
    }

    public synchronized static void init(OIDCConfig config, WebAuthClient webAuthClient, AuthClient authClient){
        if(oktaInstance != null){
            return;
        }
        oktaInstance = new OktaClient(config,webAuthClient,authClient);
    }

    public OIDCConfig getConfig() {
        if(oktaInstance == null) throw new IllegalStateException(Errors.notConfigured);
        return this.config;
    }

    public WebAuthClient getWebAuthClient() {
        if(oktaInstance == null && webAuthClient == null) throw new IllegalStateException(Errors.notConfigured);
        return this.webAuthClient;
    }

    public AuthClient getAuthClient() {
        if(oktaInstance == null && authClient == null) throw new IllegalStateException(Errors.notConfigured);
        return this.authClient;
    }
}
