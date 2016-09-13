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

import play.api.mvc.*;
import scala.Function1;
import scala.collection.Seq;
import scala.concurrent.Future;
import uk.gov.hmrc.whitelist.AkamaiWhitelistFilter;
import uk.gov.hmrc.whitelist.AkamaiWhitelistFilter$class;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static scala.collection.JavaConversions.asScalaBuffer;
import static uk.gov.hmrc.play.java.config.ServicesConfig.*;

public class WhitelistFilter implements AkamaiWhitelistFilter {
    private static String trueClient = "True-Client-IP";
    private static List<String> whitelist;
    private static Call destination;
    private static List<Call> excludedPaths;
    private static boolean enabled;

    public WhitelistFilter() {
        enabled = getBoolean("filter.whitelist.enabled", false);
        whitelist = Arrays.asList(getString("filter.whitelist.ips", "localhost").split(","));
        destination = toCall(getString("filter.whitelist.destination", null));
        excludedPaths = getStringList("filter.whitelist.exclusions", new ArrayList<>()).stream().map(WhitelistFilter::toCall).collect(Collectors.toList());
    }

    private static Call toCall(String methodAndPath) {
        if(Optional.ofNullable(methodAndPath).isPresent()) {
            String[] splitMethodAndPath = methodAndPath.split("->");
            return Call.apply(splitMethodAndPath[0], splitMethodAndPath[1]);
        } else {
            return null;
        }
    }

    @Override
    public String trueClient() {
        return trueClient;
    }

    @Override
    public Seq<String> whitelist() {
        return asScalaBuffer(whitelist).toList();
    }

    @Override
    public Seq<Call> excludedPaths() {
        return asScalaBuffer(excludedPaths).toList();
    }

    @Override
    public Call destination() {
        return destination;
    }

    @Override
    public Future<Result> apply(Function1<RequestHeader, Future<Result>> f, RequestHeader rh) {
        if(enabled) {
            return AkamaiWhitelistFilter$class.apply(this, f, rh);
        } else {
            return f.apply(rh);
        }
    }

    @Override
    public EssentialAction apply(EssentialAction next) {
        return Filter$class.apply(this, next);
    }

    // Scala compatibility required methods
    public void uk$gov$hmrc$whitelist$AkamaiWhitelistFilter$_setter_$trueClient_$eq(String trueClient) {
        WhitelistFilter.trueClient = trueClient;
    }
}
