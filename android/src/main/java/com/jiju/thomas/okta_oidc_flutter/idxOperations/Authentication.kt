package com.jiju.thomas.okta_oidc_flutter.idxOperations

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.jiju.thomas.okta_oidc_flutter.utils.OktaClient
import com.okta.authfoundation.client.OidcClientResult
import com.okta.authfoundationbootstrap.CredentialBootstrap
import com.okta.idx.kotlin.client.IdxFlow
import com.okta.idx.kotlin.client.IdxFlow.Companion.createIdxFlow
import com.okta.idx.kotlin.dto.IdxIdpCapability
import com.okta.idx.kotlin.dto.IdxRemediation
import com.okta.idx.kotlin.dto.IdxResponse
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.*

@Suppress("NAME_SHADOWING")
object Authentication {

    private var flow: IdxFlow? = null


    fun registerUserWithCredentials(
        email: String,
        password: String,
        methodChannelResult: MethodChannel.Result,context: Context
    ) {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            createCoroutineClient(email, password,  methodChannelResult, false,context)
        }
    }

    fun registerUserWithGoogle(methodChannelResult: MethodChannel.Result,context: Context) {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            scope.launch {
                createCoroutineClient("", "", methodChannelResult, false,context)
            }
        }
    }

    fun signInWithCredentials(
        email: String,
        password: String,
        methodChannelResult: MethodChannel.Result,context: Context
    ) {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            scope.launch {
                createCoroutineClient(email, password,  methodChannelResult, true,context)
            }
        }
    }


    fun logout(methodChannelResult: MethodChannel.Result) {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            scope.launch {
                AuthenticationImpl.handleLogout(methodChannelResult)
            }
        }
    }


    private suspend fun createCoroutineClient(
        email: String,
        password: String,

        methodChannelResult: MethodChannel.Result,
        isSignIn: Boolean,
        context: Context
    ) {

        when (
            val clientResult = CredentialBootstrap.oidcClient.createIdxFlow(
                redirectUrl = OktaClient.getInstance().config.redirectUri.toString(),
            )) {
            is OidcClientResult.Error -> {
                methodChannelResult.error(
                    "OIDC CLIENT INIT FAILED",
                    clientResult.exception.message,
                    clientResult.exception.cause?.message
                )
                return
            }

            is OidcClientResult.Success -> {
                flow = clientResult.result
                when (val resumeResult = clientResult.result.resume()) {
                    is OidcClientResult.Error -> {
                        methodChannelResult.error(
                            "RESUME FAILED",
                            resumeResult.exception.message,
                            resumeResult.exception.cause?.message
                        )
                        return
                    }

                    is OidcClientResult.Success -> {

                        if (email.isNotEmpty() && password.isNotEmpty() && !isSignIn) {

                            AuthenticationImpl.handleRegisterWithCredentialsResponse(
                                resumeResult.result,
                                email,
                                password,
                                methodChannelResult, flow
                            )
                        } else if (email.isNotEmpty() && password.isNotEmpty() && isSignIn) {

                            AuthenticationImpl.handleSignInWithCredentials(
                                resumeResult.result,
                                email,
                                password,
                                methodChannelResult, flow
                            )
                        } else {
                           AuthenticationImpl.handleRegisterWithGoogleResponse(
                                resumeResult.result,
                                methodChannelResult,
                                context, flow
                            )
                        }
                    }
                }
            }
        }
    }
}

