import 'dart:async';

import 'package:flutter/services.dart';

import '../okta_oidc_flutter_export.dart';

class OktaOidcFlutter {
  static const MethodChannel _channel = MethodChannel('okta_oidc_flutter');

  static bool isInitialized = false;

  /// Initialize Okta OIDC
  static Future<void> initOkta(InitOkta request) async {
    isInitialized = false;
    isInitialized =
        await _channel.invokeMethod("CREATE_CONFIG", [request.toMap()]);
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
    var tokens = await _channel
        .invokeMethod<Map<dynamic, dynamic>>("SIGN_IN_WITH_CREDENTIAL", [
      {
        "email": email,
        "password": password,
        "orgDomain": orgDomain,
      }
    ]);
    return OktaTokens.parse(tokens);
  }

  static Future<OktaTokens> sso() async {
    if (isInitialized == false) {
      throw Exception("Cannot sign in before initializing Okta SDK");
    }
    var tokens = await _channel.invokeMethod("WEB_SIGN_IN");
    return OktaTokens.parse(tokens);
  }

  /// Sign out by revoking okta tokens
  static Future<bool> signOut() async {
    if (isInitialized == false) {
      throw Exception("Cannot sign in before initializing Okta SDK");
    }

    return await _channel.invokeMethod("SIGN_OUT") as bool;
  }

  /// Check if app is already Authenticated
  static Future<bool> isAuthenticated() async {
    if (isInitialized == false) {
      throw Exception("Cannot sign in before initializing Okta SDK");
    }
    return await _channel.invokeMethod("IS_AUTHENTICATED") as bool;
  }
}
