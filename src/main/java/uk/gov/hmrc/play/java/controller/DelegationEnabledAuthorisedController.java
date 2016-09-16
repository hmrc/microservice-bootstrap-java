package uk.gov.hmrc.play.java.controller;

import scala.Option;
import scala.concurrent.Future;
import uk.gov.hmrc.play.frontend.auth.DelegationData;
import uk.gov.hmrc.play.frontend.auth.DelegationEnabled;
import uk.gov.hmrc.play.frontend.auth.DelegationEnabled$class;
import uk.gov.hmrc.play.frontend.auth.connectors.DelegationConnector;
import uk.gov.hmrc.play.http.HeaderCarrier;

public class DelegationEnabledAuthorisedController extends DelegationDisabledAuthorisedController implements DelegationEnabled {

    @Override
    public DelegationConnector delegationConnector() {
        return null;
    }

    @Override
    public Future<Option<DelegationData>> loadDelegationData(String userId, HeaderCarrier hc) {
        return DelegationEnabled$class.loadDelegationData(this, userId, hc);
    }
}
