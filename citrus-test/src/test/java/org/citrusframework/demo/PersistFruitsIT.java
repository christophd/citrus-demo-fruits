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
import com.consol.citrus.junit.JUnit4CitrusSupport;
import com.consol.citrus.message.builder.ObjectMappingPayloadBuilder;
import org.citrusframework.demo.fruits.model.Category;
import org.citrusframework.demo.fruits.model.Fruit;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;

import static com.consol.citrus.actions.ExecuteSQLQueryAction.Builder.query;
import static com.consol.citrus.http.actions.HttpActionBuilder.http;
import static com.consol.citrus.validation.json.JsonPathVariableExtractor.Builder.jsonPathExtractor;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = EndpointConfig.class)
public class PersistFruitsIT extends JUnit4CitrusSupport {

    @Autowired
    private HttpClient fruitStoreClient;

    @Autowired
    private DataSource fruitsDataSource;

    @Test
    @CitrusTest
    public void shouldPersistFruits() {
        Fruit fruit = TestHelper.createFruit("Nectarine",
                new Category( "pomme"), Fruit.Status.PENDING, "summer");

        when(http().client(fruitStoreClient)
                .send()
                .post("/fruits")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .payload(new ObjectMappingPayloadBuilder(fruit)));

        then(http().client(fruitStoreClient)
                .receive()
                .response(HttpStatus.CREATED)
                .process(jsonPathExtractor()
                        .expression("$.id", "id")));

        then(query(fruitsDataSource)
            .statement("SELECT id, name FROM fruit WHERE id=${id}")
            .validate("name", fruit.getName()));
    }
}
