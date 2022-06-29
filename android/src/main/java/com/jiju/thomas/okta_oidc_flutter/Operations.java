package com.jiju.thomas.okta_oidc_flutter;

import android.content.Context;


import com.jiju.thomas.okta_oidc_flutter.utils.Errors;
import com.jiju.thomas.okta_oidc_flutter.utils.OktaClient;
import com.jiju.thomas.okta_oidc_flutter.utils.OktaRequestParameters;
import com.okta.oidc.OIDCConfig;
import com.okta.oidc.Okta;
import com.okta.oidc.clients.AuthClient;
import com.okta.oidc.clients.web.WebAuthClient;
import com.okta.oidc.storage.SharedPreferenceStorage;



public class Operations {
    public static void createConfig(OktaRequestParameters arg, Context context){
        try{
            OIDCConfig config = new OIDCConfig.Builder()
                    .clientId(arg.getClientId())
                    .redirectUri(arg.getRedirectUri())
                    .endSessionRedirectUri(arg.getEndSessionRedirectUri())
                    .scopes(String.valueOf(arg.getScopes()))
                    .discoveryUri(arg.getDiscoveryUri())
                    .create();

            WebAuthClient webAuthClient = new Okta.WebAuthBuilder()
                    .withConfig(config)
                    .withContext(context)
                    .withStorage(new SharedPreferenceStorage(context))
                    .setRequireHardwareBackedKeyStore(arg.isRequireHardwareBackedKeyStore())
                    .create();

            AuthClient authClient = new Okta.AuthBuilder()
                    .withConfig(config)
                    .withContext(context)
                    .withStorage(new SharedPreferenceStorage(context))
                    .setRequireHardwareBackedKeyStore(arg.isRequireHardwareBackedKeyStore())
                    .create();

            OktaClient.init(config,webAuthClient,authClient);

        }catch (Exception e){
            throw new IllegalStateException(Errors.oktaOidcError);
        }
    }

}
