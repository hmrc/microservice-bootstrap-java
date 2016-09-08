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

import org.apache.commons.lang3.time.FastDateFormat;
import play.api.LoggerLike;
import play.api.mvc.EssentialAction;
import play.api.mvc.Filter$class;
import play.api.mvc.RequestHeader;
import play.api.mvc.Result;
import scala.Function1;
import scala.concurrent.Future;
import uk.gov.hmrc.play.http.HeaderCarrier;
import uk.gov.hmrc.play.http.logging.filters.FrontendLoggingFilter;
import uk.gov.hmrc.play.http.logging.filters.FrontendLoggingFilter$class;
import uk.gov.hmrc.play.http.logging.filters.LoggingFilter$class;
import uk.gov.hmrc.play.java.config.ServicesConfig;

public class LoggingFilter implements FrontendLoggingFilter {
    private FastDateFormat df = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSSZZ");

    public LoggingFilter() {
        super();
    }

    @Override
    public EssentialAction apply(EssentialAction next) {
        return Filter$class.apply(this, next);
    }

    @Override
    public HeaderCarrier buildLoggedHeaders(RequestHeader request) {
        return FrontendLoggingFilter$class.buildLoggedHeaders(this, request);
    }

    @Override
    public Future<Result> apply(Function1<RequestHeader, Future<Result>> next, RequestHeader rh) {
        return LoggingFilter$class.apply(this, next, rh);
    }

    @Override
    public boolean controllerNeedsLogging(String controllerName) {
        return ServicesConfig.getConfBool(String.format("controllers.%s.needsLogging", controllerName), true);
    }

    @Override
    public LoggerLike logger() {
        return LoggingFilter$class.logger(this);
    }

    // Scala compatibility required methods
    public FastDateFormat uk$gov$hmrc$play$http$logging$filters$LoggingFilter$$dateFormat() {
        return df;
    }

    // Scala compatibility required methods
    public void uk$gov$hmrc$play$http$logging$filters$LoggingFilter$_setter_$uk$gov$hmrc$play$http$logging$filters$LoggingFilter$$dateFormat_$eq(FastDateFormat format) {
        df = format;
    }
};
