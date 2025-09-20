package org.example.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Hotel {
    private String name;
    private String rating;
    private String location;
    private String distanceToCenter;
    private String cashbackReward;
    private String[] amenities;
    private String[] badges;
    
    @Override
    public String toString() {
        return String.format("Hotel{name='%s', rating='%s', location='%s', distance='%s', cashback='%s'}", 
                name, rating, location, distanceToCenter, cashbackReward);
    }
}
