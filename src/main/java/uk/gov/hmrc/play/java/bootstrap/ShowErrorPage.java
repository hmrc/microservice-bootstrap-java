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

import play.api.mvc.Request;
import play.api.mvc.RequestHeader;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import play.twirl.api.Html;
import scala.compat.java8.JFunction1;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import uk.gov.hmrc.play.frontend.bootstrap.ShowErrorPage$class;

@FunctionalInterface
public interface ShowErrorPage extends JavaGlobalSettings, uk.gov.hmrc.play.frontend.bootstrap.ShowErrorPage {

    default RequestHeader getCurrentRequestHeader() {
        return Http.Context.current()._requestHeader();
    }

    default F.Promise<Result> wrapAndReturn(Future<play.api.mvc.Result> result) {
        JFunction1<play.api.mvc.Result, Result> resultConverter = scalaResult -> (Result) () -> scalaResult;
        ExecutionContext ec = play.api.libs.concurrent.Execution.defaultContext();
        return F.Promise.wrap(result.map(resultConverter, ec));
    }

    default F.Promise<Result> onBadRequest(Http.RequestHeader rh, String error) {
        Future<play.api.mvc.Result> result = onBadRequest(getCurrentRequestHeader(), error);
        return wrapAndReturn(result);
    }

    default F.Promise<Result> onHandlerNotFound(Http.RequestHeader rh) {
        Future<play.api.mvc.Result> result = onHandlerNotFound(getCurrentRequestHeader());
        return wrapAndReturn(result);
    }

    default F.Promise<Result> onError(Http.RequestHeader rh, Throwable t) {
        Future<play.api.mvc.Result> result = onError(getCurrentRequestHeader(), t);
        return wrapAndReturn(result);
    }

    Html standardErrorTemplate(String pageTitle, String heading, String message, Request<?> request);

    default Html badRequestTemplate(Request<?> request) {
        return ShowErrorPage$class.badRequestTemplate(this, request);
    }

    default Html notFoundTemplate(Request<?> request) {
        return ShowErrorPage$class.notFoundTemplate(this, request);
    }

    default Html internalServerErrorTemplate(Request<?> request) {
        return ShowErrorPage$class.internalServerErrorTemplate(this, request);
    }

    default play.api.mvc.Result resolveError(RequestHeader rh, Throwable ex) {
        return ShowErrorPage$class.resolveError(this, rh, ex);
    }

    default Future<play.api.mvc.Result> onError(RequestHeader request, Throwable ex) {
        return ShowErrorPage$class.onError(this, request, ex);
    }

    default Future<play.api.mvc.Result> onHandlerNotFound(RequestHeader request) {
        return ShowErrorPage$class.onHandlerNotFound(this, request);
    }

    default Future<play.api.mvc.Result> onBadRequest(RequestHeader request, String error) {
        return ShowErrorPage$class.onBadRequest(this, request, error);
    }
}
