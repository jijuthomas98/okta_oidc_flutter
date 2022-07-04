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

  final bool requireHardwareBackedKeyStore;

  InitOkta(
    this.clientId,
    this.discoveryUrl,
    this.endSessionRedirectUri,
    this.redirectUrl,
    this.scopes,
    this.requireHardwareBackedKeyStore,
  );

  Map<String, dynamic> toMap() {
    return {
      'clientId': clientId,
      'discoveryUrl': discoveryUrl,
      'endSessionRedirectUri': endSessionRedirectUri,
      'redirectUrl': redirectUrl,
      'scopes': scopes,
      'requireHardwareBackedKeyStore': requireHardwareBackedKeyStore,
    };
  }
}
