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

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getEndSessionRedirectUri() {
        return endSessionRedirectUri;
    }

    public void setEndSessionRedirectUri(String endSessionRedirectUri) {
        this.endSessionRedirectUri = endSessionRedirectUri;
    }

    public String getDiscoveryUri() {
        return discoveryUri;
    }

    public void setDiscoveryUri(String discoveryUri) {
        this.discoveryUri = discoveryUri;
    }

    public String getScopes() {
        return String.join(" ",scopes);
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public Boolean getRequireHardwareBackedKeyStore() {
        return requireHardwareBackedKeyStore;
    }

}
