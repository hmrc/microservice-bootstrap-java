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

import org.junit.Test;
import scala.Function1;
import uk.gov.hmrc.play.java.ScalaFixtures;

import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static play.test.Helpers.*;

public class WhitelistFilterTest extends ScalaFixtures {
    @Override
    protected Map<String, Object> additionalProperties() {
        Map<String, Object> props = super.additionalProperties();
        props.put("filter.whitelist.enabled", true);
        props.put("filter.whitelist.ips", "test.host,localhost");
        props.put("filter.whitelist.destination", "GET->https://www.gov.uk/somewhere");
        props.put("filter.whitelist.exclusions", Arrays.asList("GET->/ping/ping", "GET->/"));

        return props;
    }

    @Test
    public void verifyDestinationIsSourcedFromConfiguration() {
        running(fakeApplication(), () -> {
            WhitelistFilter filter = new WhitelistFilter();
            assertThat(filter.destination().method(), is(GET));
            assertThat(filter.destination().url(), is("https://www.gov.uk/somewhere"));
        });
    }

    @Test
    public void verifyTrueClientIsSet() {
        running(fakeApplication(), () -> {
            WhitelistFilter filter = new WhitelistFilter();
            assertThat(filter.trueClient(), is("True-Client-IP"));
            filter.uk$gov$hmrc$whitelist$AkamaiWhitelistFilter$_setter_$trueClient_$eq("TEST_OVERRIDE");
            assertThat(filter.trueClient(), is("TEST_OVERRIDE"));
        });
    }

    @Test
    public void verifyRestrictions() {
        running(fakeApplication(), () -> {
            WhitelistFilter filter = new WhitelistFilter();
            filter.apply(mock(Function1.class), fakeRequest(GET, "/test").getWrappedRequest());
        });
    }
}
