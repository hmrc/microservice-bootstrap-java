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

package uk.gov.hmrc.play.java.microservice.bootstrap;

import com.kenshoo.play.metrics.JavaMetricsFilter;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.api.libs.iteratee.Iteratee;
import play.api.mvc.EssentialAction;
import play.api.mvc.Filter$class;
import play.api.mvc.RequestHeader;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import scala.Function1;
import scala.Function2;
import scala.Option;
import scala.concurrent.Future;
import scala.concurrent.Promise;
import scala.runtime.BoxedUnit;
import scala.util.Try;
import uk.gov.hmrc.play.audit.filters.AuditFilter;
import uk.gov.hmrc.play.audit.filters.AuditFilter$class;
import uk.gov.hmrc.play.audit.http.HttpAuditEvent$class;
import uk.gov.hmrc.play.audit.model.DataEvent;
import uk.gov.hmrc.play.auth.controllers.AuthConfig;
import uk.gov.hmrc.play.auth.controllers.AuthParamsControllerConfig;
import uk.gov.hmrc.play.auth.microservice.connectors.AuthConnector;
import uk.gov.hmrc.play.auth.microservice.connectors.ResourceToAuthorise;
import uk.gov.hmrc.play.auth.microservice.filters.AuthorisationFilter;
import uk.gov.hmrc.play.auth.microservice.filters.AuthorisationFilter$class;
import uk.gov.hmrc.play.http.HeaderCarrier;
import uk.gov.hmrc.play.http.logging.filters.LoggingFilter;
import uk.gov.hmrc.play.java.config.GraphiteConfig;
import uk.gov.hmrc.play.java.config.ServicesConfig;
import uk.gov.hmrc.play.java.filters.NoCacheFilter;
import uk.gov.hmrc.play.java.filters.RecoveryFilter;
import uk.gov.hmrc.play.java.filters.RoutingFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmrc.play.java.config.ServicesConfig.getConfBool;
import static uk.gov.hmrc.play.java.config.ServicesConfig.getConfString;

public abstract class DefaultMicroserviceGlobal extends GlobalSettings {
    protected abstract MicroserviceAuthFilter microserviceAuthFilter();
    protected abstract MicroserviceAuditFilter microserviceAuditFilter();
    protected abstract ErrorAuditing errorAuditing();

    private Class[] defaultMicroserviceFilters = new Class[]{
            JavaMetricsFilter.class,
            microserviceAuditFilter().getClass(),
            LoggingFilter.class,
            // Optional AuthFilter
            NoCacheFilter.class,
            RoutingFilter.class,
            RecoveryFilter.class
    };

    private JsonErrorHandling jsonErrorHandling = new JsonErrorHandling();
    private GraphiteConfig graphiteConfig = null;

    private String appName() {
        return ServicesConfig.appName();
    }

    @Override
    public void onStart(Application app) {
        Logger.info("Starting microservice : {} : in mode {}", appName(), app.getWrappedApplication().mode());
        graphiteConfig = new GraphiteConfig("microservice.metrics");
        graphiteConfig.onStart(app.getWrappedApplication());
        RoutingFilter.init(rh -> jsonErrorHandling.onHandlerNotFound(rh), getConfString("routing.blocked.paths", null));
        super.onStart(app);
    }

    @Override
    public void onStop(Application app) {
        super.onStop(app);
        if (graphiteConfig != null) {
            graphiteConfig.onStop(app.getWrappedApplication());
        }
    }

    @Override
    public <T extends play.api.mvc.EssentialFilter> Class<T>[] filters() {
        List<Class> filters = new ArrayList<>();
        Collections.addAll(filters, defaultMicroserviceFilters);

        if(Optional.ofNullable(microserviceAuthFilter()).isPresent()) {
            filters.add(1, microserviceAuthFilter().getClass());
        }

        return filters.toArray(defaultMicroserviceFilters);
    }

    @Override
    public F.Promise<Result> onBadRequest(Http.RequestHeader rh, String error) {
        errorAuditing().onBadRequest(rh, error);
        return jsonErrorHandling.onBadRequest(rh, error);
    }

    @Override
    public F.Promise<Result> onHandlerNotFound(Http.RequestHeader rh) {
        errorAuditing().onHandlerNotFound(rh);
        return jsonErrorHandling.onHandlerNotFound(rh);
    }

    @Override
    public F.Promise<Result> onError(Http.RequestHeader rh, Throwable t) {
        errorAuditing().onError(rh, t);
        return jsonErrorHandling.onError(rh, t);
    }

    interface MicroserviceAuditFilter extends AuditFilter {
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
        default Iteratee<byte[], play.api.mvc.Result> captureRequestBody(Iteratee<byte[], play.api.mvc.Result> next, Promise<byte[]> onDone) {
            return AuditFilter$class.captureRequestBody(this, next, onDone);
        }

        @Override
        default Iteratee<byte[], play.api.mvc.Result> captureResult(Iteratee<byte[], play.api.mvc.Result> next, Future<byte[]> requestBody, Function2<byte[], Try<play.api.mvc.Result>, BoxedUnit> handler) {
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

    interface MicroserviceAuthFilter extends AuthorisationFilter {
        AuthConnector authConnector();
        AuthParamsControllerConfig authParamsConfig();

        @Override
        default boolean controllerNeedsAuth(String controllerName) {
            return getConfBool(String.format("controllers.%s.needsAuth", controllerName), true);
        }

        @Override
        default EssentialAction apply(EssentialAction next) {
            return Filter$class.apply(this, next);
        }

        @Override
        default Option<AuthConfig> authConfig(RequestHeader rh) {
            return AuthorisationFilter$class.authConfig(this, rh);
        }

        @Override
        default Future<play.api.mvc.Result> apply(Function1<RequestHeader, Future<play.api.mvc.Result>> next, RequestHeader rh) {
            return AuthorisationFilter$class.apply(this, next, rh);
        }

        @Override
        default Option<ResourceToAuthorise> extractResource(String pathString, String verb, AuthConfig authConfig) {
            return AuthorisationFilter$class.extractResource(this, pathString, verb, authConfig);
        }
    }
}
