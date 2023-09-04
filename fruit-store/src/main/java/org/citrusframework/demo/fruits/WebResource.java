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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import org.citrusframework.demo.fruits.model.Category;
import org.citrusframework.demo.fruits.model.Fruit;
import org.citrusframework.demo.fruits.model.Nutrition;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;
import org.reactivestreams.Publisher;

@Path("/")
public class WebResource {

    @Inject
    Template index;

    @Inject
    @Channel("fruit-events")
    Emitter<Fruit> fruitEventsEmitter;

    @Inject
    @Channel("fruit-events-stream")
    Publisher<String> fruitEvents;

    @Inject
    FruitStore store;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance index() {
        List<Fruit> fruits = store.findAll();
        Logger.getLogger("FRUITS").info(fruits);
        return index.data("fruits", fruits)
                    .data("categories", store.getCategories());
    }

    @GET
    @Path("/fruits")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Publisher<String> fruitProcessor() {
        return fruitEvents;
    }

    @POST
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    public TemplateInstance formSubmit(MultivaluedMap<String, String> form) {
        Fruit fruit = new Fruit(form.getFirst("name"), form.getFirst("description"));

        if (form.getFirst("category") != null && !form.getFirst("category").isEmpty()) {
            fruit.setCategory(new Category(form.getFirst("category")));
        }

        if (form.getFirst("sugar") != null && !form.getFirst("sugar").isEmpty()) {
            Nutrition nutrition = new Nutrition();
            nutrition.setSugar(Integer.parseInt(form.getFirst("sugar")));
            if (form.getFirst("calories") != null && !form.getFirst("calories").isEmpty()) {
                nutrition.setCalories(Integer.parseInt(form.getFirst("calories")));
            }

            fruit.setNutrition(nutrition);
        }

        fruit.setPrice(new BigDecimal(form.getFirst("price")));
        fruit.setStatus(Fruit.Status.valueOf(form.getFirst("status")));
        fruit.setTags(Arrays.stream(form.getFirst("tags").split(","))
                .map(String::trim)
                .collect(Collectors.toList()));

        store.add(fruit);

        if (fruitEventsEmitter.hasRequests()) {
            fruitEventsEmitter.send(fruit);
        }
        return index();
    }
}
