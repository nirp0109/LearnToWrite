package com.newvision.learnwrite.beans;

/**
 * Created by nir on 08/01/2015.
 */
public class Key {
    public static final int RIGHT_EDGE = 1;
    public static final int LEFT_EDGE = 2;
    public static final int NO_EDGE = 3;

    private char label;
    private double horzintalGap = -1; //default gap
    private int edge = NO_EDGE;

    public Key() {
    }

    public Key(char label, double horzintalGap, int edge) {
        this.label = label;
        this.horzintalGap = horzintalGap;
        this.edge = edge;
    }

    public Key(char label) {
        this.label = label;
    }

    public Key(char label, int edge) {
        this.label = label;
        this.edge = edge;
    }

    public Key(char label, double horzintalGap) {
        this.label = label;
        this.horzintalGap = horzintalGap;
    }

    public char getLabel() {
        return label;
    }

    public void setLabel(char label) {
        this.label = label;
    }

    public double getHorzintalGap() {
        return horzintalGap;
    }

    public void setHorzintalGap(double horzintalGap) {
        this.horzintalGap = horzintalGap;
    }

    public int getEdge() {
        return edge;
    }

    public void setEdge(int edge) {
        this.edge = edge;
    }
}
