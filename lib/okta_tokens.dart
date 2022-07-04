class OktaTokens {
  final String idToken;
  final String accessToken;
  final String refreshToken;
  final int? expiresIn;
  final List<String>? scope;
  final double? expiresAt;

  OktaTokens({
    required this.idToken,
    required this.accessToken,
    required this.refreshToken,
    this.expiresIn,
    this.scope,
    this.expiresAt,
  });

  factory OktaTokens.fromMap(Map<String, dynamic> map) {
    return OktaTokens(
      idToken: map['idToken'],
      accessToken: map['accessToken'],
      refreshToken: map['refreshToken'],
      expiresIn: map['expiresIn']?.toInt(),
      scope: List<String>.from(map['scope']),
      expiresAt: map['expiresAt']?.toDouble(),
    );
  }
}
