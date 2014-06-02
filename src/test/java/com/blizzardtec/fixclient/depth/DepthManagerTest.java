/**
 * 
 */
package com.blizzardtec.fixclient.depth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.blizzardtec.fixclient.OrderType;

/**
 * @author Barnaby Golden
 *
 */
public final class DepthManagerTest {

    /**
     * 
     */
    private static final int NEWSIZE2 = 200;
    /**
     * 
     */
    private static final int NEWSIZE1 = 100;
    /**
     * 
     */
    private static final double NEWPRICE2 = 8.88;
    /**
     * 
     */
    private static final double NEWPRICE1 = 9.99;
    /**
     * 
     */
    private static final int SIZE = 1000;
    /**
     * 
     */
    private static final String SYMBOL = "EURUSD";

    /**
     * Test the adding of depth information.
     */
    @Test
    public void depthTest() {

        final double[] bidPrices = {1.4335, 1.4334, 1.4333};
        final double[] offerPrices = {1.4326, 1.4327, 1.4328};

        final DepthManager manager = new DepthManager();

        // first create 3 levels of bid and offer data
        for (int i = 0; i < bidPrices.length; i++) {
            PriceDepth priceDepth = new PriceDepth();
            priceDepth.setSymbol(SYMBOL);
            priceDepth.setMidPrice(bidPrices[i]);
            priceDepth.setOrderSize(SIZE);
            priceDepth.setType(OrderType.BID);
            priceDepth.setLevel(i + 1);
            manager.newDepthData(priceDepth);

            priceDepth = new PriceDepth();
            priceDepth.setSymbol(SYMBOL);
            priceDepth.setMidPrice(offerPrices[i]);
            priceDepth.setOrderSize(SIZE);
            priceDepth.setType(OrderType.OFFER);
            priceDepth.setLevel(i + 1);
            manager.newDepthData(priceDepth);
        }

        PriceDepth[] bidDepths =
                    manager.getDepth(SYMBOL, OrderType.BID);
        PriceDepth[] offerDepths =
                    manager.getDepth(SYMBOL, OrderType.OFFER);

        assertNotNull("Null bid price depths returned", bidDepths);
        assertNotNull("Null offer price depths returned", bidDepths);
        assertEquals(
             "Three bid depths not returned",
             bidDepths.length, InstrumentDepth.DEPTH_LEVELS);
        assertEquals(
                "Three offer depths not returned",
                offerDepths.length, InstrumentDepth.DEPTH_LEVELS);
        assertEquals("Bid price does not match",
                bidDepths[0].getMidPrice(), bidPrices[0], 0.0);
        assertEquals("Offer price does not match",
                offerDepths[0].getMidPrice(), offerPrices[0], 0.0);

        // next update a level
        PriceDepth pDepth = new PriceDepth();
        pDepth.setLevel(1);
        pDepth.setMidPrice(NEWPRICE1);
        pDepth.setOrderSize(NEWSIZE1);
        pDepth.setSymbol(SYMBOL);
        pDepth.setType(OrderType.BID);

        manager.updateDepthData(pDepth);

        bidDepths = manager.getDepth(SYMBOL, OrderType.BID);
        assertEquals("Invalid updated bid price",
                bidDepths[0].getMidPrice(), NEWPRICE1, 0.0);
        assertEquals("Invalid updated bid size",
                bidDepths[0].getOrderSize(), NEWSIZE1);

        // then add a new level
        pDepth = new PriceDepth();
        pDepth.setLevel(2);
        pDepth.setMidPrice(NEWPRICE2);
        pDepth.setOrderSize(NEWSIZE2);
        pDepth.setSymbol(SYMBOL);
        pDepth.setType(OrderType.BID);

        manager.newDepthData(pDepth);

        bidDepths = manager.getDepth(SYMBOL, OrderType.BID);
        assertEquals("Invalid new bid price",
                bidDepths[1].getMidPrice(), NEWPRICE2, 0.0);
        assertEquals("Invalid new bid size",
                bidDepths[1].getOrderSize(), NEWSIZE2);

        // finally delete a depth level
        pDepth = new PriceDepth();
        pDepth.setLevel(1);
        pDepth.setSymbol(SYMBOL);
        pDepth.setType(OrderType.OFFER);

        offerDepths =
            manager.getDepth(SYMBOL, OrderType.OFFER);
        // make a note of the price at level 2 and at level 3
        final double level2Price = offerDepths[1].getMidPrice();
        final double level3Price = offerDepths[2].getMidPrice();

        manager.deleteDepthLevel(pDepth);

        offerDepths =
            manager.getDepth(SYMBOL, OrderType.OFFER);

        // level 3 price should now be at level 2
        assertEquals("level 3 price not at level 2",
                offerDepths[1].getMidPrice(), level3Price, 0.0);

        // level 2 price should now be at level 1
        assertEquals("level 2 price not at level 1",
                offerDepths[0].getMidPrice(), level2Price, 0.0);
    }
}
