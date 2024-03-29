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

package org.citrusframework.demo.behavior;

import org.citrusframework.TestActionRunner;
import org.citrusframework.TestBehavior;
import org.citrusframework.demo.fruits.model.Fruit;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.message.builder.ObjectMappingPayloadBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.citrusframework.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
public class AddFruitBehavior implements TestBehavior {

    private final HttpClient client;
    private final Fruit fruit;

    private String idVariable = "id";

    public AddFruitBehavior(Fruit fruit, HttpClient client) {
        this.fruit = fruit;
        this.client = client;
    }

    @Override
    public void apply(TestActionRunner $) {
        $.run(http().client(client)
                .send()
                .post("/api/fruits")
                .message()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new ObjectMappingPayloadBuilder(fruit)));

        $.run(http().client(client)
                .receive()
                .response(HttpStatus.CREATED)
                .message()
                .extract(jsonPath()
                            .expression("$.id", idVariable)));

        $.run(context -> fruit.setId(Long.parseLong(context.getVariable(idVariable))));
    }

    public AddFruitBehavior withIdVariable(String name) {
        this.idVariable = name;
        return this;
    }
}
