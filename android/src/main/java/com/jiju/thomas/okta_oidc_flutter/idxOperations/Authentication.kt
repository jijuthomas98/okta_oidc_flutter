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
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.*



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
            createCoroutineClient(email, password,  methodChannelResult, false,context,false,null)
        }
    }

    fun registerUserWithGoogle(methodChannelResult: MethodChannel.Result,context: Context) {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            createCoroutineClient("", "", methodChannelResult, false,context,false,null)
        }
    }

    fun signInWithCredentials(
        email: String,
        password: String,
        newPassword: String?,
        methodChannelResult: MethodChannel.Result,context: Context
    ) {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            createCoroutineClient(email, password,  methodChannelResult, true,context,false,newPassword)
        }
    }


    fun logout(methodChannelResult: MethodChannel.Result,context: Context) {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            createCoroutineClient("","",methodChannelResult,false,context,true , null)
        }
    }

    fun fetchTokens(uri: Uri){
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            handleFetchToken(uri)
        }
    }

   private suspend fun handleFetchToken(uri: Uri){
        when(val response =  flow?.resume()){
            is OidcClientResult.Error ->{
            OktaOidcFlutterPlugin.methodResult.error("FETCH TOKEN FAILED",response.exception.toString(),response.exception.message)
            }
            is OidcClientResult.Success ->{
                println("Fetching token")
                when (val redirectResult = flow?.evaluateRedirectUri(uri)) {
                    is IdxRedirectResult.Error -> {
                        println("fetch token failed")

                        OktaOidcFlutterPlugin.methodResult.error("FETCH TOKEN FAILED",redirectResult.errorMessage,
                            redirectResult.exception?.message
                        )
                    }

                    is IdxRedirectResult.Tokens -> {
                        println("fetch token success")
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
        email: String,
        password: String,
        methodChannelResult: MethodChannel.Result,
        isSignIn: Boolean,
        context: Context,
        isLogoutRequest: Boolean,
        newPassword:String?
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
                                newPassword,
                                methodChannelResult, flow
                            )
                        }else if(isLogoutRequest){
                            AuthenticationImpl.handleLogout(resumeResult.result,methodChannelResult,
                                flow)
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

