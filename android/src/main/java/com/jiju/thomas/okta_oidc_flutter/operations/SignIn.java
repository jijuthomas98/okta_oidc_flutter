package com.jiju.thomas.okta_oidc_flutter.operations;


import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jiju.thomas.okta_oidc_flutter.utils.OktaClient;
import com.okta.authn.sdk.AuthenticationException;
import com.okta.authn.sdk.AuthenticationStateHandlerAdapter;
import com.okta.authn.sdk.client.AuthenticationClient;
import com.okta.authn.sdk.client.AuthenticationClients;
import com.okta.authn.sdk.resource.AuthenticationResponse;
import com.okta.oidc.AuthenticationPayload;
import com.okta.oidc.AuthorizationStatus;
import com.okta.oidc.RequestCallback;
import com.okta.oidc.ResultCallback;
import com.okta.oidc.clients.AuthClient;
import com.okta.oidc.net.response.UserInfo;
import com.okta.oidc.results.Result;
import com.okta.oidc.util.AuthorizationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.xml.transform.OutputKeys;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;


public class SignIn {

    public void withCredentials(String email, String password, String orgDomain, MethodChannel.Result result){

        final AuthenticationClient authenticationClient;
        try{
            authenticationClient = AuthenticationClients.builder().setOrgUrl(orgDomain).build();
            new Thread(new Runnable(
            ) {
                @Override
                public void run() {
                    try {

                        authenticationClient.authenticate(email, password.toCharArray(), null, new AuthenticationStateHandlerAdapter() {
                            @Override
                            public void handleSuccess(AuthenticationResponse successResponse) {
                                String sessionToken;
                                sessionToken = successResponse.getSessionToken();
                                try {
                                    signInWithSessionToken(sessionToken,result);
                                } catch (Exception e) {
                                    throw new IllegalStateException(e);
                                }
                            }
                            @Override
                            public void handleUnknown(AuthenticationResponse unknownResponse) {
                                System.out.println(unknownResponse.getStatus().name());
                            }
                        });
                    } catch (AuthenticationException e) {

                        result.error(String.valueOf(e.getCode()), e.toString(),e.getMessage());
                    }
                }
            }).start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void signInWithSessionToken(String sessionToken,MethodChannel.Result methodResult) throws Exception{
       AuthClient authClient = OktaClient.getInstance().getAuthClient();

       try {
           authClient.signIn(sessionToken, null, new RequestCallback<Result, AuthorizationException>() {
               @Override
               public void onSuccess(@NonNull Result result) {
                   try {
                       String accessToken;
                       HashMap<String,String> data = new HashMap<String,String>();
                       accessToken =  authClient.getSessionClient().getTokens().getAccessToken();
                       authClient.getSessionClient().getUserProfile(new RequestCallback<UserInfo, AuthorizationException>() {
                           @Override
                           public void onSuccess(@NonNull UserInfo result) {
                               String userId;
                               userId = result.get("sub").toString();
                               data.put("accessToken",accessToken);
                               data.put("userId",userId);
                               methodResult.success(data);
                           }

                           @Override
                           public void onError(String error, AuthorizationException exception) {
                               methodResult.error(String.valueOf(exception.code), error,exception.errorDescription);
                           }
                       });
                   }catch (Exception e){
                       throw  new IllegalStateException(e);
                   }
               }
               @Override
               public void onError(String error, AuthorizationException exception) {
                   methodResult.error(String.valueOf(exception.code), error,exception.errorDescription);
               }
           });
       }catch (Exception e){
           throw new IllegalStateException(e);
       }
    }



    public void withBrowser(String idp, Activity activity, MethodChannel.Result methodResult){
        OktaClient oktaClient = OktaClient.getInstance();
        AuthenticationPayload payload = new AuthenticationPayload.Builder()
                .setIdp(idp)
                .build();

        oktaClient.getWebAuthClient().signIn(activity,payload);
        oktaClient.getWebAuthClient().registerCallback(new ResultCallback<AuthorizationStatus, AuthorizationException>() {
            @Override
            public void onSuccess(@NonNull AuthorizationStatus result) {
                try {
                    HashMap<String,String> data = new HashMap<String,String>();
                    String accessToken =oktaClient.getWebAuthClient().getSessionClient().getTokens().getAccessToken();
                    oktaClient.getWebAuthClient().getSessionClient().getUserProfile(new RequestCallback<UserInfo, AuthorizationException>() {
                        @Override
                        public void onSuccess(@NonNull UserInfo result) {
                            String userId;
                            userId = result.get("sub").toString();
                            data.put("accessToken",accessToken);
                            data.put("userId",userId);
                            methodResult.success(data);
                        }

                        @Override
                        public void onError(String error, AuthorizationException exception) {
                            methodResult.error(String.valueOf(exception.code), error,exception.errorDescription);
                        }
                    });
                } catch (AuthorizationException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancel() {
                methodResult.error("E0000004", "Authentication exception","Authentication failed");
            }

            @Override
            public void onError(@Nullable String msg, @Nullable AuthorizationException exception) {
                methodResult.error(String.valueOf(exception.code), exception.error,exception.errorDescription);
            }
        },activity);
    }
}
