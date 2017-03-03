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

import play.api.libs.iteratee.Iteratee;
import play.api.mvc.EssentialAction;
import play.api.mvc.RequestHeader;
import play.api.mvc.Result;
import scala.Function2;
import scala.None$;
import scala.Option;
import scala.Some;
import scala.collection.JavaConversions;
import scala.collection.Seq;
import scala.collection.immutable.Map;
import scala.concurrent.Future;
import scala.concurrent.Promise;
import scala.runtime.BoxedUnit;
import scala.util.Try;
import scala.util.matching.Regex;
import uk.gov.hmrc.play.audit.filters.AuditFilter$class;
import uk.gov.hmrc.play.audit.filters.FrontendAuditFilter$class;
import uk.gov.hmrc.play.audit.http.HttpAuditEvent$class;
import uk.gov.hmrc.play.audit.http.connector.AuditConnector;
import uk.gov.hmrc.play.audit.model.DataEvent;
import uk.gov.hmrc.play.http.HeaderCarrier;
import uk.gov.hmrc.play.java.config.ServicesConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static uk.gov.hmrc.play.java.config.ServicesConfig.*;

public class FrontendAuditFilter implements uk.gov.hmrc.play.audit.filters.FrontendAuditFilter {
    private Regex textHtml = new Regex(Pattern.compile(".*(text/html).*"), JavaConversions.asScalaBuffer(new ArrayList<>()));

    private List<String> maskedFormFields;
    private Integer applicationPort = null;
    private AuditConnector auditConnector = null;
    private int maxBodySize = 32665;

    public FrontendAuditFilter() {
        maskedFormFields = getStringList("filter.audit.maskedFormFields", Collections.singletonList("password"));
        applicationPort = getInteger("filter.audit.applicationPort", null);
        auditConnector = ServicesConfig.auditConnector();
    }

    @Override
    public Future<String> getBody(Result result) {
        return AuditFilter$class.getBody(this, result);
    }

    @Override
    public String stripPasswords(Option<String> contentType, String requestBody, Seq<String> maskedFormFields) {
        return FrontendAuditFilter$class.stripPasswords(this, contentType, requestBody, maskedFormFields);
    }

    @Override
    public DataEvent dataEvent(String eventType, String transactionName, RequestHeader request, HeaderCarrier hc) {
        return HttpAuditEvent$class.dataEvent(this, eventType, transactionName, request, hc);
    }

    @Override
    public HeaderCarrier dataEvent$default$4(String eventType, String transactionName, RequestHeader request) {
        return HttpAuditEvent$class.dataEvent$default$4(this, eventType, transactionName, request);
    }

    @Override
    public Iteratee<byte[], Result> captureRequestBody(Iteratee<byte[], Result> next, Promise<byte[]> onDone) {
        return AuditFilter$class.captureRequestBody(this, next, onDone);
    }

    @Override
    public String getQueryString(Map<String, Seq<String>> queryString) {
        return FrontendAuditFilter$class.getQueryString(this, queryString);
    }

    @Override
    public String getHost(RequestHeader request) {
        return FrontendAuditFilter$class.getHost(this, request);
    }

    @Override
    public boolean needsAuditing(RequestHeader request) {
        return AuditFilter$class.needsAuditing(this, request);
    }

    @Override
    public HeaderCarrier buildAuditedHeaders(RequestHeader request) {
        return FrontendAuditFilter$class.buildAuditedHeaders(this, request);
    }

    @Override
    public EssentialAction apply(EssentialAction nextFilter) {
        return (EssentialAction) FrontendAuditFilter$class.apply(this, nextFilter);
    }

    @Override
    public String getPort() {
        return FrontendAuditFilter$class.getPort(this);
    }

    @Override
    public Seq<String> maskedFormFields() {
        return JavaConversions.asScalaBuffer(maskedFormFields);
    }

    @Override
    public Iteratee<byte[], Result> captureResult(Iteratee<byte[], Result> next, Future<byte[]> requestBody, Function2<byte[], Try<Result>, BoxedUnit> handler) {
        return AuditFilter$class.captureResult(this, next, requestBody, handler);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Option<Object> applicationPort() {
        return applicationPort != null ? new Some(applicationPort) : None$.empty();
    }

    @Override
    public AuditConnector auditConnector() {
        return auditConnector;
    }

    @Override
    public boolean controllerNeedsAuditing(String controllerName) {
        return getBoolean(String.format("controllers.%s.needsAuditing", controllerName), true);
    }

    @Override
    public String appName() {
        return ServicesConfig.appName();
    }

    @Override
    public int maxBodySize() {
        return maxBodySize;
    }

    // Scala compatibility required methods
    public auditDetailKeys$ auditDetailKeys() {
        return new auditDetailKeys$();
    }

    // Scala compatibility required methods
    public headers$ headers() {
        return new headers$();
    }

    // Scala compatibility required methods
    public Regex uk$gov$hmrc$play$audit$filters$FrontendAuditFilter$$textHtml() {
        return textHtml;
    }

    // Scala compatibility required methods
    public void uk$gov$hmrc$play$audit$filters$FrontendAuditFilter$_setter_$uk$gov$hmrc$play$audit$filters$FrontendAuditFilter$$textHtml_$eq(Regex textHtml) {
        this.textHtml = textHtml;
    }
    // Scala compatibility required methods
    public void uk$gov$hmrc$play$audit$filters$AuditFilter$_setter_$maxBodySize_$eq(int maxBodySize) {
        this.maxBodySize = maxBodySize;
    }
}
