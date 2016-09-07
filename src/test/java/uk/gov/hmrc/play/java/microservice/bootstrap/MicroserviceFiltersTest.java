/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.play.java.microservice.bootstrap;

import org.hamcrest.core.Is;
import org.junit.Test;
import uk.gov.hmrc.play.auth.controllers.AuthParamsControllerConfig;
import uk.gov.hmrc.play.auth.microservice.connectors.AuthConnector;
import uk.gov.hmrc.play.java.ScalaFixtures;
import uk.gov.hmrc.play.java.connectors.AuditConnector;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class MicroserviceFiltersTest extends ScalaFixtures {

    @Test
    public void includeAuthFilterIfDefined() {
        DefaultMicroserviceGlobal testGlobal = new DefaultMicroserviceGlobal() {
            @Override
            protected MicroserviceAuthFilter microserviceAuthFilter() {
                return new MicroserviceAuthFilter() {
                    @Override
                    public AuthConnector authConnector() {
                        return mock(AuthConnector.class);
                    }

                    @Override
                    public AuthParamsControllerConfig authParamsConfig() {
                        return mock(AuthParamsControllerConfig.class);
                    }
                };
            }

            @Override
            protected MicroserviceAuditFilter microserviceAuditFilter() {
                return () -> mock(AuditConnector.class);
            }

            @Override
            protected ErrorAuditing errorAuditing() {
                return mock(ErrorAuditing.class);
            }
        };

        assertThat(testGlobal.filters().length, Is.is(7));
    }

    @Test
    public void notIncludeAuthFilterIfNotDefined() {
        DefaultMicroserviceGlobal testGlobal = new DefaultMicroserviceGlobal() {
            @Override
            protected MicroserviceAuthFilter microserviceAuthFilter() {
                return null;
            }

            @Override
            protected MicroserviceAuditFilter microserviceAuditFilter() {
                return () -> mock(AuditConnector.class);
            }

            @Override
            protected ErrorAuditing errorAuditing() {
                return mock(ErrorAuditing.class);
            }
        };

        assertThat(testGlobal.filters().length, Is.is(6));
    }
}
