package uk.gov.hmrc.play.java.controller;

import akka.dispatch.Futures;
import org.joda.time.DateTime;
import play.api.libs.iteratee.Enumeratee;
import play.api.libs.iteratee.Iteratee;
import play.api.mvc.*;
import scala.*;
import scala.collection.Seq;
import scala.collection.immutable.Map;
import scala.compat.java8.JFunction2;
import scala.concurrent.Future;
import uk.gov.hmrc.play.frontend.auth.*;
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector;
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector$class;
import uk.gov.hmrc.play.frontend.auth.connectors.domain.Authority;
import uk.gov.hmrc.play.frontend.auth.connectors.domain.ConfidenceLevel;
import uk.gov.hmrc.play.http.HeaderCarrier;
import uk.gov.hmrc.play.http.HttpGet;
import uk.gov.hmrc.play.java.config.ServicesConfig;

import java.net.URI;

public class DelegationDisabledAuthorisedController implements UserActions, DelegationDisabled {
    public AuthenticatedBy authorisedFor(TaxRegime taxRegime, PageVisibilityPredicate pageVisibility) {
        return UserActions$class.AuthorisedFor(this, taxRegime, pageVisibility);
    }

    public AuthenticatedBy authenticatedBy(AuthenticationProvider authenticationProvider, PageVisibilityPredicate pageVisibility) {
        return UserActions$class.AuthenticatedBy(this, authenticationProvider, pageVisibility);
    }

    @Override
    public AuthenticatedBy AuthorisedFor(TaxRegime taxRegime, Function2<AuthContext, Request<AnyContent>, Future<PageVisibilityResult>> pageVisibility) {
        return UserActions$class.AuthorisedFor(this, taxRegime, pageVisibility);
    }

    @Override
    public AuthenticatedBy AuthenticatedBy(AuthenticationProvider authenticationProvider, Function2<AuthContext, Request<AnyContent>, Future<PageVisibilityResult>> pageVisibility) {
        return UserActions$class.AuthenticatedBy(this, authenticationProvider, pageVisibility);
    }

    @Override
    public AuthConnector authConnector() {
        return ServicesConfig.authConnector();
    }

    @Override
    public NonNegotiableIdentityConfidencePredicate VerifyConfidence() {
        return new NonNegotiableIdentityConfidencePredicate(ConfidenceLevel.fromInt(500));
    }

    @Override
    public NonNegotiableIdentityConfidencePredicate GGConfidence() {
        return new NonNegotiableIdentityConfidencePredicate(ConfidenceLevel.fromInt(50));
    }

    @Override
    public Future<Option<DelegationData>> loadDelegationData(String userId, HeaderCarrier hc) {
        return DelegationDisabled$class.loadDelegationData(this, userId, hc);
    }

    @Override
    public Function0<DateTime> now() {
        return SessionTimeoutWrapper$class.now(this);
    }

    @Override
    public Future<Option<AuthContext>> currentAuthContext(UserSessionData sessionData, HeaderCarrier hc) {
        return AuthContextService$class.currentAuthContext(this, sessionData, hc);
    }

    @Override
    public Action<AnyContent> WithUserAuthenticatedBy(AuthenticationProvider authenticationProvider, Option<TaxRegime> taxRegime, Function1<AuthContext, Action<AnyContent>> userAction) {
        return UserActionWrapper$class.WithUserAuthenticatedBy(this, authenticationProvider, taxRegime, userAction);
    }

    @Override
    public Enumeratee<byte[], byte[]> chunk() {
        return Results$class.chunk(this);
    }

    @Override
    public Option<Iteratee<byte[], Seq<Tuple2<String, String>>>> chunk$default$1() {
        return Results$class.chunk$default$1(this);
    }

    @Override
    public Enumeratee<byte[], byte[]> chunk(Option<Iteratee<byte[], Seq<Tuple2<String, String>>>> trailers) {
        return Results$class.chunk(this, trailers);
    }

    @Override
    public Enumeratee<byte[], byte[]> dechunk() {
        return Results$class.dechunk(this);
    }

    @Override
    public Result MovedPermanently(String url) {
        return Results$class.MovedPermanently(this, url);
    }

    @Override
    public Result Found(String url) {
        return Results$class.Found(this, url);
    }

    @Override
    public Result SeeOther(String url) {
        return Results$class.SeeOther(this, url);
    }

    @Override
    public Result TemporaryRedirect(String url) {
        return Results$class.TemporaryRedirect(this, url);
    }

    @Override
    public Status Status(int code) {
        return Results$class.Status(this, code);
    }

    @Override
    public Result Redirect(String url, int status) {
        return Results$class.Redirect(this, url, status);
    }

