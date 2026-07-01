package com.smartfuel.controller;

import com.smartfuel.entity.*;
import com.smartfuel.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/provider")
public class ProviderController {

    @Autowired
    private UserService userService;

    @Autowired
    private FuelService fuelService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        User provider = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Provider not found"));

        List<Order> orders = orderService.getOrdersForProvider(provider, PageRequest.of(0, 100)).getContent();

        // Calculate Revenue
        double revenue = orders.stream()
                .filter(o -> o.getStatus().equals("DELIVERED") || o.getStatus().equals("REVIEWED"))
                .mapToDouble(Order::getTotalPrice)
                .sum();

        long activeOrdersCount = orders.stream()
                .filter(o -> !o.getStatus().equals("DELIVERED") && 
                             !o.getStatus().equals("REVIEWED") && 
                             !o.getStatus().equals("REJECTED"))
                .count();

        List<FuelInventory> lowStock = fuelService.getLowStockAlertsForProvider(provider);
        List<FuelInventory> inventory = fuelService.getInventoryForProvider(provider);

        model.addAttribute("revenue", Math.round(revenue * 100.0) / 100.0);
        model.addAttribute("activeOrdersCount", activeOrdersCount);
        model.addAttribute("totalOrdersCount", orders.size());
        model.addAttribute("lowStockAlerts", lowStock);
        model.addAttribute("inventory", inventory);
        model.addAttribute("orders", orders.stream().limit(10).collect(Collectors.toList()));
        model.addAttribute("activePage", "dashboard");

        return "provider/dashboard";
    }

    @GetMapping("/inventory")
    public String inventory(Principal principal, Model model) {
        User provider = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<FuelInventory> inventory = fuelService.getInventoryForProvider(provider);
        List<FuelType> fuelTypes = fuelService.getAllActiveFuelTypes();

        model.addAttribute("inventory", inventory);
        model.addAttribute("fuelTypes", fuelTypes);
        model.addAttribute("activePage", "inventory");
        return "provider/inventory";
    }

    @PostMapping("/inventory")
    public String updateStock(Principal principal,
                              @RequestParam("fuelTypeId") Long fuelTypeId,
                              @RequestParam("currentStock") Double currentStock,
                              @RequestParam("maxCapacity") Double maxCapacity) {
        User provider = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        fuelService.updateInventoryStock(provider, fuelTypeId, currentStock, maxCapacity);
        return "redirect:/provider/inventory?success=true";
    }

    @GetMapping("/prices")
    public String priceManagement(Model model) {
        List<FuelType> fuels = fuelService.getAllFuelTypes();
        model.addAttribute("fuels", fuels);
        model.addAttribute("activePage", "prices");
        return "provider/price-management";
    }

    @PostMapping("/prices")
    public String updatePrice(Principal principal,
                              @RequestParam("fuelTypeId") Long fuelTypeId,
                              @RequestParam("newPrice") Double newPrice) {
        User provider = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        fuelService.updateFuelPrice(fuelTypeId, newPrice, provider);
        return "redirect:/provider/prices?success=true";
    }

    @GetMapping("/orders")
    public String manageOrders(Principal principal,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               Model model) {
        User provider = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Page<Order> orderPage = orderService.getOrdersForProvider(provider,
                PageRequest.of(page, 10, Sort.by("createdAt").descending()));

        // Delivery Agents available to be assigned
        List<User> agents = userService.findAll().stream()
                .filter(u -> u.getRole().getName().equals("ROLE_AGENT") && u.isActive())
                .collect(Collectors.toList());

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("agents", agents);
        model.addAttribute("activePage", "orders");
        return "provider/orders";
    }

    @PostMapping("/order/accept")
    public String acceptOrder(@RequestParam("orderId") Long orderId) {
        try {
            orderService.acceptOrder(orderId);
            return "redirect:/provider/orders?accepted=true";
        } catch (Exception e) {
            return "redirect:/provider/orders?error=" + e.getMessage();
        }
    }

    @PostMapping("/order/reject")
    public String rejectOrder(@RequestParam("orderId") Long orderId) {
        orderService.rejectOrder(orderId);
        return "redirect:/provider/orders?rejected=true";
    }

    @PostMapping("/order/assign-agent")
    public String assignAgent(@RequestParam("orderId") Long orderId,
                              @RequestParam("agentId") Long agentId) {
        try {
            orderService.assignAgent(orderId, agentId);
            return "redirect:/provider/orders?assigned=true";
        } catch (Exception e) {
            return "redirect:/provider/orders?error=" + e.getMessage();
        }
    }
}
