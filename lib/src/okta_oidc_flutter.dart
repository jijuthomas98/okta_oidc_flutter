import 'dart:async';

import 'package:flutter/services.dart';

import '../okta_oidc_flutter_export.dart';

class OktaOidcFlutter {
  static const MethodChannel _channel = MethodChannel('okta_oidc_flutter');

  static bool isInitialized = false;

  /// Initialize Okta OIDC
  static Future<void> initOkta(InitOkta request) async {
    isInitialized = false;
    try {
      await _channel.invokeMethod("CREATE_CONFIG", [request.toMap()]);
      isInitialized = true;
    } catch (e) {
      isInitialized = false;
      throw Exception(e);
    }
  }

  /// Sign in with Credentials (use email and password),
  /// orgDomain as "https://{your org domain}okta.com/"
  static Future<OktaTokens> signInWithCredentials({
    required String email,
    required String password,
    required String orgDomain,
  }) async {
    if (isInitialized == false) {
      throw Exception("Cannot sign in before initializing Okta SDK");
    }
    try {
      var tokens = await _channel.invokeMethod("SIGN_IN_WITH_CREDENTIAL", [
        {
          "email": email,
          "password": password,
          "orgDomain": orgDomain,
        }
      ]);
      return OktaTokens.parse(tokens);
    } catch (e) {
      throw Exception(e);
    }
  }

  /// Sign out by revoking okta tokens
  static Future<bool> signOut() async {
    if (isInitialized == false) {
      throw Exception("Cannot sign in before initializing Okta SDK");
    }
    try {
      return await _channel.invokeMethod("SIGN_OUT") as bool;
    } catch (e) {
      throw Exception(e);
    }
  }

  /// Check if app is already Authenticated
  static Future<bool> isAuthenticated() async {
    if (isInitialized == false) {
      throw Exception("Cannot sign in before initializing Okta SDK");
    }
    try {
      return await _channel.invokeMethod("IS_AUTHENTICATED") as bool;
    } catch (e) {
      throw Exception(e);
    }
  }

  /// this method will return access token when user is authenticated else will throw exception
  static Future<OktaTokens> getAccessToken() async {
    if (isInitialized == false) {
      throw Exception("Cannot sign in before initializing Okta SDK");
    }
    try {
      if (await isAuthenticated()) {
        var tokens = await _channel.invokeMethod("REFRESH_TOKENS");
        return OktaTokens.parse(tokens);
      } else {
        throw Exception("Cannot call getAccessToken before authenticating");
      }
    } catch (e) {
      throw Exception(e);
    }
  }
}
