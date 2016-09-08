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

import play.api.mvc.Session$;
import uk.gov.hmrc.crypto.Crypted;
import uk.gov.hmrc.crypto.CryptoGCMWithKeysFromConfig;
import uk.gov.hmrc.crypto.PlainText;
import uk.gov.hmrc.play.java.filters.frontend.CookieCryptoFilter;

public class SessionCookieCryptoFilter extends CookieCryptoFilter {
    private static final CryptoGCMWithKeysFromConfig CRYPTO = CryptoGCMWithKeysFromConfig.apply("cookie.encryption");

    private static final String COOKIE_NAME = Session$.MODULE$.COOKIE_NAME();
    private static final Encrypter ENCRYPTER = (plainText) -> CRYPTO.encrypt(PlainText.apply(plainText)).value();
    private static final Decrypter DECRYPTER = (encText) -> CRYPTO.decrypt(Crypted.apply(encText)).value();

    public SessionCookieCryptoFilter() {
        super(COOKIE_NAME, ENCRYPTER, DECRYPTER);
    }
}
