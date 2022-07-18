package com.jiju.thomas.okta_oidc_flutter.idxOperations

import android.content.Context
import com.okta.authfoundation.client.OidcClient
import com.okta.authfoundation.client.OidcConfiguration
import com.okta.authfoundation.credential.CredentialDataSource.Companion.createCredentialDataSource
import com.okta.authfoundationbootstrap.CredentialBootstrap
import okhttp3.HttpUrl.Companion.toHttpUrl


class CredentialDataSource {
    fun initialize(context: Context, clientId: String, discoveryUrl: String) {
        val oidcConfiguration = OidcConfiguration(
            clientId = clientId,
            defaultScope = "openid email profile offline_access",
        )
        val oidcClient = OidcClient.createFromDiscoveryUrl(
            oidcConfiguration,
            "${discoveryUrl}/.well-known/openid-configuration".toHttpUrl(),
        )
        CredentialBootstrap.initialize(oidcClient.createCredentialDataSource(context))
    }
}