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

import com.consol.citrus.context.TestContext;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import com.consol.citrus.selenium.model.PageValidator;
import com.consol.citrus.selenium.model.WebPage;
import org.citrusframework.demo.fruits.model.Fruit;
import org.junit.Assert;
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

    public FruitsPage(Fruit fruit) {
        this.fruit = fruit;
    }

    /**
     * Adds fruit via HTML form submit on the page.
     */
    public void addFruit() {
        name.sendKeys(fruit.getName());
        description.sendKeys(fruit.getDescription());

        new Select(category).selectByValue(fruit.getCategory().getName());

        price.clear();
        price.sendKeys(fruit.getPrice().toPlainString());
        tags.sendKeys(String.join(",", fruit.getTags()));

        new Select(status).selectByValue(fruit.getStatus().name());

        if (fruit.getNutrition() != null) {
            calories.clear();
            calories.sendKeys(fruit.getNutrition().getCalories().toString());

            sugar.clear();
            sugar.sendKeys(fruit.getNutrition().getSugar().toString());
        }

        saveButton.click();
    }

    @Override
    public void validate(FruitsPage webPage, SeleniumBrowser browser, TestContext context) {
        Assert.assertEquals("Fruit Store Demo", heading.getText());
        Assert.assertTrue(saveButton.isEnabled());
    }
}
