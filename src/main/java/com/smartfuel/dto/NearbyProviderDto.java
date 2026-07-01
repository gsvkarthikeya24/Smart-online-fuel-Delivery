package com.smartfuel.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NearbyProviderDto {
    private Long id;
    private String name;
    private String address;
    private Double distance;
    private Double latitude;
    private Double longitude;
}
