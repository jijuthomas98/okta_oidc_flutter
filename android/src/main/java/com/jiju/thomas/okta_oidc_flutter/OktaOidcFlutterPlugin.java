package com.jiju.thomas.okta_oidc_flutter;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiju.thomas.okta_oidc_flutter.operations.ConfigOktaClient;
import com.jiju.thomas.okta_oidc_flutter.operations.Auth;
import com.jiju.thomas.okta_oidc_flutter.utils.AvailableMethods;
import com.jiju.thomas.okta_oidc_flutter.utils.Errors;
import com.jiju.thomas.okta_oidc_flutter.utils.OktaRequestParameters;
import com.okta.oidc.Tokens;

import java.util.List;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** OktaOidcFlutterPlugin */
public class OktaOidcFlutterPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
  private MethodChannel channel;
  private Activity mainActivity;
  private Context context = null;



  //Flutter Engine overrides
  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "okta_oidc_flutter");
    context = flutterPluginBinding.getApplicationContext();
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    Tokens tokens;
    ObjectMapper mapper = new ObjectMapper();
    switch (call.method){
      case  AvailableMethods
              .CREATE_CONFIG:
        final String clientId = call.argument("clientId");
        final String redirectUri = call.argument("redirectUri");
        final String endSessionRedirectUri = call.argument("endSessionRedirectUri");
        final String discoveryUri = call.argument("discoveryUri");
        final List<String> scopes = call.argument("scopes");
        final boolean requireHardwareBackedKeyStore = Boolean.FALSE.equals(call.argument("requireHardwareBackedKeyStore"));
        OktaRequestParameters oktaRequestParameters = new OktaRequestParameters(
                clientId,
                redirectUri,
                endSessionRedirectUri,
                discoveryUri,
                scopes,
                requireHardwareBackedKeyStore);

        ConfigOktaClient.create(oktaRequestParameters,context);

      case AvailableMethods.SIGN_IN:
        tokens = Auth.webSignIn(mainActivity);
        try {
         result.success( mapper.writeValueAsString(tokens));
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }

      case AvailableMethods.SIGN_IN_WITH_CREDENTIAL:
        final String email = call.argument("email");
        final String password = call.argument("password");
        final String orgDomain = call.argument("orgDomain");
        tokens = Auth.signInWithCredentials(email, password, orgDomain);
        try {
          result.success( mapper.writeValueAsString(tokens));
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }

      case AvailableMethods.SIGN_OUT:
      boolean isSignedOut = Auth.signOut(mainActivity);
      result.success(isSignedOut);

      case AvailableMethods.IS_AUTHENTICATED:
        boolean isAuthenticated = Auth.isAuthenticated();
        result.success(isAuthenticated);

      case  AvailableMethods.CLEAR_TOKENS:
      boolean isTokenCleared = Auth.clearTokens();
      result.success(isTokenCleared);

      case AvailableMethods.REFRESH_TOKENS:
      tokens = Auth.refreshTokens();
      try {
        result.success( mapper.writeValueAsString(tokens));
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }

      default:
        result.success(Errors.genericError);
    }

    if (call.method.equals("getPlatformVersion")) {
      result.success("Testing method channel");
    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }


  //Activity aware overrides
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

  //helper method

}
