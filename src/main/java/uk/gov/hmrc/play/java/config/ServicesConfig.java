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
import uk.gov.hmrc.play.config.RunMode$;
import uk.gov.hmrc.play.java.connectors.AuditConnector;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ServicesConfig {
    private static final String rootServices = "microservice.services";
    private static final String envServices = String.format("%s.%s", env(), rootServices);
    private static final String govUkEnvServices = String.format("govuk-tax.%s.services", env());

    private static AuditConnector auditConnector;

    protected String loadConfig(String key) throws Exception {
        return Optional.ofNullable(Configuration.root().getString(key)).orElseThrow(() -> new Exception(String.format("Missing configuration key: %s", key)));
    }

    public String defaultProtocol() {
        return getConfString("protocol", "http");
    }

    public Configuration config(String serviceName) {
        return Optional.ofNullable(getConfConf(serviceName, null))
                .orElseThrow(() -> new IllegalArgumentException(String.format("Configuration for service %s not found", serviceName)));
    }

    public String baseUrl(String serviceName) {
        String protocol = getConfString(String.format("%s.protocol", serviceName), defaultProtocol());
        String host = Optional.ofNullable(getConfString(String.format("%s.host", serviceName), null)).orElseThrow(() -> new RuntimeException(String.format("Could not find config %s.host", serviceName)));
        int port = Optional.ofNullable(getConfInteger(String.format("%s.port", serviceName), null)).orElseThrow(() -> new RuntimeException(String.format("Could not find config %s.port", serviceName)));
        return String.format("%s://%s:%d", protocol, host, port);
    }

    public static String env() {
        return RunMode$.MODULE$.env();
    }

    public static String appName() {
        return getConfString("appName", "APP NAME NOT SET");
    }

    public static AuditConnector auditConnector() {
        if(auditConnector == null) {
            auditConnector = new AuditConnector();
        }

        return auditConnector;
    }

    public static Configuration getConfConf(String name, Configuration defaultVal) {
        return call(Configuration::getConfig, name, defaultVal);
    }

    public static boolean getConfBool(String name, Boolean defaultVal) {
        return call(Configuration::getBoolean, name, defaultVal);
    }

    public static String getConfString(String name, String defaultVal)  {
        return call(Configuration::getString, name, defaultVal);
    }

    public static Integer getConfInteger(String name, Integer defaultVal)  {
        return call(Configuration::getInt, name, defaultVal);
    }

    public static List<String> getConfStringList(String name, List<String> defaultVal) {
        return call(Configuration::getStringList, name, defaultVal);
    }

    private static <T> T call(ConfigFunc<T> func, String basePath, T defaultValue) {
        Configuration conf = Configuration.root();
        return Stream.of(envServices, govUkEnvServices, env(), rootServices, null)
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
