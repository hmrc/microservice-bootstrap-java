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


import akka.dispatch.Futures;
import org.junit.Before;
import org.junit.Test;
import play.api.PlayException;
import play.api.mvc.Result;
import play.api.test.FakeHeaders;
import play.mvc.Http;
import uk.gov.hmrc.play.audit.EventTypes$;
import uk.gov.hmrc.play.audit.http.connector.AuditConnector;
import uk.gov.hmrc.play.audit.http.connector.AuditResult;
import uk.gov.hmrc.play.audit.model.AuditEvent;
import uk.gov.hmrc.play.http.NotFoundException;
import uk.gov.hmrc.play.java.ScalaFixtures;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultMicroserviceGlobalTest extends ScalaFixtures {

    private AuditConnector auditConnector = mock(uk.gov.hmrc.play.java.connectors.AuditConnector.class);

    private List<AuditEvent> recordedEvents;

    @Before
    public void setUp() {
        recordedEvents = new ArrayList<>();
        initHttpContext();

        when(header.headers()).thenReturn(new FakeHeaders(FakeHeaders.apply$default$1()));
        when(header.method()).thenReturn("GET");

        when(auditConnector.sendEvent(any(), any(), any())).thenAnswer(invocation -> {
            recordedEvents.add((AuditEvent) invocation.getArguments()[0]);
            return Futures.successful(mock(AuditResult.class));
        });
    }

    private DefaultMicroserviceGlobal testRestGlobal = new DefaultMicroserviceGlobal() {
        @Override
        protected MicroserviceAuthFilter microserviceAuthFilter() {
            return mock(MicroserviceAuthFilter.class);
        }

        @Override
        protected MicroserviceAuditFilter microserviceAuditFilter() {
            return () -> auditConnector;
        }

        @Override
        protected ErrorAuditing errorAuditing() {
            return () -> auditConnector;
        }
    };

    @Test
    public void inACaseOfAnApplicationExceptionTheFrameworkShouldSendAnEventToDataStreamAndReturn404StatusCodeForANotFoundException() {
        Result result = this.await(testRestGlobal.onError(mock(Http.RequestHeader.class), new PlayException("", "", new NotFoundException("test")))).toScala();

        assertThat(result.header().status(), is(404));
        assertThat(recordedEvents.isEmpty(), is(false));
        assertThat(recordedEvents.get(0).auditType(), is(EventTypes$.MODULE$.ResourceNotFound()));
    }

    @Test
    public void inACaseOfTheMicroserviceEndpointNotBeingFoundWeShouldSendAResourceNotFoundEventToDataStream() {
        Result result = this.await(testRestGlobal.onHandlerNotFound(mock(Http.RequestHeader.class))).toScala();

        assertThat(result.header().status(), is(404));
        assertThat(recordedEvents.isEmpty(), is(false));
        assertThat(recordedEvents.get(0).auditType(), is(EventTypes$.MODULE$.ResourceNotFound()));
    }

    @Test
    public void inACaseOfIncorrectDataBeingSentToTheServiceWeShouldSendAServerValidatonErrorEventToDataStream() {
        Result result = this.await(testRestGlobal.onBadRequest(mock(Http.RequestHeader.class), "error")).toScala();

        assertThat(result.header().status(), is(400));
        assertThat(recordedEvents.isEmpty(), is(false));
        assertThat(recordedEvents.get(0).auditType(), is(EventTypes$.MODULE$.ServerValidationError()));
    }
}
