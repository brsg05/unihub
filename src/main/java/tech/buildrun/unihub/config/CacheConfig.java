package tech.buildrun.unihub.config;

import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuração de Cache usando EhCache e JCache (JSR-107).
 * Habilita o uso de anotações @Cacheable e @CacheEvict.
 */
@Configuration
@EnableCaching // Habilita o suporte a cache no Spring
public class CacheConfig {

    /**
     * Configura o CacheManager do Spring para usar JCache (EhCache).
     */
    @Bean
    public CacheManager cacheManager() {
        EhcacheCachingProvider cachingProvider = (EhcacheCachingProvider) Caching.getCachingProvider();
        javax.cache.CacheManager cacheManager = cachingProvider.getCacheManager(
                cachingProvider.getDefaultURI(),
                cachingProvider.getDefaultClassLoader()
        );

        // Configuração para o cache 'professorsList'
        MutableConfiguration<Object, Object> professorsListCacheConfig = new MutableConfiguration<>();
        professorsListCacheConfig.setStoreByValue(false) // Armazena por referência para melhor performance
                .setStatisticsEnabled(true)
                .setExpiryPolicyFactory(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.of(10, TimeUnit.MINUTES))); // Cache expira em 10 minutos

        // Configuração para o cache 'professorDetails'
        MutableConfiguration<Object, Object> professorDetailsCacheConfig = new MutableConfiguration<>();
        professorDetailsCacheConfig.setStoreByValue(false) // Armazena por referência
                .setStatisticsEnabled(true)
                .setExpiryPolicyFactory(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.of(15, TimeUnit.MINUTES))); // Cache expira em 15 minutos

        // Configuração para um cache padrão (se não for especificado um cache específico)
        MutableConfiguration<Object, Object> defaultCacheConfig = new MutableConfiguration<>();
        defaultCacheConfig.setStoreByValue(false)
                .setStatisticsEnabled(true)
                .setExpiryPolicyFactory(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.of(5, TimeUnit.MINUTES)));

        // Registra os caches no CacheManager
        // Verifica se o cache já existe antes de criá-lo para evitar IllegalStateException em recargas de contexto
        if (cacheManager.getCache("professorsList") == null) {
            cacheManager.createCache("professorsList", professorsListCacheConfig);
        }
        if (cacheManager.getCache("professorDetails") == null) {
            cacheManager.createCache("professorDetails", professorDetailsCacheConfig);
        }
        if (cacheManager.getCache("default") == null) {
            cacheManager.createCache("default", defaultCacheConfig);
        }

        return new JCacheCacheManager(cacheManager);
    }
}