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

package uk.gov.hmrc.play.java.connectors;

import play.api.Logger$;
import play.api.LoggerLike$class;
import play.api.libs.json.JsValue;
import scala.Option;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import uk.gov.hmrc.play.audit.http.config.AuditingConfig;
import uk.gov.hmrc.play.audit.http.config.LoadAuditingConfig$;
import uk.gov.hmrc.play.audit.http.connector.*;
import uk.gov.hmrc.play.audit.model.AuditEvent;
import uk.gov.hmrc.play.audit.model.MergedDataEvent;
import uk.gov.hmrc.play.http.HeaderCarrier;
import uk.gov.hmrc.play.http.HttpResponse;
import uk.gov.hmrc.play.http.logging.LoggingDetails;
import uk.gov.hmrc.play.java.config.ServicesConfig;

@FunctionalInterface
public interface AuditConnector extends Connector, uk.gov.hmrc.play.audit.http.connector.AuditConnector {
    AuditingConfig auditingConfig();

    @Override
    default Logger$ logger() {
        return Logger$.MODULE$;
    }

    @Override
    default void logError(String s) {
        LoggerProvider$class.logError(this, s);
    }

    @Override
    default void logError(String s, Throwable t) {
        LoggerProvider$class.logError(this, s, t);
    }

    @Override
    default Future<HttpResponse> callAuditConsumer(String url, JsValue body, HeaderCarrier hc, ExecutionContext ec) {
        return AuditorImpl$class.callAuditConsumer(this, url, body, hc, ec);
    }

    @Override
    default HeaderCarrier sendEvent$default$2(AuditEvent event) {
        return AuditorImpl$class.sendEvent$default$2(this, event);
    }

    @Override
    default Future<AuditResult> sendEvent(AuditEvent event, HeaderCarrier hc, ExecutionContext ec) {
        return AuditorImpl$class.sendEvent(this, event, hc, ec);
    }

    @Override
    default Future<AuditResult> sendMergedEvent(MergedDataEvent event, HeaderCarrier hc, ExecutionContext ec) {
        return AuditorImpl$class.sendMergedEvent(this, event, hc, ec);
    }

    @Override
    default HeaderCarrier sendLargeMergedEvent$default$2(MergedDataEvent event) {
        return AuditorImpl$class.sendLargeMergedEvent$default$2(this, event);
    }

    @Override
    default Future<AuditResult> sendLargeMergedEvent(MergedDataEvent event, HeaderCarrier hc) {
        return AuditorImpl$class.sendLargeMergedEvent(this, event, hc);
    }

    @Override
    default HeaderCarrier sendMergedEvent$default$2(MergedDataEvent event) {
        return AuditorImpl$class.sendMergedEvent$default$2(this, event);
    }

    @Override
    default Future<HttpResponse> handleResult(Future<HttpResponse> resultF, JsValue body, LoggingDetails ld) {
        return ResultHandler$class.handleResult(this, resultF, body, ld);
    }

    @Override
    default Option<String> checkResponse(JsValue body, HttpResponse response) {
        return ResponseFormatter$class.checkResponse(this, body, response);
    }

    @Override
    default String makeFailureMessage(JsValue body) {
        return ResponseFormatter$class.makeFailureMessage(this, body);
    }

    default void uk$gov$hmrc$play$audit$http$connector$AuditConnector$_setter_$logger_$eq(Logger$ logger) {
        // No-op
    }
}
