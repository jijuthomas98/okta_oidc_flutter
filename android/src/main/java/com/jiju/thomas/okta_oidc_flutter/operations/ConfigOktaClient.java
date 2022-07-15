package com.jiju.thomas.okta_oidc_flutter.operations;

import android.content.Context;
import com.jiju.thomas.okta_oidc_flutter.utils.Errors;
import com.jiju.thomas.okta_oidc_flutter.utils.OktaClient;
import com.jiju.thomas.okta_oidc_flutter.utils.OktaRequestParameters;
import com.okta.oidc.OIDCConfig;
import com.okta.oidc.Okta;
import com.okta.oidc.clients.AuthClient;
import com.okta.oidc.clients.web.WebAuthClient;
import com.okta.oidc.storage.SharedPreferenceStorage;
import java.util.concurrent.Executors;
import io.flutter.plugin.common.MethodChannel;


public class ConfigOktaClient {
    public static void create(OktaRequestParameters arg, Context context, MethodChannel.Result result){
        try{
            OIDCConfig config = new OIDCConfig.Builder()
                    .clientId(arg.getClientId())
                    .redirectUri(arg.getRedirectUri())
                    .endSessionRedirectUri(arg.getEndSessionRedirectUri())
                    .scopes(arg.getScopes())
                    .discoveryUri(arg.getDiscoveryUri())
                    .create();

            WebAuthClient webAuthClient = new Okta.WebAuthBuilder()
                    .withConfig(config)
                    .withContext(context)
                    .setRequireHardwareBackedKeyStore(arg.getRequireHardwareBackedKeyStore())
                    .withStorage(new SharedPreferenceStorage(context))
                    .withCallbackExecutor(Executors.newSingleThreadExecutor())
                    .create();

            AuthClient authClient = new Okta.AuthBuilder()
                    .withConfig(config)
                    .withContext(context)
                    .setRequireHardwareBackedKeyStore(arg.getRequireHardwareBackedKeyStore())
                    .withStorage(new SharedPreferenceStorage(context))
                    .withCallbackExecutor(Executors.newSingleThreadExecutor())
                    .create();

            OktaClient.init(config,webAuthClient,authClient);
            result.success(true);

        }catch (Exception e){
            throw new IllegalStateException(Errors.oktaOidcError);
        }
    }

}
