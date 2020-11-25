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
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.demo.config.EndpointConfig;
import org.citrusframework.demo.fruits.model.Category;
import org.citrusframework.demo.fruits.model.Fruit;
import org.citrusframework.demo.page.FruitsPage;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static com.consol.citrus.selenium.actions.SeleniumActionBuilder.selenium;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = EndpointConfig.class)
public class FruitsPageIT extends JUnit4CitrusSupport {

    @Autowired
    private SeleniumBrowser browser;

    @Autowired
    private HttpClient fruitStoreClient;

    @Autowired
    private DataSource fruitsDataSource;

    @Test
    @CitrusTest
    public void shouldGetFruitsWithModel() {
        Fruit fruit = TestHelper.createFruit("Grapefruit",
                new Category(2L, "tropical"), Fruit.Status.PENDING, "juicy,healthy");
        fruit.setDescription("Not everybody likes it");
        FruitsPage page = new FruitsPage(fruit);

        given(selenium()
                .browser(browser)
                .start());

        given(selenium()
                .navigate(fruitStoreClient.getEndpointConfiguration().getRequestUrl()));

        given(selenium()
                .page(page)
                .validate());

        when(selenium()
                .page(page)
                .execute("addFruit"));

        given(selenium()
                .page(page)
                .validate());
    }
}
