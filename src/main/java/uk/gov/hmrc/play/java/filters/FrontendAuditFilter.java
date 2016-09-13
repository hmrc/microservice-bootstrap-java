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

package uk.gov.hmrc.play.java.filters;

import play.api.mvc.EssentialAction;
import play.api.mvc.RequestHeader;
import scala.None$;
import scala.Option;
import scala.Some;
import scala.collection.JavaConversions;
import scala.collection.Seq;
import scala.collection.immutable.Map;
import scala.util.matching.Regex;
import uk.gov.hmrc.play.audit.filters.FrontendAuditFilter$class;
import uk.gov.hmrc.play.http.HeaderCarrier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static uk.gov.hmrc.play.java.config.ServicesConfig.getInteger;
import static uk.gov.hmrc.play.java.config.ServicesConfig.getStringList;

public class FrontendAuditFilter extends MicroserviceAuditFilter implements uk.gov.hmrc.play.audit.filters.FrontendAuditFilter {
    private Regex textHtml = new Regex(Pattern.compile(".*(text/html).*"), JavaConversions.asScalaBuffer(new ArrayList<>()));
    private List<String> maskedFormFields = getStringList("filter.audit.maskedFormFields", Collections.singletonList("password"));
    private Integer applicationPort = getInteger("filter.audit.applicationPort", null);

    @Override
    public EssentialAction apply(EssentialAction nextFilter) {
        return (EssentialAction) FrontendAuditFilter$class.apply(this, nextFilter);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Option<Object> applicationPort() {
        return applicationPort != null ? new Some(applicationPort) : None$.empty();
    }

    @Override
    public String stripPasswords(Option<String> contentType, String requestBody, Seq<String> maskedFormFields) {
        return FrontendAuditFilter$class.stripPasswords(this, contentType, requestBody, maskedFormFields);
    }

    @Override
    public String getQueryString(Map<String, Seq<String>> queryString) {
        return FrontendAuditFilter$class.getQueryString(this, queryString);
    }

    @Override
    public String getHost(RequestHeader request) {
        return FrontendAuditFilter$class.getHost(this, request);
    }

    @Override
    public HeaderCarrier buildAuditedHeaders(RequestHeader request) {
        return FrontendAuditFilter$class.buildAuditedHeaders(this, request);
    }

    @Override
    public String getPort() {
        return FrontendAuditFilter$class.getPort(this);
    }

    @Override
    public Seq<String> maskedFormFields() {
        return JavaConversions.asScalaBuffer(maskedFormFields);
    }

    // Scala compatibility required methods
    public Regex uk$gov$hmrc$play$audit$filters$FrontendAuditFilter$$textHtml() {
        return textHtml;
    }

    // Scala compatibility required methods
    public void uk$gov$hmrc$play$audit$filters$FrontendAuditFilter$_setter_$uk$gov$hmrc$play$audit$filters$FrontendAuditFilter$$textHtml_$eq(Regex textHtml) {
        this.textHtml = textHtml;
    }
}
