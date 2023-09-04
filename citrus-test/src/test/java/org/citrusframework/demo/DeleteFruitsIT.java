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

package org.citrusframework.demo;

import javax.sql.DataSource;

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.config.CitrusSpringConfig;
import org.citrusframework.demo.behavior.AddFruitBehavior;
import org.citrusframework.demo.config.EndpointConfig;
import org.citrusframework.demo.fruits.model.Category;
import org.citrusframework.demo.fruits.model.Fruit;
import org.citrusframework.demo.fruits.model.Nutrition;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.kafka.endpoint.KafkaEndpoint;
import org.citrusframework.kafka.message.KafkaMessageHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

import static org.citrusframework.actions.ApplyTestBehaviorAction.Builder.apply;
import static org.citrusframework.actions.ExecuteSQLQueryAction.Builder.query;
import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.container.RepeatOnErrorUntilTrue.Builder.repeatOnError;
import static org.citrusframework.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
@CitrusSpringSupport
@ContextConfiguration(classes = { CitrusSpringConfig.class, EndpointConfig.class })
public class DeleteFruitsIT {

    @Autowired
    private HttpClient fruitStoreClient;

    @Autowired
    private DataSource fruitsDataSource;

    @Autowired
    private KafkaEndpoint fruitEvents;

    @Test
    @CitrusTest
    public void shouldDeleteFruits(@CitrusResource GherkinTestActionRunner $) {
        Fruit fruit = TestHelper.createFruit("Watermelon",
                new Category("melon"), new Nutrition(19, 5), Fruit.Status.PENDING, "juicy");

        $.given(apply().behavior(new AddFruitBehavior(fruit, fruitStoreClient)));

        $.then(receive(fruitEvents)
                .message()
                .header(KafkaMessageHeaders.MESSAGE_KEY, "${id}")
                .body("added::" + fruit.getId()));

        $.then(repeatOnError()
            .autoSleep(1000L)
            .until((i, context) -> i > 5)
            .actions(query(fruitsDataSource)
                .statement("SELECT count(id) as found_records FROM fruit WHERE id=${id}")
                .validate("found_records", "1")));

        $.when(http().client(fruitStoreClient)
            .send()
            .delete("/api/fruits/${id}")
            .fork(true));

        $.then(receive(fruitEvents)
                .message()
                .header(KafkaMessageHeaders.MESSAGE_KEY, "${id}")
                .body("removed::" + fruit.getId()));

        $.then(http().client(fruitStoreClient)
                .receive()
                .response(HttpStatus.NO_CONTENT));

        $.then(query(fruitsDataSource)
                .statement("SELECT count(id) as found_records FROM fruit WHERE id=${id}")
                .validate("found_records", "0"));
    }

}
