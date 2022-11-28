package com.jiju.thomas.okta_oidc_flutter;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.jiju.thomas.okta_oidc_flutter.idxOperations.Authentication;
import com.jiju.thomas.okta_oidc_flutter.oktaConfigs.ConfigOktaClient;
import com.jiju.thomas.okta_oidc_flutter.utils.AvailableMethods;
import com.jiju.thomas.okta_oidc_flutter.utils.OktaRequestParameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * OktaOidcFlutterPlugin
 */
public class OktaOidcFlutterPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
    public MethodChannel channel;
    private Activity mainActivity;
    public Context context = null;
    public static MethodChannel.Result methodResult;

    // Flutter Engine overrides
    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "okta_oidc_flutter");
        context = flutterPluginBinding.getApplicationContext();
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        methodResult = result;
        switch (call.method) {
            // Create Okta Config
            case AvailableMethods.CREATE_CONFIG:
                try {
                    final String clientId = call.argument("clientId");
                    final String redirectUri = call.argument("redirectUri");
                    final String endSessionRedirectUri =  call.argument("endSessionRedirectUri");
                    final String discoveryUri = call.argument("discoveryUri");
                    final List<String> scopes = new ArrayList<>(Arrays.asList(Objects.requireNonNull(call.argument("scopes")).toString().split(",")));
                    final Boolean requireHardwareBackedKeyStore = Boolean
                            .parseBoolean(call.argument("requireHardwareBackedKeyStore"));

                    OktaRequestParameters oktaRequestParameters = new OktaRequestParameters(
                            clientId,
                            redirectUri,
                            endSessionRedirectUri,
                            discoveryUri,
                            scopes, requireHardwareBackedKeyStore);
                    Authentication.INSTANCE.init(result);
                    ConfigOktaClient.create(oktaRequestParameters, context, result);
                } catch (Exception e) {
                    result.error("1", e.getMessage(), e.getStackTrace());
                }
                break;
            // Sign in with Credentials
            case AvailableMethods.SIGN_IN_WITH_CREDENTIAL:
                final String email = call.argument("username");
                final String password = call.argument("password");
                final String newPassword = call.argument("newPassword");
                assert password != null;
                assert email != null;
                Authentication.INSTANCE.signInWithCredentials(email, password, newPassword,result);
                break;
            case AvailableMethods.SIGN_OUT:
                Authentication.INSTANCE.logout(result);
                break;
            case AvailableMethods.REGISTER_WITH_CREDENTIAL:
                final String registerEmail = call.argument("email");
                final String registerPassword = call.argument("password");

                assert registerPassword != null;
                assert registerEmail != null;
                Authentication.INSTANCE.registerUserWithCredentials(registerEmail, registerPassword, result);
                break;
            case AvailableMethods.WEB_SIGN_IN:
                Authentication.INSTANCE.registerUserWithGoogle(result, context);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    // Activity aware overrides
    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        mainActivity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        this.mainActivity = null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        this.mainActivity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivity() {
        this.mainActivity = null;
    }

    // helper method

}
