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

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.junit.JUnit4CitrusSupport;
import com.consol.citrus.message.builder.ObjectMappingPayloadBuilder;
import org.citrusframework.demo.config.EndpointConfig;
import org.citrusframework.demo.fruits.model.Category;
import org.citrusframework.demo.fruits.model.Fruit;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;

import static com.consol.citrus.actions.CreateVariablesAction.Builder.createVariable;
import static com.consol.citrus.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = EndpointConfig.class)
public class GetFruitsIT extends JUnit4CitrusSupport {

    @Autowired
    private HttpClient fruitStoreClient;

    @Test
    @CitrusTest
    public void shouldGetFruits() {
        given(createVariable("id", "1001"));

        when(http().client(fruitStoreClient)
                .send()
                .get("/fruits/${id}")
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        then(http().client(fruitStoreClient)
                .receive()
                .response(HttpStatus.OK)
                .payload("{" +
                    "\"id\": ${id}," +
                    "\"name\": \"Pineapple\"," +
                    "\"description\": \"@ignore@\"," +
                    "\"category\": {" +
                        "\"id\": 2," +
                        "\"name\":\"tropical\"" +
                    "}," +
                    "\"status\": \"PENDING\"," +
                    "\"price\": \"@greaterThan(0.00)@\"," +
                    "\"tags\": [\"cocktail\"]" +
                "}"));
    }

    @Test
    @CitrusTest
    public void shouldGetFruitsResource() {
        given(createVariable("id", "1001"));

        when(http().client(fruitStoreClient)
                .send()
                .get("/fruits/${id}")
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        then(http().client(fruitStoreClient)
                .receive()
                .response(HttpStatus.OK)
                .payload(new ClassPathResource("data/fruit_response.json")));
    }

    @Test
    @CitrusTest
    public void shouldGetFruitsWithModel() {
        Fruit fruit = TestHelper.createFruit("Pineapple",
                new Category(2L, "tropical"), Fruit.Status.PENDING, "cocktail");

        fruit.setId(1001L);
        fruit.setDescription("@ignore@");
        fruit.setPrice(BigDecimal.valueOf(1.99D));

        when(http().client(fruitStoreClient)
                .send()
                .get("/fruits/" + fruit.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE));

        then(http().client(fruitStoreClient)
                .receive()
                .response(HttpStatus.OK)
                .payload(new ObjectMappingPayloadBuilder(fruit)));
    }
}
