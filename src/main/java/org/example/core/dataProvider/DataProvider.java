package org.example.core.dataProvider;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class DataProvider {

    @org.testng.annotations.DataProvider(name = "TC01")
    public static Iterator<Object[]> TC01() {
        log.info("Creating TC01 data");
        String destination = "Da Nang";
        int rooms = 2;
        int adults = 4;
        int children = 0;
        int expectedHotelCount = 5;
        
        List<Object[]> dataToReturn = new ArrayList<>();
        dataToReturn.add(new Object[]{destination, rooms, adults, children, expectedHotelCount});
        
        log.info("Created TC01 data: destination={}, rooms={}, adults={}, children={}, expectedCount={}", 
                destination, rooms, adults, children, expectedHotelCount);
        return dataToReturn.iterator();
    }
}
