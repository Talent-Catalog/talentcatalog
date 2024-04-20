package org.tctalent.server.errors

sealed class ConfigError(override val message: String) : Error() {
    data class InvalidElasticConfig(val msg: String): ConfigError("$MSG: ElasticSearch: $msg")
    companion object {
        const val MSG: String = "Configuration Failure::"
    }
}