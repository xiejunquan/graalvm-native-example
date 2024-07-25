package com.example.graalvm.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.data.redis.*;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author xiejunquan
 * @create 2024/7/24 11:12
 */
public abstract class AbstractLettuceConnectionConfig {

    private static final boolean COMMONS_POOL2_AVAILABLE = ClassUtils.isPresent("org.apache.commons.pool2.ObjectPool",
            AbstractLettuceConnectionConfig.class.getClassLoader());

    private RedisConnectionDetails redisConnectionDetails;
    
    public abstract RedisProperties getProperties();
    
    RedisConnectionDetails getRedisConnectionDetails(){
        if(this.redisConnectionDetails == null) {
            this.redisConnectionDetails = new PropertiesRedisConnectionDetails(getProperties());
        }
        return this.redisConnectionDetails;
    }

    DefaultClientResources lettuceClientResources() {
        DefaultClientResources.Builder builder = DefaultClientResources.builder();
        return builder.build();
    }

    LettuceConnectionFactory createConnectionFactory(
            LettuceClientConfigurationBuilderCustomizer customizer) {
        ClientResources clientResources = lettuceClientResources();
        LettuceClientConfiguration clientConfig = getLettuceClientConfiguration(customizer, clientResources,
                getProperties().getLettuce().getPool());
        return createLettuceConnectionFactory(clientConfig);
    }

    private LettuceConnectionFactory createLettuceConnectionFactory(LettuceClientConfiguration clientConfiguration) {
        if (getSentinelConfig() != null) {
            return new LettuceConnectionFactory(getSentinelConfig(), clientConfiguration);
        }
        if (getClusterConfiguration() != null) {
            return new LettuceConnectionFactory(getClusterConfiguration(), clientConfiguration);
        }
        return new LettuceConnectionFactory(getStandaloneConfig(), clientConfiguration);
    }

    private LettuceClientConfiguration getLettuceClientConfiguration(
            LettuceClientConfigurationBuilderCustomizer customizer,
            ClientResources clientResources, RedisProperties.Pool pool) {
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = createBuilder(pool);
        applyProperties(builder);
        if (StringUtils.hasText(getProperties().getUrl())) {
            customizeConfigurationFromUrl(builder);
        }
        builder.clientOptions(createClientOptions());
        builder.clientResources(clientResources);
        if(Objects.nonNull(customizer)){
            customizer.customize(builder);
        }
        return builder.build();
    }

    private LettuceClientConfiguration.LettuceClientConfigurationBuilder createBuilder(RedisProperties.Pool pool) {
        if (isPoolEnabled(pool)) {
            return new PoolBuilderFactory().createBuilder(pool);
        }
        return LettuceClientConfiguration.builder();
    }

    private void applyProperties(LettuceClientConfiguration.LettuceClientConfigurationBuilder builder) {
        if (isSslEnabled()) {
            builder.useSsl();
        }
        if (getProperties().getTimeout() != null) {
            builder.commandTimeout(getProperties().getTimeout());
        }
        if (getProperties().getLettuce() != null) {
            RedisProperties.Lettuce lettuce = getProperties().getLettuce();
            if (lettuce.getShutdownTimeout() != null && !lettuce.getShutdownTimeout().isZero()) {
                builder.shutdownTimeout(getProperties().getLettuce().getShutdownTimeout());
            }
        }
        if (StringUtils.hasText(getProperties().getClientName())) {
            builder.clientName(getProperties().getClientName());
        }
    }

    private ClientOptions createClientOptions() {
        ClientOptions.Builder builder = initializeClientOptionsBuilder();
        Duration connectTimeout = getProperties().getConnectTimeout();
        if (connectTimeout != null) {
            builder.socketOptions(SocketOptions.builder().connectTimeout(connectTimeout).build());
        }
        return builder.timeoutOptions(TimeoutOptions.enabled()).build();
    }

    private ClientOptions.Builder initializeClientOptionsBuilder() {
        if (getProperties().getCluster() != null) {
            ClusterClientOptions.Builder builder = ClusterClientOptions.builder();
            RedisProperties.Lettuce.Cluster.Refresh refreshProperties = getProperties().getLettuce().getCluster().getRefresh();
            ClusterTopologyRefreshOptions.Builder refreshBuilder = ClusterTopologyRefreshOptions.builder()
                    .dynamicRefreshSources(refreshProperties.isDynamicRefreshSources());
            if (refreshProperties.getPeriod() != null) {
                refreshBuilder.enablePeriodicRefresh(refreshProperties.getPeriod());
            }
            if (refreshProperties.isAdaptive()) {
                refreshBuilder.enableAllAdaptiveRefreshTriggers();
            }
            return builder.topologyRefreshOptions(refreshBuilder.build());
        }
        return ClientOptions.builder();
    }

