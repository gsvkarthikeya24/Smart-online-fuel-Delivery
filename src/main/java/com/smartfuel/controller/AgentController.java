package com.smartfuel.controller;

import com.smartfuel.entity.DeliveryAgent;
import com.smartfuel.entity.Order;
import com.smartfuel.entity.User;
import com.smartfuel.repository.DeliveryAgentRepository;
import com.smartfuel.service.OrderService;
import com.smartfuel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/agent")
public class AgentController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private DeliveryAgentRepository agentRepository;

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        User agentUser = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Order> allOrders = orderService.getOrdersForAgent(agentUser);
        
        List<Order> activeDeliveries = allOrders.stream()
                .filter(o -> o.getStatus().equals("AGENT_ASSIGNED") || o.getStatus().equals("OUT_FOR_DELIVERY"))
                .collect(Collectors.toList());

        long completedCount = allOrders.stream()
                .filter(o -> o.getStatus().equals("DELIVERED") || o.getStatus().equals("REVIEWED"))
                .count();

        DeliveryAgent agentProfile = agentRepository.findByUser(agentUser)
                .orElse(null);

        model.addAttribute("agentProfile", agentProfile);
        model.addAttribute("activeDeliveries", activeDeliveries);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("totalDeliveries", allOrders.size());
        model.addAttribute("activePage", "dashboard");

        return "agent/dashboard";
    }

    @GetMapping("/orders")
    public String jobsQueue(Principal principal,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            Model model) {
        User agentUser = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Page<Order> orderPage = orderService.getOrdersForAgent(agentUser,
                PageRequest.of(page, 10, Sort.by("createdAt").descending()));

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("activePage", "orders");
        return "agent/orders";
    }

    @PostMapping("/order/accept")
    public String acceptDeliveryJob(@RequestParam("orderId") Long orderId) {
        try {
            orderService.acceptDelivery(orderId);
            return "redirect:/agent/dashboard?accepted=true";
        } catch (Exception e) {
            return "redirect:/agent/dashboard?error=" + e.getMessage();
        }
    }

    @PostMapping("/order/verify-otp")
    public String verifyOtpAndDeliver(@RequestParam("orderId") Long orderId,
                                      @RequestParam("otp") String otp) {
        try {
            boolean success = orderService.verifyOtpAndDeliver(orderId, otp);
            if (success) {
                return "redirect:/agent/dashboard?delivered=true";
            } else {
                return "redirect:/agent/dashboard?error=Invalid OTP Code. Please verify again.";
            }
        } catch (Exception e) {
            return "redirect:/agent/dashboard?error=" + e.getMessage();
        }
    }
}
