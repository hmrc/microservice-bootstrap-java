/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.play.java.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.junit.Test;
import play.core.j.JavaParsers;
import play.mvc.Http;
import uk.gov.hmrc.play.java.ScalaFixtures;
import uk.gov.hmrc.play.java.controller.BaseController;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.test.Helpers.contentAsString;

public class ValidationControllerTest extends ScalaFixtures {

    private static final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    public void canGenerateValidationMessageForBadRequest() throws IOException {

        BaseController controller = new BaseController();
        Http.RequestBody jsonBody = mock(JavaParsers.DefaultRequestBody.class);
        when(request.body()).thenReturn(jsonBody);
        when(jsonBody.asJson()).thenReturn(new TextNode("test"));

        play.mvc.Result result = await(controller.withJsonBody(String.class, (str) -> {
            Set<ConstraintViolation<Object>> violations = createViolations();
            throw new ConstraintViolationException("error", violations);
        }));

        assertThat(result.toScala().header().status(), is(BAD_REQUEST));
        Map<String,List<String>> errors = objectMapper.readValue(contentAsString(result), Map.class);
        assertThat(errors.get("test").size(), is(1));
        assertThat(errors.get("test.test").get(0), is("invalid format"));

    }

    @Test
    public void canGenerateBadRequestWithEmptyViolations() throws IOException {
        BaseController controller = new BaseController();
        Http.RequestBody jsonBody = mock(JavaParsers.DefaultRequestBody.class);
        when(request.body()).thenReturn(jsonBody);
        when(jsonBody.asJson()).thenReturn(new TextNode("test"));

        play.mvc.Result result = await(controller.withJsonBody(String.class, (str) -> {
            throw new ConstraintViolationException("error", null);
        }));

        assertThat(result.toScala().header().status(), is(BAD_REQUEST));
        Map<String,List<String>> errors = objectMapper.readValue(contentAsString(result), Map.class);
        assertThat(errors.size(), is(0));

    }

    private Set<ConstraintViolation<Object>> createViolations() {
        Set<ConstraintViolation<Object>> violations = new HashSet<>();
        violations.add(new SimpleConstraintViolation("test", "value missing"));
        violations.add(new SimpleConstraintViolation("test.test", "invalid format"));
        return violations;
    }


    private static class SimpleConstraintViolation implements ConstraintViolation<Object> {

        private String path;
        private String message;

        public SimpleConstraintViolation(String path, String message) {
            this.path = path;
            this.message = message;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public String getMessageTemplate() {
            return null;
        }

        @Override
        public Object getRootBean() {
            return null;
        }

        @Override
        public Class getRootBeanClass() {
            return null;
        }

        @Override
        public Object getLeafBean() {
            return null;
        }

        @Override
        public Object[] getExecutableParameters() {
            return new Object[0];
        }

        @Override
        public Object getExecutableReturnValue() {
            return null;
        }

        @Override
        public Path getPropertyPath() {
            PathImpl rootPath = PathImpl.createRootPath();
            rootPath.addPropertyNode(path);
            return rootPath;
        }

        @Override
        public Object getInvalidValue() {
            return null;
        }

        @Override
        public ConstraintDescriptor<?> getConstraintDescriptor() {
            return null;
        }

        @Override
        public Object unwrap(Class type) {
            return null;
        }
    }
}
