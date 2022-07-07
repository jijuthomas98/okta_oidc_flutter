class OktaTokens {
  final String? id;
  final String? accessToken;

  OktaTokens({
    this.id,
    this.accessToken,
  });

  factory OktaTokens.parse(map) {
    return OktaTokens(
      id: map['id'],
      accessToken: map['access_token'],
    );
  }
}
