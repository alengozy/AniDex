package com.example.anidex.network

import android.net.Uri
import com.example.anidex.private.ClientData
import net.openid.appauth.*


class AuthServiceConfig {
    private var clientId = ClientData.clientId
    private var codeChallenge = ClientData.codeChallenge
    private var serviceConfig: AuthorizationServiceConfiguration = AuthorizationServiceConfiguration(
        Uri.parse("https://myanimelist.net/v1/oauth2/authorize"),  // authorization endpoint
        Uri.parse("https://myanimelist.net/v1/oauth2/token")
    )

    fun getAuthRequest(): AuthorizationRequest{
        val authRequestBuilder = AuthorizationRequest.Builder(
            serviceConfig,  // the authorization service configuration
            clientId,  // the client ID, typically pre-registered and static
            ResponseTypeValues.CODE,  // the response_type value: we want a code
            Uri.parse("com.example.anidex://oauth2redirect")
        ) // the redirect URI to which the auth response is sent
        return authRequestBuilder
            .setCodeVerifier(codeChallenge, codeChallenge, AuthorizationRequest.CODE_CHALLENGE_METHOD_PLAIN)
            .build()
    }

    fun getTokenRequest(): TokenRequest{
        val tokenRequestBuilder = TokenRequest.Builder(
            serviceConfig,  // the authorization service configuration
            clientId  // the client ID, typically pre-registered and static
        )
        return tokenRequestBuilder
            .setCodeVerifier(codeChallenge)
            .build()
    }

    fun getServiceConfig(): AuthorizationServiceConfiguration{
        return serviceConfig

    }

}
