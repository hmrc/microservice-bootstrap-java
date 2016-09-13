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
import uk.gov.hmrc.crypto.ApplicationCrypto;
import uk.gov.hmrc.play.java.config.ServicesConfig;
import uk.gov.hmrc.play.java.filters.*;
import uk.gov.hmrc.play.java.filters.frontend.CSRFExceptionsFilter;
import uk.gov.hmrc.play.java.filters.frontend.HeadersFilter;

import static uk.gov.hmrc.play.java.config.ServicesConfig.getBoolean;
import static uk.gov.hmrc.play.java.config.ServicesConfig.getString;

public abstract class DefaultFrontendGlobal extends BootstrapParent {
    private Class[] defaultFilters = new Class[]{
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

    protected abstract ShowErrorPage showErrorPage();

    public GlobalSettings errorHandler() {
        return showErrorPage().asJavaGlobalSettings();
    }

    @Override
    public void onStart(Application app) {
        Logger.info("Starting frontend : {} : in mode {}", ServicesConfig.appName(), app.getWrappedApplication().mode());
        ApplicationCrypto.verifyConfiguration();
        RoutingFilter.init(rh -> showErrorPage().onHandlerNotFound(rh), getString("routing.blocked.paths", null));
        super.onStart(app);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends EssentialFilter> Class<T>[] filters() {
        if(getBoolean("security.headers.filter.enabled", true)) {
            return ArrayUtils.addAll(new Class[] {SecurityHeadersFilter.class}, defaultFilters);
        } else {
            return defaultFilters;
        }
    }
}
