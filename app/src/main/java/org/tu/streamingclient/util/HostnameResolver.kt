package org.tu.streamingclient.util

class HostnameResolver {

    public fun resolve(hostname: String?): String? {
        if (hostname == null) {
            return null
        }
        return resolveHostname(hostname)
    }

    private external fun resolveHostname(host: String): String

    companion object {
        init {
            System.loadLibrary("streamingclient")
        }
    }
}
