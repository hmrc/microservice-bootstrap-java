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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;
import play.api.mvc.Request;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import uk.gov.hmrc.play.http.HeaderCarrier;

import javax.validation.ConstraintViolationException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;
import static play.libs.F.Promise.pure;
import static play.libs.Json.toJson;
import static play.libs.Scala.Option;

public class BaseController extends Controller {

    public HeaderCarrier hc(Request<?> request) {
        return HeaderCarrier.fromHeadersAndSession(request.headers(), Option(request.session()));
    }

    protected <T> F.Promise<Result> withJsonBody(Class<T> klass, ToPromiseResult<T> f) {
        JsonNode jsonNode = request().body().asJson();

        if (jsonNode == null) {
            return pure(badRequest("could not parse body to JSON"));
        } else {
            try {
                T obj = play.libs.Json.fromJson(jsonNode, klass);
                F.Tuple<Integer, Object> r = f.apply(obj);
                return pure(Results.status(r._1, toJson(r._2)));
            } catch (ConstraintViolationException ex) {
                return pure((badRequest(handleValidationException(ex))));
            } catch (RuntimeException e) {
                if (e.getCause() instanceof JsonProcessingException) {
                    return pure(badRequest(handleProcessingException((JsonProcessingException) e.getCause())));
                } else {
                    throw e;
                }
            }
        }
    }

    private JsonNode handleProcessingException(JsonProcessingException jpe) {
        return toJson(jpe);
    }

    private JsonNode handleValidationException(ConstraintViolationException cex) {
        Map<String, List<String>> validationMessages = Optional
                .fromNullable(cex.getConstraintViolations())
                .or(new HashSet<>())
                .stream()
                .map(v -> new F.Tuple<>(v.getPropertyPath().toString(), v.getMessage()))
                .collect(groupingBy(t -> t._1, mapping(t -> t._2, toList())));

        return toJson(validationMessages);
    }

    protected F.Tuple<Integer, Object> response(Integer status, Object value) {
        return new F.Tuple<>(status, value);
    }

    protected F.Tuple<Integer, Object> response(Integer status) {
        return new F.Tuple<>(status, null);
    }

/*
    public F.Promise<Result> doSomething() {
        withJsonBody(String.class, object -> {
            // Do something with object
            return response(OK, "Hello World!");
        });
    }
*/

    @FunctionalInterface
    public interface ToPromiseResult<T> {
        F.Tuple<Integer, Object> apply(T type);
    }
}
