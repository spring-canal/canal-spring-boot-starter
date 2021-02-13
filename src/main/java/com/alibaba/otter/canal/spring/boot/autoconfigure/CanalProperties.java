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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.canal.config.ConsumerProperties;
import org.springframework.canal.config.ContainerProperties;

/**
 * @author 橙子
 * @since 2020/10/11
 */
@ConfigurationProperties("spring.canal")
public class CanalProperties {
    private final ConsumerProperties consumer = new ConsumerProperties();
    private final ContainerProperties listener = new ContainerProperties();
    private final TemplateProperties template = new TemplateProperties();

    public ConsumerProperties getConsumer() {
        return this.consumer;
    }

    public ContainerProperties getListener() {
        return this.listener;
    }

    public TemplateProperties getTemplate() {
        return this.template;
    }

    public static class TemplateProperties {
        private Class<?> defaultPayloadType;
        private String defaultSubscribe;
        private Boolean startRollback;

        public Class<?> getDefaultPayloadType() {
            return this.defaultPayloadType;
        }

        public void setDefaultPayloadType(Class<?> defaultPayloadType) {
            this.defaultPayloadType = defaultPayloadType;
        }

        public String getDefaultSubscribe() {
            return this.defaultSubscribe;
        }

        public void setDefaultSubscribe(String defaultSubscribe) {
            this.defaultSubscribe = defaultSubscribe;
        }

        public Boolean isStartRollback() {
            return this.startRollback;
        }

        public void setStartRollback(Boolean startRollback) {
            this.startRollback = startRollback;
        }
    }
}
