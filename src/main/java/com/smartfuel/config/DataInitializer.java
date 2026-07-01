package com.smartfuel.config;

import com.smartfuel.dto.RegistrationDto;
import com.smartfuel.entity.*;
import com.smartfuel.repository.*;
import com.smartfuel.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FuelTypeRepository fuelTypeRepository;

    @Autowired
    private FuelInventoryRepository inventoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private DeliveryAgentRepository agentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private DemandPredictionService predictionService;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() > 0) {
            // Update existing dollar-based prices to Indian Rupee rates if they haven't been updated
            List<FuelType> fuels = fuelTypeRepository.findAll();
            for (FuelType fuel : fuels) {
                if (fuel.getName().contains("Petrol") && fuel.getBasePricePerLiter() < 5.0) {
                    fuel.setBasePricePerLiter(103.50);
                    fuelTypeRepository.save(fuel);
                } else if (fuel.getName().contains("Diesel") && fuel.getBasePricePerLiter() < 5.0) {
                    fuel.setBasePricePerLiter(92.00);
                    fuelTypeRepository.save(fuel);
                } else if (fuel.getName().contains("CNG") && fuel.getBasePricePerLiter() < 5.0) {
                    fuel.setBasePricePerLiter(82.00);
                    fuelTypeRepository.save(fuel);
                } else if (fuel.getName().contains("Charging") && fuel.getBasePricePerLiter() < 5.0) {
                    fuel.setBasePricePerLiter(15.00);
                    fuelTypeRepository.save(fuel);
                }
            }
            return; // Data already initialized
        }

        System.out.println("Initializing Smart Fuel database sample records...");

        // 1. Roles
        Role adminRole = roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
        Role customerRole = roleRepository.save(Role.builder().name("ROLE_CUSTOMER").build());
        Role providerRole = roleRepository.save(Role.builder().name("ROLE_PROVIDER").build());
        Role agentRole = roleRepository.save(Role.builder().name("ROLE_AGENT").build());

        // 2. Users (using UserService to leverage proper setup)
        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin"))
                .email("admin@smartfuel.com")
                .fullName("System Administrator")
                .phoneNumber("+111222333")
                .role(adminRole)
                .active(true)
                .build();
        userRepository.save(admin);

        // Preload Customer
        RegistrationDto custDto = RegistrationDto.builder()
                .username("customer")
                .password("password")
                .email("customer@gmail.com")
                .fullName("Sarah Connor")
                .phoneNumber("+198765432")
                .address("Kompally, Medchal, Telangana")
                .role("CUSTOMER")
                .build();
        User customerUser = userService.registerUser(custDto);

        // Preload Providers - Telangana & Hyderabad regions for nearby testing
        RegistrationDto provDto1 = RegistrationDto.builder()
                .username("provider1")
                .password("password")
                .email("provider1@smartfuel.com")
                .fullName("Apex Gas Station")
                .phoneNumber("+123456001")
                .address("Koti, Hyderabad, Telangana")
                .role("PROVIDER")
                .build();
        User provider1 = userService.registerUser(provDto1);
        provider1.setLatitude(17.3850);
        provider1.setLongitude(78.4867);
        provider1 = userRepository.save(provider1);

        RegistrationDto provDto2 = RegistrationDto.builder()
                .username("provider2")
                .password("password")
                .email("provider2@smartfuel.com")
                .fullName("Metro Petroleum Depot")
                .phoneNumber("+123456002")
                .address("Medchal Mandal, Medchal-Malkajgiri, Telangana")
                .role("PROVIDER")
                .build();
        User provider2 = userService.registerUser(provDto2);
        provider2.setLatitude(17.6295);
        provider2.setLongitude(78.4814);
        provider2 = userRepository.save(provider2);

        RegistrationDto provDto3 = RegistrationDto.builder()
                .username("provider3")
                .password("password")
                .email("provider3@smartfuel.com")
                .fullName("LA Fuel Express")
                .phoneNumber("+123456003")
                .address("Banswada Road, Kamareddy, Telangana")
                .role("PROVIDER")
                .build();
        User provider3 = userService.registerUser(provDto3);
        provider3.setLatitude(18.3846);
        provider3.setLongitude(77.8824);
        provider3 = userRepository.save(provider3);

        RegistrationDto provDto4 = RegistrationDto.builder()
                .username("provider4")
                .password("password")
                .email("provider4@smartfuel.com")
                .fullName("Pacific Gas Stations")
                .phoneNumber("+123456004")
                .address("Paradise Circle, Secunderabad, Telangana")
                .role("PROVIDER")
                .build();
        User provider4 = userService.registerUser(provDto4);
        provider4.setLatitude(17.4399);
        provider4.setLongitude(78.4983);
        provider4 = userRepository.save(provider4);

        RegistrationDto provDto5 = RegistrationDto.builder()
                .username("provider5")
                .password("password")
                .email("provider5@smartfuel.com")
                .fullName("Midwest Fuel Hub")
                .phoneNumber("+123456005")
                .address("Kamareddy Bypass Road, Kamareddy, Telangana")
                .role("PROVIDER")
                .build();
        User provider5 = userService.registerUser(provDto5);
        provider5.setLatitude(18.3182);
        provider5.setLongitude(78.3347);
        provider5 = userRepository.save(provider5);

        RegistrationDto provDto6 = RegistrationDto.builder()
                .username("provider6")
                .password("password")
                .email("provider6@smartfuel.com")
                .fullName("Great Lakes Petroleum")
                .phoneNumber("+123456006")
                .address("Kompally Highway, Medchal, Telangana")
                .role("PROVIDER")
                .build();
        User provider6 = userService.registerUser(provDto6);
        provider6.setLatitude(17.5600);
        provider6.setLongitude(78.4500);
        provider6 = userRepository.save(provider6);

        RegistrationDto provDto7 = RegistrationDto.builder()
                .username("provider7")
                .password("password")
                .email("provider7@smartfuel.com")
                .fullName("Bay Area Fuel Station")
                .phoneNumber("+123456007")
                .address("Pragathi Nagar, Nizampet, Hyderabad, Telangana")
                .role("PROVIDER")
                .build();
        User provider7 = userService.registerUser(provDto7);
        provider7.setLatitude(17.5186);
        provider7.setLongitude(78.3845);
        provider7 = userRepository.save(provider7);

        RegistrationDto provDto8 = RegistrationDto.builder()
                .username("provider8")
                .password("password")
                .email("provider8@smartfuel.com")
                .fullName("Golden Gate Petroleum")
                .phoneNumber("+123456008")
                .address("Dundigal Road, Kompally, Telangana")
                .role("PROVIDER")
                .build();
        User provider8 = userService.registerUser(provDto8);
        provider8.setLatitude(17.5410);
        provider8.setLongitude(78.4820);
        provider8 = userRepository.save(provider8);

        // Preload Delivery Agents
        RegistrationDto agentDto1 = RegistrationDto.builder()
                .username("agent1")
                .password("password")
                .email("agent1@smartfuel.com")
                .fullName("Mark Swift")
                .phoneNumber("+15550099")
                .address("Fleet Depot East")
                .role("AGENT")
                .vehicleNumber("TX-7890-C")
                .vehicleType("E-Tanker Truck 1")
                .build();
        User agent1 = userService.registerUser(agentDto1);

        RegistrationDto agentDto2 = RegistrationDto.builder()
                .username("agent2")
                .password("password")
                .email("agent2@smartfuel.com")
                .fullName("Luke Driver")
                .phoneNumber("+15550088")
                .address("Fleet Depot West")
                .role("AGENT")
                .vehicleNumber("TX-1234-A")
                .vehicleType("Heavy Hauler Mini-Tanker")
                .build();
        User agent2 = userService.registerUser(agentDto2);

        // 3. Fuel Types
        FuelType petrol = fuelTypeRepository.save(FuelType.builder().name("Premium Petrol").description("91 Octane premium unleaded gasoline").basePricePerLiter(103.50).active(true).build());
        FuelType diesel = fuelTypeRepository.save(FuelType.builder().name("Clean Diesel").description("Ultra-low sulfur environment friendly diesel").basePricePerLiter(92.00).active(true).build());
        FuelType cng = fuelTypeRepository.save(FuelType.builder().name("Compressed Natural Gas (CNG)").description("Highly compressed eco-fuel").basePricePerLiter(82.00).active(true).build());
        FuelType electric = fuelTypeRepository.save(FuelType.builder().name("Fast Charging").description("High voltage electric charging supply").basePricePerLiter(15.00).active(true).build());

        // 4. Fuel Inventories
        // Provider 1 - New York
        inventoryRepository.save(FuelInventory.builder().provider(provider1).fuelType(petrol).currentStockLiters(4500.0).maxCapacityLiters(5000.0).build());
        inventoryRepository.save(FuelInventory.builder().provider(provider1).fuelType(diesel).currentStockLiters(500.0).maxCapacityLiters(5000.0).build()); // Low stock alert demo
        // Provider 2 - New York
        inventoryRepository.save(FuelInventory.builder().provider(provider2).fuelType(cng).currentStockLiters(3500.0).maxCapacityLiters(5000.0).build());
        inventoryRepository.save(FuelInventory.builder().provider(provider2).fuelType(electric).currentStockLiters(4000.0).maxCapacityLiters(5000.0).build());
        // Provider 3 - Los Angeles
        inventoryRepository.save(FuelInventory.builder().provider(provider3).fuelType(petrol).currentStockLiters(4800.0).maxCapacityLiters(5000.0).build());
        inventoryRepository.save(FuelInventory.builder().provider(provider3).fuelType(diesel).currentStockLiters(4200.0).maxCapacityLiters(5000.0).build());
        // Provider 4 - Los Angeles
        inventoryRepository.save(FuelInventory.builder().provider(provider4).fuelType(cng).currentStockLiters(3200.0).maxCapacityLiters(5000.0).build());
        inventoryRepository.save(FuelInventory.builder().provider(provider4).fuelType(petrol).currentStockLiters(4600.0).maxCapacityLiters(5000.0).build());
        // Provider 5 - Chicago
        inventoryRepository.save(FuelInventory.builder().provider(provider5).fuelType(diesel).currentStockLiters(4100.0).maxCapacityLiters(5000.0).build());
        inventoryRepository.save(FuelInventory.builder().provider(provider5).fuelType(electric).currentStockLiters(3900.0).maxCapacityLiters(5000.0).build());
        // Provider 6 - Chicago
        inventoryRepository.save(FuelInventory.builder().provider(provider6).fuelType(petrol).currentStockLiters(4700.0).maxCapacityLiters(5000.0).build());
        inventoryRepository.save(FuelInventory.builder().provider(provider6).fuelType(cng).currentStockLiters(3600.0).maxCapacityLiters(5000.0).build());
        // Provider 7 - San Francisco
        inventoryRepository.save(FuelInventory.builder().provider(provider7).fuelType(diesel).currentStockLiters(4300.0).maxCapacityLiters(5000.0).build());
        inventoryRepository.save(FuelInventory.builder().provider(provider7).fuelType(electric).currentStockLiters(4100.0).maxCapacityLiters(5000.0).build());
        // Provider 8 - San Francisco
        inventoryRepository.save(FuelInventory.builder().provider(provider8).fuelType(petrol).currentStockLiters(4900.0).maxCapacityLiters(5000.0).build());
        inventoryRepository.save(FuelInventory.builder().provider(provider8).fuelType(cng).currentStockLiters(3800.0).maxCapacityLiters(5000.0).build());

        // 5. Historical Orders (to build Linear Regression demand forecasting logs)
        LocalDateTime now = LocalDateTime.now();
        
        // Generate orders spanning the last 10 days
        for (int i = 9; i >= 0; i--) {
            LocalDateTime orderTime = now.minusDays(i);
            
            // Generate some random quantity
            double qty = 50.0 + (Math.random() * 40.0);
            double total = qty * petrol.getBasePricePerLiter();
            
            Order order = Order.builder()
                    .customer(customerUser)
                    .provider(provider1)
                    .fuelType(petrol)
                    .quantityLiters(qty)
                    .totalPrice(total)
                    .deliveryAddress(customerUser.getAddress())
                    .status("DELIVERED")
                    .paymentMethod("CARD")
                    .paymentStatus("PAID")
                    .createdAt(orderTime)
                    .updatedAt(orderTime)
                    .build();
            Order savedOrder = orderRepository.save(order);

            // Save payments
            paymentRepository.save(Payment.builder()
                    .order(savedOrder)
                    .transactionId("TXN-INIT-00" + i)
                    .amount(total)
                    .paymentMethod("CARD")
                    .paymentStatus("PAID")
                    .paymentDate(orderTime)
                    .build());
        }

        // Initialize first run of AI prediction forecasts
        predictionService.predictAllActiveFuels();

        System.out.println("Smart Fuel Database initialized successfully!");
    }
}
