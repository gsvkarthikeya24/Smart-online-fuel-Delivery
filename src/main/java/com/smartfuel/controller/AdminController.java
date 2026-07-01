package com.smartfuel.controller;

import com.smartfuel.entity.*;
import com.smartfuel.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private FuelService fuelService;

    @Autowired
    private DemandPredictionService predictionService;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<User> users = userService.findAll();
        List<Order> allOrders = orderService.searchAndFilterOrders(null, null, null, null, null, PageRequest.of(0, 1000)).getContent();

        // Compute metrics
        long customerCount = users.stream().filter(u -> u.getRole().getName().equals("ROLE_CUSTOMER")).count();
        long providerCount = users.stream().filter(u -> u.getRole().getName().equals("ROLE_PROVIDER")).count();
        long agentCount = users.stream().filter(u -> u.getRole().getName().equals("ROLE_AGENT")).count();
        
        double totalRevenue = allOrders.stream()
                .filter(o -> o.getStatus().equals("DELIVERED") || o.getStatus().equals("REVIEWED"))
                .mapToDouble(Order::getTotalPrice)
                .sum();

        // Chart Data 1: Fuel Type sales quantity
        Map<String, Double> fuelQuantityMap = new HashMap<>();
        // Chart Data 2: Order status distribution
        Map<String, Integer> statusCountMap = new HashMap<>();

        for (Order o : allOrders) {
            statusCountMap.put(o.getStatus(), statusCountMap.getOrDefault(o.getStatus(), 0) + 1);
            if (o.getStatus().equals("DELIVERED") || o.getStatus().equals("REVIEWED")) {
                String name = o.getFuelType().getName();
                fuelQuantityMap.put(name, fuelQuantityMap.getOrDefault(name, 0.0) + o.getQuantityLiters());
            }
        }

        List<String> fuelLabels = new ArrayList<>(fuelQuantityMap.keySet());
        List<Double> fuelData = new ArrayList<>(fuelQuantityMap.values());

        List<String> statusLabels = new ArrayList<>(statusCountMap.keySet());
        List<Integer> statusData = new ArrayList<>(statusCountMap.values());

        // Simple default data for weekly trend
        List<String> weeklyLabels = Arrays.asList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
        List<Integer> weeklyOrders = Arrays.asList(15, 22, 18, 30, 25, 40, 35);

        model.addAttribute("customerCount", customerCount);
        model.addAttribute("providerCount", providerCount);
        model.addAttribute("agentCount", agentCount);
        model.addAttribute("totalOrders", allOrders.size());
        model.addAttribute("totalRevenue", Math.round(totalRevenue * 100.0) / 100.0);
        
        model.addAttribute("fuelLabels", fuelLabels);
        model.addAttribute("fuelData", fuelData);
        model.addAttribute("statusLabels", statusLabels);
        model.addAttribute("statusData", statusData);
        model.addAttribute("weeklyLabels", weeklyLabels);
        model.addAttribute("weeklyOrders", weeklyOrders);
        model.addAttribute("activePage", "dashboard");

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String userControlPanel(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("activePage", "users");
        return "admin/users";
    }

    @GetMapping("/orders")
    public String systemOrders(@RequestParam(value = "status", required = false) String status,
                               @RequestParam(value = "fuelTypeId", required = false) Long fuelTypeId,
                               @RequestParam(value = "customer", required = false) String customer,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               Model model) {
        Page<Order> orderPage = orderService.searchAndFilterOrders(status, fuelTypeId, customer, null, null, 
                PageRequest.of(page, 10, Sort.by("createdAt").descending()));

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("fuelTypes", fuelService.getAllFuelTypes());
        model.addAttribute("status", status);
        model.addAttribute("fuelTypeId", fuelTypeId);
        model.addAttribute("customer", customer);
        model.addAttribute("activePage", "orders");
        return "admin/orders";
    }

    @GetMapping("/predictions")
    public String predictionsDashboard(Model model) {
        List<DemandPrediction> predictions = predictionService.getLatestPredictions();
        if (predictions.isEmpty()) {
            predictions = predictionService.predictAllActiveFuels();
        }

        model.addAttribute("predictions", predictions);
        model.addAttribute("fuelTypes", fuelService.getAllActiveFuelTypes());
        model.addAttribute("activePage", "predictions");
        return "admin/predictions";
    }

    @PostMapping("/predictions/run")
    public String runPredictions(Principal principal) {
        predictionService.predictAllActiveFuels();
        User admin = userService.findByUsername(principal.getName()).orElse(null);
        auditLogService.log(admin, "AI_DEMAND_PREDICTION", "Manually executed demand forecasts for all fuel types.");
        return "redirect:/admin/predictions?success=true";
    }

    @GetMapping("/logs")
    public String auditLogs(Model model) {
        model.addAttribute("logs", auditLogService.getAllLogs());
        model.addAttribute("activePage", "logs");
        return "admin/audit-logs";
    }
}
