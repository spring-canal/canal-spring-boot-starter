/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.otter.canal.spring.boot.autoconfigure;

import com.alibaba.otter.canal.protocol.CanalEntry;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.canal.annotation.EnableCanal;
import org.springframework.canal.bootstrap.CanalBootstrapConfiguration;
import org.springframework.canal.config.ContainerProperties;
import org.springframework.canal.config.listener.container.CanalListenerContainerFactory;
import org.springframework.canal.config.listener.container.ConcurrencyCanalListenerContainerFactory;
import org.springframework.canal.core.CanalConnectorFactory;
import org.springframework.canal.core.SimpleCanalConnectorFactory;
import org.springframework.canal.listener.adapter.BatchToEachAdapter;
import org.springframework.canal.listener.container.CanalListenerContainerBatchErrorHandler;
import org.springframework.canal.listener.container.CanalListenerContainerEachErrorHandler;
import org.springframework.canal.support.converter.record.BatchCanalMessageConverter;
import org.springframework.canal.support.converter.record.BatchEntryCanalMessageConverter;
import org.springframework.canal.support.converter.record.BatchRawCanalMessageConverter;
import org.springframework.canal.support.converter.record.DelegatingBatchCanalMessageConverter;
import org.springframework.canal.support.converter.record.EachCanalMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author 橙子
 * @since 2020/10/11
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EnableCanal.class)
public class CanalAnnotationDrivenConfiguration {
    private final CanalProperties canalProperties;
    private final EachCanalMessageConverter<?> eachMessageConverter;
    private final BatchCanalMessageConverter<?> batchMessageConverter;
    private final CanalListenerContainerEachErrorHandler<?> eachErrorHandler;
    private final CanalListenerContainerBatchErrorHandler batchErrorHandler;
    private final BatchToEachAdapter<?> batchToEachAdapter;

    public CanalAnnotationDrivenConfiguration(CanalProperties canalProperties,
                                              ObjectProvider<EachCanalMessageConverter<?>> eachMessageConverter,
                                              ObjectProvider<BatchCanalMessageConverter<?>> batchMessageConverter,
                                              ObjectProvider<CanalListenerContainerEachErrorHandler<?>> listenerContainerEachErrorHandler,
                                              ObjectProvider<CanalListenerContainerBatchErrorHandler> listenerContainerBatchErrorHandler,
                                              ObjectProvider<BatchToEachAdapter<?>> batchToEachAdapter) {
        this.canalProperties = canalProperties;
        this.eachMessageConverter = eachMessageConverter.getIfUnique();
        this.batchMessageConverter = batchMessageConverter.getIfUnique(() -> {
            try {
                return new DelegatingBatchCanalMessageConverter(
                        new BatchEntryCanalMessageConverter((EachCanalMessageConverter<CanalEntry.Entry>) this.eachMessageConverter),
                        new BatchRawCanalMessageConverter());
            } catch (IllegalArgumentException e) {
                return null;
            }
        });
        this.eachErrorHandler = listenerContainerEachErrorHandler.getIfUnique();
        this.batchErrorHandler = listenerContainerBatchErrorHandler.getIfUnique();
        this.batchToEachAdapter = batchToEachAdapter.getIfUnique();
    }


    @Bean(CanalBootstrapConfiguration.CANAL_LISTENER_CONTAINER_FACTORY_BEAN_NAME)
    @ConditionalOnMissingBean(name = CanalBootstrapConfiguration.CANAL_LISTENER_CONTAINER_FACTORY_BEAN_NAME)
    public CanalListenerContainerFactory<?> canalListenerContainerFactory(ObjectProvider<CanalConnectorFactory> canalConnectorFactory,
                                                                          ObjectProvider<PlatformTransactionManager> transactionManager) {
        ConcurrencyCanalListenerContainerFactory<Object> listenerContainerFactory = new ConcurrencyCanalListenerContainerFactory<>();
        listenerContainerFactory.setConnectorFactory(canalConnectorFactory.getIfAvailable(() -> new SimpleCanalConnectorFactory(this.canalProperties.getConsumer())));
        listenerContainerFactory.setDefaultBatchListener(ContainerProperties.Type.BATCH.equals(this.canalProperties.getListener().getType()));
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
//        map.from(this.replyTemplate).to(listenerContainerFactory::setReplyTemplate)
//        map.from(this.afterRollbackProcessor).to(listenerContainerFactory::setAfterRollbackProcessor)
//        map.from(this.recordInterceptor).to(listenerContainerFactory::setRecordInterceptor)
        map.from(this.batchToEachAdapter).to(listenerContainerFactory::setBatchToEachAdapter);
        map.from(transactionManager.getIfUnique()).to(listenerContainerFactory::setTransactionManager);
        if (ContainerProperties.Type.BATCH.equals(this.canalProperties.getListener().getType())) {
            map.from(this.batchMessageConverter).to(listenerContainerFactory::setMessageConverter);
            map.from(this.batchErrorHandler).to(listenerContainerFactory::setErrorHandler);
        } else {
            map.from(this.eachMessageConverter).to(listenerContainerFactory::setMessageConverter);
            map.from(this.eachErrorHandler).to(listenerContainerFactory::setErrorHandler);
        }
        listenerContainerFactory.setContainerProperties(this.canalProperties.getListener());
        return listenerContainerFactory;
    }

    @Configuration(proxyBeanMethods = false)
    @EnableCanal
    static class EnableCanalConfiguration {
    }
}
