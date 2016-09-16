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

package uk.gov.hmrc.play.java.config;

import play.Configuration;
import uk.gov.hmrc.play.audit.http.config.LoadAuditingConfig;
import uk.gov.hmrc.play.config.RunMode$;
import uk.gov.hmrc.play.http.HttpGet;
import uk.gov.hmrc.play.http.ws.WSHttp;
import uk.gov.hmrc.play.java.connectors.AuthConnector;
import uk.gov.hmrc.play.java.connectors.AuditConnector;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ServicesConfig {
    private static final String ROOT_SERVICES = "microservice.services";
    private static final String ENV_SERVICES = String.format("%s.%s", env(), ROOT_SERVICES);
    private static final String GOV_UK_ENV_SERVICES = String.format("govuk-tax.%s.services", env());

    private static AuditConnector auditConnector = () -> LoadAuditingConfig.apply("auditing");
    private static AuthConnector authConnector = () -> baseUrl("auth");

    public static void initConnectors(AuditConnector auditConnector, AuthConnector authConnector) {
        ServicesConfig.auditConnector = auditConnector;
        ServicesConfig.authConnector = authConnector;
    }

    public static String loadConfig(String key) throws Exception {
        return Optional.ofNullable(Configuration.root().getString(key)).orElseThrow(() -> new Exception(String.format("Missing configuration key: %s", key)));
    }

    public static String defaultProtocol() {
        return getString("protocol", "http");
    }

    public static Configuration config(String serviceName) {
        return Optional.ofNullable(getConfiguration(serviceName, null))
                .orElseThrow(() -> new IllegalArgumentException(String.format("Configuration for service %s not found", serviceName)));
    }

    public static String baseUrl(String serviceName) {
        String protocol = getString(String.format("%s.protocol", serviceName), defaultProtocol());
        String host = Optional.ofNullable(getString(String.format("%s.host", serviceName), null)).orElseThrow(() -> new RuntimeException(String.format("Could not find config %s.host", serviceName)));
        int port = Optional.ofNullable(getInteger(String.format("%s.port", serviceName), null)).orElseThrow(() -> new RuntimeException(String.format("Could not find config %s.port", serviceName)));
        return String.format("%s://%s:%d", protocol, host, port);
    }

    public static String env() {
        return RunMode$.MODULE$.env();
    }

    public static String appName() {
        return getString("appName", "APP NAME NOT SET");
    }

    public static AuditConnector auditConnector() {
        return auditConnector;
    }

    public static AuthConnector authConnector() {
        return authConnector;
    }

    public static WSHttp wsHttp() {
        return null;
    }

    public static Configuration getConfiguration(String name, Configuration defaultVal) {
        return call(Configuration::getConfig, name, defaultVal);
    }

    public static boolean getBoolean(String name, Boolean defaultVal) {
        return call(Configuration::getBoolean, name, defaultVal);
    }

    public static String getString(String name, String defaultVal)  {
        return call(Configuration::getString, name, defaultVal);
    }

    public static Integer getInteger(String name, Integer defaultVal)  {
        return call(Configuration::getInt, name, defaultVal);
    }

    public static List<String> getStringList(String name, List<String> defaultVal) {
        return call(Configuration::getStringList, name, defaultVal);
    }

    private static <T> T call(ConfigFunc<T> func, String basePath, T defaultValue) {
        Configuration conf = Configuration.root();
        return Stream.of(ENV_SERVICES, GOV_UK_ENV_SERVICES, env(), ROOT_SERVICES, null)
                .map((v) -> Optional.ofNullable(func.get(conf, v == null ? basePath : String.format("%s.%s", v, basePath))))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(defaultValue);
    }

    private interface ConfigFunc<T> {
        T get(Configuration config, String path);
    }
}
