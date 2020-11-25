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
import com.consol.citrus.kafka.endpoint.KafkaEndpoint;
import com.consol.citrus.kafka.message.KafkaMessageHeaders;
import org.citrusframework.demo.behavior.AddFruitBehavior;
import org.citrusframework.demo.config.EndpointConfig;
import org.citrusframework.demo.fruits.model.Category;
import org.citrusframework.demo.fruits.model.Fruit;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

import static com.consol.citrus.actions.ExecuteSQLQueryAction.Builder.query;
import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.container.RepeatOnErrorUntilTrue.Builder.repeatOnError;
import static com.consol.citrus.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = EndpointConfig.class)
public class DeleteFruitsIT extends JUnit4CitrusSupport {

    @Autowired
    private HttpClient fruitStoreClient;

    @Autowired
    private DataSource fruitsDataSource;

    @Autowired
    private KafkaEndpoint fruitEvents;

    @Test
    @CitrusTest
    public void shouldDeleteFruits() {
        Fruit fruit = TestHelper.createFruit("Watermelon",
                new Category("melon"), Fruit.Status.PENDING, "juicy");

        given(applyBehavior(new AddFruitBehavior(fruit, fruitStoreClient)));

        then(receive(fruitEvents)
                .header(KafkaMessageHeaders.MESSAGE_KEY, "${id}")
                .payload("added::" + fruit.getId()));

        then(repeatOnError()
            .autoSleep(1000L)
            .until((i, context) -> i > 5)
            .actions(query(fruitsDataSource)
                .statement("SELECT count(id) as found_records FROM fruit WHERE id=${id}")
                .validate("found_records", "1")));

        when(http().client(fruitStoreClient)
            .send()
            .delete("/fruits/${id}")
            .fork(true));

        then(receive(fruitEvents)
                .header(KafkaMessageHeaders.MESSAGE_KEY, "${id}")
                .payload("removed::" + fruit.getId()));

        then(http().client(fruitStoreClient)
                .receive()
                .response(HttpStatus.NO_CONTENT));

        then(query(fruitsDataSource)
                .statement("SELECT count(id) as found_records FROM fruit WHERE id=${id}")
                .validate("found_records", "0"));
    }

}
