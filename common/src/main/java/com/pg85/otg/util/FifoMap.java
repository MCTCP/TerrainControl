package com.pg85.otg.util;

import java.util.Iterator;
import java.util.LinkedHashMap;

public class FifoMap<T, U> extends LinkedHashMap<T, U>
{
    int max;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public FifoMap (int max){
        super(max + 1);
        this.max = max;

    }

    @Override
    public U put (T key, U value) {
        U forReturn =  super.put(key, value);
        if (super.size() > max){
            removeEldest();
        }

        return forReturn;
    }

    private void removeEldest() {
        Iterator<T> iterator = this.keySet().iterator();
        if (iterator.hasNext()){
            this.remove(iterator.next());
        }
    }

}