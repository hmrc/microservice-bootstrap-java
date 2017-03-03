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

import com.typesafe.config.Config;
import net.ceedubs.ficus.readers.StringReader$;
import net.ceedubs.ficus.readers.ValueReader;
import play.Configuration;
import play.api.mvc.EssentialAction;
import play.api.mvc.Filter$class;
import play.api.mvc.RequestHeader;
import play.api.mvc.Result;
import scala.Function1;
import scala.Option;
import scala.compat.java8.JFunction1;
import scala.concurrent.Future;
import scala.util.matching.Regex;
import uk.gov.hmrc.play.auth.controllers.AuthConfig;
import uk.gov.hmrc.play.auth.controllers.AuthParamsControllerConfig;
import uk.gov.hmrc.play.auth.controllers.AuthParamsControllerConfig$class;
import uk.gov.hmrc.play.auth.microservice.connectors.AuthConnector;
import uk.gov.hmrc.play.auth.microservice.connectors.ConfidenceLevel;
import uk.gov.hmrc.play.auth.microservice.connectors.ResourceToAuthorise;
import uk.gov.hmrc.play.auth.microservice.filters.AuthorisationFilter;
import uk.gov.hmrc.play.auth.microservice.filters.AuthorisationFilter$class;
import uk.gov.hmrc.play.java.config.ServicesConfig;

import static net.ceedubs.ficus.readers.AnyValReaders$.MODULE$;
import static uk.gov.hmrc.play.java.config.ServicesConfig.getBoolean;
import static uk.gov.hmrc.play.java.config.ServicesConfig.getConfiguration;

public class MicroserviceAuthFilter implements AuthorisationFilter {
    public AuthConnector authConnector() {
        return ServicesConfig.authConnector();
    }

    @Override
    public AuthParamsControllerConfig authParamsConfig() {
        return new AuthParamsControllerConfig() {
            private ValueReader<ConfidenceLevel> confidenceLevelValueReader = MODULE$.intValueReader().map((JFunction1<Object, ConfidenceLevel>) v1 -> ConfidenceLevel.fromInt((Integer)v1));;
            private ValueReader<Regex> regexValueReader = StringReader$.MODULE$.stringValueReader().map((JFunction1<String, Regex>) v1 -> new Regex(v1, null));

            @Override
            public AuthConfig authConfig(String controllerName) {
                return AuthParamsControllerConfig$class.authConfig(this, controllerName);
            }

            @Override
            public Config controllerConfigs() {
                Configuration conf = getConfiguration("controllers", null);

                if(conf != null) {
                    return conf.underlying();
                } else {
                    return null;
                }
            }

            public Option<ConfidenceLevel> uk$gov$hmrc$play$auth$controllers$AuthParamsControllerConfig$$globalConfidenceLevel() {
                return Option.apply(confidenceLevelValueReader.read(controllerConfigs(), "confidenceLevel"));
            }

            public ValueReader<ConfidenceLevel> uk$gov$hmrc$play$auth$controllers$AuthParamsControllerConfig$$ConfidenceLevelValueReader() {
                return confidenceLevelValueReader;
            }

            public ValueReader<Regex> uk$gov$hmrc$play$auth$controllers$AuthParamsControllerConfig$$RegexValueReader() {
                return regexValueReader;
            }

            // Scala compatibility
            public void uk$gov$hmrc$play$auth$controllers$AuthParamsControllerConfig$_setter_$uk$gov$hmrc$play$auth$controllers$AuthParamsControllerConfig$$ConfidenceLevelValueReader_$eq(ValueReader r) {
                this.confidenceLevelValueReader = r;
            }

            // Scala compatibility
            public void uk$gov$hmrc$play$auth$controllers$AuthParamsControllerConfig$_setter_$uk$gov$hmrc$play$auth$controllers$AuthParamsControllerConfig$$RegexValueReader_$eq(ValueReader r) {
                this.regexValueReader = r;
            }
        };
    }

    @Override
    public boolean controllerNeedsAuth(String controllerName) {
        return getBoolean(String.format("controllers.%s.needsAuth", controllerName), true);
    }

    @Override
    public EssentialAction apply(EssentialAction next) {
        return Filter$class.apply(this, next);
    }

    @Override
    public Option<AuthConfig> authConfig(RequestHeader rh) {
        return AuthorisationFilter$class.authConfig(this, rh);
    }

    @Override
    public Future<Result> apply(Function1<RequestHeader, Future<Result>> next, RequestHeader rh) {
        return AuthorisationFilter$class.apply(this, next, rh);
    }

    @Override
    public Option<ResourceToAuthorise> extractResource(String pathString, String verb, AuthConfig authConfig) {
        return AuthorisationFilter$class.extractResource(this, pathString, verb, authConfig);
    }
}
