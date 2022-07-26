import 'package:flutter/material.dart';
import 'package:okta_oidc_flutter/okta_oidc_flutter.dart';

void main() {
  runApp(const MaterialApp(home: MyApp()));
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
        clientId: '0oa1m1yl1txz8Zyyn697',
        issuer: 'https://magnifi.okta.com/oauth2/default',
        endSessionRedirectUri: 'com.magnifi.app.staging:/app',
        redirectUrl: 'com.magnifi.app.staging:/app',
        scopes: ['openid', 'profile', 'email', 'offline_access'],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            TextButton(
              onPressed: () async {
                OktaResponse oktaResposne =
                    await OktaOidcFlutter.instance.signInWithCredentials(
                  email: 're@re.com',
                  password: 'Magnifi@123456',
                );
                if (oktaResposne.reEnroll) {
                  showDialog(
                      context: context,
                      builder: (context) {
                        return AlertDialog(
                          alignment: Alignment.center,
                          title: const Text('reset'),
                          actions: [
                            Center(
                              child: SizedBox(
                                height: 30,
                                width: 100,
                                child: TextButton(
                                  onPressed: () async {
                                    OktaResponse oktaResposne =
                                        await OktaOidcFlutter.instance
                                            .signInWithCredentials(
                                      email: 're@re.com',
                                      password: '3#x8LK*c',
                                      newPassword: 'Magnifi@123456',
                                    );

                                    print(oktaResposne.accessToken);
                                  },
                                  child: const Text('RESET'),
                                ),
                              ),
                            )
                          ],
                        );
                      });
                } else {
                  print(oktaResposne.accessToken!);
                }
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
                      issuer: 'https://magnifi.okta.com/oauth2/default',
                      endSessionRedirectUri: 'com.magnifi.app.staging:/splash',
                      redirectUrl: 'com.magnifi.app.staging:/app',
                      scopes: ['openid', 'profile', 'email', 'offline_access'],
                      idp: "0oa1k4ywmmhmfXmOT697"),
                );
                // google - 0oa1k4ywmmhmfXmOT697
                // apple - 0oa1k4zg0hmpVLa6H697
                await OktaOidcFlutter.instance.sso(idp: '0oa1k4ywmmhmfXmOT697');
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
                OktaResponse token =
                    await OktaOidcFlutter.instance.registerWithCreds(
                  'jijujoel15@tifin.com',
                  'Test100@',
                );
                print(token.accessToken);
              },
              child: const Text('Register'),
            ),
          ],
        ),
      ),
    );
  }
}
