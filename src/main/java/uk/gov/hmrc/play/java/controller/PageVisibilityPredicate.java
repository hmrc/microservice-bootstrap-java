package uk.gov.hmrc.play.java.controller;

import akka.dispatch.Futures;
import play.api.mvc.AnyContent;
import play.mvc.Request;
import play.mvc.Results$;
import play.mvc.Result;
import scala.compat.java8.JFunction2;
import scala.concurrent.Future;
import uk.gov.hmrc.play.frontend.auth.AuthContext;
import uk.gov.hmrc.play.frontend.auth.PageVisibilityResult;
import uk.gov.hmrc.play.frontend.auth.PageVisibilityResult$class;
import uk.gov.hmrc.play.frontend.auth.connectors.domain.ConfidenceLevel;

import java.net.URI;

@FunctionalInterface
public interface PageVisibilityPredicate extends JFunction2<AuthContext, Request<AnyContent>, Future<PageVisibilityResult>> {
    static PageVisibilityPredicate identityConfidencePredicate(ConfidenceLevel requiredConfidenceLevel, Future<Result> failedConfidenceResult) {
        return (authContext, request) -> Futures.successful(new PageVisibilityResult() {
            @Override
            public boolean isVisible() {
                return authContext.user().confidenceLevel().$greater$eq(requiredConfidenceLevel);
            }

            @Override
            public Future<Result> nonVisibleResult() {
                return failedConfidenceResult;
            }
        });
    }

    static PageVisibilityPredicate upliftingIdentityConfidencePredicate(ConfidenceLevel requiredConfidenceLevel, URI upliftConfidenceUri) {
        return identityConfidencePredicate(requiredConfidenceLevel, Futures.successful(Results$.MODULE$.Redirect(upliftConfidenceUri.toString(), Results$.MODULE$.Redirect$default$2())));
    }

    static PageVisibilityPredicate nonNegotiableIdentityConfidencePredicate(ConfidenceLevel requiredConfidenceLevel) {
        return identityConfidencePredicate(requiredConfidenceLevel, Futures.successful(Results$.MODULE$.Forbidden()));
    }

    static PageVisibilityPredicate allowAll() {
        return (authContext, request) -> Futures.successful((PageVisibilityResultWrapper) () -> true);
    }

    @FunctionalInterface
    interface PageVisibilityResultWrapper extends uk.gov.hmrc.play.frontend.auth.PageVisibilityResult {
        @Override
        default Future<Result> nonVisibleResult() {
            return PageVisibilityResult$class.nonVisibleResult(this);
        }
    }
}
