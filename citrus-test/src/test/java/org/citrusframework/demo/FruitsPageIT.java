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

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.config.CitrusSpringConfig;
import org.citrusframework.demo.config.EndpointConfig;
import org.citrusframework.demo.fruits.model.Category;
import org.citrusframework.demo.fruits.model.Fruit;
import org.citrusframework.demo.fruits.model.Nutrition;
import org.citrusframework.demo.page.FruitsPage;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.citrusframework.selenium.actions.SeleniumActionBuilder.selenium;

/**
 * @author Christoph Deppisch
 */
@CitrusSpringSupport
@ContextConfiguration(classes = { CitrusSpringConfig.class, EndpointConfig.class })
public class FruitsPageIT {

    @Autowired
    private SeleniumBrowser browser;

    @Autowired
    private HttpClient fruitStoreClient;

    @Test
    @CitrusTest
    public void shouldSaveFruitWithForm(@CitrusResource GherkinTestActionRunner $) {
        Fruit fruit = TestHelper.createFruit("Grapefruit",
                new Category(2L, "tropical"), new Nutrition(21, 3), Fruit.Status.PENDING, "juicy,healthy");
        fruit.setDescription("Not everybody likes it");
        fruit.setPrice(BigDecimal.valueOf(1.59D));
        FruitsPage page = new FruitsPage(fruit);

        $.given(selenium()
                .browser(browser)
                .start());

        $.given(selenium()
                .navigate(fruitStoreClient.getEndpointConfiguration().getRequestUrl()));

        $.given(selenium()
                .page(page)
                .validate());

        $.when(selenium()
                .page(page)
                .execute("addFruit"));

        $.given(selenium()
                .page(page)
                .validate());
    }
}
