package com.jiju.thomas.okta_oidc_flutter.idxOperations


import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.okta.authfoundation.client.OidcClientResult
import com.okta.authfoundation.credential.RevokeTokenType
import com.okta.authfoundationbootstrap.CredentialBootstrap
import com.okta.idx.kotlin.client.IdxFlow
import com.okta.idx.kotlin.dto.IdxIdpCapability
import com.okta.idx.kotlin.dto.IdxRemediation
import com.okta.idx.kotlin.dto.IdxResponse
import io.flutter.plugin.common.MethodChannel


object AuthenticationImpl {

    suspend fun handleLogout(
        response: IdxResponse,
        methodChannelResult: MethodChannel.Result,
        flow: IdxFlow?
    ) {
        if (flow == null) {
            methodChannelResult.success(false)
            return
        }
        println("logout started")
        for (remediation in response.remediations) {
            if (remediation.type == IdxRemediation.Type.CANCEL) {
                when (val cancelResponse = flow.proceed(remediation)) {
                    is OidcClientResult.Error -> {
                        println("canceling error")
                        methodChannelResult.error(
                            "REVOKE_ACCESS_TOKEN Failed in remediation",
                            cancelResponse.exception.message,
                            cancelResponse.exception.cause
                        )
                        return
                    }
                    is OidcClientResult.Success -> {
                        println("canceled success")
                        if (CredentialBootstrap.defaultCredential().token != null) {
                            when (val revokeAccessTokenResponse =
                                CredentialBootstrap.defaultCredential()
                                    .revokeToken(RevokeTokenType.ACCESS_TOKEN)) {
                                is OidcClientResult.Error -> {
                                    println("revoke access token failed")
                                    methodChannelResult.error(
                                        "REVOKE_ACCESS_TOKEN Failed",
                                        revokeAccessTokenResponse.exception.message,
                                        revokeAccessTokenResponse.exception.cause
                                    )
                                    return
                                }
                                is OidcClientResult.Success -> {
                                    println("revoked access token")
                                    when (CredentialBootstrap.defaultCredential().refreshToken()) {
                                        is OidcClientResult.Error -> {
                                            println("revoke refresh token failed")
                                            methodChannelResult.success(false)
                                            return
                                        }
                                        is OidcClientResult.Success -> {
                                            println("revoke refresh token success")
                                            methodChannelResult.success(true)
                                            return
                                        }
                                    }

                                }
                            }
                        } else {
                            methodChannelResult.success(true)
                            return
                        }
                    }
                }
            }
        }
    }


