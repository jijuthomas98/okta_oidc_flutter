package com.jiju.thomas.okta_oidc_flutter.idxOperations


import android.content.Context
import android.net.Uri
import com.jiju.thomas.okta_oidc_flutter.OktaOidcFlutterPlugin
import com.jiju.thomas.okta_oidc_flutter.utils.OktaClient
import com.okta.authfoundation.client.OidcClientResult
import com.okta.authfoundationbootstrap.CredentialBootstrap
import com.okta.idx.kotlin.client.IdxFlow
import com.okta.idx.kotlin.client.IdxFlow.Companion.createIdxFlow
import com.okta.idx.kotlin.client.IdxRedirectResult
import com.okta.idx.kotlin.dto.IdxResponse
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.*


object Authentication {
    private var flow: IdxFlow? = null
    private lateinit var methodChannelResultForAuth: MethodChannel.Result


    fun init(methodChannelResult: MethodChannel.Result) {
        methodChannelResultForAuth = methodChannelResult
    }

    fun registerUserWithCredentials(
        email: String,
        password: String,
        methodChannelResult: MethodChannel.Result
    ) {

        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            val result = createCoroutineClient(methodChannelResult)
            if (result != null) {
                AuthenticationImpl.handleRegisterWithCredentials(
                    result,
                    email,
                    password,
                    methodChannelResult, flow
                )
            }
        }
    }

    fun registerUserWithGoogle(methodChannelResult: MethodChannel.Result, context: Context) {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            val result = createCoroutineClient(methodChannelResult)
            if (result != null) {
                AuthenticationImpl.handleRegisterWithGoogle(
                    result,
                    context, flow
                )
            }
        }
    }

    fun signInWithCredentials(
        email: String,
        password: String,
        newPassword: String?,
        methodChannelResult: MethodChannel.Result,
    ) {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            val result = createCoroutineClient(methodChannelResult)
            if (result != null) {

                AuthenticationImpl.handleSignInWithCredentials(
                    result,
                    email,
                    password,
                    newPassword,
                    methodChannelResult, flow
                )
            }
        }
    }


    fun logout(methodChannelResult: MethodChannel.Result) {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            val result = createCoroutineClient(methodChannelResult)
            if (result != null) {
                AuthenticationImpl.handleLogout(
                    result, methodChannelResult, flow
                )
            }
        }
    }

    fun fetchTokensFromRedirectUri(uri: Uri) {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            handleEvaluateRedirectUri(uri)
        }
    }

    private suspend fun handleEvaluateRedirectUri(uri: Uri) {
        when (val response = flow?.resume()) {
            is OidcClientResult.Error -> {
                OktaOidcFlutterPlugin.methodResult.error(
                    "FETCH TOKEN FAILED",
                    response.exception.toString(),
                    response.exception.message
                )
            }
            is OidcClientResult.Success -> {
                println("Fetching token")
                when (val redirectResult = flow?.evaluateRedirectUri(uri)) {
                    is IdxRedirectResult.Error -> {
                        println("Fetch token failed")

                        OktaOidcFlutterPlugin.methodResult.error(
                            "FETCH TOKEN FAILED", redirectResult.errorMessage,
                            redirectResult.exception?.message
                        )
                    }

                    is IdxRedirectResult.Tokens -> {
                        println("Fetch token success")
                        CredentialBootstrap.defaultCredential().storeToken(redirectResult.response)
                        val tokenMap = mapOf(
                            "accessToken" to redirectResult.response.accessToken,
                            "userId" to redirectResult.response.idToken!!
                        )

                        OktaOidcFlutterPlugin.methodResult.success(tokenMap)
                        return
                    }
                    else -> {}
                }
            }
            else -> {}
        }

    }


    private suspend fun createCoroutineClient(
        methodChannelResult: MethodChannel.Result,
    ): IdxResponse? {
        var response: IdxResponse? = null

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
                return null
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
                        return null
                    }
                    is OidcClientResult.Success -> {
                        response = resumeResult.result
                    }
                }
            }
        }
        return response
    }
}

