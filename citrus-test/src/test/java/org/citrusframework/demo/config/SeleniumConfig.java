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

package org.citrusframework.demo.config;

import com.consol.citrus.container.AfterSuite;
import com.consol.citrus.container.AfterTest;
import com.consol.citrus.container.SequenceAfterSuite;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import com.consol.citrus.selenium.endpoint.SeleniumBrowserBuilder;
import org.openqa.selenium.remote.Browser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import static com.consol.citrus.actions.SleepAction.Builder.sleep;
import static com.consol.citrus.selenium.actions.SeleniumActionBuilder.selenium;

/**
 * @author Christoph Deppisch
 */
@Configuration
public class SeleniumConfig {

    @Bean
    public SeleniumBrowser browser() {
        return new SeleniumBrowserBuilder()
                .type(Browser.HTMLUNIT.browserName())
                .build();
    }

    @Bean
    @DependsOn("browser")
    public AfterSuite afterSuite(SeleniumBrowser browser) {
        return new SequenceAfterSuite() {
            @Override
            public void doExecute(TestContext context) {
                selenium().browser(browser)
                        .stop()
                        .build()
                        .execute(context);
            }
        };
    }

    @Bean
    public AfterTest afterTest() {
        return new SequenceAfterTest() {
            @Override
            public void doExecute(TestContext context) {
                sleep().milliseconds(500L)
                        .build()
                        .execute(context);
            }
        };
    }
}
