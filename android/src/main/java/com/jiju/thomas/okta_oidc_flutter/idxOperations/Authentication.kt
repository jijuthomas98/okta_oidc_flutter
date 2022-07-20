package com.jiju.thomas.okta_oidc_flutter.idxOperations

import android.R.attr.identifier
import android.content.Context
import android.net.Uri
import com.jiju.thomas.okta_oidc_flutter.OktaOidcFlutterPlugin
import com.jiju.thomas.okta_oidc_flutter.utils.OktaClient
import com.jiju.thomas.okta_oidc_flutter.utils.OktaRequestParameters
import com.okta.authfoundation.client.OidcClientResult
import com.okta.authfoundationbootstrap.CredentialBootstrap
import com.okta.idx.kotlin.client.IdxFlow
import com.okta.idx.kotlin.client.IdxFlow.Companion.createIdxFlow
import com.okta.idx.kotlin.client.IdxRedirectResult
import com.okta.oidc.net.request.web.LogoutRequest
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.*


@Suppress("NAME_SHADOWING")
object Authentication {
    private var flow: IdxFlow? = null
    private lateinit var methodChannelResultForAuth: MethodChannel.Result

    fun init( methodChannelResult: MethodChannel.Result) {
        methodChannelResultForAuth = methodChannelResult
    }

    fun registerUserWithCredentials(
        email: String,
        password: String,
        methodChannelResult: MethodChannel.Result,context: Context
    ) {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            createCoroutineClient(email, password,  methodChannelResult, false,context,false)
        }
    }

    fun registerUserWithGoogle(methodChannelResult: MethodChannel.Result,context: Context) {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            createCoroutineClient("", "", methodChannelResult, false,context,false)
        }
    }

    fun signInWithCredentials(
        email: String,
        password: String,
        methodChannelResult: MethodChannel.Result,context: Context
    ) {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            createCoroutineClient(email, password,  methodChannelResult, true,context,false)
        }
    }


    fun logout(methodChannelResult: MethodChannel.Result,context: Context) {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            createCoroutineClient("","",methodChannelResult,false,OktaOidcFlutterPlugin.context,true )
        }
    }

    fun fetchTokens(uri: Uri){
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            handleFetchToken(uri)
        }
    }

   private suspend fun handleFetchToken(uri: Uri){
        when(val resumeResponse = flow?.resume()){
            is OidcClientResult.Error ->{

            }
            is OidcClientResult.Success ->{
                when (val redirectResult = flow?.evaluateRedirectUri(uri)) {
                    is IdxRedirectResult.Error -> {

                    }

                    is IdxRedirectResult.Tokens -> {
                        CredentialBootstrap.defaultCredential().storeToken(redirectResult.response)
                        val tokenMap = mapOf(
                            "accessToken" to redirectResult.response.accessToken,
                            "userId" to redirectResult.response.idToken!!
                        )

                        OktaOidcFlutterPlugin.methodResult.success(tokenMap)
                        return
                    }
                }
            }
        }

    }


    private suspend fun createCoroutineClient(
        email: String,
        password: String,
        methodChannelResult: MethodChannel.Result,
        isSignIn: Boolean,
        context: Context,
        isLogoutRequest: Boolean,
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
                        }else if(isLogoutRequest){
                            AuthenticationImpl.handleLogout(resumeResult.result ,methodChannelResult, flow)
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

