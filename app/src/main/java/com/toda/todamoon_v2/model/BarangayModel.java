package com.toda.todamoon_v2.model;

import java.util.List;
import java.util.Map;

public class BarangayModel {
    public String name;
    public List<Map<String, String>> drivers;

    public BarangayModel() {
        // Default constructor
    }

    public BarangayModel(String name, List<Map<String, String>> drivers) {

        this.name = name;
        this.drivers = drivers;

    }
}
