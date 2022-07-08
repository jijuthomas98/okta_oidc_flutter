import 'dart:async';

import 'package:flutter/material.dart';
import 'package:okta_oidc_flutter/okta_oidc_flutter_export.dart';

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
          discoveryUrl: 'https://dev-24779440.okta.com/oauth2/default',
          endSessionRedirectUri: 'com.magnifi.app.staging:/splash',
          redirectUrl: 'com.magnifi.app.staging:/app',
          scopes: ['openid', 'profile', 'email', 'offline_access'],
        ),
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
                    email: 'jiju.thomas@tifin.com',
                    password: 'Thombra15@',
                    orgDomain: 'https://dev-24779440.okta.com/',
                  );
                  print(tokens.accessToken);
                },
                child: const Text('Sign In'),
              ),
              TextButton(
                onPressed: () async {
                  bool isAuthenticated =
                      await OktaOidcFlutter.isAuthenticated();
                  if (isAuthenticated) {
                    print(isAuthenticated);
                    ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('User Authenticated')));
                  }
                },
                child: const Text('Is Authenticated'),
              ),
              TextButton(
                onPressed: () async {
                  bool result = await OktaOidcFlutter.signOut();
                  if (result) {
                    print(result);
                    ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('Signed out')));
                  }
                },
                child: const Text('Sign Out'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
