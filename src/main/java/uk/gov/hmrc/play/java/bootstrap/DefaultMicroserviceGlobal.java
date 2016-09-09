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
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import uk.gov.hmrc.play.java.config.GraphiteConfig;
import uk.gov.hmrc.play.java.config.ServicesConfig;
import uk.gov.hmrc.play.java.filters.*;

import java.util.Arrays;

import static uk.gov.hmrc.play.java.config.ServicesConfig.getConfString;

public abstract class DefaultMicroserviceGlobal extends GlobalSettings {
    private Class[] defaultMicroserviceFilters = new Class[]{
            JavaMetricsFilter.class,
            MicroserviceAuthFilter.class,
            MicroserviceAuditFilter.class,
            LoggingFilter.class,
            NoCacheFilter.class,
            RoutingFilter.class,
            RecoveryFilter.class
    };

    private JsonErrorHandling jsonErrorHandling = new JsonErrorHandling();
    private GraphiteConfig graphiteConfig = null;

    private ErrorAuditing errorAuditing = new ErrorAuditing();

    private String appName() {
        return ServicesConfig.appName();
    }

    public ErrorAuditing errorAuditing() {
        return errorAuditing;
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
        if(ServicesConfig.authConnector() == null) {
            return (Class[]) Arrays.stream(defaultMicroserviceFilters).filter(f -> !f.isAssignableFrom(MicroserviceAuthFilter.class)).toArray(size -> new Class[size]);
        } else {
            return defaultMicroserviceFilters;
        }
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
}
