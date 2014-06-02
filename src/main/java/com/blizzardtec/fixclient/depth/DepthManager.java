/**
 * 
 */
package com.blizzardtec.fixclient.depth;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blizzardtec.fixclient.OrderType;

/**
 * @author Barnaby Golden
 *
 */
public final class DepthManager
        extends HashMap<String, InstrumentDepth> {

    /**
     * 
     */
    private static final long serialVersionUID = 4442926059155101544L;

    /**
     * Logger.
     */
    private static final Logger LOG =
        LoggerFactory.getLogger(DepthManager.class);

    /**
     * Add a new price depth entry.
     * @param priceDepth price depth
     */
    public void newDepthData(final PriceDepth priceDepth) {

        final String symbol = priceDepth.getSymbol();

        if (priceDepth.getType() == OrderType.BID) {
            LOG.info("New Bid price depth "
                    + priceDepth.getLevel()
                    + " data for " + symbol);            
        } else if (priceDepth.getType() == OrderType.OFFER) {
            LOG.info("New Offer price depth "
                    + priceDepth.getLevel()
                    + " data for " + symbol);
        }

        InstrumentDepth iDepth = get(symbol);

        // if the depth for this instrument is null
        // then it needs to be initialised
        if (iDepth == null) {

            iDepth = new InstrumentDepth(symbol);
            put(symbol, iDepth);
        }

        iDepth.newDepth(priceDepth);

        logDepth(symbol);
    }

    /**
     * Update a given price depth level.
     * @param priceDepth price depth data
     */
    public void updateDepthData(final PriceDepth priceDepth) {

        final String symbol = priceDepth.getSymbol();

        LOG.info("Update price depth "
                + priceDepth.getLevel()
                + " for " + symbol);

        final InstrumentDepth iDepth = get(symbol);

        iDepth.updateDepth(priceDepth);

        logDepth(symbol);
    }

    /**
     * Delete a given price depth level.
     * @param priceDepth price depth data
     */
    public void deleteDepthLevel(final PriceDepth priceDepth) {

        final String symbol = priceDepth.getSymbol();

        LOG.info("Delete price depth "
                + priceDepth.getLevel()
                + " for " + symbol);

        final InstrumentDepth iDepth = get(symbol);

        iDepth.deleteDepth(priceDepth);

        logDepth(symbol);
    }

    /**
     * Get the price depth for a given instrument and order type.
     * @param symbol instrument symbol
     * @param type order type (bid or offer)
     * @return price depth information
     */
    public PriceDepth[] getDepth(final String symbol, final char type) {

        final InstrumentDepth iDepth = get(symbol);

        return iDepth.getDepth(symbol, type);
    }

    /**
     * Print the depth information for a given instrument symbol.
     * @param symbol instrument symbol
     * @return String containing depth information
     */
    public String printDepth(final String symbol) {

        final InstrumentDepth iDepth = get(symbol);

        return iDepth.printDepth();
    }

    /**
     * Log the current price depths for a given symbol.
     * @param symbol the symbol to log information for
     */
    public void logDepth(final String symbol) {

        final InstrumentDepth iDepth = get(symbol);

        LOG.info(iDepth.printDepth());
    }
}
