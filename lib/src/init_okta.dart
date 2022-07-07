class InitOkta {
  /// The client id.
  final String clientId;

  /// The URL of where the discovery document can be found.
  final String discoveryUrl;

  /// The redirect URL when session ended.
  final String endSessionRedirectUri;

  /// The redirect URL.
  final String redirectUrl;

  /// The request scopes.
  final List<String> scopes;

  final bool? requireHardwareBackedKeyStore;

  InitOkta({
    required this.clientId,
    required this.discoveryUrl,
    required this.endSessionRedirectUri,
    required this.redirectUrl,
    required this.scopes,
    this.requireHardwareBackedKeyStore = false,
  });

  Map<String, dynamic> toMap() {
    return {
      'clientId': clientId,
      'discoveryUri': discoveryUrl,
      'endSessionRedirectUri': endSessionRedirectUri,
      'redirectUri': redirectUrl,
      'scopes': scopes.join(','),
      'requireHardwareBackedKeyStore': (requireHardwareBackedKeyStore != null &&
              requireHardwareBackedKeyStore!)
          ? 'true'
          : 'false'
    };
  }
}
