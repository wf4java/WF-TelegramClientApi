package wf.utils.telegram_client_api.models;

import it.tdlight.jni.TdApi;

public enum SimpleAuthorizationState {


    WAIT,
    CLOSED,
    LOGGED;


    public static SimpleAuthorizationState getFromAuthorizationState(TdApi.AuthorizationState authorizationState) {
        return switch (authorizationState.getConstructor()) {
            case 1526047584 -> CLOSED; //AuthorizationStateClosed
            case 445855311 -> CLOSED; // AuthorizationStateClosing
            case 154449270 -> CLOSED; // AuthorizationStateLoggingOut
            case -1834871737 -> LOGGED; // AuthorizationStateReady
            case 52643073 -> WAIT; // AuthorizationStateWaitCode
            case 1040478663 -> WAIT; // AuthorizationStateWaitEmailAddress
            case 174262505 -> WAIT; // AuthorizationStateWaitEmailCode
            case 860166378 -> WAIT; // AuthorizationStateWaitOtherDeviceConfirmation
            case 187548796 -> WAIT; // AuthorizationStateWaitPassword
            case 306402531 -> WAIT; // AuthorizationStateWaitPhoneNumber
            case 550350511 -> WAIT; // AuthorizationStateWaitRegistration
            case 904720988 -> WAIT; // AuthorizationStateWaitTdlibParameters
            default -> CLOSED;
        };
    }


}
