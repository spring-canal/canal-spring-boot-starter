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

import com.alibaba.otter.canal.client.CanalConnector;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.canal.core.CanalConnectorFactory;
import org.springframework.canal.core.CanalTemplate;
import org.springframework.canal.core.SharedCanalConnectorFactory;
import org.springframework.canal.core.SimpleCanalConnectorFactory;
import org.springframework.canal.support.converter.record.BatchCanalMessageConverter;
import org.springframework.canal.transaction.CanalTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author 橙子
 * @since 2020/10/11
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(CanalConnector.class)
@EnableConfigurationProperties(CanalProperties.class)
@Import({CanalAnnotationDrivenConfiguration.class})
public class CanalAutoConfiguration {
    private final CanalProperties canalProperties;

    public CanalAutoConfiguration(CanalProperties canalProperties) {
        this.canalProperties = canalProperties;
    }

    /**
     * {@link CanalConnector#rollback() @Bean(initMethod = "rollback")} to get last position
     */
//    @Bean(initMethod = "rollback", destroyMethod = "disconnect")
    @Bean
    @ConditionalOnMissingBean
    public SimpleCanalConnectorFactory canalConnectorFactory(ObjectProvider<CanalConnectorFactoryCustomizer> customizers) {
        final SimpleCanalConnectorFactory factory = new SharedCanalConnectorFactory(this.canalProperties.getConsumer());
        customizers.orderedStream().forEach(customizer -> customizer.customize(factory));
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    public CanalTemplate<?> canalTemplate(ObjectProvider<CanalConnectorFactory> canalConnectorFactory,
                                          ObjectProvider<BatchCanalMessageConverter<?>> messageConverter) {
        final CanalTemplate<Object> canalTemplate = new CanalTemplate<>(canalConnectorFactory.getIfAvailable());
        messageConverter.ifUnique(canalTemplate::setMessageConverter);
        final CanalProperties.TemplateProperties templateProperties = this.canalProperties.getTemplate();
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(templateProperties::getDefaultPayloadType).to(canalTemplate::setPayloadType);
        map.from(templateProperties::getDefaultSubscribe).to(canalTemplate::setSubscribe);
        map.from(templateProperties::isStartRollback).to(canalTemplate::setStartRollback);
        return canalTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public CanalTransactionManager canalTransactionManager(ObjectProvider<CanalConnectorFactory> canalConnectorFactory) {
        return new CanalTransactionManager(canalConnectorFactory.getIfAvailable());
    }

}
