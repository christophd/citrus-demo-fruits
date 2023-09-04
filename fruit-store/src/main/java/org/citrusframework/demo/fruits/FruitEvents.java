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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import io.smallrye.reactive.messaging.annotations.Merge;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.citrusframework.demo.fruits.model.Fruit;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

/**
 * @author Christoph Deppisch
 */
@ApplicationScoped
public class FruitEvents {

    private static final Logger LOG = Logger.getLogger(FruitEvents.class);

    @Inject
    ObjectMapper mapper;

    public void onAdded(String id) {
        sendEvent(id, "added::" + id);
    }

    public void onRemoved(String id) {
        sendEvent(id, "removed::" + id);
    }

    @Incoming("fruit-events")
    @Outgoing("fruit-events-stream")
    @Merge
    @Broadcast
    public String processEvent(Fruit fruit) throws JsonProcessingException {
        LOG.info("Processing fruit id:" + fruit.getId());
        return mapper.writeValueAsString(fruit);
    }

    private void sendEvent(String id, String value) {
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(getProducerConfig())) {
            RecordMetadata metadata = producer.send(new ProducerRecord<>("fruits.events", 0, id, value)).get();
            LOG.info(String.format("FruitEvent successfully sent to %s", metadata.toString()));
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Failed to send FruitEvent", e);
        }
    }

    private Map<String, Object> getProducerConfig() {
        Map<String, Object> producerConfig = new HashMap<>();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9090");
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        producerConfig.put(ProducerConfig.CLIENT_ID_CONFIG, "producer_" + UUID.randomUUID().toString());

        return producerConfig;
    }
}
