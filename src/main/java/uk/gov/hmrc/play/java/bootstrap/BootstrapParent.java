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

import play.Application;
import play.GlobalSettings;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import uk.gov.hmrc.play.java.config.GraphiteConfig;

public abstract class BootstrapParent extends GlobalSettings {
    protected GraphiteConfig graphiteConfig = null;

    private ErrorAuditing errorAuditing = new ErrorAuditing();

    public ErrorAuditing errorAuditing() {
        return errorAuditing;
    }

    public abstract GlobalSettings errorHandler();

    @Override
    public void onStart(Application app) {
        graphiteConfig = new GraphiteConfig("microservice.metrics");
        graphiteConfig.onStart(app);
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
    public F.Promise<Result> onBadRequest(Http.RequestHeader rh, String error) {
        errorAuditing().onBadRequest(rh, error);
        return errorHandler().onBadRequest(rh, error);
    }

    @Override
    public F.Promise<Result> onHandlerNotFound(Http.RequestHeader rh) {
        errorAuditing().onHandlerNotFound(rh);
        return errorHandler().onHandlerNotFound(rh);
    }

    @Override
    public F.Promise<Result> onError(Http.RequestHeader rh, Throwable t) {
        errorAuditing().onError(rh, t);
        return errorHandler().onError(rh, t);
    }
}
