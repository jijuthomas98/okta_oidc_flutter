import 'dart:async';

import 'package:flutter/services.dart';
import 'package:okta_oidc_flutter/Init_okta.dart';
import 'package:okta_oidc_flutter/okta_tokens.dart';

class OktaOidcFlutter {
  static const MethodChannel _channel = MethodChannel('okta_oidc_flutter');

  static bool isInitialized = false;

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<void> initOkta(InitOkta request) async {
    isInitialized = false;
    await _channel.invokeMethod("CREATE_CONFIG", request.toMap());
    isInitialized = true;
  }

  static Future<OktaTokens> webSignIn() async {
    if (isInitialized == false) {
      throw Exception("Cannot sign in before initializing Okta SDK");
    }
    Map<dynamic, dynamic> tokens = await _channel.invokeMethod("SIGN_IN");
    return OktaTokens.fromMap(tokens as Map<String, dynamic>);
  }

  static Future<OktaTokens> signInWithCredentials(
      String username, String password) async {
    if (isInitialized == false) {
      throw Exception("Cannot sign in before initializing Okta SDK");
    }
    Map<dynamic, dynamic> tokens = await _channel.invokeMethod(
      "SIGN_IN_WITH_CREDENTIAL",
      {'username': username, 'password': password},
    );
    return OktaTokens.fromMap(tokens as Map<String, dynamic>);
  }

  static Future<bool> signOut() async {
    if (isInitialized == false) {
      throw Exception("Cannot sign in before initializing Okta SDK");
    }
    return await _channel.invokeMethod("SIGN_OUT") as bool;
  }

  static Future<bool> isAuthenticated() async {
    if (isInitialized == false) {
      throw Exception("Cannot sign in before initializing Okta SDK");
    }
    return await _channel.invokeMethod("IS_AUTHENTICATED") as bool;
  }

  static Future<bool> clearTokens() async {
    if (isInitialized == false) {
      throw Exception("Cannot sign in before initializing Okta SDK");
    }
    return await _channel.invokeMethod("CLEAR_TOKENS") as bool;
  }

  static Future<OktaTokens> refreshToken() async {
    if (isInitialized == false) {
      throw Exception("Cannot sign in before initializing Okta SDK");
    }
    Map<dynamic, dynamic> tokens =
        await _channel.invokeMethod("REFRESH_TOKENS");
    return OktaTokens.fromMap(tokens as Map<String, dynamic>);
  }
}
