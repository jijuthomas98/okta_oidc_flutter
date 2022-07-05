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
                onPressed: () {
                  var result = OktaOidcFlutter.signInWithCredentials(
                    email: 'jiju.thomas@tifin.com',
                    password: 'Thombra15@',
                    orgDomain: 'https://dev-24779440.okta.com/',
                  );
                  print(result);
                },
                child: const Text('Sign In'),
              ),
              TextButton(
                onPressed: () {
                  var result = OktaOidcFlutter.getAccessToken();
                  print(result);
                },
                child: const Text('Get access token'),
              ),
              TextButton(
                onPressed: () {
                  var result = OktaOidcFlutter.isAuthenticated();
                  print(result);
                },
                child: const Text('Is Authenticated'),
              ),
              TextButton(
                onPressed: () {
                  var result = OktaOidcFlutter.signOut();
                  print(result);
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
