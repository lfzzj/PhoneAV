package com.lf.phoneav.test;

import java.util.Comparator;

public class MapComparator implements Comparator<Person> {

    public int compare(Person lhs, Person rhs) {
        return lhs.beginTime.compareTo(rhs.beginTime);
    }

}
