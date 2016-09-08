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

package uk.gov.hmrc.play.java.bootstrap;

import akka.dispatch.Futures;
import org.junit.Test;
import play.GlobalSettings;
import play.api.mvc.EssentialFilter;
import play.api.mvc.Handler;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Http;
import uk.gov.hmrc.play.http.NotFoundException;
import uk.gov.hmrc.play.java.ScalaFixtures;
import uk.gov.hmrc.play.java.filters.RecoveryFilter;
import uk.gov.hmrc.play.java.filters.SecurityHeadersFilter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

public class FilterChainExceptionTest extends ScalaFixtures {
    private class PartialGlobalSettings extends GlobalSettings {
        @Override
        public Handler onRouteRequest(Http.RequestHeader request) {
            if(request.method().equals("GET") && request.path().equals("/ok")) {
                return generateActionWithOkResponse();
            } else if(request.method().equals("GET") && request.path().equals("/error-async-404")) {
                return generateAction(Futures.failed(new NotFoundException("Expect 404")));
            } else {
                return null;
            }
        }
    }

    private GlobalSettings withSecurityFirst = new PartialGlobalSettings() {
        @SuppressWarnings("unchecked")
        @Override
        public <T extends EssentialFilter> Class<T>[] filters() {
            return new Class[]{SecurityHeadersFilter.class, RecoveryFilter.class};
        }
    };

    private GlobalSettings withSecurityLast = new PartialGlobalSettings() {
        @SuppressWarnings("unchecked")
        @Override
        public <T extends EssentialFilter> Class<T>[] filters() {
            return new Class[]{RecoveryFilter.class, SecurityHeadersFilter.class};
        }
    };

    @Test
    public void actionThrowsNoExceptionAndReturns200OK() {
        running(testServer(3333, fakeApplication(withSecurityFirst)), () -> {
            WSResponse response = WS.url("http://localhost:3333/ok").get().get(5000);
            assertThat(response.getStatus(), is(200));
        });
    }

    @Test
    public void actionThrowsNotFoundExceptionAndReturns404() {
        running(testServer(3333, fakeApplication(withSecurityFirst)), () -> {
            WSResponse response = WS.url("http://localhost:3333/error-async-404").get().get(5000);
            assertThat(response.getStatus(), is(404));
        });
    }

    @Test
    public void noEndpointInRouterAndReturns404() {
        running(testServer(3333, fakeApplication(withSecurityFirst)), () -> {
            WSResponse response = WS.url("http://localhost:3333/no-end-point").get().get(5000);
            assertThat(response.getStatus(), is(404));
        });
    }

    @Test
    public void actionThrowsNotFoundExceptionButFiltersThrowAnInternalServerError() {
        running(testServer(3333, fakeApplication(withSecurityLast)), () -> {
            WSResponse response = WS.url("http://localhost:3333/error-async-404").get().get(5000);
            assertThat(response.getStatus(), is(500));
        });
    }
}
