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
                  await OktaOidcFlutter.initOkta(
                    InitOkta(
                        clientId: '0oa5gieiczjZLXlnd5d7',
                        issuer: 'https://dev-24779440.okta.com/oauth2/default',
                        endSessionRedirectUri:
                            'com.magnifi.app.staging:/splash',
                        redirectUrl: 'com.magnifi.app.staging:/app',
                        scopes: [
                          'openid',
                          'profile',
                          'email',
                          'offline_access'
                        ],
                        requireHardwareBackedKeyStore: false),
                  );
                  await OktaOidcFlutter.signInWithCredentials(
                    email: 'gokul.krishnan@tifin.com',
                    password: 'tPEGc96\$tT!7z',
                  );
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
                  await OktaOidcFlutter.sso(idp: '0oa5o7sccuy5YgrIz5d7');
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
