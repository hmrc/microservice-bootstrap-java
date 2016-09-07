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

package uk.gov.hmrc.play.java;

import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;
import org.scalatest.concurrent.*;
import org.scalatest.time.Millis$;
import org.scalatest.time.Span;
import play.GlobalSettings;
import play.Logger;
import play.api.libs.iteratee.Iteratee;
import play.api.mvc.*;
import play.libs.F;
import play.mvc.Http;
import play.test.FakeApplication;
import play.test.Helpers;
import play.test.WithApplication;
import scala.Function1;
import scala.compat.java8.JFunction0;
import scala.concurrent.Future;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public abstract class ScalaFixtures extends WithApplication implements ScalaFutures {

    private PatienceConfig patienceConfig = new PatienceConfig(scaled(Span.apply(150, Millis$.MODULE$)), scaled(Span.apply(15, Millis$.MODULE$)));

    protected Result okResult = Results$.MODULE$.Ok();
    protected play.api.mvc.Request header = mock(play.api.mvc.Request.class);
    protected Http.Request request = mock(Http.Request.class);

    @Before
    public void initHttpContext() {
        Map<String, String> flashData = Collections.emptyMap();
        Map<String, Object> argData = Collections.emptyMap();
        Long id = 2L;
        Http.Context ctx = new Http.Context(id, header, request, flashData, flashData, argData);
        Http.Context.current.set(ctx);
    }

    protected Action generateActionWithOkResponse() {
        return generateAction(okResult);
    }

    protected Action generateAction(Result result) {
        return generateAction(akka.dispatch.Futures.successful(result));
    }

    protected Action generateAction(Future<Result> result) {
        Action mockAction = mock(Action.class, (Answer) invocation -> {
            Logger.info("Invoking answer with {} - {}", invocation, invocation.getArguments()[0].getClass());

            if(invocation.getMethod().getName().equals("apply")) {
                JFunction0<Future<Result>> fn = () -> result;
                return ActionBuilder$class.async(Action$.MODULE$, fn).apply((RequestHeader) invocation.getArguments()[0]);
            } else {
                return invocation.callRealMethod();
            }
        });

        return mockAction;
    }

    protected RequestHeader captureRequest(Action action) {
        ArgumentCaptor<RequestHeader> rh = ArgumentCaptor.forClass(RequestHeader.class);
        verify(action).apply((Object)rh.capture());
        return rh.getValue();
    }

    protected FakeApplication fakeApplication() {
        return fakeApplication(new GlobalSettings());
    }

    protected FakeApplication fakeApplication(GlobalSettings global) {
        return fakeApplication(global, additionalProperties());
    }

    protected Map<String, Object> additionalProperties() {
        return new HashMap<>();
    }

    protected FakeApplication fakeApplication(GlobalSettings global, Map<String, Object> additionalConfig) {
        return new FakeApplication(new java.io.File("."), Helpers.class.getClassLoader(), additionalConfig, new ArrayList<>(), global);
    }

    protected <T> T await(Iteratee<byte[], T> it) {
        return await(it.run());
    }

    protected <T> T await(F.Promise<T> promise) {
        return convertScalaFuture(promise.wrapped()).futureValue(patienceConfig());
    }

    protected <T> T await(Future<T> scalaFuture) {
        return convertScalaFuture(scalaFuture).futureValue(patienceConfig());
    }

    @Override
    public Span scaled(Span span) {
        return ScaledTimeSpans$class.scaled(this, span);
    }

    @Override
    public <T> FutureConcept<T> convertScalaFuture(Future<T> scalaFuture) {
        return ScalaFutures$class.convertScalaFuture(this, scalaFuture);
    }

    @Override
    public PatienceConfig patienceConfig() {
        return patienceConfig;
    }

    public AbstractPatienceConfiguration.PatienceConfig$ PatienceConfig() {
        return new AbstractPatienceConfiguration.PatienceConfig$();
    }

    @Override
    public double spanScaleFactor() {
        return ScaledTimeSpans$class.spanScaleFactor(this);
    }

    @Override
    public Timeout timeout(Span value) {
        return PatienceConfiguration$class.timeout(this, value);
    }

    @Override
    public Interval interval(Span value) {
        return PatienceConfiguration$class.interval(this, value);
    }

    @Override
    public <T, U> U whenReady(FutureConcept<T> future, Timeout timeout, Interval interval, Function1<T, U> fun, PatienceConfig config) {
        return (U) Futures$class.whenReady(this, future, timeout, interval, fun, config);
    }

    @Override
    public <T, U> U whenReady(FutureConcept<T> future, Timeout timeout, Function1<T, U> fun, PatienceConfig config) {
        return (U)Futures$class.whenReady(this, future, timeout, fun, config);
    }

    @Override
    public <T, U> U whenReady(FutureConcept<T> future, Interval interval, Function1<T, U> fun, PatienceConfig config) {
        return (U)Futures$class.whenReady(this, future, interval, fun, config);
    }

    @Override
    public <T, U> U whenReady(FutureConcept<T> future, Function1<T, U> fun, PatienceConfig config) {
        return (U)Futures$class.whenReady(this, future, fun, config);
    }

    public PatienceConfig org$scalatest$concurrent$PatienceConfiguration$$defaultPatienceConfig() {
        return patienceConfig;
    }

    public void org$scalatest$concurrent$PatienceConfiguration$_setter_$org$scalatest$concurrent$PatienceConfiguration$$defaultPatienceConfig_$eq(PatienceConfig patienceConfig) {
        this.patienceConfig = patienceConfig;
    }
}
