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
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;

import static org.citrusframework.actions.ApplyTestBehaviorAction.Builder.apply;
import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariable;
import static org.citrusframework.actions.ExecuteSQLQueryAction.Builder.query;
import static org.citrusframework.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
@CitrusSpringSupport
@ContextConfiguration(classes = { CitrusSpringConfig.class, EndpointConfig.class })
public class UpdatePriceIT {

    @Autowired
    private HttpClient fruitStoreClient;

    @Autowired
    private HttpServer foodMarketService;

    @Autowired
    private DataSource fruitsDataSource;

    @Test
    @CitrusTest
    public void shouldUpdatePrice(@CitrusResource GherkinTestActionRunner $) {
        Fruit fruit = TestHelper.createFruit("Peach",
                new Category( "pomme"), new Nutrition(48, 10), Fruit.Status.PENDING, "summer");

        $.given(createVariable("price", "0.99"));
        $.given(apply().behavior(new AddFruitBehavior(fruit, fruitStoreClient)));

        $.when(http().client(fruitStoreClient)
                .send()
                .get("/api/fruits/price/" + fruit.getId())
                .fork(true)
                .message()
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        $.then(http().server(foodMarketService)
                .receive()
                .get("/market/fruits/price/" + fruit.getName().toLowerCase()));

        $.then(http().server(foodMarketService)
                .send()
                .response(HttpStatus.OK)
                .message()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("{ \"name\": \"" + fruit.getName().toLowerCase() + "\", \"value\": ${price} }"));

        $.then(http().client(fruitStoreClient)
                .receive()
                .response(HttpStatus.OK));

        $.then(query(fruitsDataSource)
                .statement("SELECT id, price FROM fruit WHERE id=${id}")
                .validate("price", "${price}"));
    }
}
