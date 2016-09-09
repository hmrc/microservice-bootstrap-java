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

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import play.GlobalSettings;
import play.api.mvc.Action;
import play.api.mvc.Cookie;
import play.api.mvc.Cookies$;
import play.api.mvc.Result;
import play.api.test.FakeRequest;
import scala.Some;
import scala.collection.JavaConversions;
import scala.collection.Seq;
import uk.gov.hmrc.play.filters.frontend.DeviceId;
import uk.gov.hmrc.play.filters.frontend.DeviceIdCookie;
import uk.gov.hmrc.play.filters.frontend.DeviceIdCookie$class;
import uk.gov.hmrc.play.java.ScalaFixtures;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.running;

public class DeviceIdCookieFilterTest extends ScalaFixtures {

    private final String theSecret = "some_secret";
    private final String thePreviousSecret = "some previous secret with spaces since spaces cause an issue unless encoded!!!";
    private final String previousSecret = new String(Base64.encodeBase64(thePreviousSecret.getBytes()));
    private final List<String> previousSecretValues = Collections.singletonList(previousSecret);

    private DeviceIdCookie deviceIdCookie(String secret, List<String> previousSecretValues) {
        return new MockDeviceIdCookie(secret, previousSecretValues);
    }

    private Cookie createCookie(String secret, List<String> previousSecretValues) {
        return deviceIdCookie(secret, previousSecretValues).buildNewDeviceIdCookie();
    }

    @Override
    protected Map<String, Object> additionalProperties() {
        Map<String, Object> map = additionalPropertiesWithNoPreviousKey();
        map.put("cookie.deviceId.previous.secret", previousSecretValues);
        return map;
    }

    private Map<String, Object> additionalPropertiesWithNoPreviousKey() {
        Map<String, Object> map = new HashMap<>();
        map.put("cookie.deviceId.secret", theSecret);
        map.put("Test.auditing.enabled", false);
        return map;
    }

    @Test
    public void createTheDeviceIdWhenNoCookieExists() {
        running(fakeApplication(new GlobalSettings()), () -> {
            FakeRequest incomingRequest = FakeRequest.apply();
            Action action = generateActionWithOkResponse();

            Result response = await(new DeviceIdCookieFilter().apply(action).apply(incomingRequest));
            Cookie deviceIdRequestCookie = captureRequest(action).cookies().get(DeviceId.MdtpDeviceId()).get();
            String responseDeviceIdCookie = Cookies$.MODULE$.decode(response.header().headers().get(play.api.http.HeaderNames$.MODULE$.SET_COOKIE()).get()).head().value();
            assertThat(responseDeviceIdCookie, is(deviceIdRequestCookie.value()));
        });
    }

    @Test
    public void createTheDeviceIdWhenNoCookieExistsAndNoPreviousKeys() {
        running(fakeApplication(new GlobalSettings(), additionalPropertiesWithNoPreviousKey()), () -> {
            FakeRequest incomingRequest = FakeRequest.apply();
            Action action = generateActionWithOkResponse();

            Result response = await(new DeviceIdCookieFilter().apply(action).apply(incomingRequest));
            Cookie deviceIdRequestCookie = captureRequest(action).cookies().get(DeviceId.MdtpDeviceId()).get();
            String responseDeviceIdCookie = Cookies$.MODULE$.decode(response.header().headers().get(play.api.http.HeaderNames$.MODULE$.SET_COOKIE()).get()).head().value();
            assertThat(responseDeviceIdCookie, is(deviceIdRequestCookie.value()));
        });
    }

    @Test
    public void doNothingWhenAValidCookieExists() {
        running(fakeApplication(new GlobalSettings()), () -> {
            Cookie cookie = createCookie(theSecret, previousSecretValues);
            FakeRequest incomingRequest = FakeRequest.apply().withCookies(JavaConversions.asScalaBuffer(Collections.singletonList(cookie)));
            Action action = generateActionWithOkResponse();

            Result response = await(new DeviceIdCookieFilter().apply(action).apply(incomingRequest));
            Cookie deviceIdRequestCookie = captureRequest(action).cookies().get(DeviceId.MdtpDeviceId()).get();
            assertTrue(response.header().headers().isEmpty());
            assertThat(deviceIdRequestCookie.value(), is(cookie.value()));
        });
    }

    @Test
    public void successfullyDecodeADeviceIdFromAPreviousSecret() {
        running(fakeApplication(new GlobalSettings()), () -> {
            DeviceIdCookie cookie = deviceIdCookie(theSecret, previousSecretValues);
            String uuid = cookie.generateUUID();
            Long timestamp = cookie.getTimeStamp();

            DeviceId fromPreviousKey = new DeviceId(uuid, Some.apply(timestamp), DeviceId.generateHash(uuid, Some.apply(timestamp), thePreviousSecret));
            Cookie cookieFromPrevious = cookie.makeCookie(fromPreviousKey);

            FakeRequest incomingRequest = FakeRequest.apply().withCookies(JavaConversions.asScalaBuffer(Collections.singletonList(cookieFromPrevious)));
            Action action = generateActionWithOkResponse();

            Result response = await(new DeviceIdCookieFilter().apply(action).apply(incomingRequest));
            Cookie deviceIdRequestCookie = captureRequest(action).cookies().get(DeviceId.MdtpDeviceId()).get();
            assertTrue(response.header().headers().isEmpty());
            assertThat(deviceIdRequestCookie.value(), is(cookieFromPrevious.value()));
        });
    }

    private class MockDeviceIdCookie implements DeviceIdCookie {
        private String secret;
        private List<String> previous;

        MockDeviceIdCookie(String secret, List<String> previous) {
            super();
            this.secret = secret;
            this.previous = previous;
        }

        @Override
        public long getTimeStamp() {
            return DeviceIdCookie$class.getTimeStamp(this);
        }

        @Override
        public String generateUUID() {
            return DeviceIdCookie$class.generateUUID(this);
        }

        @Override
        public String generateDeviceId$default$1() {
            return DeviceIdCookie$class.generateDeviceId$default$1(this);
        }

        @Override
        public DeviceId generateDeviceId(String uuid) {
            return DeviceIdCookie$class.generateDeviceId(this, uuid);
        }

        @Override
        public Cookie buildNewDeviceIdCookie() {
            return DeviceIdCookie$class.buildNewDeviceIdCookie(this);
        }

        @Override
        public Cookie makeCookie(DeviceId deviceId) {
            return DeviceIdCookie$class.makeCookie(this, deviceId);
        }

        @Override
        public Seq<String> previousSecrets() {
            return JavaConversions.asScalaBuffer(previous);
        }

        @Override
        public String secret() {
            return secret;
        }
    }
}
