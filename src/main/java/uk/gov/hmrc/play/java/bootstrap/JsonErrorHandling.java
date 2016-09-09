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

import akka.dispatch.Futures;
import play.GlobalSettings;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import scala.Some;
import scala.concurrent.Future;
import uk.gov.hmrc.play.http.HttpException;
import uk.gov.hmrc.play.http.Upstream4xxResponse;
import uk.gov.hmrc.play.http.Upstream5xxResponse;
import uk.gov.hmrc.play.microservice.bootstrap.ErrorResponse;

import static play.libs.Json.toJson;
import static play.mvc.Http.Status.*;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.notFound;
import static play.mvc.Results.status;
import static uk.gov.hmrc.play.microservice.bootstrap.ErrorResponse.*;

public class JsonErrorHandling extends GlobalSettings {

    public F.Promise<Result> onError(Http.RequestHeader request, Throwable ex) {
        ErrorResponse resp;

        if (ex.getCause() instanceof HttpException) {
            resp = apply(((HttpException) ex.getCause()).responseCode(), ex.getMessage(), apply$default$3(), apply$default$4());
        } else if (ex.getCause() instanceof Upstream4xxResponse) {
            resp = apply(((Upstream4xxResponse) ex).reportAs(), ex.getMessage(), apply$default$3(), apply$default$4());
        } else if (ex.getCause() instanceof Upstream5xxResponse) {
            resp = apply(((Upstream5xxResponse) ex).reportAs(), ex.getMessage(), apply$default$3(), apply$default$4());
        } else {
            resp = apply(INTERNAL_SERVER_ERROR, ex.getMessage(), apply$default$3(), apply$default$4());
        }

        return F.Promise.pure(status(resp.statusCode(), toJson(resp)));
    }

    public Future<play.api.mvc.Result> onHandlerNotFound(play.api.mvc.RequestHeader request) {
        ErrorResponse er = apply(NOT_FOUND, "URI not found", apply$default$3(), Some.apply(request.path()));
        return Futures.successful(notFound(toJson(er)).toScala());
    }

    public F.Promise<Result> onHandlerNotFound(Http.RequestHeader request) {
        ErrorResponse er = apply(NOT_FOUND, "URI not found", apply$default$3(), Some.apply(request.path()));
        return F.Promise.pure(notFound(toJson(er)));
    }

    public F.Promise<Result> onBadRequest(Http.RequestHeader request, String error) {
        ErrorResponse er = apply(BAD_REQUEST, error, apply$default$3(), apply$default$4());
        return F.Promise.pure(badRequest(toJson(er)));
    }
}
