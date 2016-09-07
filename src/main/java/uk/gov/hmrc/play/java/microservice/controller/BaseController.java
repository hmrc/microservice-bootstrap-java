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
        // Grab the json body
        JsonNode jsonNode = request().body().asJson();

        if (jsonNode == null) {
            return pure(badRequest("could not parse body due to..."));
        } else {
            try {
                // TODO - Would rather do this with the Spring/Hibernate validation
                T obj = play.libs.Json.fromJson(jsonNode, klass);
                // Apply the function
                return f.apply(obj);

            } catch (ConstraintViolationException ex) {
                return pure((badRequest(createJsonResponse(ex))));
            } catch (RuntimeException e) {
                // TODO - Would rather do this with the Spring/Hibernate validation
                if (e.getCause() instanceof JsonProcessingException) {
                    JsonProcessingException jpe = (JsonProcessingException) e.getCause();
                    // TODO: Want to generate a proper JsError object here
                    throw e;
                } else {
                    // TODO: Throw the RuntimeException
                    throw e;
                }
            }
        }
    }


    private JsonNode createJsonResponse(ConstraintViolationException cex) {
        Map<String, List<String>> validationMessages = Optional
                .fromNullable(cex.getConstraintViolations())
                .or(new HashSet<>())
                .stream()
                .map(v -> new F.Tuple<>(v.getPropertyPath().toString(), v.getMessage()))
                .collect(groupingBy(t -> t._1, mapping(t -> t._2, toList())));

        return toJson(validationMessages);
    }


    // EXAMPLE usage
/*
    public F.Promise<Result> doSomething() {
        return withJsonBody(String.class, obj -> {
            return pure(ok());
        });
    }
*/

    @FunctionalInterface
    public interface ToPromiseResult<T> {
        F.Promise<Result> apply(T type);
    }
}
