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

package org.citrusframework.demo.page;

import org.citrusframework.context.TestContext;
import org.citrusframework.demo.fruits.model.Fruit;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.model.PageValidator;
import org.citrusframework.selenium.model.WebPage;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

/**
 * @author Christoph Deppisch
 */
public class FruitsPage implements WebPage, PageValidator<FruitsPage> {

    @FindBy(tagName="h1")
    private WebElement heading;

    @FindBy(id="save")
    private WebElement saveButton;

    @FindBy(id="name")
    private WebElement name;

    @FindBy(id="description")
    private WebElement description;

    @FindBy(id="category")
    private WebElement category;

    @FindBy(id="price")
    private WebElement price;

    @FindBy(id="calories")
    private WebElement calories;

    @FindBy(id="sugar")
    private WebElement sugar;

    @FindBy(id="tags")
    private WebElement tags;

    @FindBy(id="status")
    private WebElement status;

    private final Fruit fruit;

    private static final boolean DELAY_ENABLED = false;

    public FruitsPage(Fruit fruit) {
        this.fruit = fruit;
    }

    /**
     * Adds fruit via HTML form submit on the page.
     */
    public void addFruit() {
        delay(2000L);

        name.sendKeys(fruit.getName());
        delay();

        description.sendKeys(fruit.getDescription());

        new Select(category).selectByValue(fruit.getCategory().getName());

        delay();

        price.clear();
        price.sendKeys(fruit.getPrice().toPlainString());
        tags.sendKeys(String.join(",", fruit.getTags()));

        delay();
        new Select(status).selectByValue(fruit.getStatus().name());

        if (fruit.getNutrition() != null) {
            calories.clear();
            calories.sendKeys(fruit.getNutrition().getCalories().toString());

            delay();

            sugar.clear();
            sugar.sendKeys(fruit.getNutrition().getSugar().toString());
        }

        delay();
        saveButton.click();

        delay(5000L);
    }

    @Override
    public void validate(FruitsPage webPage, SeleniumBrowser browser, TestContext context) {
        Assertions.assertEquals("Fruit Store Demo", heading.getText());
        Assertions.assertTrue(saveButton.isEnabled());
    }

    private static void delay() {
        delay(500L);
    }

    private static void delay(Long milliseconds) {
        if (DELAY_ENABLED) {
            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
