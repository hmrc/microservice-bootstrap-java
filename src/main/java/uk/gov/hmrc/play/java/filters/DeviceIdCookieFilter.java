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
import play.Logger;
import uk.gov.hmrc.play.java.config.ServicesConfig;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public class DeviceIdCookieFilter extends uk.gov.hmrc.play.java.filters.frontend.DeviceIdFilter {
    private static final String CURRENT_SECRET = "cookie.deviceId.secret";
    private static final String PREVIOUS_SECRET = "cookie.deviceId.previous.secret";
    private static final String MESSAGE = "Missing required configuration entry for deviceIdFilter : %s";

    public DeviceIdCookieFilter() {
        super(ServicesConfig.appName(), currentSecret(), ServicesConfig.auditConnector(), buildListOfPreviousSecrets());
    }

    private static String currentSecret() {
        return Optional.ofNullable(ServicesConfig.getConfString(CURRENT_SECRET, null)).orElseThrow(() -> {
            String errorMessage = String.format(MESSAGE, CURRENT_SECRET);
            Logger.error(errorMessage);
            return new SecurityException(errorMessage);
        });
    }

    private static List<String> buildListOfPreviousSecrets() {
        return ServicesConfig.getConfStringList(PREVIOUS_SECRET, emptyList())
                .stream()
                .map(item -> new String(Base64.decodeBase64(item)))
                .collect(Collectors.toList());
    }
}
