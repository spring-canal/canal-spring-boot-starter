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
import net.bytebuddy.utility.RandomString;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.canal.core.CanalTemplate;
import org.springframework.canal.support.CanalMessageUtils;
import org.springframework.context.annotation.Bean;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * @author 橙子
 * @since 2021/1/2
 */
@SpringBootTest(classes = CanalAutoConfigurationTest.AppConfig.class)
class CanalAutoConfigurationTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CanalAutoConfigurationTest.class);
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private CanalTemplate<CanalEntry.Entry> canalTemplate;

    @BeforeEach
    void setUp() {
        this.canalTemplate.setAllowNonTransactional(true);
        prepareData();
    }

    @Test
    void canalListenerTest() {
        LOGGER.info(() -> CanalMessageUtils.toString(this.canalTemplate.getLeast(), false));
    }

    private void prepareData() {
        final Session session = this.entityManagerFactory.createEntityManager().unwrap(Session.class);
        Transaction transaction = session.beginTransaction();
        final TestBean testBean = new TestBean(1, "Hello");
        session.save(testBean);
        session.remove(testBean);
        session.saveOrUpdate(new TestBean(2, "SpringBoot"));
        session.saveOrUpdate(new TestBean(3, RandomString.make()));
        transaction.commit();
        session.close();
    }

    @SpringBootApplication
    static class AppConfig {
        @Bean
        public EntityManagerFactory entityManagerFactory() {
            return Persistence.createEntityManagerFactory("org.springframework.boot.canal.listener");
        }
    }
}