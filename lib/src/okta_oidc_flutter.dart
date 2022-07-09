import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';
import 'package:okta_oidc_flutter/src/init_okta.dart';
import 'package:okta_oidc_flutter/src/okta_tokens.dart';

class OktaOidcFlutter {
  static const MethodChannel _channel = MethodChannel('okta_oidc_flutter');

  static bool isInitialized = false;

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<void> initOkta(InitOkta request) async {
    isInitialized = false;
    try {
      await _channel.invokeMethod("CREATE_CONFIG", request.toMap());
      isInitialized = true;
    } catch (_) {
      isInitialized = false;
    }
  }

  static Future<OktaTokens> signInWithCredentials(
      String username, String password) async {
    if (isInitialized == false) {
      throw Exception("Cannot sign in before initializing Okta SDK");
    }
    Map tokens;
    if (Platform.isAndroid) {
      tokens = await _channel.invokeMethod(
        "SIGN_IN_WITH_CREDENTIAL",
        [
          {'username': username, 'password': password},
        ],
      );
    } else {
      tokens = await _channel.invokeMethod(
        "SIGN_IN_WITH_CREDENTIAL",
        {'username': username, 'password': password},
      );
    }
    return OktaTokens.parse(tokens);
  }

  static Future<OktaTokens> sso() async {
    if (isInitialized == false) {
      throw Exception("Cannot sign in before initializing Okta SDK");
    }
    var tokens = await _channel.invokeMethod("WEB_SIGN_IN");
    return OktaTokens.parse(tokens);
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

  static Future<Map>? forgotPassword(String userName) async {
    if (isInitialized == false) {
      throw Exception("Cannot sign in before initializing Okta SDK");
    }
    return await _channel.invokeMethod("FORGOT_PASSWORD", {
      "username": userName,
    });
  }
}
