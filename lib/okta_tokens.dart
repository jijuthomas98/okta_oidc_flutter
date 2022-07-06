class OktaTokens {
  final String? id;
  final String? accessToken;

  OktaTokens({
    required this.id,
    required this.accessToken,
  });

  factory OktaTokens.parse(map) {
    return OktaTokens(
      id: map['id'],
      accessToken: map['access_token'],
    );
  }
}
