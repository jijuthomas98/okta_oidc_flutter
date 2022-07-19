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
                  var token =
                      await OktaOidcFlutter.instance.signInWithCredentials(
                    email: 'gokul.krishnan@tifin.com',
                    password: 'tPEGc96\$tT!7z',
                  );
                  print(token.accessToken);
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
                  await OktaOidcFlutter.instance
                      .sso(idp: '0oa5o7sccuy5YgrIz5d7');
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
