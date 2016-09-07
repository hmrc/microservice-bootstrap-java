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

import play.api.Logger;
import play.api.Logger$;
import play.api.libs.json.JsValue;
import play.api.libs.ws.WSRequestHolder;
import scala.Function0;
import scala.Option;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.util.Try;
import uk.gov.hmrc.play.audit.http.config.AuditingConfig;
import uk.gov.hmrc.play.audit.http.config.LoadAuditingConfig$;
import uk.gov.hmrc.play.audit.http.connector.*;
import uk.gov.hmrc.play.audit.model.AuditEvent;
import uk.gov.hmrc.play.audit.model.MergedDataEvent;
import uk.gov.hmrc.play.connectors.PlayWSRequestBuilder$class;
import uk.gov.hmrc.play.http.HeaderCarrier;
import uk.gov.hmrc.play.http.HttpResponse;
import uk.gov.hmrc.play.http.logging.ConnectionTracing$class;
import uk.gov.hmrc.play.http.logging.LoggingDetails;
import uk.gov.hmrc.play.java.config.ServicesConfig;

public class AuditConnector implements uk.gov.hmrc.play.audit.http.connector.AuditConnector {
    private AuditingConfig auditingConfig;
    private Logger$ logger;
    private Logger connectionLogger;

    public AuditConnector() {
        auditingConfig = LoadAuditingConfig$.MODULE$.apply(String.format("%s.auditing", ServicesConfig.env()));
        connectionLogger = Logger.apply("connector");
        logger = Logger$.MODULE$;
    }

    @Override
    public Logger connectionLogger() {
        return connectionLogger;
    }

    @Override
    public Logger$ logger() {
        return logger;
    }

    @Override
    public AuditingConfig auditingConfig() {
        return auditingConfig;
    }

    @Override
    public void logError(String s) {
        LoggerProvider$class.logError(this, s);
    }

    @Override
    public void logError(String s, Throwable t) {
        LoggerProvider$class.logError(this, s, t);
    }

    @Override
    public Future<HttpResponse> callAuditConsumer(String url, JsValue body, HeaderCarrier hc, ExecutionContext ec) {
        return AuditorImpl$class.callAuditConsumer(this, url, body, hc, ec);
    }

    @Override
    public HeaderCarrier sendEvent$default$2(AuditEvent event) {
        return AuditorImpl$class.sendEvent$default$2(this, event);
    }

    @Override
    public Future<AuditResult> sendEvent(AuditEvent event, HeaderCarrier hc, ExecutionContext ec) {
        return AuditorImpl$class.sendEvent(this, event, hc, ec);
    }

    @Override
    public Future<AuditResult> sendMergedEvent(MergedDataEvent event, HeaderCarrier hc, ExecutionContext ec) {
        return AuditorImpl$class.sendMergedEvent(this, event, hc, ec);
    }

    @Override
    public HeaderCarrier sendLargeMergedEvent$default$2(MergedDataEvent event) {
        return AuditorImpl$class.sendLargeMergedEvent$default$2(this, event);
    }

    @Override
    public Future<AuditResult> sendLargeMergedEvent(MergedDataEvent event, HeaderCarrier hc) {
        return AuditorImpl$class.sendLargeMergedEvent(this, event, hc);
    }

    @Override
    public HeaderCarrier sendMergedEvent$default$2(MergedDataEvent event) {
        return AuditorImpl$class.sendMergedEvent$default$2(this, event);
    }

    @Override
    public <T> Future<T> withTracing(String method, String uri, Function0<Future<T>> body, LoggingDetails ld) {
        return ConnectionTracing$class.withTracing(this, method, uri, body, ld);
    }

    @Override
    public <A> void logResult(LoggingDetails ld, String method, String uri, long startAge, Try<A> result) {
        ConnectionTracing$class.logResult(this, ld, method, uri, startAge, result);
    }

    @Override
    public String formatMessage(LoggingDetails ld, String method, String uri, long startAge, String message) {
        return ConnectionTracing$class.formatMessage(this, ld, method, uri, startAge, message);
    }

    @Override
    public WSRequestHolder buildRequest(String url, HeaderCarrier hc) {
        return PlayWSRequestBuilder$class.buildRequest(this, url, hc);
    }

    @Override
    public Future<HttpResponse> handleResult(Future<HttpResponse> resultF, JsValue body, LoggingDetails ld) {
        return ResultHandler$class.handleResult(this, resultF, body, ld);
    }

    @Override
    public Option<String> checkResponse(JsValue body, HttpResponse response) {
        return ResponseFormatter$class.checkResponse(this, body, response);
    }

    @Override
    public String makeFailureMessage(JsValue body) {
        return ResponseFormatter$class.makeFailureMessage(this, body);
    }

    public void uk$gov$hmrc$play$audit$http$connector$AuditConnector$_setter_$logger_$eq(Logger$ logger) {
        this.logger = logger;
    }
}
