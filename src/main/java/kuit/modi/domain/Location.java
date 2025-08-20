package kuit.modi.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "location")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 255)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    public static Location create(String address, double latitude, double longitude) {
        Location location = new Location();
        location.setAddress(address);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }
}