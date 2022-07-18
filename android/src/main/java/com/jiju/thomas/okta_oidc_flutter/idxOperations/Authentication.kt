package com.jiju.thomas.okta_oidc_flutter.idxOperations

import com.jiju.thomas.okta_oidc_flutter.operations.SignIn
import com.jiju.thomas.okta_oidc_flutter.utils.OktaClient
import com.okta.authfoundation.client.OidcClientResult
import com.okta.authfoundation.credential.RevokeTokenType
import com.okta.authfoundationbootstrap.CredentialBootstrap
import com.okta.idx.kotlin.client.IdxFlow
import com.okta.idx.kotlin.client.IdxFlow.Companion.createIdxFlow
import com.okta.idx.kotlin.dto.IdxAuthenticator
import com.okta.idx.kotlin.dto.IdxRemediation
import com.okta.idx.kotlin.dto.IdxResponse
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.*

@Suppress("NAME_SHADOWING")
object Authentication {

    private var flow: IdxFlow? = null


    fun registerUserWithCredentials(email: String, password: String, result: MethodChannel.Result) {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            createCoroutineClient(email, password, result, false)
        }
    }

    fun registerUserWithGoogle(idp: String, methodChannelResult: MethodChannel.Result) {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            scope.launch {
                createCoroutineClient("", "", methodChannelResult, false)
            }
        }
    }

    fun signInWithCredentials(
        email: String,
        password: String,
        methodChannelResult: MethodChannel.Result
    ) {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            scope.launch {
                createCoroutineClient(email, password, methodChannelResult, true)
            }
        }
    }



    fun logout(methodChannelResult: MethodChannel.Result){
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
                                methodChannelResult,flow
                            )
                        } else if (email.isNotEmpty() && password.isNotEmpty() && isSignIn) {

                            AuthenticationImpl.handleSignInWithCredentials(
                                resumeResult.result,
                                email,
                                password,
                                methodChannelResult, flow
                            )
                        } else {
                            //handle register user with google
                        }
                    }
                }
            }
        }
    }


    private suspend fun handleRegisterWithGoogleResponse(
        idp: String,
        response: IdxResponse,
        methodChannelResult: MethodChannel.Result,
    ) {
        if (response.isLoginSuccessful) {
            when (val result =
                flow?.exchangeInteractionCodeForTokens(response.remediations[IdxRemediation.Type.ISSUE]!!)) {
                is OidcClientResult.Error -> {
                    methodChannelResult.error(
                        "TOKEN ERROR",
                        result.exception.message,
                        result.exception.cause?.message
                    )
                    return
                }

                is OidcClientResult.Success -> {
                    CredentialBootstrap.defaultCredential().storeToken(result.result)
                    val tokenMap = mapOf(
                        "accessToken" to result.result.accessToken,
                        "userId" to result.result.idToken!!
                    )
                    methodChannelResult.success(tokenMap)
                    return

                }
                else -> {
                    handleRegisterWithGoogleResponse(
                        idp,
                        response,
                        methodChannelResult
                    )
                }
            }
            return
        }


    }
}

