package com.sersoft.dailyplanner.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.util.ArrayList;
@XStreamAlias("Events")
public class Events {
    @XStreamImplicit(itemFieldName = "Data")
    public ArrayList<Data> data;
    public ArrayList<Data> getData() {
        return data;
    }
}
