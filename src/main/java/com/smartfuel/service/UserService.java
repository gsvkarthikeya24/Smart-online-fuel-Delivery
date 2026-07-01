package com.smartfuel.service;

import com.smartfuel.dto.RegistrationDto;
import com.smartfuel.entity.*;
import com.smartfuel.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DeliveryAgentRepository agentRepository;

    @Autowired
    private TrustScoreRepository trustScoreRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(RegistrationDto dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Get or Create Role
        String roleName = "ROLE_" + dto.getRole().toUpperCase();
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build()));

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .phoneNumber(dto.getPhoneNumber())
                .address(dto.getAddress())
                .role(role)
                .active(true)
                .trustScore(roleName.equals("ROLE_CUSTOMER") ? 100 : null)
                .trustLevel(roleName.equals("ROLE_CUSTOMER") ? "BRONZE" : null)
                .build();

        User savedUser = userRepository.save(user);

        // If the role is agent, save extra details
        if (roleName.equals("ROLE_AGENT")) {
            DeliveryAgent agent = DeliveryAgent.builder()
                    .user(savedUser)
                    .vehicleNumber(dto.getVehicleNumber() != null ? dto.getVehicleNumber() : "N/A")
                    .vehicleType(dto.getVehicleType() != null ? dto.getVehicleType() : "N/A")
                    .status("AVAILABLE")
                    .currentLatitude(12.9716) // Default coordinates (e.g. Bangalore)
                    .currentLongitude(77.5946)
                    .build();
            agentRepository.save(agent);
        }

        // Log initial trust score for customers
        if (roleName.equals("ROLE_CUSTOMER")) {
            TrustScore initialScore = TrustScore.builder()
                    .customer(savedUser)
                    .score(100)
                    .level("BRONZE")
                    .build();
            trustScoreRepository.save(initialScore);
        }

        return savedUser;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateProfile(String username, String fullName, String email, String phoneNumber, String address) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setAddress(address);
        return userRepository.save(user);
    }

    @Transactional
    public void updateTrustScore(User customer, int delta) {
        int newScore = customer.getTrustScore() + delta;
        // Clamp between 0 and 200
        newScore = Math.max(0, Math.min(200, newScore));
        
        String newLevel = calculateLevel(newScore);
        
        customer.setTrustScore(newScore);
        customer.setTrustLevel(newLevel);
        userRepository.save(customer);

        // Log the change
        TrustScore log = TrustScore.builder()
                .customer(customer)
                .score(newScore)
                .level(newLevel)
                .build();
        trustScoreRepository.save(log);
    }
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    

    private String calculateLevel(int score) {
        if (score <= 80) return "BRONZE";
        if (score <= 120) return "SILVER";
        if (score <= 160) return "GOLD";
        return "PLATINUM";
    }
}
