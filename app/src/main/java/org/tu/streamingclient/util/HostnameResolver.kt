package org.tu.streamingclient.util

import java.lang.Exception

class HostnameResolver {

    public fun resolve(hostname: String?, exceptionHandler: (Exception) -> Unit): String? {
        if (hostname == null) {
            return null
        }
        var resolved: String? = null
        try {
            resolved = resolveHostname(hostname)
        } catch (e: Exception) {
            exceptionHandler(e)
        }
        return resolved
    }

    private external fun resolveHostname(host: String): String?

    companion object {
        init {
            System.loadLibrary("streamingclient")
        }
    }
}
