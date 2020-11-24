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

import com.consol.citrus.TestActionRunner;
import com.consol.citrus.TestBehavior;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.message.builder.ObjectMappingPayloadBuilder;
import org.citrusframework.demo.fruits.model.Fruit;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static com.consol.citrus.http.actions.HttpActionBuilder.http;
import static com.consol.citrus.validation.json.JsonPathVariableExtractor.Builder.jsonPathExtractor;

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
    public void apply(TestActionRunner runner) {
        runner.run(http().client(client)
                .send()
                .post("/fruits")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .payload(new ObjectMappingPayloadBuilder(fruit)));

        runner.run(http().client(client)
                .receive()
                .response(HttpStatus.CREATED)
                .process(jsonPathExtractor()
                        .expression("$.id", idVariable)));

        runner.run(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                fruit.setId(Long.parseLong(context.getVariable(idVariable)));
            }
        });
    }

    public AddFruitBehavior withIdVariable(String name) {
        this.idVariable = name;
        return this;
    }
}
