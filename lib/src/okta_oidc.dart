import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

import '../okta_oidc_flutter.dart';

class OktaOidcFlutter {
  static const MethodChannel _channel = MethodChannel('okta_oidc_flutter');

  static OktaOidcFlutter? _instance;
  OktaOidcFlutter._();
  static OktaOidcFlutter get instance => _instance ??= OktaOidcFlutter._();

  bool _isInitialized = false;

  /// Initialize Okta OIDC
  Future<void> initOkta(InitOkta request) async {
    _isInitialized = false;
    try {
      if (Platform.isAndroid) {
        await _channel.invokeMethod("CREATE_CONFIG", [request.toMap()]);
      } else {
        await _channel.invokeMethod("CREATE_CONFIG", request.toMap());
      }
      _isInitialized = true;
    } catch (e) {
      _isInitialized = false;
    }
  }

  Future<OktaTokens> signInWithCredentials(
      {required String email,
      required String password,
      required String domainUrl}) async {
    if (_isInitialized == false) {
      throw Exception("Cannot sign in before initializing Okta SDK");
    }
    Map? tokens;

    if (Platform.isAndroid) {
      tokens = await _channel
          .invokeMethod<Map<dynamic, dynamic>>("SIGN_IN_WITH_CREDENTIAL", [
        {
          "email": email,
          "password": password,
          "orgDomain": domainUrl,
        }
      ]);
    } else {
      tokens = await _channel.invokeMethod(
        "SIGN_IN_WITH_CREDENTIAL",
        {'username': email, 'password': password},
      );
    }

    return OktaTokens.parse(tokens);
  }

  Future<OktaTokens> sso({String? idp}) async {
    if (_isInitialized == false) {
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
  Future<bool> signOut() async {
    if (_isInitialized == false) {
      throw Exception("Cannot sign in before initializing Okta SDK");
    }

    return await _channel.invokeMethod("SIGN_OUT") as bool;
  }

  /// Check if app is already Authenticated
  Future<bool> isAuthenticated() async {
    if (_isInitialized == false) {
      throw Exception("Cannot sign in before initializing Okta SDK");
    }
    return await _channel.invokeMethod("IS_AUTHENTICATED") as bool;
  }

  Future<Map>? forgotPassword(String userName, String domainUrl) async {
    if (_isInitialized == false) {
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
}
