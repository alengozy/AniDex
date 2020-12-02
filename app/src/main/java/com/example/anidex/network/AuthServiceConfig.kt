package com.example.anidex.network

import android.net.Uri
import net.openid.appauth.*


class AuthServiceConfig {
    var clientId = "c957f1e46b7e1b75909ae75da67d93a9"
    var codeChallenge = "vK62fevFKa6FSRcf2T-ilR7VistaripbXSBuljiqT1euD3ALajIqQ-CtSboGH-CxKMUZcMOiYPIWRBcqdXPui_gXVsHCsgcjvpkezSSwnBLlvTsEhOBEjat8tjI1WSXK"
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
            .setCodeVerifier(codeChallenge)
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
