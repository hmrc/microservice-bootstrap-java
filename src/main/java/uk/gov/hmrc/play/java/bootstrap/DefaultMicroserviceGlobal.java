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

package uk.gov.hmrc.play.java.bootstrap;

import com.kenshoo.play.metrics.JavaMetricsFilter;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.api.mvc.EssentialFilter;
import uk.gov.hmrc.play.java.config.ServicesConfig;
import uk.gov.hmrc.play.java.filters.*;

import java.util.Arrays;

import static uk.gov.hmrc.play.java.config.ServicesConfig.*;

public abstract class DefaultMicroserviceGlobal extends BootstrapParent {
    private Class[] defaultFilters = new Class[]{
            JavaMetricsFilter.class,
            MicroserviceAuthFilter.class,
            MicroserviceAuditFilter.class,
            LoggingFilter.class,
            NoCacheFilter.class,
            RoutingFilter.class,
            RecoveryFilter.class
    };

    private JsonErrorHandling jsonErrorHandling = new JsonErrorHandling();

    public GlobalSettings errorHandler() {
        return jsonErrorHandling;
    }

    @Override
    public void onStart(Application app) {
        Logger.info("Starting microservice : {} : in mode {}", ServicesConfig.appName(), app.getWrappedApplication().mode());
        RoutingFilter.init(rh -> jsonErrorHandling.onHandlerNotFound(rh), getString("routing.blocked.paths", null));
        super.onStart(app);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends EssentialFilter> Class<T>[] filters() {
        Logger.info("Authentication enabled? ({})", getBoolean("authentication.enabled", false));
        if(!getBoolean("authentication.enabled", false) || authConnector() == null) {
            return (Class[]) Arrays.stream(defaultFilters).filter(f -> !f.equals(MicroserviceAuthFilter.class)).toArray(size -> new Class[size]);
        } else {
            return defaultFilters;
        }
    }
}
