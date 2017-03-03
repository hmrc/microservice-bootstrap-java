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

package uk.gov.hmrc.play.java.filters;

import org.junit.Test;
import play.GlobalSettings;
import play.api.mvc.*;
import play.api.test.FakeRequest;
import scala.Tuple2;
import uk.gov.hmrc.crypto.ApplicationCrypto;
import uk.gov.hmrc.crypto.Crypted;
import uk.gov.hmrc.crypto.PlainText;
import uk.gov.hmrc.play.java.ScalaFixtures;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static play.api.mvc.Cookie.*;
import static play.mvc.Http.HeaderNames.SET_COOKIE;
import static play.test.Helpers.running;
import static scala.collection.JavaConversions.asScalaBuffer;

public class SessionCookieCryptoFilterTest extends ScalaFixtures {

    private Result okWithHeaders() {
        return okResult.withHeaders(asScalaBuffer(singletonList(new Tuple2<>(SET_COOKIE, Cookies$.MODULE$.encode(asScalaBuffer(singletonList(createCookie("our new cookie", false))))))));
    }

    @Override
    protected Map<String, Object> additionalProperties() {
        Map<String, Object> map = new HashMap<>();
        map.put("cookie.encryption.key", "gvBoGdgzqG1AarzF1LY0zQ==");
        return map;
    }

    @Test
    public void decryptTheSessionCookieOnTheWayInAndEncryptItAgainOnTheWayBack() {
        running(fakeApplication(new GlobalSettings()), () -> {
            Cookie encryptedIncomingCookie = createEncryptedCookie("our cookie");
            Cookie unencryptedIncomingCookie = createUnencryptedCookie("our cookie");
            Action action = generateAction(okWithHeaders());

            Request incomingRequest = FakeRequest.apply().withCookies(asScalaBuffer(singletonList(encryptedIncomingCookie)));
            Result response = await(new SessionCookieCryptoFilter().apply(action).apply(incomingRequest));

            assertThat(captureRequest(action).cookies().get(Session.COOKIE_NAME()).get(), is(unencryptedIncomingCookie));

            String encryptedOutgoingCookieValue = Cookies$.MODULE$.decode(response.header().headers().get(SET_COOKIE).get()).head().value();
            assertThat(ApplicationCrypto.SessionCookieCrypto().decrypt(Crypted.apply(encryptedOutgoingCookieValue)).value(), is("our new cookie"));
        });
    }

    private Cookie createEncryptedCookie(String value) {
        return createCookie(value, true);
    }

    private Cookie createUnencryptedCookie(String value) {
        return createCookie(value, false);
    }

    private Cookie createCookie(String value, Boolean encrypted) {
        String cookieName = Session.COOKIE_NAME();
        String val = encrypted ? ApplicationCrypto.SessionCookieCrypto().encrypt(PlainText.apply(value)).value() : value;
        return Cookie.apply(cookieName, val, apply$default$3(), apply$default$4(), apply$default$5(), apply$default$6(), apply$default$7());
    }
}
