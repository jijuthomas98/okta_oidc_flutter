package com.jiju.thomas.okta_oidc_flutter.idxOperations

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jiju.thomas.okta_oidc_flutter.OktaOidcFlutterPlugin
import kotlinx.coroutines.*

class SocialRedirectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, this::class.java)
        intent.action = "SocialRedirect"
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.data = getIntent().data
        println(getIntent().data.toString())
        startActivity(intent)
        finish()
    }

    public override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == "SocialRedirect") {
            intent.data?.let {
                val scope = CoroutineScope(Dispatchers.IO + Job())
                scope.launch {
                    Authentication.fetchTokens(it)
                }
            }
        }
    }
}