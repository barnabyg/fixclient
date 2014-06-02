/**
 * 
 */
package com.blizzardtec.fixclient;

import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blizzardtec.fixclient.depth.DepthManager;
import com.blizzardtec.fixclient.depth.PriceDepth;

import quickfix.Application;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;
import quickfix.field.MDUpdateAction;
import quickfix.fix44.MarketDataIncrementalRefresh;
import quickfix.fix44.MarketDataSnapshotFullRefresh;
import quickfix.fix44.MessageCracker;

/**
 * @author Barnaby Golden
 * 
 */
public final class ClientApplication
            extends MessageCracker implements Application {

    /**
     * Logger.
     */
    private static final Logger LOG =
        LoggerFactory.getLogger(ClientApplication.class);

    /**
     * SessionID.
     */
    private transient SessionID sessionId;
    /**
     * Price depth manager.
     */
    private final transient DepthManager manager;

    /**
     * Logon.
     */
    private final transient ObservableLogon observableLogon =
                                            new ObservableLogon();

    /**
     * Observable logon.
     * 
     * @author Barnaby Golden
     * 
     */
    private static class ObservableLogon extends Observable {
        /**
         * Set.
         */
        private final transient Set<SessionID> set = new HashSet<SessionID>();

        /**
         * Logon.
         * 
         * @param sessionID
         *            id
         */
        public void logon(final SessionID sessionID) {
            set.add(sessionID);
            setChanged();
            notifyObservers(new LogonEvent(sessionID, true));
            clearChanged();
        }

        /**
         * Logoff.
         * 
         * @param sessionID
         *            id
         */
        public void logoff(final SessionID sessionID) {
            set.remove(sessionID);
            setChanged();
            notifyObservers(new LogonEvent(sessionID, false));
            clearChanged();
        }
    }

    /**
     * Constructor.
     */
    public ClientApplication() {
        super();
        manager = new DepthManager();
    }

    /*
     * (non-Javadoc)
     * 
     * @see quickfix.Application#fromAdmin(quickfix.Message, quickfix.SessionID)
     */
    @Override
    public void fromAdmin(final Message arg0, final SessionID arg1)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue,
            RejectLogon {

        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see quickfix.Application#fromApp(quickfix.Message, quickfix.SessionID)
     */
    @Override
    public void fromApp(final Message message, final SessionID sessionId)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue,
            UnsupportedMessageType {

        crack(message, sessionId);
    }

    /**
     * Handle a full market data snapshot.
     * @param snapshot snapshot
     * @param sessionID id
     */
    public void onMessage(final MarketDataSnapshotFullRefresh snapshot,
                          final SessionID sessionID) {

        LOG.info("Received MarketDataSnapshotFullRefresh");

        try {
            final String symbol =
                snapshot.get(new quickfix.field.Symbol()).getValue();

            final int entryCount =
                snapshot.get(new quickfix.field.NoMDEntries()).getValue();

            for (int i = 1; i < (entryCount + 1); i++) {

                final quickfix.fix44.MarketDataSnapshotFullRefresh.NoMDEntries
                    group =
                 new quickfix.fix44.MarketDataSnapshotFullRefresh.NoMDEntries();

                snapshot.getGroup(i, group);

                final quickfix.field.MDEntryType mdEntryType =
                            new quickfix.field.MDEntryType();
                final quickfix.field.MDEntryPx mdEntryPx =
                            new quickfix.field.MDEntryPx();
                final quickfix.field.MDEntrySize mdEntrySize =
                            new quickfix.field.MDEntrySize();
                final quickfix.field.MDEntryPositionNo mdPosition =
                            new quickfix.field.MDEntryPositionNo();

                group.get(mdEntryType);
                group.get(mdEntryPx);
                group.get(mdEntrySize);
                group.get(mdPosition);

                final char type = mdEntryType.getObject();
                final double price = mdEntryPx.getValue();
                final int size = (int) mdEntrySize.getValue();
                final int level = mdPosition.getValue();

                String typeStr = "Bid";

                if (type == OrderType.OFFER) {
                    typeStr = "Offer";
                }

                LOG.info(
                        typeStr + " Level " + level + " of "
                        + symbol + " " + size + " at " + price);

                final PriceDepth priceDepth = new PriceDepth();

                priceDepth.setMidPrice(price);
                priceDepth.setOrderSize(size);
                priceDepth.setSymbol(symbol);
                priceDepth.setType(type);
                priceDepth.setLevel(level);

                manager.newDepthData(priceDepth);
            }
            
        } catch (FieldNotFound e) {
            LOG.error(e.getMessage());
        }
    }

    /**
     * Handle a market data refresh.
     * @param refresh snapshot
     * @param sessionID id
     */
    public void onMessage(final MarketDataIncrementalRefresh refresh,
                          final SessionID sessionID) {

        LOG.info("Received MarketDataIncrementalRefresh");

        try {

            final int entryCount =
                refresh.get(new quickfix.field.NoMDEntries()).getValue();

            for (int i = 1; i < (entryCount + 1); i++) {

                final quickfix.fix44.MarketDataIncrementalRefresh.NoMDEntries
                    group =
                 new quickfix.fix44.MarketDataIncrementalRefresh.NoMDEntries();

                refresh.getGroup(i, group);

                final quickfix.field.MDEntryType mdEntryType =
                            new quickfix.field.MDEntryType();
                final quickfix.field.MDEntryPx mdEntryPx =
                            new quickfix.field.MDEntryPx();
                final quickfix.field.MDEntrySize mdEntrySize =
                            new quickfix.field.MDEntrySize();
                final quickfix.field.Symbol symbol =
                            new quickfix.field.Symbol();
                final quickfix.field.MDUpdateAction mdUpdate =
                            new quickfix.field.MDUpdateAction();
                final quickfix.field.MDEntryPositionNo mdPosition =
                    new quickfix.field.MDEntryPositionNo();

                group.get(mdEntryType);
                group.get(mdEntryPx);
                group.get(mdEntrySize);
                group.get(symbol);
                group.get(mdUpdate);
                group.get(mdPosition);

                final char type = mdEntryType.getObject();
                final double price = mdEntryPx.getValue();
                final int size = (int) mdEntrySize.getValue();
                final int level = mdPosition.getValue();

                final PriceDepth priceDepth = new PriceDepth();
                priceDepth.setLevel(level);
                priceDepth.setMidPrice(price);
                priceDepth.setOrderSize(size);
                priceDepth.setSymbol(symbol.getObject());
                priceDepth.setType(type);

                String updateAction;

                if (mdUpdate.getObject() == MDUpdateAction.CHANGE) {
                    updateAction = "CHANGE";
                    manager.updateDepthData(priceDepth);
                } else if (mdUpdate.getObject() == MDUpdateAction.DELETE) {
                    updateAction = "DELETE";
                    manager.deleteDepthLevel(priceDepth);
                } else if (mdUpdate.getObject() == MDUpdateAction.NEW) {
                    updateAction = "NEW";
                    manager.newDepthData(priceDepth);
                } else {
                    updateAction = "UNKNOWN";
                }


                LOG.info(
                    "Received " + updateAction + " refresh: "
                        + symbol.getObject() + " " + size + " at " + price);

            }
            
        } catch (FieldNotFound e) {
            LOG.error(e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see quickfix.Application#onCreate(quickfix.SessionID)
     */
    @Override
    public void onCreate(final SessionID arg0) {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see quickfix.Application#onLogon(quickfix.SessionID)
     */
    @Override
    public void onLogon(final SessionID arg0) {
        observableLogon.logon(arg0);
        this.sessionId = arg0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see quickfix.Application#onLogout(quickfix.SessionID)
     */
    @Override
    public void onLogout(final SessionID arg0) {
        observableLogon.logoff(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see quickfix.Application#toAdmin(quickfix.Message, quickfix.SessionID)
     */
    @Override
    public void toAdmin(final Message arg0, final SessionID arg1) {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see quickfix.Application#toApp(quickfix.Message, quickfix.SessionID)
     */
    @Override
    public void toApp(final Message arg0, final SessionID arg1)
            throws DoNotSend {

        // TODO Auto-generated method stub
    }

    /**
     * @return the sessionId
     */
    public SessionID getSessionId() {
        return sessionId;
    }

    /**
     * Send a message.
     * @param sessionID id
     * @param message message to send
     */
    public void sendMessage(
            final SessionID sessionID, final Message message) {
        FixHelper.sendMessage(sessionID, message);
    }
}
