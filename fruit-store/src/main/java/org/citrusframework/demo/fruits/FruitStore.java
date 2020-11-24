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

package org.citrusframework.demo.fruits;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.citrusframework.demo.fruits.model.Category;
import org.citrusframework.demo.fruits.model.Fruit;
import org.h2.tools.Server;

/**
 * @author Christoph Deppisch
 */
@Singleton
public class FruitStore {

    @Inject
    EntityManager em;

    @PostConstruct
    public void setup() throws SQLException {
        Server.createTcpServer().start();

        Category pome = new Category("pome");
        Category tropical = new Category("tropical");
        Category berry = new Category("berry");

        Fruit apple = new Fruit("Apple", "Winter fruit");
        apple.setCategory(pome);
        apple.setTags(Arrays.asList("winter", "juicy"));
        apple.setPrice(new BigDecimal("1.59"));
        apple.setStatus(Fruit.Status.AVAILABLE);
        add(apple);

        Fruit pineapple = new Fruit("Pineapple", "Tropical fruit");
        pineapple.setTags(Collections.singletonList("cocktail"));
        pineapple.setCategory(tropical);
        pineapple.setPrice(new BigDecimal("1.99"));
        add(pineapple);

        Fruit strawberry = new Fruit("Strawberry", "Delicious");
        strawberry.setTags(Arrays.asList("summer", "smoothie"));
        strawberry.setCategory(berry);
        strawberry.setStatus(Fruit.Status.SOLD);
        strawberry.setPrice(new BigDecimal("2.55"));
        add(strawberry);
    }

    public Fruit findById(Long id) {
        return em.createNamedQuery("Fruits.findById", Fruit.class)
                        .setParameter("id", id)
                        .getSingleResult();
    }

    @Transactional
    public void add(Fruit fruit) {
        try {
            Category category = em.createNamedQuery("Categories.findByName", Category.class)
                            .setParameter("name", fruit.getCategory().getName())
                            .getSingleResult();
            fruit.setCategory(category);
        } catch (NoResultException e) {
            em.persist(fruit.getCategory());
        }

        em.persist(fruit);
    }

    @Transactional
    public void update(Fruit fruit) {
        em.merge(fruit);
    }

    public List<Fruit> findAll() {
        return em.createNamedQuery("Fruits.findAll", Fruit.class).getResultList();
    }

    @Transactional
    public void remove(Long id) {
        em.remove(findById(id));
    }
}
