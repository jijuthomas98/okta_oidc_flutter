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

  final bool? requireHardwareBackedKeyStore;
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

  Map<String, dynamic> toMap() {
    return {
      'clientId': clientId,
      'issuer': issuer,
      'discoveryUri': issuer,
      'endSessionRedirectUri': endSessionRedirectUri,
      'redirectUri': redirectUrl,
      'scopes': Platform.isAndroid ? scopes.join(',') : scopes,
      'requireHardwareBackedKeyStore': false,
      'idp': idp,
    };
  }
}
