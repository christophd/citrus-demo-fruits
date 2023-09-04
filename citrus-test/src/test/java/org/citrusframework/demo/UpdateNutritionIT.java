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

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.junit.spring.JUnit4CitrusSpringSupport;
import org.citrusframework.demo.behavior.AddFruitBehavior;
import org.citrusframework.demo.config.EndpointConfig;
import org.citrusframework.demo.fruits.model.Category;
import org.citrusframework.demo.fruits.model.Fruit;
import org.citrusframework.demo.fruits.model.Nutrition;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;

import static com.consol.citrus.actions.CreateVariablesAction.Builder.createVariable;
import static com.consol.citrus.actions.ExecuteSQLQueryAction.Builder.query;
import static com.consol.citrus.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = EndpointConfig.class)
public class UpdateNutritionIT extends JUnit4CitrusSpringSupport {

    @Autowired
    private HttpClient fruitStoreClient;

    @Autowired
    private HttpServer foodMarketService;

    @Autowired
    private DataSource fruitsDataSource;

    @Test
    @CitrusTest
    public void shouldUpdatePrice() {
        Fruit fruit = TestHelper.createFruit("Mango",
                new Category( "tropical"), new Nutrition(54, 11), Fruit.Status.PENDING, "summer");

        given(createVariable("calories", "60"));
        given(createVariable("sugar", "12"));
        given(applyBehavior(new AddFruitBehavior(fruit, fruitStoreClient)));

        when(http().client(fruitStoreClient)
                .send()
                .get("/api/fruits/nutrition/" + fruit.getId())
                .fork(true)
                .message()
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        then(http().server(foodMarketService)
                .receive()
                .get("/market/fruits/nutrition/" + fruit.getName().toLowerCase()));

        then(http().server(foodMarketService)
                .send()
                .response(HttpStatus.OK)
                .message()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("{ \"calories\": ${calories}, \"sugar\": ${sugar} }"));

        then(http().client(fruitStoreClient)
                .receive()
                .response(HttpStatus.OK));

        then(query(fruitsDataSource)
                .statement("SELECT calories, sugar FROM nutrition INNER JOIN fruit ON nutrition.id = fruit.nutrition_id WHERE fruit.id=${id}")
                .validate("calories", "${calories}")
                .validate("sugar", "${sugar}"));
    }
}
