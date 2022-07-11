import 'dart:async';
import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import '../okta_oidc_flutter.dart';

class OktaOidcFlutter {
  static const MethodChannel _channel = MethodChannel('okta_oidc_flutter');

  static bool isInitialized = false;

  /// Initialize Okta OIDC
  static Future<void> initOkta(InitOkta request) async {
    isInitialized = false;
    try {
      if (Platform.isAndroid) {
        await _channel.invokeMethod("CREATE_CONFIG", [request.toMap()]);
      } else {
        await _channel.invokeMethod("CREATE_CONFIG", request.toMap());
      }
      isInitialized = true;
    } catch (e) {
      isInitialized = false;
    }
  }

  static Future<OktaTokens> signInWithCredentials(
      {required String email, required String password}) async {
    if (isInitialized == false) {
      throw Exception("Cannot sign in before initializing Okta SDK");
    }
    Map? tokens;
    try {
      if (Platform.isAndroid) {
        tokens = await _channel
            .invokeMethod<Map<dynamic, dynamic>>("SIGN_IN_WITH_CREDENTIAL", [
          {
            "email": email,
            "password": password,
            "orgDomain": 'https://dev-24779440.okta.com/',
          }
        ]);
      } else {
        tokens = await _channel.invokeMethod(
          "SIGN_IN_WITH_CREDENTIAL",
          {'username': email, 'password': password},
        );
      }
    } catch (e) {
      if (kDebugMode) {
        print(e);
      }
    }

    return OktaTokens.parse(tokens);
  }

  static Future<OktaTokens> sso(String? idp) async {
    if (isInitialized == false) {
      throw Exception("Cannot sign in before initializing Okta SDK");
    }

    // ignore: prefer_typing_uninitialized_variables
    var tokens;

    if (Platform.isAndroid) {
      tokens = await _channel.invokeMethod("WEB_SIGN_IN", [
        {"idp": idp}
      ]);
    } else {
      tokens = await _channel.invokeMethod("WEB_SIGN_IN");
    }

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

  static Future<Map>? forgotPassword(String userName) async {
    if (isInitialized == false) {
      throw Exception("Cannot sign in before initializing Okta SDK");
    }

    if (Platform.isAndroid) {
      return await _channel.invokeMethod("FORGOT_PASSWORD", [
        {
          "username": userName,
          "orgDomain": 'https://dev-24779440.okta.com/',
        }
      ]);
    } else {
      return await _channel.invokeMethod("FORGOT_PASSWORD", {
        "username": userName,
      });
    }
  }
}
