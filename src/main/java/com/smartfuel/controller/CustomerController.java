package com.smartfuel.controller;

import com.smartfuel.entity.*;
import com.smartfuel.service.*;
import com.smartfuel.dto.NearbyProviderDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private UserService userService;

    @Autowired
    private FuelService fuelService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PdfInvoiceService pdfInvoiceService;

    @Autowired
    private LocationService locationService;

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        User customer = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Order> orders = orderService.getOrdersForCustomer(customer, PageRequest.of(0, 100)).getContent();
        
        // Compute metrics
        double totalSpent = orders.stream()
                .filter(o -> o.getStatus().equals("DELIVERED") || o.getStatus().equals("REVIEWED"))
                .mapToDouble(Order::getTotalPrice)
                .sum();

        double totalLiters = orders.stream()
                .filter(o -> o.getStatus().equals("DELIVERED") || o.getStatus().equals("REVIEWED"))
                .mapToDouble(Order::getQuantityLiters)
                .sum();

        List<Order> activeOrders = orders.stream()
                .filter(o -> !o.getStatus().equals("DELIVERED") && 
                             !o.getStatus().equals("REVIEWED") && 
                             !o.getStatus().equals("REJECTED"))
                .collect(Collectors.toList());

        List<FuelType> fuelPrices = fuelService.getAllActiveFuelTypes();

        model.addAttribute("customer", customer);
        model.addAttribute("totalSpent", Math.round(totalSpent * 100.0) / 100.0);
        model.addAttribute("totalLiters", Math.round(totalLiters * 100.0) / 100.0);
        model.addAttribute("activeOrders", activeOrders);
        model.addAttribute("fuelPrices", fuelPrices);
        model.addAttribute("activePage", "dashboard");

        return "customer/dashboard";
    }

    @GetMapping("/order-fuel")
    public String orderFuelForm(Principal principal, Model model) {
        User customer = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        List<FuelType> fuelTypes = fuelService.getAllActiveFuelTypes();
        // Providers are users with ROLE_PROVIDER
        List<User> providers = userService.findAll().stream()
                .filter(u -> u.getRole() != null)
                .filter(u -> "ROLE_PROVIDER".equals(u.getRole().getName()))
                .collect(Collectors.toList());

        model.addAttribute("customer", customer);
        model.addAttribute("fuelTypes", fuelTypes);
        model.addAttribute("providers", providers);
        model.addAttribute("activePage", "order-fuel");
        return "customer/order-fuel";
    }

    @PostMapping("/order-fuel")
    public String placeOrder(Principal principal,
                             @RequestParam("providerId") Long providerId,
                             @RequestParam("fuelTypeId") Long fuelTypeId,
                             @RequestParam("quantity") Double quantity,
                             @RequestParam("deliveryAddress") String address,
                             @RequestParam(value = "isEmergency", defaultValue = "false") boolean isEmergency,
                             @RequestParam("paymentMethod") String paymentMethod,
                             @RequestParam(value = "latitude", required = false) Double latitude,
                             @RequestParam(value = "longitude", required = false) Double longitude) {

        User customer = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Save current live location
        if (latitude != null && longitude != null) {
            customer.setLatitude(latitude);
            customer.setLongitude(longitude);

            userService.save(customer); // save updated coordinates
        }

        orderService.placeOrder(
                customer,
                providerId,
                fuelTypeId,
                quantity,
                address,
                isEmergency,
                paymentMethod
        );

        return "redirect:/customer/dashboard?ordered=true";
    }

    @GetMapping("/track-order/{id}")
    public String trackOrder(@PathVariable("id") Long id, Model model) {
        Order order = orderService.getOrderById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        model.addAttribute("order", order);
        model.addAttribute("activePage", "history");
        return "customer/track-order";
    }

    @GetMapping("/history")
    public String orderHistory(Principal principal,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               Model model) {
        User customer = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Page<Order> orderPage = orderService.getOrdersForCustomer(customer, 
                PageRequest.of(page, 5, Sort.by("createdAt").descending()));

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("activePage", "history");
        return "customer/history";
    }

    @GetMapping("/invoice/{orderId}")
    public ResponseEntity<InputStreamResource> downloadInvoice(@PathVariable("orderId") Long orderId) {
        Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        ByteArrayInputStream bis = pdfInvoiceService.generateInvoicePdf(order);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=invoice-" + orderId + ".pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    @PostMapping("/review")
    public String submitReview(@RequestParam("orderId") Long orderId,
                               @RequestParam("rating") Integer rating,
                               @RequestParam("comment") String comment) {
        orderService.submitReview(orderId, rating, comment);
        return "redirect:/customer/dashboard?reviewed=true";
    }

    @GetMapping("/profile")
    public String profileForm(Principal principal, Model model) {
        User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        model.addAttribute("user", user);
        model.addAttribute("activePage", "profile");
        return "customer/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(Principal principal,
                                @RequestParam("fullName") String fullName,
                                @RequestParam("email") String email,
                                @RequestParam("phoneNumber") String phoneNumber,
                                @RequestParam("address") String address) {
        userService.updateProfile(principal.getName(), fullName, email, phoneNumber, address);
        return "redirect:/customer/profile?success=true";
    }

    /**
     * REST API: Find nearby fuel providers
     * Parameters: lat (customer latitude), lon (customer longitude), radius (search radius in km, default 10)
     */
    @GetMapping("/api/nearby-providers")
    @ResponseBody
    public ResponseEntity<?> getNearbyProviders(@RequestParam("lat") Double latitude,
                                                 @RequestParam("lon") Double longitude,
                                                 @RequestParam(value = "radius", defaultValue = "10") Double radius) {
        try {
            if (latitude == null || longitude == null) {
                return ResponseEntity.badRequest().body("Latitude and Longitude are required");
            }

            List<NearbyProviderDto> nearbyProviders = locationService.findNearbyProviders(latitude, longitude, radius);
            return ResponseEntity.ok(nearbyProviders);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error finding nearby providers: " + e.getMessage());
        }
    }

    /**
     * REST API: Get provider distance from customer location
     */
    @GetMapping("/api/provider-distance/{providerId}")
    @ResponseBody
    public ResponseEntity<?> getProviderDistance(@PathVariable("providerId") Long providerId,
                                                  @RequestParam("lat") Double latitude,
                                                  @RequestParam("lon") Double longitude) {
        try {
            NearbyProviderDto provider = locationService.getProviderWithDistance(providerId, latitude, longitude);
            if (provider == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(provider);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error calculating distance: " + e.getMessage());
        }
    }
}
