import 'dart:io';

class InitOkta {
  /// The client id.
  final String clientId;

  /// The redirect URL when session ended.
  final String endSessionRedirectUri;

  /// The redirect URL.
  final String redirectUrl;

  /// The request scopes.
  final List<String> scopes;

  final bool requireHardwareBackedKeyStore;
  final String? issuer;

  final String? idp;

  InitOkta({
    required this.clientId,
    required this.endSessionRedirectUri,
    required this.redirectUrl,
    required this.scopes,
    this.requireHardwareBackedKeyStore = false,
    this.idp,
    this.issuer,
  });

  Map<dynamic, dynamic> toMap() {
    Map<dynamic, dynamic> initOkta = {};

    initOkta['clientId'] = clientId;
    Platform.isIOS ? initOkta['issuer'] = issuer : null;
    Platform.isAndroid ? initOkta['discoveryUri'] = issuer : null;
    initOkta['endSessionRedirectUri'] = endSessionRedirectUri;
    initOkta['redirectUri'] = redirectUrl;
    Platform.isAndroid
        ? initOkta['scopes'] = scopes.join(',')
        : initOkta['scopes'] = scopes;

    Platform.isAndroid
        ? initOkta['requireHardwareBackedKeyStore'] =
            requireHardwareBackedKeyStore ? 'true' : 'false'
        : initOkta['requireHardwareBackedKeyStore'] =
            requireHardwareBackedKeyStore;
    Platform.isIOS ? initOkta['idp'] = idp : null;
    return initOkta;
  }
}
