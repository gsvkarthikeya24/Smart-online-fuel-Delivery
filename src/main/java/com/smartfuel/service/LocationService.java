package com.smartfuel.service;

import com.smartfuel.dto.NearbyProviderDto;
import com.smartfuel.entity.User;
import com.smartfuel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LocationService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Calculate distance between two coordinates using Haversine formula (in kilometers)
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in km
    }

    /**
     * Find nearby fuel providers within specified radius
     */
    public List<NearbyProviderDto> findNearbyProviders(double customerLat, double customerLon, double radiusKm) {
        // Get all active providers
        List<User> providers = userRepository.findAll()
                .stream()
                .filter(u -> u.getRole() != null && u.getRole().getName().equals("ROLE_PROVIDER") && u.isActive())
                .collect(Collectors.toList());

        List<NearbyProviderDto> nearbyProviders = new ArrayList<>();

        for (User provider : providers) {

            // Provider location from database
            Double providerLat = provider.getLatitude();
            Double providerLon = provider.getLongitude();

            // Skip providers without location
            if (providerLat == null || providerLon == null) {
                continue;
            }

            double distance = calculateDistance(
                    customerLat,
                    customerLon,
                    providerLat,
                    providerLon
            );

            if (distance <= radiusKm) {
                NearbyProviderDto dto = NearbyProviderDto.builder()
                        .id(provider.getId())
                        .name(provider.getFullName())
                        .address(provider.getAddress())
                        .distance(Math.round(distance * 100.0) / 100.0)
                        .latitude(providerLat)
                        .longitude(providerLon)
                        .build();

                nearbyProviders.add(dto);
            }
        }
            
         

            

           

        // Sort by distance (closest first)
        nearbyProviders.sort(Comparator.comparingDouble(NearbyProviderDto::getDistance));
        return nearbyProviders;
    }

    /**
     * Get provider by ID with distance from customer location
     */
    public NearbyProviderDto getProviderWithDistance(Long providerId, double customerLat, double customerLon) {
        User provider = userRepository.findById(providerId).orElse(null);
        if (provider == null) {
            return null;
        }

        Double providerLat = provider.getLatitude();
        Double providerLon = provider.getLongitude();

        if (providerLat == null || providerLon == null) {
            return null;
        }

        double distance = calculateDistance(
                customerLat,
                customerLon,
                providerLat,
                providerLon
        );

        return NearbyProviderDto.builder()
                .id(provider.getId())
                .name(provider.getFullName())
                .address(provider.getAddress())
                .distance(Math.round(distance * 100.0) / 100.0)
                .latitude(providerLat)
                .longitude(providerLon)
                .build();
    }
}