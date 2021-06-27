package com.gmail.johnokoroukwu.zephyrng.http

import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

/**
 * Default trust manager used by [AbstractRequestSender.defaultRequestFactory].
 * Simply trusts any client and server
 */
object TrustAllCertManager : X509TrustManager {

    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return emptyArray()
    }

}