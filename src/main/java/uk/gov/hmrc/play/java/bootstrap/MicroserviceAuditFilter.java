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

import play.api.libs.iteratee.Iteratee;
import play.api.mvc.EssentialAction;
import play.api.mvc.RequestHeader;
import play.api.mvc.Result;
import scala.Function2;
import scala.concurrent.Future;
import scala.concurrent.Promise;
import scala.runtime.BoxedUnit;
import scala.util.Try;
import uk.gov.hmrc.play.audit.filters.AuditFilter;
import uk.gov.hmrc.play.audit.filters.AuditFilter$class;
import uk.gov.hmrc.play.audit.http.HttpAuditEvent$class;
import uk.gov.hmrc.play.audit.model.DataEvent;
import uk.gov.hmrc.play.http.HeaderCarrier;
import uk.gov.hmrc.play.java.config.ServicesConfig;

import static uk.gov.hmrc.play.java.config.ServicesConfig.getConfBool;

@FunctionalInterface
public interface MicroserviceAuditFilter extends AuditFilter {
    int maxBodySize = 32665;

    uk.gov.hmrc.play.audit.http.connector.AuditConnector auditConnector();

    @Override
    default String appName() {
        return ServicesConfig.appName();
    }

    @Override
    default HeaderCarrier dataEvent$default$4(String eventType, String transactionName, RequestHeader request) {
        return HttpAuditEvent$class.dataEvent$default$4(this, eventType, transactionName, request);
    }

    @Override
    default DataEvent dataEvent(String eventType, String transactionName, RequestHeader request, HeaderCarrier hc) {
        return HttpAuditEvent$class.dataEvent(this, eventType, transactionName, request, hc);
    }

    @Override
    default boolean controllerNeedsAuditing(String controllerName) {
        return getConfBool(String.format("controllers.%s.needsAuditing", controllerName), true);
    }

    @Override
    default boolean needsAuditing(RequestHeader request) {
        return AuditFilter$class.needsAuditing(this, request);
    }

    @Override
    default Future<String> getBody(play.api.mvc.Result result) {
        return AuditFilter$class.getBody(this, result);
    }

    @Override
    default Iteratee<byte[], Result> captureRequestBody(Iteratee<byte[], play.api.mvc.Result> next, Promise<byte[]> onDone) {
        return AuditFilter$class.captureRequestBody(this, next, onDone);
    }

    @Override
    default Iteratee<byte[], play.api.mvc.Result> captureResult(Iteratee<byte[], play.api.mvc.Result> next, Future<byte[]> requestBody, Function2<byte[], Try<Result>, BoxedUnit> handler) {
        return AuditFilter$class.captureResult(this, next, requestBody, handler);
    }

    @Override
    default EssentialAction apply(EssentialAction nextFilter) {
        return (EssentialAction) AuditFilter$class.apply(this, nextFilter);
    }

    @Override
    default int maxBodySize() {
        return maxBodySize;
    }

    // Scala compatibility required methods
    default void uk$gov$hmrc$play$audit$filters$AuditFilter$_setter_$maxBodySize_$eq(int maxBodySize) {
        // No-op
    }

    // Scala compatibility required methods
    default auditDetailKeys$ auditDetailKeys() {
        return new auditDetailKeys$();
    }

    // Scala compatibility required methods
    default headers$ headers() {
        return new headers$();
    }
}
