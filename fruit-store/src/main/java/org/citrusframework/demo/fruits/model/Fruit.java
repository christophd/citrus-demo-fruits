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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import javax.persistence.AttributeConverter;
import javax.persistence.CascadeType;
import javax.persistence.Convert;
import javax.persistence.Converter;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;

/**
 * @author Christoph Deppisch
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "Fruits.findById",
            query = "SELECT f FROM Fruit f WHERE f.id = :id"),
    @NamedQuery(name = "Fruits.findAll",
            query = "SELECT f FROM Fruit f ORDER BY f.id")
})
public class Fruit {

    private Long id;
    private String name;
    private String description;
    private Category category;
    private List<String> tags;
    private Status status = Status.PENDING;
    private Nutrition nutrition;
    private BigDecimal price = new BigDecimal("0.1");

    public Fruit() {
    }

    public Fruit(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public enum Status {
        SOLD,
        PENDING,
        AVAILABLE
    }

    @Converter
    private static class StringListConverter implements AttributeConverter<List<String>, String> {
        private static final String DELIMITER = ",";

        @Override
        public String convertToDatabaseColumn(List<String> stringList) {
            return String.join(DELIMITER, stringList);
        }

        @Override
        public List<String> convertToEntityAttribute(String string) {
            return Arrays.asList(string.split(DELIMITER));
        }
    }

    @Id
    @SequenceGenerator(name = "fruitSeq", sequenceName = "fruit_id_seq", allocationSize = 1, initialValue = 1000)
    @GeneratedValue(generator = "fruitSeq")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ManyToOne(cascade = CascadeType.ALL)
    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @ManyToOne(cascade = CascadeType.ALL)
    public Nutrition getNutrition() {
        return nutrition;
    }

    public void setNutrition(Nutrition nutrition) {
        this.nutrition = nutrition;
    }

    @Convert(converter = StringListConverter.class)
    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
