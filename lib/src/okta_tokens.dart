class OktaTokens {
  final String? id;
  final String? accessToken;

  OktaTokens({
    required this.id,
    required this.accessToken,
  });

  factory OktaTokens.parse(map) {
    return OktaTokens(
      id: map['userId'] != null ? map['userId'] as String : null,
      accessToken:
          map['accessToken'] != null ? map['accessToken'] as String : null,
    );
  }
}
