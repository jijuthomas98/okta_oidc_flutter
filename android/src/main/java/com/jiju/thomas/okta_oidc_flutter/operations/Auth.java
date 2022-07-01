package com.jiju.thomas.okta_oidc_flutter.operations;





import android.app.Activity;

import androidx.annotation.NonNull;

import com.jiju.thomas.okta_oidc_flutter.utils.Errors;
import com.jiju.thomas.okta_oidc_flutter.utils.OktaClient;
import com.okta.authn.sdk.AuthenticationStateHandlerAdapter;
import com.okta.authn.sdk.client.AuthenticationClient;
import com.okta.authn.sdk.client.AuthenticationClients;
import com.okta.authn.sdk.resource.AuthenticationResponse;
import com.okta.oidc.RequestCallback;
import com.okta.oidc.Tokens;
import com.okta.oidc.clients.sessions.SessionClient;

import com.okta.oidc.results.Result;
import com.okta.oidc.util.AuthorizationException;

public class Auth {
    public static Tokens webSignIn(Activity activity){
        try {
            OktaClient.getInstance().getWebAuthClient().signIn(activity,null);
            SessionClient sessionClient = OktaClient.getInstance().getWebAuthClient().getSessionClient();
            if(sessionClient.isAuthenticated()){

                return  sessionClient.getTokens();
            }
        }catch (Exception e){
            throw  new IllegalStateException(Errors.signInFailed);
        }
        return null;
    }

    public static boolean signOut(Activity activity){
        OktaClient oktaClient = OktaClient.getInstance();
        final boolean[] isSignedOut = {false};
        if(oktaClient.getWebAuthClient()!=null && oktaClient.getAuthClient().getSessionClient().isAuthenticated()){
           oktaClient.getWebAuthClient().signOutOfOkta(activity);
           oktaClient.getWebAuthClient().getSessionClient().clear();
           return true;
        }else if(oktaClient.getAuthClient()!=null && oktaClient.getAuthClient().getSessionClient().isAuthenticated()){
            try {
              String accessToken =  oktaClient.getAuthClient().getSessionClient().getTokens().getAccessToken();
              oktaClient.getAuthClient().getSessionClient().revokeToken(accessToken, new RequestCallback<Boolean, AuthorizationException>() {
                  @Override
                  public void onSuccess(@NonNull Boolean result) {
                      isSignedOut[0] = true;
                  }

                  @Override
                  public void onError(String error, AuthorizationException exception) {

                  }
              });
            } catch (AuthorizationException e) {
                e.printStackTrace();
            }
        }
        return isSignedOut[0];
    }

    public static Tokens signInWithCredentials(String email, String password, String orgDomain){
        AuthenticationClient authenticationClient;
        final Tokens[] tokens = new Tokens[1];
        authenticationClient = AuthenticationClients.builder().setOrgUrl(orgDomain).build();

        try {
            if(authenticationClient == null) return null;
            authenticationClient.authenticate(email, password.toCharArray(), null, new AuthenticationStateHandlerAdapter() {
                @Override
                public void handleUnknown(AuthenticationResponse unknownResponse) {

                }
                @Override
                public void handleLockedOut(AuthenticationResponse lockedOut) {
                    //Handle response
                }

                @Override
                public void handleSuccess(AuthenticationResponse successResponse) {
                    String sessionToken;
                    sessionToken = successResponse.getSessionToken();
                    OktaClient.getInstance().getAuthClient().signIn(sessionToken, null, new RequestCallback<Result, AuthorizationException>() {
                        @Override
                        public void onSuccess(@NonNull Result result) {
                            try {
                                tokens[0] =  OktaClient.getInstance().getAuthClient().getSessionClient().getTokens();
                            } catch (AuthorizationException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(String error, AuthorizationException exception) {
                            throw new IllegalStateException(error);
                        }
                    });
                }

            });
            return tokens[0];
        }catch ( Exception e){
            throw new IllegalStateException(Errors.signInFailed);
        }

    }

    public static boolean isAuthenticated(){
        return OktaClient.getInstance().getAuthClient().getSessionClient().isAuthenticated();
    }

    public static boolean clearTokens(){
        OktaClient.getInstance().getAuthClient().getSessionClient().clear();
        OktaClient.getInstance().getAuthClient().getSessionClient().clear();
        return  true;
    }

    public  static Tokens refreshTokens(){
        OktaClient oktaClient = OktaClient.getInstance();
        final Tokens[] tokens = {null};
        if(oktaClient.getAuthClient() != null && oktaClient.getAuthClient().getSessionClient().isAuthenticated()){
            oktaClient.getAuthClient().getSessionClient().refreshToken(new RequestCallback<Tokens, AuthorizationException>() {
                @Override
                public void onSuccess(@NonNull Tokens result) {
                    tokens[0] = result;
                }

                @Override
                public void onError(String error, AuthorizationException exception) {
                    throw new IllegalStateException(Errors.noAccessToken);
                }
            });
        }else if(oktaClient.getWebAuthClient() != null && oktaClient.getAuthClient().getSessionClient().isAuthenticated()){
            oktaClient.getAuthClient().getSessionClient().refreshToken(new RequestCallback<Tokens, AuthorizationException>() {
                @Override
                public void onSuccess(@NonNull Tokens result) {
                    tokens[0] = result;
                }

                @Override
                public void onError(String error, AuthorizationException exception) {
                    throw new IllegalStateException(Errors.noAccessToken);
                }
            });
        }

        return tokens[0];
    }
}