    private void customizeConfigurationFromUrl(LettuceClientConfiguration.LettuceClientConfigurationBuilder builder) {
        if (urlUsesSsl()) {
            builder.useSsl();
        }
    }

    /**
     * Inner class to allow optional commons-pool2 dependency.
     */
    private static final class PoolBuilderFactory {

        LettuceClientConfiguration.LettuceClientConfigurationBuilder createBuilder(RedisProperties.Pool properties) {
            return LettucePoolingClientConfiguration.builder().poolConfig(getPoolConfig(properties));
        }

        private GenericObjectPoolConfig<?> getPoolConfig(RedisProperties.Pool properties) {
            GenericObjectPoolConfig<?> config = new GenericObjectPoolConfig<>();
            config.setMaxTotal(properties.getMaxActive());
            config.setMaxIdle(properties.getMaxIdle());
            config.setMinIdle(properties.getMinIdle());
            if (properties.getTimeBetweenEvictionRuns() != null) {
                config.setTimeBetweenEvictionRuns(properties.getTimeBetweenEvictionRuns());
            }
            if (properties.getMaxWait() != null) {
                config.setMaxWait(properties.getMaxWait());
            }
            return config;
        }

    }

    protected final RedisStandaloneConfiguration getStandaloneConfig() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(getRedisConnectionDetails().getStandalone().getHost());
        config.setPort(getRedisConnectionDetails().getStandalone().getPort());
        config.setUsername(getRedisConnectionDetails().getUsername());
        config.setPassword(RedisPassword.of(getRedisConnectionDetails().getPassword()));
        config.setDatabase(getRedisConnectionDetails().getStandalone().getDatabase());
        return config;
    }

    protected final RedisSentinelConfiguration getSentinelConfig() {
        if (getRedisConnectionDetails().getSentinel() != null) {
            RedisSentinelConfiguration config = new RedisSentinelConfiguration();
            config.master(getRedisConnectionDetails().getSentinel().getMaster());
            config.setSentinels(createSentinels(getRedisConnectionDetails().getSentinel()));
            config.setUsername(getRedisConnectionDetails().getUsername());
            String password = getRedisConnectionDetails().getPassword();
            if (password != null) {
                config.setPassword(RedisPassword.of(password));
            }
            config.setSentinelUsername(getRedisConnectionDetails().getSentinel().getUsername());
            String sentinelPassword = getRedisConnectionDetails().getSentinel().getPassword();
            if (sentinelPassword != null) {
                config.setSentinelPassword(RedisPassword.of(sentinelPassword));
            }
            config.setDatabase(getRedisConnectionDetails().getSentinel().getDatabase());
            return config;
        }
        return null;
    }

    /**
     * Create a {@link RedisClusterConfiguration} if necessary.
     * @return {@literal null} if no cluster settings are set.
     */
    protected final RedisClusterConfiguration getClusterConfiguration() {
        RedisProperties.Cluster clusterProperties = getProperties().getCluster();
        if (getRedisConnectionDetails().getCluster() != null) {
            RedisClusterConfiguration config = new RedisClusterConfiguration(
                    getNodes(getRedisConnectionDetails().getCluster()));
            if (clusterProperties != null && clusterProperties.getMaxRedirects() != null) {
                config.setMaxRedirects(clusterProperties.getMaxRedirects());
            }
            config.setUsername(getRedisConnectionDetails().getUsername());
            String password = getRedisConnectionDetails().getPassword();
            if (password != null) {
                config.setPassword(RedisPassword.of(password));
            }
            return config;
        }
        return null;
    }

    private List<String> getNodes(RedisConnectionDetails.Cluster cluster) {
        return cluster.getNodes().stream().map((node) -> "%s:%d".formatted(node.host(), node.port())).toList();
    }

    protected boolean isSslEnabled() {
        return getProperties().getSsl().isEnabled();
    }

    protected boolean isPoolEnabled(RedisProperties.Pool pool) {
        Boolean enabled = pool.getEnabled();
        return (enabled != null) ? enabled : COMMONS_POOL2_AVAILABLE;
    }

    private List<RedisNode> createSentinels(RedisConnectionDetails.Sentinel sentinel) {
        List<RedisNode> nodes = new ArrayList<>();
        for (RedisConnectionDetails.Node node : sentinel.getNodes()) {
            nodes.add(new RedisNode(node.host(), node.port()));
        }
        return nodes;
    }

    protected final boolean urlUsesSsl() {
        return parseUrl(getProperties().getUrl()).isUseSsl();
    }

    static ConnectionInfo parseUrl(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (!"redis".equals(scheme) && !"rediss".equals(scheme)) {
                throw new RuntimeException(url);
            }
            boolean useSsl = ("rediss".equals(scheme));
            String username = null;
            String password = null;
            if (uri.getUserInfo() != null) {
                String candidate = uri.getUserInfo();
                int index = candidate.indexOf(':');
                if (index >= 0) {
                    username = candidate.substring(0, index);
                    password = candidate.substring(index + 1);
                }
                else {
                    password = candidate;
                }
            }
            return new ConnectionInfo(uri, useSsl, username, password);
        }
        catch (URISyntaxException ex) {
            throw new RuntimeException(url, ex);
        }
    }

    static class ConnectionInfo {

        private final URI uri;

        private final boolean useSsl;

        private final String username;

        private final String password;

        ConnectionInfo(URI uri, boolean useSsl, String username, String password) {
            this.uri = uri;
            this.useSsl = useSsl;
            this.username = username;
            this.password = password;
        }

        URI getUri() {
            return this.uri;
        }

        boolean isUseSsl() {
            return this.useSsl;
        }

        String getUsername() {
            return this.username;
        }

        String getPassword() {
            return this.password;
        }

    }

    static class PropertiesRedisConnectionDetails implements RedisConnectionDetails {

        private final RedisProperties properties;

        PropertiesRedisConnectionDetails(RedisProperties properties) {
            this.properties = properties;
        }

        @Override
        public String getUsername() {
            if (this.properties.getUrl() != null) {
                ConnectionInfo connectionInfo = connectionInfo(this.properties.getUrl());
                return connectionInfo.getUsername();
            }
            return this.properties.getUsername();
        }

        @Override
        public String getPassword() {
            if (this.properties.getUrl() != null) {
                ConnectionInfo connectionInfo = connectionInfo(this.properties.getUrl());
                return connectionInfo.getPassword();
            }
            return this.properties.getPassword();
        }

        @Override
        public Standalone getStandalone() {
            if (this.properties.getUrl() != null) {
                ConnectionInfo connectionInfo = connectionInfo(this.properties.getUrl());
                return Standalone.of(connectionInfo.getUri().getHost(), connectionInfo.getUri().getPort(),
                        this.properties.getDatabase());
            }
            return Standalone.of(this.properties.getHost(), this.properties.getPort(), this.properties.getDatabase());
        }

        private ConnectionInfo connectionInfo(String url) {
            return (url != null) ? parseUrl(url) : null;
        }

        @Override
        public Sentinel getSentinel() {
            org.springframework.boot.autoconfigure.data.redis.RedisProperties.Sentinel sentinel = this.properties
                    .getSentinel();
            if (sentinel == null) {
                return null;
            }
            return new Sentinel() {

                @Override
                public int getDatabase() {
                    return PropertiesRedisConnectionDetails.this.properties.getDatabase();
                }

                @Override
                public String getMaster() {
                    return sentinel.getMaster();
                }

                @Override
                public List<Node> getNodes() {
                    return sentinel.getNodes().stream().map(PropertiesRedisConnectionDetails.this::asNode).toList();
                }

                @Override
                public String getUsername() {
                    return sentinel.getUsername();
                }

                @Override
                public String getPassword() {
                    return sentinel.getPassword();
                }

            };
        }

        @Override
        public Cluster getCluster() {
            RedisProperties.Cluster cluster = this.properties.getCluster();
            List<Node> nodes = (cluster != null) ? cluster.getNodes().stream().map(this::asNode).toList() : null;
            return (nodes != null) ? () -> nodes : null;
        }

        private Node asNode(String node) {
            String[] components = node.split(":");
            return new Node(components[0], Integer.parseInt(components[1]));
        }
    }
}
