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

package org.citrusframework.demo.fruits.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;

/**
 * @author Christoph Deppisch
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "Nutrition.findById",
                query = "SELECT c FROM Nutrition c WHERE c.id = :id"),
})
public class Nutrition {

    public Long id;
    public Integer calories;
    public Integer sugar;

    public Nutrition() {
    }

    public Nutrition(Long id, Integer calories, Integer sugar) {
        this.id = id;
        this.calories = calories;
        this.sugar = sugar;
    }

    public Nutrition(Integer calories, Integer sugar) {
        this.calories = calories;
        this.sugar = sugar;
    }

    @Id
    @SequenceGenerator(name = "nutSeq", sequenceName = "nut_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "nutSeq")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSugar() {
        return sugar;
    }

    public void setSugar(Integer sugar) {
        this.sugar = sugar;
    }

    public Integer getCalories() {
        return calories;
    }

    public void setCalories(Integer calories) {
        this.calories = calories;
    }
}
