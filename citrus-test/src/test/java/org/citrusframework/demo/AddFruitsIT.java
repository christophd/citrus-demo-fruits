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

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.junit.JUnit4CitrusSupport;
import com.consol.citrus.message.builder.ObjectMappingPayloadBuilder;
import org.citrusframework.demo.behavior.AddFruitBehavior;
import org.citrusframework.demo.config.EndpointConfig;
import org.citrusframework.demo.fruits.model.Category;
import org.citrusframework.demo.fruits.model.Fruit;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;

import static com.consol.citrus.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = EndpointConfig.class)
public class AddFruitsIT extends JUnit4CitrusSupport {

    @Autowired
    private HttpClient fruitStoreClient;

    @Test
    @CitrusTest
    public void shouldAddFruits() {
        when(http().client(fruitStoreClient)
                .send()
                .post("/fruits")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .payload("{" +
                    "\"name\": \"Banana\"," +
                    "\"description\": \"citrus:randomString(10)\"," +
                    "\"category\": {" +
                        "\"id\": 2," +
                        "\"name\": \"tropical\"" +
                    "}," +
                    "\"status\": \"PENDING\"," +
                    "\"tags\": [\"sweet\"]" +
                "}"));

        then(http().client(fruitStoreClient)
                .receive()
                .response(HttpStatus.CREATED));
    }

    @Test
    @CitrusTest
    public void shouldAddFruitsFromResource() {
        when(http().client(fruitStoreClient)
                .send()
                .post("/fruits")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .payload(new ClassPathResource("data/fruit.json")));

        then(http().client(fruitStoreClient)
                .receive()
                .response(HttpStatus.CREATED));
    }

    @Test
    @CitrusTest
    public void shouldAddFruitsFromModel() {
        Fruit fruit = TestHelper.createFruit("Blueberry",
                new Category("berry"), Fruit.Status.PENDING, "smoothie");

        when(http().client(fruitStoreClient)
                .send()
                .post("/fruits")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .payload(new ObjectMappingPayloadBuilder(fruit)));

        then(http().client(fruitStoreClient)
                .receive()
                .response(HttpStatus.CREATED));
    }

    @Test
    @CitrusTest
    public void shouldAddFruitsFromBehavior() {
        Fruit fruit = TestHelper.createFruit("Raspberry",
                new Category("berry"), Fruit.Status.PENDING, "smoothie");

        given(applyBehavior(new AddFruitBehavior(fruit, fruitStoreClient)));
    }
}
