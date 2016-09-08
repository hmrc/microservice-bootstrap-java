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

package uk.gov.hmrc.play.java.filters;

import org.junit.Test;
import play.GlobalSettings;
import play.api.mvc.Request;
import play.api.mvc.Result;
import play.api.test.FakeRequest;
import uk.gov.hmrc.play.java.ScalaFixtures;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.running;

public class SecurityHeaderFilterTest extends ScalaFixtures {

    private Map<String, Object> additionalConfig(boolean decodingEnabled) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("security.headers.filter.decoding.enabled", decodingEnabled);
        if(decodingEnabled) {
            map.put("play.filters.headers.contentSecurityPolicy", "ZGVmYXVsdC1zcmMgJ3NlbGYn");
        }
        return map;
    }

    @Test
    public void addSecurityHeaderToAnHttpResponseWithFilterEnabledAndSettingsDecodingDisabled() {
        running(fakeApplication(new GlobalSettings(), additionalConfig(true)), () -> {
            Request incomingRequest = FakeRequest.apply();
            Result response = await(new SecurityHeadersFilter().apply(generateActionWithOkResponse()).apply(incomingRequest));

            assertTrue(response.header().headers().contains("Content-Security-Policy"));
            assertTrue(response.header().headers().contains("X-Content-Type-Options"));
            assertTrue(response.header().headers().contains("X-Frame-Options"));
            assertTrue(response.header().headers().contains("X-Permitted-Cross-Domain-Policies"));
            assertTrue(response.header().headers().contains("X-XSS-Protection"));

            assertThat(response.header().headers().get("Content-Security-Policy").get(), is(SecurityHeadersFilter.DEFAULT_CONTENT_SECURITY_POLICY()));
            assertThat(response.header().headers().get("X-Content-Type-Options").get(), is(SecurityHeadersFilter.DEFAULT_CONTENT_TYPE_OPTIONS()));
            assertThat(response.header().headers().get("X-Frame-Options").get(), is(SecurityHeadersFilter.DEFAULT_FRAME_OPTIONS()));
            assertThat(response.header().headers().get("X-Permitted-Cross-Domain-Policies").get(), is(SecurityHeadersFilter.DEFAULT_PERMITTED_CROSS_DOMAIN_POLICIES()));
            assertThat(response.header().headers().get("X-XSS-Protection").get(), is(SecurityHeadersFilter.DEFAULT_XSS_PROTECTION()));
        });
    }

    @Test
    public void addSecurityHeaderToAnHttpResponseWithFilterEnabledAndSettingsDecodingEnabled() {
        running(fakeApplication(new GlobalSettings(), additionalConfig(false)), () -> {
            Request incomingRequest = FakeRequest.apply();
            Result response = await(new SecurityHeadersFilter().apply(generateActionWithOkResponse()).apply(incomingRequest));

            assertTrue(response.header().headers().contains("Content-Security-Policy"));
            assertTrue(response.header().headers().contains("X-Content-Type-Options"));
            assertTrue(response.header().headers().contains("X-Frame-Options"));
            assertTrue(response.header().headers().contains("X-Permitted-Cross-Domain-Policies"));
            assertTrue(response.header().headers().contains("X-XSS-Protection"));

            assertThat(response.header().headers().get("Content-Security-Policy").get(), is("default-src 'self'"));
            assertThat(response.header().headers().get("X-Content-Type-Options").get(), is(SecurityHeadersFilter.DEFAULT_CONTENT_TYPE_OPTIONS()));
            assertThat(response.header().headers().get("X-Frame-Options").get(), is(SecurityHeadersFilter.DEFAULT_FRAME_OPTIONS()));
            assertThat(response.header().headers().get("X-Permitted-Cross-Domain-Policies").get(), is(SecurityHeadersFilter.DEFAULT_PERMITTED_CROSS_DOMAIN_POLICIES()));
            assertThat(response.header().headers().get("X-XSS-Protection").get(), is(SecurityHeadersFilter.DEFAULT_XSS_PROTECTION()));
        });
    }
}