    public Result Redirect(String url) {
        return Results$class.Redirect(this, url, Results$class.Redirect$default$2(this));
    }

    @Override
    public int Redirect$default$3() {
        return Results$class.Redirect$default$3(this);
    }

    @Override
    public Map<String, Seq<String>> Redirect$default$2() {
        return Results$class.Redirect$default$2(this);
    }

    @Override
    public Result Redirect(String url, Map<String, Seq<String>> queryString, int status) {
        return Results$class.Redirect(this, url, queryString, status);
    }

    @Override
    public Result Redirect(Call call) {
        return Results$class.Redirect(this, call);
    }

    @Override
    public Function1<AuthContext, Action<AnyContent>> makeAction(Function1<AuthContext, Function1<Request<AnyContent>, Result>> body) {
        return UserActions$class.makeAction(this, body);
    }

    @Override
    public Function1<AuthContext, Action<AnyContent>> makeFutureAction(Function1<AuthContext, Function1<Request<AnyContent>, Future<Result>>> body) {
        return UserActions$class.makeFutureAction(this, body);
    }

    @Override
    public Status Ok() {
        return Results$.MODULE$.Ok();
    }

    @Override
    public Status Created() {
        return Results$.MODULE$.Created();
    }

    @Override
    public Status Accepted() {
        return Results$.MODULE$.Accepted();
    }

    @Override
    public Status NonAuthoritativeInformation() {
        return Results$.MODULE$.NonAuthoritativeInformation();
    }

    @Override
    public Result NoContent() {
        return Results$.MODULE$.NoContent();
    }

    @Override
    public Result ResetContent() {
        return Results$.MODULE$.ResetContent();
    }

    @Override
    public Status PartialContent() {
        return Results$.MODULE$.PartialContent();
    }

    @Override
    public Status MultiStatus() {
        return Results$.MODULE$.MultiStatus();
    }

    @Override
    public Result NotModified() {
        return Results$.MODULE$.NotModified();
    }

    @Override
    public Status BadRequest() {
        return Results$.MODULE$.BadRequest();
    }

    @Override
    public Status Unauthorized() {
        return Results$.MODULE$.Unauthorized();
    }

    @Override
    public Status Forbidden() {
        return Results$.MODULE$.Forbidden();
    }

    @Override
    public Status NotFound() {
        return Results$.MODULE$.NotFound();
    }

    @Override
    public Status MethodNotAllowed() {
        return Results$.MODULE$.MethodNotAllowed();
    }

    @Override
    public Status NotAcceptable() {
        return Results$.MODULE$.NotAcceptable();
    }

    @Override
    public Status RequestTimeout() {
        return Results$.MODULE$.RequestTimeout();
    }

    @Override
    public Status Conflict() {
        return Results$.MODULE$.Conflict();
    }

    @Override
    public Status Gone() {
        return Results$.MODULE$.Gone();
    }

    @Override
    public Status PreconditionFailed() {
        return Results$.MODULE$.PreconditionFailed();
    }

    @Override
    public Status EntityTooLarge() {
        return Results$.MODULE$.EntityTooLarge();
    }

    @Override
    public Status UriTooLong() {
        return Results$.MODULE$.UriTooLong();
    }

    @Override
    public Status UnsupportedMediaType() {
        return Results$.MODULE$.UnsupportedMediaType();
    }

    @Override
    public Status ExpectationFailed() {
        return Results$.MODULE$.ExpectationFailed();
    }

    @Override
    public Status UnprocessableEntity() {
        return Results$.MODULE$.UnprocessableEntity();
    }

    @Override
    public Status Locked() {
        return Results$.MODULE$.Locked();
    }

    @Override
    public Status FailedDependency() {
        return Results$.MODULE$.FailedDependency();
    }

    @Override
    public Status TooManyRequest() {
        return Results$.MODULE$.TooManyRequest();
    }

    @Override
    public Status InternalServerError() {
        return Results$.MODULE$.InternalServerError();
    }

    @Override
    public Status NotImplemented() {
        return Results$.MODULE$.NotImplemented();
    }

    @Override
    public Status BadGateway() {
        return Results$.MODULE$.BadGateway();
    }

    @Override
    public Status ServiceUnavailable() {
        return Results$.MODULE$.ServiceUnavailable();
    }

    @Override
    public Status GatewayTimeout() {
        return Results$.MODULE$.GatewayTimeout();
    }

    @Override
    public Status HttpVersionNotSupported() {
        return Results$.MODULE$.HttpVersionNotSupported();
    }

    @Override
    public Status InsufficientStorage() {
        return Results$.MODULE$.InsufficientStorage();
    }
}
