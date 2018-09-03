/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.test.app.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.app.client.model.Quote;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
class EventConsumer {
	private static Logger logger = LoggerFactory.getLogger(EventConsumer.class);


    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KieSession kieSession;

    @Autowired
    private ActionProducer actionProducer;

	@KafkaListener(topics = "${cloudkarafka.topic.event}")
	public void processMessage(String message,
							   @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
							   @Header(KafkaHeaders.RECEIVED_TOPIC) List<String> topics,
							   @Header(KafkaHeaders.OFFSET) List<Long> offsets) throws IOException {

        Quote quote = objectMapper.readValue(message, Quote.class);

        kieSession.setGlobal("actionProducer", actionProducer);
        kieSession.insert(quote);
        int ruleNumber = kieSession.fireAllRules();
		System.out.printf("%s-%d[%d] \"%s\"\n", topics.get(0), partitions.get(0), offsets.get(0), message);
	}
}
