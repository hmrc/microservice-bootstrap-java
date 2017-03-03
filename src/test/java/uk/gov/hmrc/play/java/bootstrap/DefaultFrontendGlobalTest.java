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

import com.typesafe.config.ConfigFactory;
import org.junit.Before;
import org.junit.Test;
import play.GlobalSettings;
import play.api.mvc.EssentialFilter;
import play.api.test.FakeHeaders;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Result;
import uk.gov.hmrc.play.java.ScalaFixtures;
import uk.gov.hmrc.play.java.config.ServicesConfig;
import uk.gov.hmrc.play.java.connectors.AuditConnector;
import uk.gov.hmrc.play.java.connectors.AuthConnector;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.test.Helpers.*;

public class DefaultFrontendGlobalTest extends ScalaFixtures {
    private Http.RequestHeader rh = mock(Http.RequestHeader.class);
    private int futureTimeout = 3000;

    private class DefaultFrontendGlobalWithShowError extends DefaultFrontendGlobal {
        private ShowErrorPage errorPage = (pageTitle, heading, message, request) -> views.html.global_error.render(pageTitle, heading, message);

        @Override
        protected ShowErrorPage showErrorPage() {
            return errorPage;
        }
    }

    @Before
    public void setUp() {
        Map<String, String> flashData = Collections.emptyMap();
        Map<String, Object> argData = Collections.emptyMap();
        Long id = 2L;
        play.api.mvc.RequestHeader header = mock(play.api.mvc.RequestHeader.class);

        when(header.headers()).thenReturn(new FakeHeaders(FakeHeaders.apply$default$1()));
        when(header.method()).thenReturn("GET");

        Http.Request request = mock(Http.Request.class);

        Http.Context ctx = new Http.Context(id, header, request, flashData, flashData, argData);
        Http.Context.current.set(ctx);
    }

    @Test
    public void renderInternalServerError() {
        GlobalSettings settings = new DefaultFrontendGlobalWithShowError();
        running(fakeApplication(settings), () -> {
            Exception exception = new Exception("Runtime exception");
            Result result = settings.onError(rh, exception).get(futureTimeout);
            assertThat(status(result), is(INTERNAL_SERVER_ERROR));
            assertThat(contentAsString(result), containsString("Sorry, we’re experiencing technical difficulties"));
            assertThat(contentAsString(result), containsString("Please try again in a few minutes"));
        });
    }

    protected Map<String, Object> additionalProperties() {
        Map<String, Object> props = super.additionalProperties();
        props.putAll(ConfigFactory.parseFileAnySyntax(new File("src/main/resources/common.conf")).resolve().root().unwrapped());
        props.put("Test.auditing.enabled", false);
       return props;
    }

    @Test
    public void renderPageNotFound() {
        GlobalSettings settings = new DefaultFrontendGlobalWithShowError();
        running(fakeApplication(settings), () -> {
            Result result = settings.onHandlerNotFound(rh).get(futureTimeout);
            assertThat(status(result), is(NOT_FOUND));
            assertThat(contentAsString(result), containsString("This page can’t be found"));
            assertThat(contentAsString(result), containsString("Please check that you have entered the correct web address"));
        });
    }

    @Test
    public void renderBadRequest() {
        GlobalSettings settings = new DefaultFrontendGlobalWithShowError();
        running(fakeApplication(settings), () -> {
            Result result = settings.onBadRequest(rh, "").get(futureTimeout);
            assertThat(status(result), is(BAD_REQUEST));
            assertThat(contentAsString(result), containsString("Bad request"));
            assertThat(contentAsString(result), containsString("Please check that you have entered the correct web address"));
        });
    }

    @Test
    public void renderNotFoundWithServer() {
        DefaultFrontendGlobal minusWhitelist = new DefaultFrontendGlobalWithShowError() {
            @Override
            public <T extends EssentialFilter> Class<T>[] filters() {
                return (Class[]) Arrays.stream(super.filters()).toArray(size -> new Class[size]);
            }
        };

        running(testServer(3333, fakeApplication(minusWhitelist)), HTMLUNIT, browser -> {
            browser.goTo("http://localhost:3333/notFound").pageSource();
            assertThat(browser.pageSource(), containsString(Messages.get("global.error.pageNotFound404.title")));
        });
    }
}
