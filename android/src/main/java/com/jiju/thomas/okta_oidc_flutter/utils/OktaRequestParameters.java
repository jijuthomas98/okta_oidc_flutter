package com.jiju.thomas.okta_oidc_flutter.utils;

import java.util.List;

public class OktaRequestParameters {
    private String clientId;
    private String redirectUri;
    private String endSessionRedirectUri;
    private String discoveryUri;
    private List<String> scopes;
    private Boolean requireHardwareBackedKeyStore;

    public OktaRequestParameters(
            String clientId,
            String redirectUri,
            String endSessionRedirectUri,
            String discoveryUri,
            List<String> scopes,
            Boolean requireHardwareBackedKeyStore
    ) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.endSessionRedirectUri = endSessionRedirectUri;
        this.discoveryUri = discoveryUri;
        this.scopes = scopes;
        this.requireHardwareBackedKeyStore = requireHardwareBackedKeyStore;
    }

    public String getClientId() {
        return clientId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getEndSessionRedirectUri() {
        return endSessionRedirectUri;
    }

    public String getDiscoveryUri() {
        return discoveryUri;
    }

    public String getScopes() {
        return String.join(" ",scopes);
    }

    public Boolean getRequireHardwareBackedKeyStore() {
        return requireHardwareBackedKeyStore;
    }

}
