package com.jiju.thomas.okta_oidc_flutter.operations;


import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.jiju.thomas.okta_oidc_flutter.utils.OktaClient;
import com.okta.oidc.AuthenticationPayload;
import com.okta.oidc.AuthorizationStatus;
import com.okta.oidc.RequestCallback;
import com.okta.oidc.ResultCallback;

import com.okta.oidc.net.response.UserInfo;

import com.okta.oidc.util.AuthorizationException;

import java.util.HashMap;

import io.flutter.plugin.common.MethodChannel;


public class SignIn {

    public void withBrowser(String idp, Activity activity, MethodChannel.Result methodResult) {
        OktaClient oktaClient = OktaClient.getInstance();
        AuthenticationPayload payload = new AuthenticationPayload.Builder()
                .setIdp(idp)
                .build();

        assert oktaClient != null;
        oktaClient.getWebAuthClient().signIn(activity, payload);
        oktaClient.getWebAuthClient().registerCallback(new ResultCallback<AuthorizationStatus, AuthorizationException>() {
            @Override
            public void onSuccess(@NonNull AuthorizationStatus result) {
                try {
                    HashMap<String, String> data = new HashMap<>();
                    String accessToken = oktaClient.getWebAuthClient().getSessionClient().getTokens().getAccessToken();
                    oktaClient.getWebAuthClient().getSessionClient().getUserProfile(new RequestCallback<UserInfo, AuthorizationException>() {
                        @Override
                        public void onSuccess(@NonNull UserInfo result) {
                            String userId;
                            userId = result.get("sub").toString();
                            data.put("accessToken", accessToken);
                            data.put("userId", userId);
                            methodResult.success(data);
                        }

                        @Override
                        public void onError(String error, AuthorizationException exception) {
                            methodResult.error(String.valueOf(exception.code), error, exception.errorDescription);
                        }
                    });
                } catch (AuthorizationException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancel() {
                methodResult.error("E0000004", "Authentication exception", "Authentication failed");
            }

            @Override
            public void onError(@Nullable String msg, @Nullable AuthorizationException exception) {
                if (exception != null) {
                    methodResult.error(String.valueOf(exception.code), exception.error, exception.errorDescription);
                }
            }
        }, activity);
    }
}
