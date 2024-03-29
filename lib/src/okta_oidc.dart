import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

import '../okta_oidc_flutter.dart';

class OktaOidcFlutter {
  static const MethodChannel _channel = MethodChannel('okta_oidc_flutter');

  static OktaOidcFlutter? _instance;
  OktaOidcFlutter._();
  static OktaOidcFlutter get instance => _instance ??= OktaOidcFlutter._();

  bool isInitialized = false;

  /// Initialize Okta OIDC
  Future<void> initOkta(InitOkta request) async {
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

  Future<OktaTokens> signInWithCredentials({
    required String email,
    required String password,
  }) async {
    if (isInitialized == false) {
      throw Exception("Cannot sign in before initializing Okta SDK");
    }
    Map? tokens;

    if (Platform.isAndroid) {
      tokens = await _channel
          .invokeMethod<Map<dynamic, dynamic>>("SIGN_IN_WITH_CREDENTIAL", [
        {"email": email, "password": password}
      ]);
    } else {
      tokens = await _channel.invokeMethod(
        "SIGN_IN_WITH_CREDENTIAL",
        {'username': email, 'password': password},
      );
    }

    return OktaTokens.parse(tokens);
  }

  Future<OktaTokens> sso({
    required String idp,
  }) async {
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
      tokens = await _channel.invokeMethod("WEB_SIGN_IN", idp);
    }
    return OktaTokens.parse(tokens);
  }

  /// Sign out by revoking okta tokens
  Future<bool> signOut() async {
    if (isInitialized == false) {
      throw Exception("Cannot sign in before initializing Okta SDK");
    }

    return await _channel.invokeMethod("SIGN_OUT") as bool;
  }

  Future<Map>? forgotPassword(String userName, String domainUrl) async {
    if (isInitialized == false) {
      throw Exception("Cannot sign in before initializing Okta SDK");
    }

    if (Platform.isAndroid) {
      return await _channel.invokeMethod("FORGOT_PASSWORD", [
        {
          "username": userName,
          "orgDomain": domainUrl,
        }
      ]);
    } else {
      return await _channel.invokeMethod("FORGOT_PASSWORD", {
        "username": userName,
      });
    }
  }

  Future<OktaTokens> registerWithCreds(String email, String password) async {
    // ignore: prefer_typing_uninitialized_variables
    var tokens;
    if (Platform.isAndroid) {
      tokens = await _channel.invokeMethod("REGISTER_WITH_CREDENTIAL", [
        {
          "email": email,
          "password": password,
        }
      ]);
    } else {
      tokens = await _channel.invokeMethod("REGISTER_WITH_CREDENTIAL", {
        "email": email,
        "password": password,
      });
    }

    return OktaTokens.parse(tokens);
  }

  Future<OktaTokens> registerWithGoogle() async {
    if (isInitialized == false) {
      throw Exception("Cannot sign in before initializing Okta SDK");
    }
    // ignore: prefer_typing_uninitialized_variables

    var tokens;
    if (Platform.isAndroid) {
      tokens = await _channel.invokeMethod("REGISTER_WITH_GOOGLE");
    }
    print(tokens);
    return OktaTokens.parse(tokens);
  }
}
