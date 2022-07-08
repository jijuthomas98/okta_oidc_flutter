class OktaTokens {
  final String? id;
  final String? accessToken;

  OktaTokens({
    this.id,
    this.accessToken,
  });

  factory OktaTokens.parse(map) {
    return OktaTokens(
      id: map['userId'] as String,
      accessToken: map['accessToken'] as String,
    );
  }
}
