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
import com.jiju.thomas.okta_oidc_flutter.utils.OktaClient;
import com.jiju.thomas.okta_oidc_flutter.utils.OktaRequestParameters;
import com.okta.oidc.Tokens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

/** OktaOidcFlutterPlugin */
public class OktaOidcFlutterPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
  private MethodChannel channel;
  private Activity mainActivity;
  private Context context = null;

  // Flutter Engine overrides
  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "okta_oidc_flutter");
    context = flutterPluginBinding.getApplicationContext();
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
      // Create Okta Config
    if(call.method.equals(AvailableMethods.CREATE_CONFIG)){
      try{
          ArrayList arguments = (ArrayList) call.arguments;
          HashMap<String, String> argMap = new HashMap<String,String>((Map< String,String>) arguments.get(0));
          final String clientId = argMap.get("clientId");
          final String redirectUri = argMap.get("redirectUri");
          final String endSessionRedirectUri = argMap.get("endSessionRedirectUri");
          final String discoveryUri = argMap.get("discoveryUri");
          final List<String> scopes = new ArrayList<String>(Arrays.asList(argMap.get("scopes").split(",")));
          final Boolean requireHardwareBackedKeyStore =  Boolean.parseBoolean(argMap.get("requireHardwareBackedKeyStore"));

        OktaRequestParameters oktaRequestParameters = new OktaRequestParameters(
            clientId,
            redirectUri,
            endSessionRedirectUri,
            discoveryUri,
            scopes, requireHardwareBackedKeyStore);
        ConfigOktaClient.create(oktaRequestParameters, context);
        result.success(true);

      }catch (Exception e){
        result.error("1",e.getMessage(),e.getStackTrace());
      }
    }
    // Sign in with Credentials
     else if(call.method.equals(AvailableMethods.SIGN_IN_WITH_CREDENTIAL)){
        Tokens tokens;
        ObjectMapper mapper = new ObjectMapper();


        ArrayList arguments = (ArrayList) call.arguments;
        HashMap<String, String> argMap = new HashMap<String,String>((Map< String,String>) arguments.get(0));
        final String email = argMap.get("email");
        final String password = argMap.get("password");
        final String orgDomain = argMap.get("orgDomain");
        try {
            Auth.signInWithCredentials(email, password, orgDomain);
         //result.success(authData);
        } catch (Exception e) {
          e.printStackTrace();
        }
    }else if(call.method.equals(AvailableMethods.SIGN_OUT)){
         boolean isSignedOut = Auth.signOut(mainActivity);
         result.success(isSignedOut);
    }
    else {
      result.notImplemented();
    }

//    switch (call.method) {
//      case AvailableMethods.CREATE_CONFIG:
//        System.out.println("+++++++++++++");
//        System.out.println(call.method);
////        final String clientId = call.argument("clientId");
////        final String redirectUri = call.argument("redirectUri");
////        final String endSessionRedirectUri = call.argument("endSessionRedirectUri");
////        final String discoveryUri = call.argument("discoveryUri");
////        final List<String> scopes = call.argument("scopes");
////        final boolean requireHardwareBackedKeyStore = Boolean.FALSE
////            .equals(call.argument("requireHardwareBackedKeyStore"));
////        OktaRequestParameters oktaRequestParameters = new OktaRequestParameters(
////            clientId,
////            redirectUri,
////            endSessionRedirectUri,
////            discoveryUri,
////            scopes,
////            requireHardwareBackedKeyStore);
////        ConfigOktaClient.create(oktaRequestParameters, context);
//        try{
//
//        }catch (Exception e){
//          System.out.println("error");
//        }finally {
//          result.success(true);
//        }
//
//        break;
//
//      case AvailableMethods.SIGN_IN:
//        tokens = Auth.webSignIn(mainActivity);
//        try {
//          result.success(mapper.writeValueAsString(tokens));
//        } catch (JsonProcessingException e) {
//          e.printStackTrace();
//        }
//        break;
//
//      case AvailableMethods.SIGN_IN_WITH_CREDENTIAL:
//        System.out.println("----------");
//        System.out.println(call.method);
////        final String email = call.argument("email");
////        final String password = call.argument("password");
////        final String orgDomain = call.argument("orgDomain");
////        try {
////        tokens = Auth.signInWithCredentials(email, password, orgDomain);
////          result.success(mapper.writeValueAsString(tokens));
////        } catch (JsonProcessingException e) {
////          e.printStackTrace();
////        }
//        break;
//
//      case AvailableMethods.SIGN_OUT:
//        boolean isSignedOut = Auth.signOut(mainActivity);
//        result.success(isSignedOut);
//        break;
//
//      case AvailableMethods.IS_AUTHENTICATED:
//        boolean isAuthenticated = Auth.isAuthenticated();
//        result.success(isAuthenticated);
//        break;
//
//      case AvailableMethods.CLEAR_TOKENS:
//        boolean isTokenCleared = Auth.clearTokens();
//        result.success(isTokenCleared);
//        break;
//
//      case AvailableMethods.REFRESH_TOKENS:
//        tokens = Auth.refreshTokens();
//        try {
//          result.success(mapper.writeValueAsString(tokens));
//        } catch (JsonProcessingException e) {
//          e.printStackTrace();
//        }
//        break;
//
//      default:
//        result.success(Errors.genericError);
//        break;
//    }

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
