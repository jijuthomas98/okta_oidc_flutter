package com.jiju.thomas.okta_oidc_flutter.entities;

public class Errors {
    public static final String notConfigured =  "OktaOidc client isn't configured, check if you have created a configuration with createConfig";
    public static final String noView = "No current view exists";
    public static final String noIdToken = "Id token does not exist";
    public static final String oktaOidcError = "Okta Oidc error";
    public static final String errorTokenType = "Token type not found";
    public static final String noAccessToken = "No access token found";
    public static final String signInFailed = "Sign in was not authorized";
    public static final String genericError = "Generic Error";
    public static final String methodNotImplemented = "This method is not implemented";
    public static final String noContext = "No current context exits";
    public static final String cancelledError = "Operation cancelled";
}
