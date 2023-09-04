/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.demo.config;

import com.consol.citrus.container.BeforeTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.client.HttpClientBuilder;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.http.server.HttpServerBuilder;
import com.consol.citrus.kafka.embedded.EmbeddedKafkaServer;
import com.consol.citrus.kafka.embedded.EmbeddedKafkaServerBuilder;
import com.consol.citrus.kafka.endpoint.KafkaEndpoint;
import com.consol.citrus.kafka.endpoint.KafkaEndpointBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

import static com.consol.citrus.actions.PurgeEndpointAction.Builder.purgeEndpoints;

@Configuration
@Import(SeleniumConfig.class)
public class EndpointConfig {

    private static final int FRUIT_STORE_SERVICE_PORT = 8080;
    private static final int FOOD_MARKET_SERVICE_PORT = 8081;
    private static final int KAFKA_BROKER_PORT = 9090;

    @Bean
    public HttpClient fruitStoreClient() {
        return new HttpClientBuilder()
                .requestUrl(String.format("http://localhost:%s", FRUIT_STORE_SERVICE_PORT))
            .build();
    }

    @Bean
    public HttpServer foodMarketService() {
        return new HttpServerBuilder()
                .port(FOOD_MARKET_SERVICE_PORT)
                .autoStart(true)
            .build();
    }

    @Bean
    @DependsOn("embeddedKafkaServer")
    public KafkaEndpoint fruitEvents() {
        return new KafkaEndpointBuilder()
                .server(String.format("localhost:%s", KAFKA_BROKER_PORT))
                .topic("fruits.events")
                .build();
    }

    @Bean
    public EmbeddedKafkaServer embeddedKafkaServer() {
        return new EmbeddedKafkaServerBuilder()
                .kafkaServerPort(KAFKA_BROKER_PORT)
                .topics("fruits.events")
                .build();
    }

    @Bean(destroyMethod = "close")
    public BasicDataSource fruitsDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:tcp://localhost/mem:test");
        dataSource.setUsername("sa");
        dataSource.setPassword("password");
        dataSource.setInitialSize(1);
        dataSource.setMaxTotal(3);
        dataSource.setMaxIdle(2);
        return dataSource;
    }

    @Bean
    public ObjectMapper mapper() {
        return new ObjectMapper();
    }

    @Bean
    public BeforeTest beforeTest(KafkaEndpoint fruitEvents) {
        return new SequenceBeforeTest() {
            @Override
            public void doExecute(TestContext context) {
                purgeEndpoints()
                        .endpoint(fruitEvents)
                        .build()
                        .execute(context);
            }
        };
    }
}
