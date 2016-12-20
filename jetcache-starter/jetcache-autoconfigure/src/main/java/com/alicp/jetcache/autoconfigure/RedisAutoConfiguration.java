package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.external.ExternalCacheBuilder;
import com.alicp.jetcache.redis.RedisCacheBuilder;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.bind.RelaxedDataBinder;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Created on 2016/11/25.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Component
@Conditional(RedisAutoConfiguration.RedisCondition.class)
public class RedisAutoConfiguration extends ExternalCacheAutoConfiguration {

    @Autowired(required = false)
    private JedisPool jedisPool;

    public RedisAutoConfiguration() {
        super("redis");
    }

    @Override
    protected CacheBuilder initCache(RelaxedPropertyResolver r, String cacheArea) {
        if (jedisPool == null) {
            GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
            Map<String, Object> props = r.getSubProperties("poolConfig");
            RelaxedDataBinder binder = new RelaxedDataBinder(poolConfig);
            binder.bind(new MutablePropertyValues(props));

            String host = r.getRequiredProperty("host");
            int port = Integer.parseInt(r.getRequiredProperty("port"));
            int timeout = Integer.parseInt(r.getProperty("timeout", String.valueOf(Protocol.DEFAULT_TIMEOUT)));
            String password = r.getProperty("password", (String) null);
            int database = Integer.parseInt(r.getProperty("database", String.valueOf(Protocol.DEFAULT_DATABASE)));
            String clientName = r.getProperty("clientName", (String) null);
            boolean ssl = Boolean.parseBoolean(r.getProperty("ssl", "false"));

            jedisPool = new JedisPool(poolConfig, host, port, timeout, password, database, clientName, ssl);
        }

        ExternalCacheBuilder externalCacheBuilder = RedisCacheBuilder.createRedisCacheBuilder()
                .jedisPool(jedisPool);
        parseGeneralConfig(externalCacheBuilder, r);
        return externalCacheBuilder;
    }

    public static class RedisCondition extends JetCacheConditon {
        public RedisCondition() {
            super("redis");
        }
    }


}