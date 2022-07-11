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
import com.okta.authn.sdk.resource.FactorType;
import com.okta.oidc.AuthenticationPayload;
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

import io.flutter.plugin.common.MethodChannel;


public class Auth {

    public static void signInWithCredentials(String email, String password, String orgDomain, MethodChannel.Result result){
        final SignIn signIn = new SignIn();
        signIn.withCredentials(email,password,orgDomain,result);
    }



    public static  void signInWithBrowser(String idp,Activity activity,MethodChannel.Result result){
        SignIn signIn = new SignIn();
        signIn.withBrowser(idp,activity,result);
    }


    public static void forgotPassword(String orgDomain,String userName,MethodChannel.Result result){
        if(OktaClient.getInstance().getAuthClient().getSessionClient().isAuthenticated() || OktaClient.getInstance().getWebAuthClient().getSessionClient().isAuthenticated() ){
            return;
        }
        final AuthenticationClient authenticationClient;
        authenticationClient = AuthenticationClients.builder().setOrgUrl(orgDomain).build();
        try{
            authenticationClient.recoverPassword(userName, FactorType.EMAIL, null, new AuthenticationStateHandlerAdapter() {
                @Override
                public void handleLockedOut(AuthenticationResponse lockedOut) {
                    super.handleLockedOut(lockedOut);
                }

                @Override
                public void handleSuccess(AuthenticationResponse successResponse) {
                    HashMap<String,String> status = new HashMap<String,String>();
                    status.put("status","WAITING");
                    result.success(status);
                }

                @Override
                public void handleUnknown(AuthenticationResponse unknownResponse) {
                    result.error("400","Unknown error","Unable to recover password");
                }
            });
        }catch (AuthenticationException e){
            result.error(e.getCode(),e.getMessage(),e.getCauses());
        }
    }



    public static void signOut(Activity activity,MethodChannel.Result methodResult){
        OktaClient oktaClient = OktaClient.getInstance();
        if(oktaClient.getAuthClient() == null){
            oktaClient.getWebAuthClient().signOutOfOkta(activity);
            methodResult.success(true);
            return;
        }
        oktaClient.getAuthClient().signOut(new ResultCallback<Integer, AuthorizationException>() {
            @Override
            public void onSuccess(@NonNull Integer result) {
                methodResult.success(true);
            }

            @Override
            public void onCancel() {
                methodResult.success(false);
            }

            @Override
            public void onError(@Nullable String msg, @Nullable AuthorizationException exception) {
                assert exception != null;
                methodResult.error(String.valueOf(exception.code), exception.toString(),exception.getMessage());
            }
        });
    }



    public static void isAuthenticated(MethodChannel.Result result){
         if(OktaClient.getInstance().getAuthClient().getSessionClient().isAuthenticated()){
             result.success(true);
         }else {
             result.success(false);
         }
    }
}
