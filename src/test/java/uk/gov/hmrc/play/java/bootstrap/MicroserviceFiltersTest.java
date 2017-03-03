/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.play.java.bootstrap;

import org.hamcrest.core.Is;
import org.junit.Test;
import play.Configuration;
import uk.gov.hmrc.play.java.ScalaFixtures;
import uk.gov.hmrc.play.java.config.ServicesConfig;
import uk.gov.hmrc.play.java.connectors.AuthConnector;
import uk.gov.hmrc.play.java.connectors.AuditConnector;
import uk.gov.hmrc.play.java.filters.MicroserviceAuditFilter;
import uk.gov.hmrc.play.java.filters.MicroserviceAuthFilter;

import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static play.test.Helpers.running;

public class MicroserviceFiltersTest extends ScalaFixtures {

    private Map<String, Object> additionalWithAuth() {
        Map<String, Object> props = super.additionalProperties();
        props.put("authentication.enabled", true);
        return props;
    }

    private Map<String, Object> additionalWithoutAuth() {
        Map<String, Object> props = super.additionalProperties();
        props.put("authentication.enabled", false);
        return props;
    }

    @Test
    public void includeAuthFilterIfDefined() {
        DefaultMicroserviceGlobal global = new DefaultMicroserviceGlobal() {};

        running(fakeApplication(global, additionalWithAuth()), () -> {
            assertThat(global.filters().length, is(7));
        });
    }

    @Test
    public void notIncludeAuthFilterIfNotDefined() {
        DefaultMicroserviceGlobal global = new DefaultMicroserviceGlobal() {};

        running(fakeApplication(global, additionalWithoutAuth()), () -> {
            assertThat(global.filters().length, is(6));
        });
    }
}
