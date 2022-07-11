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
  @override
  void initState() {
    super.initState();
    initOktaOidc();
  }

  Future<void> initOktaOidc() async {
    try {
      OktaOidcFlutter.initOkta(
        InitOkta(
            clientId: '0oa5gieiczjZLXlnd5d7',
            endSessionRedirectUri: 'com.magnifi.app.staging:/app',
            redirectUrl: 'com.magnifi.app.staging:/app',
            scopes: ['openid', 'profile', 'email', 'offline_access'],
            issuer: 'https://dev-24779440.okta.com/oauth2/default'),
      );
    } catch (e) {
      print(e);
    }
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
                  OktaTokens tokens =
                      await OktaOidcFlutter.signInWithCredentials(
                    'jiju.thomas@tifin.com',
                    'Thombra15@',
                  );
                  print(tokens.accessToken);
                },
                child: const Text('Sign In'),
              ),
              TextButton(
                onPressed: () async {
                  bool isAuthenticated =
                      await OktaOidcFlutter.isAuthenticated();

                  print(isAuthenticated);
                },
                child: const Text('Is Authenticated'),
              ),
              TextButton(
                onPressed: () async {
                  bool result = await OktaOidcFlutter.signOut();

                  print(result);
                },
                child: const Text('Sign Out'),
              ),
              TextButton(
                onPressed: () async {
                  await OktaOidcFlutter.sso();
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
