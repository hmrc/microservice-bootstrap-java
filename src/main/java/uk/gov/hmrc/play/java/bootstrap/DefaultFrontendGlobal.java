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

import com.kenshoo.play.metrics.JavaMetricsFilter;
import org.apache.commons.lang3.ArrayUtils;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.api.mvc.EssentialFilter;
import play.filters.csrf.CSRFFilter;
import play.filters.headers.SecurityHeadersFilter;
import play.libs.F;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;
import uk.gov.hmrc.crypto.ApplicationCrypto;
import uk.gov.hmrc.play.audit.filters.FrontendAuditFilter;
import uk.gov.hmrc.play.frontend.filters.DeviceIdCookieFilter;
import uk.gov.hmrc.play.frontend.filters.SessionCookieCryptoFilter;
import uk.gov.hmrc.play.java.config.GraphiteConfig;
import uk.gov.hmrc.play.java.config.ServicesConfig;
import uk.gov.hmrc.play.java.filters.CacheControlFilter;
import uk.gov.hmrc.play.java.filters.LoggingFilter;
import uk.gov.hmrc.play.java.filters.RecoveryFilter;
import uk.gov.hmrc.play.java.filters.RoutingFilter;
import uk.gov.hmrc.play.java.filters.frontend.CSRFExceptionsFilter;
import uk.gov.hmrc.play.java.filters.frontend.HeadersFilter;

import static uk.gov.hmrc.play.java.config.ServicesConfig.getConfBool;

public abstract class DefaultFrontendGlobal extends GlobalSettings {
    private Class[] frontendFilters = new Class[]{
            JavaMetricsFilter.class,
            HeadersFilter.class,
            SessionCookieCryptoFilter.class,
            DeviceIdCookieFilter.class,
            LoggingFilter.class,
            FrontendAuditFilter.class,
            CSRFExceptionsFilter.class,
            CSRFFilter.class,
            CacheControlFilter.class,
            RoutingFilter.class,
            RecoveryFilter.class
    };

    private GraphiteConfig graphiteConfig = null;

    private final Class[] securityFilters = new Class[]{SecurityHeadersFilter.class};

    private boolean enableSecurityHeaderFilter() {
        return getConfBool("security.headers.filter.enabled", true);
    }

    protected abstract ShowErrorPage showErrorPage();
    protected abstract ErrorAuditing errorAuditing();

    @Override
    public void onStart(Application app) {
        Logger.info("Starting frontend : {} : in mode {}", ServicesConfig.appName(), app.getWrappedApplication().mode());
        ApplicationCrypto.verifyConfiguration();
        graphiteConfig = new GraphiteConfig("microservice.metrics");
        graphiteConfig.onStart(app);
        RoutingFilter.init(rh -> showErrorPage().onHandlerNotFound(rh), ServicesConfig.getConfString("routing.blocked.paths", null));
        super.onStart(app);
    }

    @Override
    public void onStop(Application app) {
        super.onStop(app);
        if(graphiteConfig != null) {
            graphiteConfig.onStop(app);
        }
    }

    @Override
    public F.Promise<Result> onBadRequest(RequestHeader rh, String error) {
        errorAuditing().onBadRequest(rh, error);
        return showErrorPage().onBadRequest(rh, error);
    }

    @Override
    public F.Promise<Result> onHandlerNotFound(RequestHeader rh) {
        errorAuditing().onHandlerNotFound(rh);
        return showErrorPage().onHandlerNotFound(rh);
    }

    @Override
    public F.Promise<Result> onError(RequestHeader rh, Throwable t) {
        errorAuditing().onError(rh, t);
        return showErrorPage().onError(rh, t);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends EssentialFilter> Class<T>[] filters() {
        if (enableSecurityHeaderFilter()) {
            return ArrayUtils.addAll(securityFilters, frontendFilters);
        } else {
            return frontendFilters;
        }
    }
}
