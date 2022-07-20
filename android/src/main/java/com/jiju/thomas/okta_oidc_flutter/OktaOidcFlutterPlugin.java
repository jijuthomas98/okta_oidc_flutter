package com.jiju.thomas.okta_oidc_flutter;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.jiju.thomas.okta_oidc_flutter.idxOperations.Authentication;
import com.jiju.thomas.okta_oidc_flutter.operations.ConfigOktaClient;
import com.jiju.thomas.okta_oidc_flutter.operations.Auth;
import com.jiju.thomas.okta_oidc_flutter.utils.AvailableMethods;
import com.jiju.thomas.okta_oidc_flutter.utils.OktaRequestParameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static Context context = null;
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
                    ArrayList arguments = (ArrayList) call.arguments;
                    HashMap<String, String> argMap = new HashMap<String, String>(
                            (Map<String, String>) arguments.get(0));
                    final String clientId = argMap.get("clientId");
                    final String redirectUri = argMap.get("redirectUri");
                    final String endSessionRedirectUri = argMap.get("endSessionRedirectUri");
                    final String discoveryUri = argMap.get("discoveryUri");
                    final List<String> scopes = new ArrayList<String>(Arrays.asList(argMap.get("scopes").split(",")));
                    final Boolean requireHardwareBackedKeyStore = Boolean
                            .parseBoolean(argMap.get("requireHardwareBackedKeyStore"));

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
                ArrayList arguments = (ArrayList) call.arguments;
                HashMap<String, String> argMap = new HashMap<String, String>((Map<String, String>) arguments.get(0));
                final String email = argMap.get("email");
                final String password = argMap.get("password");
                assert password != null;
                assert email != null;
                Authentication.INSTANCE.signInWithCredentials(email, password, result, context);
                break;
            case AvailableMethods.SIGN_OUT:
                Authentication.INSTANCE.logout(result);
                break;
            case AvailableMethods.WEB_SIGN_IN:
                ArrayList argument = (ArrayList) call.arguments;
                HashMap<String, String> arg = new HashMap<String, String>((Map<String, String>) argument.get(0));
                final String idp = arg.get("idp");
                Auth.signInWithBrowser(idp, mainActivity, result);
                break;
            case AvailableMethods.FORGOT_PASSWORD:
                ArrayList forgotPasswordArguments = (ArrayList) call.arguments;
                HashMap<String, String> forgotPasswordMap = new HashMap<String, String>(
                        (Map<String, String>) forgotPasswordArguments.get(0));
                final String orgDom = forgotPasswordMap.get("orgDomain");
                final String userName = forgotPasswordMap.get("userName");
                Auth.forgotPassword(orgDom, userName, result);
                break;
            case AvailableMethods.REGISTER_WITH_CREDENTIAL:
                ArrayList registerUserArguments = (ArrayList) call.arguments;
                HashMap<String, String> registerUserArgMap = new HashMap<String, String>(
                        (Map<String, String>) registerUserArguments.get(0));
                final String registerEmail = registerUserArgMap.get("email");
                final String registerPassword = registerUserArgMap.get("password");

                Authentication.INSTANCE.registerUserWithCredentials(registerEmail, registerPassword, result, context);
                break;
            case AvailableMethods.REGISTER_WITH_GOOGLE:
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
