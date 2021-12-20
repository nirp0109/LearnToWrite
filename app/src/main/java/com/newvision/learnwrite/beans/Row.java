package com.newvision.learnwrite.beans;

import java.util.ArrayList;

/**
 * Created by nir on 08/01/2015.
 */
public class Row {
    ArrayList<Key> keys;

    public Row(ArrayList<Key> keys) {
        this.keys = keys;
    }

    public Row() {
        keys = new ArrayList<Key>();
    }

    public ArrayList<Key> getKeys() {
        return keys;
    }

    public void setKeys(ArrayList<Key> keys) {
        this.keys = keys;
    }



}
