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

package uk.gov.hmrc.play.java.microservice.controller;

import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.Test;
import play.api.mvc.Result;
import play.core.j.JavaParsers;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Results;
import uk.gov.hmrc.play.java.ScalaFixtures;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.mvc.Http.Status.OK;

public class Utf8MimeTypesTest extends ScalaFixtures {

    @Test
    public void controllerMinetypesShouldHaveDefaultApplicationJson() {
        BaseController controller = new BaseController();
        Http.RequestBody jsonBody = mock(JavaParsers.DefaultRequestBody.class);
        when(request.body()).thenReturn(jsonBody);
        when(jsonBody.asJson()).thenReturn(new TextNode("test"));

        Result applicationJsonWithUtf8Charset = await(controller.withJsonBody(String.class, (str) -> F.Tuple(OK, str))).toScala();

        assertThat(applicationJsonWithUtf8Charset.header().headers().get("Content-Type").get(), is("application/json; charset=utf-8"));
    }

    @Test
    public void controllerMinetypesShouldHaveUtf8SetTextPlain() {
        class BaseControllerExt extends BaseController {
            public F.Promise<play.mvc.Result> doSomething() {
                return F.Promise.pure(Results.ok(""));
            }
        };

        Http.RequestBody jsonBody = mock(JavaParsers.DefaultRequestBody.class);
        when(request.body()).thenReturn(jsonBody);
        when(jsonBody.asJson()).thenReturn(new TextNode("test"));

        Result applicationJsonWithUtf8Charset = await(new BaseControllerExt().doSomething()).toScala();

        assertThat(applicationJsonWithUtf8Charset.header().headers().get("Content-Type").get(), is("text/plain; charset=utf-8"));
    }
}
