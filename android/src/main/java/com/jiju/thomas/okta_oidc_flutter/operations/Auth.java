package com.jiju.thomas.okta_oidc_flutter.operations;





import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jiju.thomas.okta_oidc_flutter.utils.OktaClient;
import com.okta.authn.sdk.AuthenticationStateHandlerAdapter;
import com.okta.authn.sdk.client.AuthenticationClient;
import com.okta.authn.sdk.client.AuthenticationClients;
import com.okta.authn.sdk.resource.AuthenticationResponse;
import com.okta.oidc.RequestCallback;

import com.okta.oidc.ResultCallback;
import com.okta.oidc.net.response.UserInfo;
import com.okta.oidc.results.Result;
import com.okta.oidc.util.AuthorizationException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Auth {
    public static HashMap<String,String> signInWithCredentials(String email, String password, String orgDomain){
        AuthAndSignIn authAndSignIn = new AuthAndSignIn(email,password,orgDomain);

        ExecutorService taskExecutor = Executors.newSingleThreadExecutor();
        List<Callable<Void>> tasks = Arrays.asList(authAndSignIn);
        try{
            taskExecutor.invokeAll(tasks);
            awaitTerminationAfterShutdown(taskExecutor);
        }catch (Exception e){
            e.printStackTrace();
        }
        return  authAndSignIn.getAuthData();
    }


    private static class AuthAndSignIn implements Callable<Void>{
        private  String accessToken;
        private  String userId;


        String email;
        String password;
        String orgDomain;

        public AuthAndSignIn(String email, String password, String orgDomain) {
            this.email = email;
            this.password = password;
            this.orgDomain = orgDomain;
        }

        public synchronized void notifyToThread() {
            notify();
        }

        @Override
        public synchronized Void call() {
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
                                                notifyToThread();
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
                wait();

            }catch (Exception ex) {
                ex.printStackTrace();
            }
             return null;
        }

        public HashMap<String,String> getAuthData(){
            HashMap<String,String> authData = new HashMap<String,String>();
            authData.put("accessToken",accessToken);
            authData.put("userId",userId);
            return  authData;
        }
    }

    public static  void signInWithBrowser(Activity activity){
        System.out.println("ASASASS");
        OktaClient oktaClient = OktaClient.getInstance();
        oktaClient.getWebAuthClient().signIn(activity,null);

    }

    private static void awaitTerminationAfterShutdown(ExecutorService threadPool) {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }




    public synchronized static boolean signOut(Activity activity){
        OktaClient oktaClient = OktaClient.getInstance();
        final boolean[] isSignedOut = {false};

        oktaClient.getAuthClient().signOut(new ResultCallback<Integer, AuthorizationException>() {
            @Override
            public  void onSuccess(@NonNull Integer result) {
                isSignedOut[0] = true;
            }

            @Override
            public void onCancel() {
                isSignedOut[0] = false;
            }

            @Override
            public void onError(@Nullable String msg, @Nullable AuthorizationException exception) {
                isSignedOut[0]= false;
            }
        });
        return isSignedOut[0];
    }



    public static boolean isAuthenticated(){
        return OktaClient.getInstance().getAuthClient().getSessionClient().isAuthenticated();
    }
}
