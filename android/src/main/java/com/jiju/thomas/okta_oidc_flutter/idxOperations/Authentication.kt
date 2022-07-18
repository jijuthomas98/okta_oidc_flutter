package com.jiju.thomas.okta_oidc_flutter.idxOperations

import com.jiju.thomas.okta_oidc_flutter.utils.OktaClient
import com.okta.authfoundation.client.OidcClientResult
import com.okta.authfoundationbootstrap.CredentialBootstrap
import com.okta.idx.kotlin.client.IdxFlow
import com.okta.idx.kotlin.client.IdxFlow.Companion.createIdxFlow
import com.okta.idx.kotlin.dto.IdxRemediation
import com.okta.idx.kotlin.dto.IdxResponse
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.*

@Suppress("NAME_SHADOWING")
class Authentication {

    private var flow: IdxFlow? = null

    fun registerUserWithCredentials(email: String, password: String, result: MethodChannel.Result) {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            createCoroutineClient(email, password, result)
        }
    }

    fun registerUserWithGoogle(idp: String, methodChannelResult: MethodChannel.Result) {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            scope.launch {
                createCoroutineClient("", "", methodChannelResult)
            }
        }
    }

    private suspend fun createCoroutineClient(
        email: String,
        password: String,
        methodChannelResult: MethodChannel.Result
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
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            handleRegisterWithCredentialsResponse(
                                resumeResult.result,
                                email,
                                password,
                                methodChannelResult
                            )
                        } else {
                            println("SOCIAL LOGIN")
                        }
                    }
                }
            }
        }
    }


    private suspend fun handleRegisterWithGoogleResponse(

        idp: String,
        response: IdxResponse,
        methodChannelResult: MethodChannel.Result

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

        for ()
    }

    private suspend fun handleRegisterWithCredentialsResponse(
        response: IdxResponse,
        email: String,
        password: String,
        methodChannelResult: MethodChannel.Result
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
                    handleRegisterWithCredentialsResponse(
                        response,
                        email,
                        password,
                        methodChannelResult
                    )
                }
            }
            return
        }


        for (remediation in response.remediations) {
            if (remediation.type == IdxRemediation.Type.SELECT_ENROLL_PROFILE) {
                when (val selectEnrollProfileResponse =
                    flow?.proceed(response.remediations[IdxRemediation.Type.SELECT_ENROLL_PROFILE]!!)) {
                    is OidcClientResult.Error -> {
                        methodChannelResult.error(
                            "SELECT_ENROLL_PROFILE FAILED",
                            selectEnrollProfileResponse.exception.message,
                            selectEnrollProfileResponse.exception.cause?.message
                        )
                        return
                    }
                    is OidcClientResult.Success -> {

                        val enrollProfileRemediation =
                            selectEnrollProfileResponse.result.remediations[IdxRemediation.Type.ENROLL_PROFILE]
                        enrollProfileRemediation!!["userProfile.email"]?.value =
                            email
                        enrollProfileRemediation["userProfile.magnifi_self_role"]?.value =
                            "Individual Investor"

                        when (val enrollProfileResponse =
                            flow?.proceed(enrollProfileRemediation)) {

                            is OidcClientResult.Error -> {
                                methodChannelResult.error(
                                    "ENROLL_PROFILE FAILED",
                                    enrollProfileResponse.exception.message,
                                    enrollProfileResponse.exception.cause?.message
                                )
                                return
                            }


                            is OidcClientResult.Success -> {
                                for (rem in enrollProfileResponse.result.remediations) {
                                    if (rem.type != IdxRemediation.Type.SELECT_AUTHENTICATOR_ENROLL) {
                                        methodChannelResult.error(
                                            "USER ALREADY REGISTERED",
                                            "enrollProfileResponse failed",
                                            "enrollProfileResponse"
                                        )
                                        return

                                    }

                                    val selectAuthenticatorEnrollRemediation =
                                        enrollProfileResponse.result.remediations[IdxRemediation.Type.SELECT_AUTHENTICATOR_ENROLL]

                                    val authenticationOption =
                                        selectAuthenticatorEnrollRemediation?.form?.get("authenticator")?.options?.get(
                                            0
                                        )
                                    if (selectAuthenticatorEnrollRemediation != null) {
                                        selectAuthenticatorEnrollRemediation.form["authenticator"]?.selectedOption =
                                            authenticationOption

                                        when (val selectAuthenticatorEnrollResponse =
                                            flow?.proceed(selectAuthenticatorEnrollRemediation)) {
                                            is OidcClientResult.Error -> {
                                                methodChannelResult.error(
                                                    "SELECT_AUTHENTICATOR_ENROLL FAILED",
                                                    selectAuthenticatorEnrollResponse.exception.message,
                                                    selectAuthenticatorEnrollResponse.exception.cause?.message
                                                )
                                                return
                                            }

                                            is OidcClientResult.Success -> {
                                                val remediation =
                                                    selectAuthenticatorEnrollResponse.result.remediations[IdxRemediation.Type.ENROLL_AUTHENTICATOR]
                                                val passcode =
                                                    remediation?.get("credentials.passcode")
                                                if (passcode != null) {
                                                    passcode.value = password

                                                    when (val passcodeResponse =
                                                        flow?.proceed(remediation)) {
                                                        is OidcClientResult.Error -> {
                                                            methodChannelResult.error(
                                                                "ENROLL_AUTHENTICATOR FAILED",
                                                                passcodeResponse.exception.message,
                                                                passcodeResponse.exception.cause?.message
                                                            )
                                                            return
                                                        }
                                                        is OidcClientResult.Success -> {
                                                            val skipRemediation =
                                                                passcodeResponse.result.remediations[IdxRemediation.Type.SKIP]
                                                            if (skipRemediation != null) {
                                                                when (val skipResponse =
                                                                    flow?.proceed(skipRemediation)) {
                                                                    is OidcClientResult.Error -> {
                                                                        methodChannelResult.error(
                                                                            "SKIP FAILED",
                                                                            skipResponse.exception.message,
                                                                            skipResponse.exception.cause?.message
                                                                        )
                                                                        return
                                                                    }
                                                                    is OidcClientResult.Success -> {
                                                                        if (skipResponse.result.isLoginSuccessful) {
                                                                            when (val finalResponse =
                                                                                flow?.exchangeInteractionCodeForTokens(
                                                                                    skipResponse.result.remediations[IdxRemediation.Type.ISSUE]!!
                                                                                )) {
                                                                                is OidcClientResult.Error -> {
                                                                                    methodChannelResult.error(
                                                                                        "TOKEN ERROR",
                                                                                        finalResponse.exception.message,
                                                                                        finalResponse.exception.cause?.message
                                                                                    )
                                                                                    return
                                                                                }

                                                                                is OidcClientResult.Success -> {
                                                                                    val tokenMap =
                                                                                        mapOf(
                                                                                            "accessToken" to finalResponse.result.accessToken,
                                                                                            "userId" to finalResponse.result.idToken!!
                                                                                        )
                                                                                    methodChannelResult.success(
                                                                                        tokenMap
                                                                                    )
                                                                                    return
                                                                                }
                                                                                else -> {}
                                                                            }

                                                                        }
                                                                    }
                                                                    else -> {}
                                                                }
                                                            }
                                                        }
                                                        else -> {}
                                                    }
                                                }
                                            }
                                            else -> {}
                                        }
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}