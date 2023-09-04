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
import org.citrusframework.message.builder.ObjectMappingPayloadBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;

import static org.citrusframework.actions.ApplyTestBehaviorAction.Builder.apply;
import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
@CitrusSpringSupport
@ContextConfiguration(classes = { CitrusSpringConfig.class, EndpointConfig.class })
public class AddFruitsIT {

    @Autowired
    private HttpClient fruitStoreClient;

    @Autowired
    private KafkaEndpoint fruitEvents;

    @Test
    @CitrusTest
    public void shouldAddFruits(@CitrusResource GherkinTestActionRunner $) {
        $.when(http().client(fruitStoreClient)
                .send()
                .post("/api/fruits")
                .message()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                    {
                        "name": "Banana",
                        "description": "citrus:randomString(10)",
                        "category": {
                            "id": 2,
                            "name": "tropical"
                        },
                        "nutrition": {
                            "calories": 97,
                            "sugar": 14
                        },
                        "status": "PENDING",
                        "tags": ["sweet"]
                    }
                """));

        $.then(http().client(fruitStoreClient)
                .receive()
                .response(HttpStatus.CREATED));
    }

    @Test
    @CitrusTest
    public void shouldAddFruitsFromResource(@CitrusResource GherkinTestActionRunner $) {
        $.when(http().client(fruitStoreClient)
                .send()
                .post("/api/fruits")
                .message()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new ClassPathResource("data/fruit.json")));

        $.then(http().client(fruitStoreClient)
                .receive()
                .response(HttpStatus.CREATED));
    }

    @Test
    @CitrusTest
    public void shouldAddFruitsFromModel(@CitrusResource GherkinTestActionRunner $) {
        Fruit fruit = TestHelper.createFruit("Blueberry",
                new Category("berry"), new Nutrition(34, 8), Fruit.Status.PENDING, "smoothie");

        $.when(http().client(fruitStoreClient)
                .send()
                .post("/api/fruits")
                .message()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new ObjectMappingPayloadBuilder(fruit)));

        $.then(http().client(fruitStoreClient)
                .receive()
                .response(HttpStatus.CREATED));
    }

    @Test
    @CitrusTest
    public void shouldAddFruitsFromBehavior(@CitrusResource GherkinTestActionRunner $) {
        Fruit fruit = TestHelper.createFruit("Raspberry",
                new Category("berry"), new Nutrition(42, 9), Fruit.Status.PENDING, "smoothie");

        $.given(apply().behavior(new AddFruitBehavior(fruit, fruitStoreClient)));

        $.then(receive(fruitEvents)
                .message()
                .header(KafkaMessageHeaders.MESSAGE_KEY, "${id}")
                .body("added::${id}"));
    }
}
