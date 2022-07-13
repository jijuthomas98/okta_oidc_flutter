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
        clientId: '0oa5gieiczjZLXlnd5d7',
        issuer: 'https://dev-24779440.okta.com/oauth2/default',
        endSessionRedirectUri: 'com.magnifi.app.staging:/splash',
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
                  bool isAuthenticated =
                      await OktaOidcFlutter.instance.isAuthenticated();
                  print(isAuthenticated);
                },
                child: const Text('Is Authenticated'),
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
            ],
          ),
        ),
      ),
    );
  }
}
