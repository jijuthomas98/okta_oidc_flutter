import 'package:flutter/material.dart';
import 'package:okta_oidc_flutter/okta_oidc_flutter.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  OktaOidcFlutter? oktaOidcFlutter;
  @override
  void initState() {
    super.initState();

    OktaOidcFlutter.instance.initOkta(
      InitOkta(
        clientId: '0oa1k4uyv06twnAW8697',
        issuer: 'https://magnifi-dev.okta.com/oauth2/default',
        endSessionRedirectUri: 'com.magnifi.app.staging:/app',
        redirectUrl: 'com.magnifi.app.staging:/app',
        scopes: ['openid', 'profile', 'email', 'offline_access'],
      ),
    );
  }

  int f = 0;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              TextButton(
                onPressed: () async {
                  // await OktaOidcFlutter.instance.initOkta(
                  //   InitOkta(
                  //     clientId: '0oa1k4uyv06twnAW8697',
                  //     issuer: 'https://magnifi-dev.okta.com/oauth2/default',
                  //     endSessionRedirectUri: 'com.magnifi.app.staging:/splash',
                  //     redirectUrl: 'com.magnifi.app.staging:/app',
                  //     scopes: ['openid', 'profile', 'email', 'offline_access'],
                  //   ),
                  // );
                  OktaTokens token =
                      await OktaOidcFlutter.instance.signInWithCredentials(
                    email: 'gokul2+0@magnifi.com',
                    password: '12345!Aa',
                  );

                  // OktaTokens token = await OktaOidcFlutter.instance
                  //     .registerWithCreds('gokul3+$f@magnifi.com', '12345!Aa');

                  // setState(() {
                  //   f++;
                  // });
                  print(token.id);
                },
                child: const Text('Sign In'),
              ),
              TextButton(
                onPressed: () async {
                  bool result = await OktaOidcFlutter.instance.signOut();
                  print(result);
                },
                child: const Text('Sign Out'),
              ),
              TextButton(
                onPressed: () async {
                  await OktaOidcFlutter.instance.initOkta(
                    InitOkta(
                        clientId: '0oa1k4uyv06twnAW8697',
                        issuer: 'https://magnifi-dev.okta.com/oauth2/default',
                        endSessionRedirectUri:
                            'com.magnifi.app.staging:/splash',
                        redirectUrl: 'com.magnifi.app.staging:/app',
                        scopes: [
                          'openid',
                          'profile',
                          'email',
                          'offline_access'
                        ],
                        idp: "0oa1k4ywmmhmfXmOT697"),
                  );
                  // google - 0oa1k4ywmmhmfXmOT697
                  // apple - 0oa1k4zg0hmpVLa6H697
                  await OktaOidcFlutter.instance
                      .sso(idp: '0oa1k4ywmmhmfXmOT697');
                },
                child: const Text('SSO'),
              ),
              TextButton(
                onPressed: () async {
                  await OktaOidcFlutter.instance.registerWithGoogle();
                },
                child: const Text('Register with google'),
              ),
              TextButton(
                onPressed: () async {
                  OktaTokens token =
                      await OktaOidcFlutter.instance.registerWithCreds(
                    'jiju.thomas987879asasas89@tifin.com',
                    'tPEGc96\$tT!7z',
                  );
                  print(token.accessToken);
                },
                child: const Text('Register'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
