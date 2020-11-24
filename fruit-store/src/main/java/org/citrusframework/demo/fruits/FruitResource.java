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

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.citrusframework.demo.fruits.model.Fruit;
import org.citrusframework.demo.fruits.model.Price;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/fruits")
@Produces(MediaType.APPLICATION_JSON)
public class FruitResource {

    @Inject
    FruitStore store;

    @Inject
    @RestClient
    MarketClient marketClient;

    @Inject
    FruitEvents fruitEvents;

    @GET
    @Operation(operationId = "listFruits")
    public List<Fruit> list() {
        return store.findAll();
    }

    @POST
    @Operation(operationId = "addFruit")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response add(Fruit fruit) {
        if (fruit.getId() != null &&
                store.findAll().stream().anyMatch(f -> f.getId().equals(fruit.getId()))) {
            throw new IllegalArgumentException(String.format("Fruit with id '%s' already exists", fruit.getId()));
        }

        store.add(fruit);
        fruitEvents.onAdded(fruit.getId().toString());
        return Response.status(Response.Status.CREATED)
                .entity(fruit).build();
    }

    @GET
    @Path("/{id}")
    @Operation(operationId = "getFruitById")
    public Response find(@PathParam("id") String id) {
        if (!Pattern.compile("\\d+").matcher(id).matches()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Optional<Fruit> found = store.findAll().stream()
                .filter(fruit -> fruit.getId().toString().equals(id))
                .findFirst();

        if (found.isPresent()) {
            return Response.ok(found.get()).build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/price/{id}")
    @Operation(operationId = "getPriceUpdate")
    public Response updatePrice(@PathParam("id") String id) {
        Response response = find(id);
        Fruit fruit = (Fruit) response.getEntity();

        if (fruit != null) {
            Price price = marketClient.getByName(fruit.getName().toLowerCase());
            fruit.setPrice(price.value);
            store.update(fruit);
            return Response.ok(fruit).build();
        }

        return response;
    }

    @DELETE
    @Path("/{id}")
    @Operation(operationId = "deleteFruit")
    public Response delete(@PathParam("id") String id) {
        if (!Pattern.compile("\\d+").matcher(id).matches()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        store.remove(Long.parseLong(id));
        fruitEvents.onRemoved(id);
        return Response.noContent().build();
    }
}

