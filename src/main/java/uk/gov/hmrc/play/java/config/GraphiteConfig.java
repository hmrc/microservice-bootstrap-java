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

package uk.gov.hmrc.play.java.config;

import play.api.Application;
import play.api.Configuration;
import scala.Option;
import uk.gov.hmrc.play.graphite.GraphiteConfig$class;
import uk.gov.hmrc.play.java.bootstrap.JavaGlobalSettings;

public class GraphiteConfig implements uk.gov.hmrc.play.graphite.GraphiteConfig, JavaGlobalSettings {
    private final String confBase;

    public GraphiteConfig(String confBase) {
        this.confBase = confBase;
    }

    @Override
    public void onStart(Application app) {
        GraphiteConfig$class.onStart(this, app);
    }

    @Override
    public void onStop(Application app) {
        GraphiteConfig$class.onStop(this, app);
    }

    @Override
    public Option<Configuration> microserviceMetricsConfig(Application app) {
        return app.configuration().getConfig(confBase);
    }

    // Because trait implementation calls super.onStart()
    public void uk$gov$hmrc$play$graphite$GraphiteConfig$$super$onStart(Application app) {
        JavaGlobalSettings.super.onStart(app);
    }

    // Because trait implementation calls super.onStop()
    public void uk$gov$hmrc$play$graphite$GraphiteConfig$$super$onStop(Application app) {
        JavaGlobalSettings.super.onStop(app);
    }
}
