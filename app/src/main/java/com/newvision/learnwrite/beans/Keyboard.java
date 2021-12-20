package com.newvision.learnwrite.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by nir on 08/01/2015.
 */
public class Keyboard {
    private double keyWidth;
    private double keyHeight;
    private ArrayList<Row> rows;
    private HashMap<Character,Boolean> map = null;


    public Keyboard(double keyWidth, double keyHeight) {
        this();
        this.keyWidth = keyWidth;
        this.keyHeight = keyHeight;

    }

    public Keyboard(int keyWidth, int keyHeight, ArrayList<Row> rows) {
        this(keyWidth,keyHeight);
        this.rows = rows;
    }

    public Keyboard() {
        rows = new ArrayList<Row>();
    }

    public double getKeyWidth() {
        return keyWidth;
    }

    public void setKeyWidth(double keywidth) {
        this.keyWidth = keywidth;
    }

    public double getKeyHeight() {
        return keyHeight;
    }

    public void setKeyHeight(double keyHeight) {
        this.keyHeight = keyHeight;
    }

    public ArrayList<Row> getRows() {
        return rows;
    }

    public void setRows(ArrayList<Row> rows) {
        this.rows = rows;
        map = new HashMap<Character, Boolean>();
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            Iterator<Key> iterator = row.getKeys().iterator();
            while (iterator.hasNext()) {
                Key key = iterator.next();
                char label = key.getLabel();
                Character character = Character.valueOf(label);
                map.put(character,true);
            }
        }
    }

    public boolean hasChar(char ch) {
        Character character = Character.valueOf(ch);
        return  map.containsKey(character);
    }
}
