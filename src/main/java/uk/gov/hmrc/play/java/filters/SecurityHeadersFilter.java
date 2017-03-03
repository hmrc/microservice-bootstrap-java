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

import scala.compat.java8.JFunction0;
import uk.gov.hmrc.play.frontend.filters.SecurityHeadersFilterFactory$;

public class SecurityHeadersFilter extends play.filters.headers.SecurityHeadersFilter {

    public SecurityHeadersFilter() {
        super((SecurityHeadersConfig) SecurityHeadersFilterFactory$.MODULE$::config);
    }

    public interface SecurityHeadersConfig extends JFunction0<play.filters.headers.SecurityHeadersConfig> {
        play.filters.headers.SecurityHeadersConfig apply();
    }
}
