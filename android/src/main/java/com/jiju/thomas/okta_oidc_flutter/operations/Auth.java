package com.jiju.thomas.okta_oidc_flutter.operations;





import android.app.Activity;
import androidx.annotation.NonNull;
import com.jiju.thomas.okta_oidc_flutter.utils.OktaClient;
import com.okta.authn.sdk.AuthenticationException;
import com.okta.authn.sdk.AuthenticationStateHandlerAdapter;
import com.okta.authn.sdk.client.AuthenticationClient;
import com.okta.authn.sdk.client.AuthenticationClients;
import com.okta.authn.sdk.resource.AuthenticationResponse;
import com.okta.authn.sdk.resource.FactorType;
import com.okta.oidc.RequestCallback;
import com.okta.oidc.Tokens;
import com.okta.oidc.util.AuthorizationException;
import java.util.HashMap;
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
            try {
                Tokens tokens = oktaClient.getWebAuthClient().getSessionClient().getTokens();
                oktaClient.getWebAuthClient().signOutOfOkta(activity);
                oktaClient.getWebAuthClient().getSessionClient().revokeToken(tokens.getRefreshToken(), new RequestCallback<Boolean, AuthorizationException>() {
                    @Override
                    public void onSuccess(@NonNull Boolean result) {
                        oktaClient.getWebAuthClient().getSessionClient().clear();
                        methodResult.success(true);
                    }

                    @Override
                    public void onError(String error, AuthorizationException exception) {
                        methodResult.error(String.valueOf(exception.code), exception.toString(),exception.getMessage());
                    }
                });
                methodResult.success(true);
                return;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        try{
            Tokens tokens = oktaClient.getAuthClient().getSessionClient().getTokens();
            oktaClient.getAuthClient().getSessionClient().revokeToken(tokens.getRefreshToken(), new RequestCallback<Boolean, AuthorizationException>() {
                @Override
                public void onSuccess(@NonNull Boolean result) {
                    oktaClient.getAuthClient().getSessionClient().clear();
                    methodResult.success(true);
                }

                @Override
                public void onError(String error, AuthorizationException exception) {
                    methodResult.error(String.valueOf(exception.code), exception.toString(),exception.getMessage());
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }


    }


    public static void register(MethodChannel.Result result){

    }



    public static void isAuthenticated(MethodChannel.Result result){
         if(OktaClient.getInstance().getAuthClient().getSessionClient().isAuthenticated()){
             result.success(true);
         }else {
             result.success(false);
         }
    }
}
