class OktaResponse {
  final String? id;
  final String? accessToken;
  final bool reEnroll;

  OktaResponse({
    required this.id,
    required this.accessToken,
    required this.reEnroll,
  });

  factory OktaResponse.parse(map) {
    return OktaResponse(
      id: map['userId'] != null ? map['userId'] as String : null,
      accessToken:
          map['accessToken'] != null ? map['accessToken'] as String : null,
      reEnroll: map['reEnroll'] != null ? true : false,
    );
  }
}
