/**
 *
 */
package com.blizzardtec.fixclient.depth;

import com.blizzardtec.fixclient.OrderType;

/**
 * @author Barnaby Golden
 *
 */
public final class InstrumentDepth {

    /**
     * Number of depth levels to maintain.
     */
    public static final int DEPTH_LEVELS = 3;
    /**
     * Instrument symbol.
     */
    private String symbol;
    /**
     * List containing bid price depth levels.
     */
    private final transient PriceDepth[] bidPriceDepths;
    /**
     * List containing offer price depth levels.
     */
    private final transient PriceDepth[] offerPriceDepths;

    /**
     * Constructor.
     * @param symbol the instrument symbol for this depth data
     */
    public InstrumentDepth(final String symbol) {

        this.symbol = symbol;

        // initialise the price depth levels
        // for both bid and offer
        bidPriceDepths = new PriceDepth[DEPTH_LEVELS];
        offerPriceDepths = new PriceDepth[DEPTH_LEVELS];
    }

    /**
     * Update a given price depth level.
     * @param priceDepth price depth update
     */
    public void updateDepth(final PriceDepth priceDepth) {

        if (priceDepth.getType() == OrderType.BID) {
            bidPriceDepths[priceDepth.getLevel() - 1] = priceDepth;
        } else if (priceDepth.getType() == OrderType.OFFER) {
            offerPriceDepths[priceDepth.getLevel() - 1] = priceDepth;
        }
    }

    /**
     * Add a new price depth data entry.
     * @param priceDepth price depth data
     */
    public void newDepth(final PriceDepth priceDepth) {

        if (priceDepth.getType() == OrderType.BID) {
            insertDepthLevel(bidPriceDepths, priceDepth);
        } else if (priceDepth.getType() == OrderType.OFFER) {
            insertDepthLevel(offerPriceDepths, priceDepth);
        }
    }


    /**
     * Insert a new depth level into a list of depth levels
     * retaining order dropping level off the bottom.
     * @param depths list of depths
     * @param priceDepth new price depth to be inserted
     */
    private void insertDepthLevel(
            final PriceDepth[] depths,
            final PriceDepth priceDepth) {

        final int level = priceDepth.getLevel();
        final int length = depths.length;

        for (int i = (length - 1); i > (level - 1); i--) {
            depths[i] = depths[i - 1];
            if (depths[i] != null) {
                depths[i].setLevel(i + 1);
            }
        }

        depths[level - 1] = priceDepth;
    }

    /**
     * Delete a given price depth.
     * @param priceDepth price depth to remove
     */
    public void deleteDepth(final PriceDepth priceDepth) {

        if (priceDepth.getType() == OrderType.BID) {
            deleteDepthLevel(bidPriceDepths, priceDepth.getLevel());
        } else if (priceDepth.getType() == OrderType.OFFER) {
            deleteDepthLevel(offerPriceDepths, priceDepth.getLevel());
        }
    }

    /**
     * Delete a given level from an array of price depths.
     * @param depths price depths
     * @param level level to remove
     */
    private void deleteDepthLevel(
            final PriceDepth[] depths,
            final int level) {

        final int length = depths.length;

        for (int i = level; i < length; i++) {
            depths[i - 1] = depths[i];
            if (depths[i - 1] != null) {
                depths[i - 1].setLevel(i);
            }
        }

        depths[length - 1] = null;
    }

    /**
     * Get the depth for a given instrument symbol and order type.
     * @param symbol instrument symbol
     * @param type order type (BID/OFFER)
     * @return list of price depths
     */
    public PriceDepth[] getDepth(final String symbol, final char type) {

        PriceDepth[] depths;

        if (type == OrderType.BID) {
            depths = bidPriceDepths;
        } else {
            depths = offerPriceDepths;
        }

        return depths;
    }

    /**
     * Returns a String containing depth level information.
     * @return depth level information
     */
    public String printDepth() {

        final String nwl = System.getProperty("line.separator");

        final StringBuilder buffer = new StringBuilder();

        buffer.append(nwl + this.symbol + " BID" + nwl);

        for (int i = 0; i < bidPriceDepths.length; i++) {
            final PriceDepth pDepth = bidPriceDepths[i];
            if (pDepth == null) {
                buffer.append("NULL");
            } else {
                buffer.append("Level " + pDepth.getLevel()
                        + " "
                        + pDepth.getOrderSize()
                        + " at "
                        + pDepth.getMidPrice() + nwl);
            }
        }

        buffer.append(nwl + symbol + " OFFER" + nwl);

        for (int i = 0; i < offerPriceDepths.length; i++) {
            final PriceDepth pDepth = offerPriceDepths[i];
            if (pDepth == null) {
                buffer.append("NULL" + nwl);
            } else {
                buffer.append("Level " + pDepth.getLevel()
                        + " "
                        + pDepth.getOrderSize()
                        + " at "
                        + pDepth.getMidPrice() + nwl);
            }
        }

        return buffer.toString();
    }

    /**
     * @return the symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * @param symbol the symbol to set
     */
    public void setSymbol(final String symbol) {
        this.symbol = symbol;
    }
}
