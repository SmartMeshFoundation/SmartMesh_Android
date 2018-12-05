
package com.lingtuan.meshbox.custom;

/**
 * Class representing one entry in the chart. Might contain multiple values.
 * Might only contain a single value depending on the used constructor.
 * 
 * @author Philipp Jahoda
 */
public class Entry {

    /** the x value */
    private String x ;

    private float y;

    public Entry() {

    }

    /**
     * A Entry represents one single entry in the chart.
     *
     * @param x the x value
     * @param y the y value (the actual value of the entry)
     */
    public Entry(float y, String x) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the x-value of this Entry object.
     * 
     * @return
     */
    public String getX() {
        return x;
    }

    /**
     * Sets the x-value of this Entry object.
     * 
     * @param x
     */
    public void setX(String x) {
        this.x = x;
    }


    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
