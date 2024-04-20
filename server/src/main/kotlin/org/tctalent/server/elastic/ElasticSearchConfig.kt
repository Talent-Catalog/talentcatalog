package org.tctalent.server.elastic

import arrow.core.Either
import arrow.core.getOrElse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories
import org.tctalent.server.errors.ConfigError
import java.net.URI


@Configuration
@EnableElasticsearchRepositories(basePackages = ["org.tctalent.server.repository.es"])
open class ElasticSearchConfig : ElasticsearchConfiguration() {
    private val log: Logger = LoggerFactory.getLogger(ElasticSearchConfig::class.java)

    @Value("\${spring.elasticsearch.uris}")
    private val uris: List<String> = emptyList()

    @Value("\${spring.elasticsearch.username}")
    private val username: String = ""

    @Value("\${spring.elasticsearch.password}")
    private val password: String = ""

    override fun clientConfiguration(): ClientConfiguration {
        validateUrisExist(uris).getOrElse { throw it }

        val uri = URI(uris[0])
        val hostAndPort = uri.authority

        log.info("Connecting to Elasticsearch at $hostAndPort");

        var builder = ClientConfiguration.builder()
            .connectedTo(hostAndPort)
        builder = addSsl(uri, builder)
        builder = addBasicAuth(builder)

        return builder.build()
    }

    private fun validateUrisExist(uris: List<String>): Either<ConfigError.InvalidElasticConfig, Boolean> {
        if (uris.isEmpty()) {
            return Either.Left(ConfigError.InvalidElasticConfig("Require Uri to be set."))
        }
        return Either.Right(true)
    }

    private fun addSsl(
        uri: URI,
        builder: ClientConfiguration.MaybeSecureClientConfigurationBuilder
    ): ClientConfiguration.MaybeSecureClientConfigurationBuilder {
        val protocol = uri.scheme
        return if (protocol == "https") ({
            builder.usingSsl()
        }) as ClientConfiguration.MaybeSecureClientConfigurationBuilder else {
            builder
        }
    }

    private fun addBasicAuth(builder: ClientConfiguration.MaybeSecureClientConfigurationBuilder): ClientConfiguration.MaybeSecureClientConfigurationBuilder {
        return if (username.isNotEmpty()) ({
            builder.withBasicAuth(username, password)
        }) as ClientConfiguration.MaybeSecureClientConfigurationBuilder else {
            builder
        }
    }
}