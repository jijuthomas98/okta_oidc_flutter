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

    OktaOidcFlutter.initOkta(InitOkta(
        clientId: '0oa5gieiczjZLXlnd5d7',
        issuer: 'https://dev-24779440.okta.com/oauth2/default',
        endSessionRedirectUri: 'com.magnifi.app.staging:/splash',
        redirectUrl: 'com.magnifi.app.staging:/app',
        scopes: ['openid', 'profile', 'email', 'offline_access'],
        requireHardwareBackedKeyStore: false));
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: TextButton(
            child: const Text('Sign in'),
            onPressed: () async {
              OktaTokens o = await OktaOidcFlutter.signInWithCredentials(
                  'gokul.krishnan@tifin.com', 'tPEGc96\$tT!7z');
              print(o);
            },
          ),
        ),
      ),
    );
  }
}
