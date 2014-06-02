/**
 * 
 */
package com.blizzardtec.fixclient;

import quickfix.SessionID;

/**
 * @author Barnaby Golden
 *
 */
public final class LogonEvent {

    /**
     * Session id.
     */
    private final transient SessionID sessionID;
    /**
     * Logged on flag.
     */
    private final transient boolean loggedOn;

    /**
     * Const.
     * @param sessionID param
     * @param loggedOn param
     */
    public LogonEvent(final SessionID sessionID, final boolean loggedOn) {
        this.sessionID = sessionID;
        this.loggedOn = loggedOn;
    }

    /**
     * Get session id.
     * @return id
     */
    public SessionID getSessionID() {
        return sessionID;
    }
    /**
     * Is logged on.
     * @return true if logged on
     */
    public boolean isLoggedOn() {
        return loggedOn;
    }
}
