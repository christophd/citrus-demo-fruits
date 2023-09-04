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

import java.math.BigDecimal;
import java.util.Map;

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.config.CitrusSpringConfig;
import org.citrusframework.context.TestContext;
import org.citrusframework.demo.config.EndpointConfig;
import org.citrusframework.demo.fruits.model.Category;
import org.citrusframework.demo.fruits.model.Fruit;
import org.citrusframework.demo.fruits.model.Nutrition;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.message.builder.ObjectMappingPayloadBuilder;
import org.citrusframework.validation.json.JsonMappingValidationProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;

import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariable;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.validation.json.JsonPathMessageValidationContext.Builder.jsonPath;

/**
 * @author Christoph Deppisch
 */
@CitrusSpringSupport
@ContextConfiguration(classes = { CitrusSpringConfig.class, EndpointConfig.class })
public class GetFruitsIT {

    @Autowired
    private HttpClient fruitStoreClient;

    @Test
    @CitrusTest
    public void shouldGetFruits(@CitrusResource GherkinTestActionRunner $) {
        $.given(createVariable("id", "1001"));

        $.when(http().client(fruitStoreClient)
                .send()
                .get("/api/fruits/${id}")
                .message()
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        $.then(http().client(fruitStoreClient)
                .receive()
                .response(HttpStatus.OK)
                .message()
                .body("""
                    {
                        "id": ${id},
                        "name": "Pineapple",
                        "description": "@ignore@",
                        "category": {
                            "id": 2,
                            "name":"tropical"
                        },
                        "nutrition": {
                            "id": 2,
                            "calories": 97,
                            "sugar": 14
                        },
                        "status": "PENDING",
                        "price": "@greaterThan(0.00)@",
                        "tags": ["cocktail"]
                    }
                """));
    }

    @Test
    @CitrusTest
    public void shouldGetFruitsResource(@CitrusResource GherkinTestActionRunner $) {
        $.given(createVariable("id", "1001"));

        $.when(http().client(fruitStoreClient)
                .send()
                .get("/api/fruits/${id}")
                .message()
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        $.then(http().client(fruitStoreClient)
                .receive()
                .response(HttpStatus.OK)
                .message()
                .body(new ClassPathResource("data/fruit_response.json")));
    }

    @Test
    @CitrusTest
    public void shouldGetFruitsWithModel(@CitrusResource GherkinTestActionRunner $) {
        Fruit fruit = TestHelper.createFruit("Pineapple",
                new Category(2L, "tropical"), new Nutrition(2L, 97, 14), Fruit.Status.PENDING, "cocktail");

        fruit.setId(1001L);
        fruit.setDescription("@ignore@");
        fruit.setPrice(BigDecimal.valueOf(1.99D));

        $.when(http().client(fruitStoreClient)
                .send()
                .get("/api/fruits/" + fruit.getId())
                .message()
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        $.then(http().client(fruitStoreClient)
                .receive()
                .response(HttpStatus.OK)
                .message()
                .body(new ObjectMappingPayloadBuilder(fruit)));
    }

    @Test
    @CitrusTest
    public void shouldGetFruitsWithJsonPath(@CitrusResource GherkinTestActionRunner $) {
        $.given(createVariable("id", "1001"));

        $.when(http().client(fruitStoreClient)
                .send()
                .get("/api/fruits/${id}")
                .message()
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        $.then(http().client(fruitStoreClient)
                .receive()
                .response(HttpStatus.OK)
                .validate(jsonPath()
                        .expression("$.id", "${id}")
                        .expression("$.name", "Pineapple")
                        .expression("$.category.name", "tropical")
                        .expression("$.status", Fruit.Status.PENDING.name())
                        .expression("$.tags.size()", 1L)
                        .expression("$.price", BigDecimal.valueOf(1.99D))));
    }

    @Test
    @CitrusTest
    public void shouldGetFruitsWithValidator(@CitrusResource GherkinTestActionRunner $) {
        $.given(createVariable("id", "1001"));

        $.when(http().client(fruitStoreClient)
                .send()
                .get("/api/fruits/${id}")
                .message()
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        $.then(http().client(fruitStoreClient)
                .receive()
                .response(HttpStatus.OK)
                .validate(new JsonMappingValidationProcessor<Fruit>(Fruit.class) {
                    @Override
                    public void validate(Fruit fruit, Map<String, Object> headers, TestContext context) {
                        Assertions.assertEquals("Pineapple", fruit.getName());
                        Assertions.assertEquals("tropical", fruit.getCategory().getName());
                    }
                }));
    }
}
