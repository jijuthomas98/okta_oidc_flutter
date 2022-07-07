package com.jiju.thomas.okta_oidc_flutter.operations;





import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import com.jiju.thomas.okta_oidc_flutter.utils.OktaClient;
import com.okta.authn.sdk.AuthenticationStateHandlerAdapter;
import com.okta.authn.sdk.client.AuthenticationClient;
import com.okta.authn.sdk.client.AuthenticationClients;
import com.okta.authn.sdk.resource.AuthenticationResponse;
import com.okta.oidc.RequestCallback;

import com.okta.oidc.net.response.UserInfo;
import com.okta.oidc.results.Result;
import com.okta.oidc.util.AuthorizationException;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;


public class Auth {

    private static String kAccessToke;

    public static void signInWithCredentials(String email, String password, String orgDomain){
        AuthAndSignIn authAndSignIn = new AuthAndSignIn(email,password,orgDomain);
        ExecutorService executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
        Future<String> result = executor.submit(authAndSignIn);
    }


    private static class AuthAndSignIn implements Callable<String>{
        private  String accessToken;
        private  String userId;

        String email;
        String password;

        public AuthAndSignIn(String email, String password, String orgDomain) {
            this.email = email;
            this.password = password;
            this.orgDomain = orgDomain;
        }

        String orgDomain;
        @Override
        public String call() {
            try{
                AuthenticationClient authenticationClient;
                authenticationClient = AuthenticationClients.builder().setOrgUrl(orgDomain).build();
                try {
                    if(authenticationClient == null) return null;
                    //Auth SDK
                    authenticationClient.authenticate(email, password.toCharArray(), null, new AuthenticationStateHandlerAdapter() {
                        @Override
                        public void handleUnknown(AuthenticationResponse unknownResponse) {}
                        @Override
                        public void handleLockedOut(AuthenticationResponse lockedOut) {}
                        @Override
                        public void handleSuccess(AuthenticationResponse successResponse) {
                            String sessionToken;
                            sessionToken = successResponse.getSessionToken();
                            //OKTA OIDC SDK
                            OktaClient.getInstance().getAuthClient().signIn(sessionToken, null, new RequestCallback<Result, AuthorizationException>() {
                                @Override
                                public void onSuccess(@NonNull Result result) {
                                    try {
                                        accessToken = OktaClient.getInstance().getAuthClient().getSessionClient().getTokens().getAccessToken();
                                        OktaClient.getInstance().getAuthClient().getSessionClient().getUserProfile(new RequestCallback<UserInfo, AuthorizationException>() {
                                            @Override
                                            public void onSuccess(@NonNull UserInfo result) {
                                                userId = result.get("sub").toString();
                                                System.out.println(userId);
                                            }
                                            @Override
                                            public void onError(String error, AuthorizationException e) {
                                                e.printStackTrace();
                                            }
                                        });
                                    } catch (AuthorizationException e) {
                                        e.printStackTrace();
                                    }
                                }
                                @Override
                                public void onError(String s, AuthorizationException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    });
                }catch ( Exception e){
                    e.printStackTrace();
                }
            }catch (Exception ex) {
                ex.printStackTrace();
            }
            if(accessToken != null){
                return accessToken;
            }else return "EMPTY";
        }

//        public HashMap<String,String> getAuthData(){
//            HashMap<String,String> authData = new HashMap<String,String>();
//            authData.put("accessToken",accessToken);
//            authData.put("userId",userId);
//            return  authData;
//        }
    }




    public static boolean signOut(Activity activity){
        OktaClient oktaClient = OktaClient.getInstance();
        final boolean[] isSignedOut = {false};
        if(oktaClient.getAuthClient()!=null && oktaClient.getAuthClient().getSessionClient().isAuthenticated()){
            try {
                String accessToken =  oktaClient.getAuthClient().getSessionClient().getTokens().getAccessToken();
                oktaClient.getAuthClient().getSessionClient().revokeToken(accessToken, new RequestCallback<Boolean, AuthorizationException>() {
                    @Override
                    public void onSuccess(@NonNull Boolean result) {
                        isSignedOut[0] = true;
                        System.out.println(" SIGNED OUT");
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



    public static boolean isAuthenticated(){
        return OktaClient.getInstance().getAuthClient().getSessionClient().isAuthenticated();
    }
}
