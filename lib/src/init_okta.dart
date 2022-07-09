class InitOkta {
  final String clientId;
  final String? issuer;
  final String endSessionRedirectUri;
  final String redirectUrl;
  final List<String> scopes;
  final bool requireHardwareBackedKeyStore;
  final String? idp;

  InitOkta({
    required this.clientId,
    required this.endSessionRedirectUri,
    required this.redirectUrl,
    required this.scopes,
    required this.requireHardwareBackedKeyStore,
    this.idp,
    this.issuer,
  });

  Map<String, dynamic> toMap() {
    return {
      'clientId': clientId,
      'issuer': issuer,
      'endSessionRedirectUri': endSessionRedirectUri,
      'redirectUrl': redirectUrl,
      'scopes': scopes,
      'requireHardwareBackedKeyStore': requireHardwareBackedKeyStore,
      'idp': idp,
    };
  }
}
