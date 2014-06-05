/**
 *
 */
package com.blizzardtec.fixclient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import quickfix.ConfigError;
import quickfix.FileStoreFactory;
import quickfix.Initiator;
import quickfix.LogFactory;
import quickfix.MessageStoreFactory;
import quickfix.ScreenLogFactory;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.MarketDepth;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.fix44.MessageFactory;
import quickfix.fix44.MarketDataRequest;
import quickfix.Message;
import quickfix.fix44.MarketDataRequest.NoMDEntryTypes;
import quickfix.fix44.MarketDataRequest.NoRelatedSym;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Barnaby Golden
 *
 */
@SuppressWarnings("PMD.UnnecessaryFullyQualifiedName")
public final class FixClient {

    /**
     * Logger.
     */
    private static final Logger LOG =
        LoggerFactory.getLogger(FixClient.class);

    /**
     * Sleep time.
     */
    private static final int SLEEP_TIME = 3000;
    /**
     * Client configuration file.
     */
    private static final String CONFIG =
           "c:\\docs\\workspace\\fixclient\\src\\main\\resources\\fixconfig";

    /**
     * Run the fix client.
     */
    public void run() {

        final ClientApplication application = new ClientApplication();

        try {

            final SessionSettings settings = getSettings();

            final MessageStoreFactory storeFactory =
                        new FileStoreFactory(settings);

            final LogFactory logFactory =
                new ScreenLogFactory(true, true, true, true);

            final MessageFactory messageFactory = new MessageFactory();

            final Initiator initiator =
                new SocketInitiator(application, storeFactory,
                                        settings, logFactory, messageFactory);

            LOG.info("STARTING CLIENT...");

            initiator.start();

            // short sleep to allow login to complete
            Thread.sleep(SLEEP_TIME);

            sendMarketDataRequests(application);

            System.out.println("press <enter> to quit");

            System.in.read();
            sendMarketDataCancels(application);

            // short sleep to allow cancel request to complete
            Thread.sleep(SLEEP_TIME);

            initiator.stop();

        } catch (FileNotFoundException fnf) {
            LOG.error(fnf.getMessage());
        } catch (ConfigError ce) {
            LOG.error(ce.getMessage());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        } catch (InterruptedException e1) {
            LOG.info(e1.getMessage());
        }
    }

    /**
     * Cancel an existing MarketData request.
     *
     * @param application app
     */
    private void sendMarketDataCancels(final ClientApplication application) {

        // send a market data request
        LOG.info("Sending CANCEL MarketDataRequests");

        // send bid cancel
        application.sendMessage(
                application.getSessionId(),
                buildMarketDataCancel(OrderType.BID));

        // send offer cancel
        application.sendMessage(
                application.getSessionId(),
                buildMarketDataCancel(OrderType.OFFER));
    }

    /**
     * Build a market data cancel message.
     * @param type order type (BID/OFFER)
     * @return completed message
     */
    private Message buildMarketDataCancel(final char type) {

        return buildMsg(
           type,
         SubscriptionRequestType.DISABLE_PREVIOUS_SNAPSHOT_PLUS_UPDATE_REQUEST);
    }

    /**
     * Build the market data request message.
     * @param type the message type (BID/OFFER)
     * @return built message
     */
    private Message buildMarketDataRequest(final char type) {

        return buildMsg(
                type, SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES);
    }

    /**
     * Build message.
     * @param type the type
     * @param subType the sub type
     * @return msg
     */
    private Message buildMsg(final char type, final char subType) {

        final Message msg = new MarketDataRequest();
        // 262* Unique MDID
        msg.setField(new MDReqID("EURUSD01"));
        // 263* Subscription request type
        msg.setField(
          new SubscriptionRequestType(subType));
        // 264* Market depth
        msg.setField(new MarketDepth(1));

        // * MD request group
        msg.setField(new quickfix.field.NoMDEntryTypes('1'));
        final NoMDEntryTypes entryGroup = new NoMDEntryTypes();

        if (type == OrderType.OFFER) {
            entryGroup.set(new quickfix.field.MDEntryType(MDEntryType.OFFER));
        } else if (type == OrderType.BID) {
            entryGroup.set(new quickfix.field.MDEntryType(MDEntryType.BID));
        }

        msg.addGroup(entryGroup);

        // * Instrument MD request group
        msg.setField(new quickfix.field.NoRelatedSym('1'));
        final NoRelatedSym symGroup = new NoRelatedSym();
        //symGroup.set(new Symbol("EURUSD"));
        symGroup.set(new Symbol("EUR/USD"));
        msg.addGroup(symGroup);

        return msg;
    }

    /**
     * Send market data request messages for bid and offer for
     * a given instrument.
     *
     * @param application FIX application
     */
    private void sendMarketDataRequests(
            final ClientApplication application) {

        // send a bid market data request
        LOG.info("Sending Bid MarketDataRequest");
        application.sendMessage(
                application.getSessionId(),
                buildMarketDataRequest(OrderType.BID));

        // now send the offer request
        LOG.info("Sending Offer MarketDataRequest");
        application.sendMessage(
                application.getSessionId(),
                buildMarketDataRequest(OrderType.OFFER));
    }

    /**
     * Get the settings.
     * @return settings
     * @throws ConfigError thrown
     * @throws FileNotFoundException thrown if config file does not exist
     */
    private SessionSettings getSettings()
            throws ConfigError, FileNotFoundException {

        return new SessionSettings(
                new FileInputStream(CONFIG));
    }
}
