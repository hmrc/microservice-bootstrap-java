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


import org.junit.Before;
import org.junit.Test;
import play.api.PlayException;
import play.api.mvc.Result;
import play.api.test.FakeHeaders;
import play.mvc.Http;
import uk.gov.hmrc.play.http.BadRequestException;
import uk.gov.hmrc.play.http.NotFoundException;
import uk.gov.hmrc.play.http.UnauthorizedException;
import uk.gov.hmrc.play.java.ScalaFixtures;
import uk.gov.hmrc.play.java.filters.MicroserviceAuditFilter;
import uk.gov.hmrc.play.java.filters.MicroserviceAuthFilter;
import uk.gov.hmrc.play.java.connectors.AuditConnector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JsonErrorHandlingTest extends ScalaFixtures {

    private DefaultMicroserviceGlobal jsh = new DefaultMicroserviceGlobal() {};

    private Http.RequestHeader requestHeader = mock(Http.RequestHeader.class);

    @Before
    public void setUp() {
        when(header.headers()).thenReturn(new FakeHeaders(FakeHeaders.apply$default$1()));
        when(header.method()).thenReturn("GET");
    }

    @Test
    public void errorHandlingInOnErrorFunctionShouldConvertANotFoundExceptionToNotFoundResponse() {
        Result result = this.await(jsh.onError(requestHeader, new PlayException("", "", new NotFoundException("test")))).toScala();
        assertThat(result.header().status(), is(404));
    }

    @Test
    public void errorHandlingInOnErrorFunctionShouldConvertABadRequestExceptionToNotFoundResponse() {
        Result result = this.await(jsh.onError(requestHeader, new PlayException("", "", new BadRequestException("bad request")))).toScala();
        assertThat(result.header().status(), is(400));
    }

    @Test
    public void errorHandlingInOnErrorFunctionShouldConvertAnUnauthorizedExceptionToUnauthorizedResponse() {
        Result result = this.await(jsh.onError(requestHeader, new PlayException("", "", new UnauthorizedException("unauthorized")))).toScala();
        assertThat(result.header().status(), is(401));
    }

    @Test
    public void errorHandlingInOnErrorFunctionShouldConvertAnExceptionToInternalServerError() {
        Result result = this.await(jsh.onError(requestHeader, new PlayException("", "", new Exception("any applicable exception")))).toScala();
        assertThat(result.header().status(), is(500));
    }
}
