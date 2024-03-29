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

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import org.citrusframework.demo.fruits.model.Nutrition;
import org.citrusframework.demo.fruits.model.Price;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * @author Christoph Deppisch
 */
@Path("/market/fruits")
@RegisterRestClient(configKey="food-market-client")
public interface FoodMarketClient {

    @GET
    @Path("/price/{name}")
    @Produces("application/json")
    Price getPrice(@PathParam("name") String name);

    @GET
    @Path("/nutrition/{name}")
    @Produces("application/json")
    Nutrition getNutrition(@PathParam("name") String name);
}