    suspend fun handleSignInWithCredentials(
        response: IdxResponse,
        email: String,
        password: String,
        methodChannelResult: MethodChannel.Result,
        flow: IdxFlow?,
    ) {
        println("start")
        if (flow == null) {
            methodChannelResult.error(
                "FLOW ERROR",
               "failed to init IDXFlow",
                "At handleSignInWithCredentials method"
            )
            println("flow is empty")
            return
        }


        println("Starting remediation check")
        for (remediation in response.remediations) {
            if (remediation.type == IdxRemediation.Type.IDENTIFY) {
                val userName = remediation["identifier"]
                val rememberThisDevice = remediation["rememberMe"]
                userName?.value = email
                rememberThisDevice?.value = true
                when (val identifyResponse = flow.proceed(remediation)) {
                    is OidcClientResult.Error -> {
                        println("IDENTIFY failed")
                        methodChannelResult.error(
                            "IDENTIFY ERROR",
                            identifyResponse.exception.message,
                            identifyResponse.exception.cause?.message
                        )
                        return
                    }
                    is OidcClientResult.Success -> {
                        println("IDENTIFY remediation success")
                        val challengeAuthenticatorRemediation = identifyResponse.result.remediations[IdxRemediation.Type.CHALLENGE_AUTHENTICATOR]
                        val passwordField =
                            challengeAuthenticatorRemediation?.get("credentials.passcode")
                        if (passwordField != null) {
                            passwordField.value = password
                        }
                        if (challengeAuthenticatorRemediation != null) {
                            when (val challengeAuthenticatorResponse =
                                flow.proceed(challengeAuthenticatorRemediation)) {
                                is OidcClientResult.Error -> {
                                    println("CHALLENGE_AUTHENTICATOR failed")
                                    methodChannelResult.error(
                                        "CHALLENGE_AUTHENTICATOR ERROR",
                                        challengeAuthenticatorResponse.exception.message,
                                        challengeAuthenticatorResponse.exception.cause?.message
                                    )
                                    return
                                }
                                is OidcClientResult.Success -> {
                                    println("CHALLENGE_AUTHENTICATOR success")
                                    if (challengeAuthenticatorResponse.result.isLoginSuccessful) {
                                        when (val tokenResponse =
                                            flow.exchangeInteractionCodeForTokens(
                                                challengeAuthenticatorResponse.result.remediations[IdxRemediation.Type.ISSUE]!!
                                            )) {
                                            is OidcClientResult.Error -> {
                                                println("Exchange failed")
                                                methodChannelResult.error(
                                                    "TOKEN ERROR",
                                                    tokenResponse.exception.message,
                                                    tokenResponse.exception.cause?.message
                                                )
                                                return
                                            }

                                            is OidcClientResult.Success -> {
                                                println("ISSUE success")
                                                CredentialBootstrap.defaultCredential()
                                                    .storeToken(tokenResponse.result)
                                                val tokenMap =
                                                    mapOf(
                                                        "accessToken" to tokenResponse.result.accessToken,
                                                        "userId" to tokenResponse.result.idToken!!
                                                    )
                                                methodChannelResult.success(
                                                    tokenMap
                                                )
                                                return
                                            }
                                            else -> {
                                                println("challengeAuthenticatorResponse failed to run ISSUE")
                                            }
                                        }
                                    }else{
                                        println("NOT LOGIN")
                                        methodChannelResult.error("E0000004","User not found","E0000004")
                                    }
                                }
                                else -> {}
                            }
                        }else{
                            methodChannelResult.error("E0000004","User not found","E0000004")
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    suspend fun handleRegisterWithCredentialsResponse(
        response: IdxResponse,
        email: String,
        password: String,
        methodChannelResult: MethodChannel.Result,
        flow: IdxFlow?
    ) {
        if (flow == null) return



        for (remediation in response.remediations) {
            if (remediation.type == IdxRemediation.Type.SELECT_ENROLL_PROFILE) {
                when (val selectEnrollProfileResponse =
                    flow.proceed(response.remediations[IdxRemediation.Type.SELECT_ENROLL_PROFILE]!!)) {
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
                            flow.proceed(enrollProfileRemediation)) {

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
                                        selectAuthenticatorEnrollRemediation?.form?.get("authenticator")?.options?.get(0)
                                    if (selectAuthenticatorEnrollRemediation != null) {
                                        selectAuthenticatorEnrollRemediation.form["authenticator"]?.selectedOption =
                                            authenticationOption

                                        when (val selectAuthenticatorEnrollResponse =
                                            flow.proceed(selectAuthenticatorEnrollRemediation)) {
                                            is OidcClientResult.Error -> {
                                                methodChannelResult.error(
                                                    "SELECT_AUTHENTICATOR_ENROLL FAILED",
                                                    selectAuthenticatorEnrollResponse.exception.message,
                                                    selectAuthenticatorEnrollResponse.exception.cause?.message
                                                )
                                                return
                                            }

                                            is OidcClientResult.Success -> {
                                                val enrollAuthenticatorRemediation =
                                                    selectAuthenticatorEnrollResponse.result.remediations[IdxRemediation.Type.ENROLL_AUTHENTICATOR]
                                                val passcode =
                                                    enrollAuthenticatorRemediation?.get("credentials.passcode")
                                                if (passcode != null) {
                                                    passcode.value = password

                                                    when (val passcodeResponse =
                                                        flow.proceed(enrollAuthenticatorRemediation)) {
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
                                                                    flow.proceed(skipRemediation)) {
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
                                                                                flow.exchangeInteractionCodeForTokens(
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
                                                                                    CredentialBootstrap.defaultCredential()
                                                                                        .storeToken(
                                                                                            finalResponse.result
                                                                                        )
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


    suspend fun handleRegisterWithGoogleResponse(
        response: IdxResponse,
        methodChannelResult: MethodChannel.Result,
        context: Context, flow: IdxFlow?,
    ) {
        if (flow == null) return
        println("login with Google started")

        val redirectRemediation = response.remediations[IdxRemediation.Type.REDIRECT_IDP]
        val idpCapability = redirectRemediation?.capabilities?.get<IdxIdpCapability>()

        if (idpCapability != null) {
            println("Got remediation")
            try {
                val redirectUri = Uri.parse(idpCapability.redirectUrl.toString())
                val browserIntent = Intent(Intent.ACTION_VIEW, redirectUri)
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(browserIntent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        }
    }
}