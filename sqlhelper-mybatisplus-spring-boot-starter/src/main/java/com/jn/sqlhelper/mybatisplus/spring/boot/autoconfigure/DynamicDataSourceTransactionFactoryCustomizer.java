/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at  http://www.gnu.org/licenses/lgpl-2.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jn.sqlhelper.mybatisplus.spring.boot.autoconfigure;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.jn.sqlhelper.datasource.NamedDataSource;
import com.jn.sqlhelper.datasource.supports.spring.boot.DynamicTransactionAutoConfiguration;
import com.jn.sqlhelper.mybatis.spring.session.factory.dynamicdatasource.DynamicDataSourceManagedTransactionFactory;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.transaction.TransactionFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import javax.sql.DataSource;

@org.springframework.context.annotation.Configuration
@ConditionalOnProperty(name = "sqlhelper.dynamic-datasource.enabled", havingValue = "true")
@ConditionalOnBean(name = "dynamicDataSourceTransactionAdvisor")
@AutoConfigureAfter(DynamicTransactionAutoConfiguration.class)
@AutoConfigureBefore(MybatisPlusAutoConfiguration.class)
public class DynamicDataSourceTransactionFactoryCustomizer implements ConfigurationCustomizer {
    @Override
    public void customize(MybatisConfiguration configuration) {
        Environment oldEnv = configuration.getEnvironment();
        if (oldEnv != null) {
            boolean replaceIt = oldEnv.getTransactionFactory() == null || !(oldEnv.getTransactionFactory() instanceof DynamicDataSourceManagedTransactionFactory);
            if(replaceIt) {
                String id = oldEnv.getId();
                DataSource dataSource = oldEnv.getDataSource();
                if (dataSource instanceof NamedDataSource) {
                    id = ((NamedDataSource) dataSource).getDataSourceKey().getId();
                }

                TransactionFactory transactionFactory = new DynamicDataSourceManagedTransactionFactory();
                Environment newEnv = new Environment(id, transactionFactory, dataSource);
                configuration.setEnvironment(newEnv);
            }
        }
    }
}
