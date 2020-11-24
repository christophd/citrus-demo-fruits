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
import java.util.Arrays;

import org.citrusframework.demo.fruits.model.Category;
import org.citrusframework.demo.fruits.model.Fruit;

/**
 * @author Christoph Deppisch
 */
public class TestHelper {

    private TestHelper() {
        // prevent instantiation of utility class.
    }

    public static Fruit createFruit(String name, Category category,
                                    Fruit.Status status, String... tags) {
        Fruit fruit = new Fruit();
        fruit.setName(name);
        fruit.setCategory(category);
        fruit.setStatus(status);
        fruit.setPrice(BigDecimal.valueOf(0.00D));
        fruit.setTags(Arrays.asList(tags));
        return fruit;
    }
}
