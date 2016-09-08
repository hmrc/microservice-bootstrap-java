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

import play.api.GlobalSettings$class;
import play.api.mvc.RequestHeader;
import play.api.mvc.Result;
import play.mvc.Http;
import scala.concurrent.Future;
import uk.gov.hmrc.play.audit.http.HttpAuditEvent$class;
import uk.gov.hmrc.play.audit.http.config.ErrorAuditingSettings;
import uk.gov.hmrc.play.audit.http.config.ErrorAuditingSettings$class;
import uk.gov.hmrc.play.audit.model.DataEvent;
import uk.gov.hmrc.play.http.HeaderCarrier;
import uk.gov.hmrc.play.java.config.ServicesConfig;

@FunctionalInterface
public interface ErrorAuditing extends ErrorAuditingSettings, JavaGlobalSettings {
    String unexpectedError = "Unexpected error";
    String notFoundError = "Resource Endpoint Not Found";
    String badRequestError = "Request bad format exception";

    default RequestHeader getCurrentRequestHeader() {
        return Http.Context.current()._requestHeader();
    }

    default void onError(Http.RequestHeader rh, Throwable ex) {
        onError(getCurrentRequestHeader(), ex);
    }

    default void onHandlerNotFound(Http.RequestHeader rh) {
        onHandlerNotFound(getCurrentRequestHeader());
    }

    default void onBadRequest(Http.RequestHeader rh, String error) {
        onBadRequest(getCurrentRequestHeader(), error);
    }

    @Override
    default Future<Result> onError(RequestHeader request, Throwable ex) {
        return ErrorAuditingSettings$class.onError(this, request, ex);
    }

    @Override
    default Future<Result> onHandlerNotFound(RequestHeader request) {
        return ErrorAuditingSettings$class.onHandlerNotFound(this, request);
    }

    @Override
    default Future<Result> onBadRequest(RequestHeader request, String error) {
        return ErrorAuditingSettings$class.onBadRequest(this, request, error);
    }

    @Override
    default String appName() {
        return ServicesConfig.appName();
    }

    default HeaderCarrier getDefaultHeaderCarrier(String eventType, String transactionName, RequestHeader request) {
        return dataEvent$default$4(eventType, transactionName, request);
    }

    @Override
    default HeaderCarrier dataEvent$default$4(String eventType, String transactionName, RequestHeader request) {
        return HttpAuditEvent$class.dataEvent$default$4(this, eventType, transactionName, request);
    }

    @Override
    default DataEvent dataEvent(String eventType, String transactionName, RequestHeader request, HeaderCarrier hc) {
        return HttpAuditEvent$class.dataEvent(this, eventType, transactionName, request, hc);
    }

    uk.gov.hmrc.play.audit.http.connector.AuditConnector auditConnector();

    // Scala compatibility required methods
    default auditDetailKeys$ auditDetailKeys() {
        return new auditDetailKeys$();
    }

    // Scala compatibility required methods
    default headers$ headers() {
        return new headers$();
    }

    // Scala compatibility required methods
    default String uk$gov$hmrc$play$audit$http$config$ErrorAuditingSettings$$unexpectedError() {
        return unexpectedError;
    }

    // Scala compatibility required methods
    default void uk$gov$hmrc$play$audit$http$config$ErrorAuditingSettings$_setter_$uk$gov$hmrc$play$audit$http$config$ErrorAuditingSettings$$unexpectedError_$eq(String unexpectedError) {
        //this.unexpectedError = unexpectedError;
    }

    // Scala compatibility required methods
    default String uk$gov$hmrc$play$audit$http$config$ErrorAuditingSettings$$notFoundError() {
        return notFoundError;
    }

    // Scala compatibility required methods
    default void uk$gov$hmrc$play$audit$http$config$ErrorAuditingSettings$_setter_$uk$gov$hmrc$play$audit$http$config$ErrorAuditingSettings$$notFoundError_$eq(String notFoundError) {
        //this.notFoundError = notFoundError;
    }

    // Scala compatibility required methods
    default String uk$gov$hmrc$play$audit$http$config$ErrorAuditingSettings$$badRequestError() {
        return badRequestError;
    }

    // Scala compatibility required methods
    default void uk$gov$hmrc$play$audit$http$config$ErrorAuditingSettings$_setter_$uk$gov$hmrc$play$audit$http$config$ErrorAuditingSettings$$badRequestError_$eq(String badRequestError) {
        //this.badRequestError = badRequestError;
    }

    // Scala compatibility required methods
    default Future<Result> uk$gov$hmrc$play$audit$http$config$ErrorAuditingSettings$$super$onHandlerNotFound(RequestHeader rh) {
        return GlobalSettings$class.onHandlerNotFound(this, rh);
    }

    // Scala compatibility required methods
    default Future<Result> uk$gov$hmrc$play$audit$http$config$ErrorAuditingSettings$$super$onBadRequest(RequestHeader rh, String err) {
        return GlobalSettings$class.onBadRequest(this, rh, err);
    }

    // Scala compatibility required methods
    default Future<Result> uk$gov$hmrc$play$audit$http$config$ErrorAuditingSettings$$super$onError(RequestHeader rh, Throwable ex) {
        return GlobalSettings$class.onError(this, rh, ex);
    }
}
