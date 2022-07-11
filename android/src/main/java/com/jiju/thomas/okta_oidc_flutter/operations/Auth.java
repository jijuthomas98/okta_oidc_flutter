package com.jiju.thomas.okta_oidc_flutter.operations;





import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jiju.thomas.okta_oidc_flutter.utils.OktaClient;
import com.okta.authn.sdk.AuthenticationStateHandlerAdapter;
import com.okta.authn.sdk.client.AuthenticationClient;
import com.okta.authn.sdk.client.AuthenticationClients;
import com.okta.authn.sdk.resource.AuthenticationResponse;
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



    public static  void signInWithBrowser(Activity activity){
        String accessToken = null;
        final String[] userId = new String[1];
        OktaClient oktaClient = OktaClient.getInstance();
        String clientId = oktaClient.getConfig().getClientId();
        AuthenticationPayload payload = new AuthenticationPayload.Builder()
                .setIdp(clientId)
                .setIdpScope("scope_of_your_idp")
                .build();

        oktaClient.getWebAuthClient().signIn(activity,payload);
//        try {
//            accessToken = OktaClient.getInstance().getWebAuthClient().getSessionClient().getTokens().getAccessToken();
//            OktaClient.getInstance().getAuthClient().getSessionClient().getUserProfile(new RequestCallback<UserInfo, AuthorizationException>() {
//                @Override
//                public void onSuccess(@NonNull UserInfo result) {
//                    userId[0] = result.get("sub").toString();
//                }
//
//                @Override
//                public void onError(String error, AuthorizationException exception) {
//
//                }
//            });
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        System.out.println(accessToken);
//        System.out.println(userId[0]);

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
