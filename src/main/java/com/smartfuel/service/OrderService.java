package com.smartfuel.service;

import com.smartfuel.entity.*;
import com.smartfuel.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FuelTypeRepository fuelTypeRepository;

    @Autowired
    private FuelInventoryRepository inventoryRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private DeliveryAgentRepository agentRepository;

    @Autowired
    private OtpVerificationRepository otpVerificationRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public Order placeOrder(User customer, Long providerId, Long fuelTypeId, Double quantity, 
                            String address, boolean isEmergency, String paymentMethod) {
        
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Provider not found"));
        FuelType fuelType = fuelTypeRepository.findById(fuelTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Fuel type not found"));

        // Price calculation: emergency adds 15% surcharge
        double pricePerLiter = fuelType.getBasePricePerLiter();
        double totalPrice = quantity * pricePerLiter;
        if (isEmergency) {
            totalPrice *= 1.15; // 15% emergency surcharge
        }

        // Generate OTP for delivery verification
        String otpCode = generateOtp();

        Order order = Order.builder()
                .customer(customer)
                .provider(provider)
                .fuelType(fuelType)
                .quantityLiters(quantity)
                .totalPrice(totalPrice)
                .deliveryAddress(address)
                .isEmergency(isEmergency)
                .status("CREATED")
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentMethod.equals("COD") ? "PENDING" : "PAID")
                .otp(otpCode)
                .build();

        Order savedOrder = orderRepository.save(order);

        // Save OTP Verification record
        OtpVerification otpVerification = OtpVerification.builder()
                .order(savedOrder)
                .otpCode(otpCode)
                .expiryTime(LocalDateTime.now().plusHours(2)) // 2 hours expiry
                .verified(false)
                .build();
        otpVerificationRepository.save(otpVerification);

        // Process Simulated Payment
        String transactionId = "TXN-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 9000 + 1000);
        Payment payment = Payment.builder()
                .order(savedOrder)
                .transactionId(transactionId)
                .amount(totalPrice)
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentMethod.equals("COD") ? "PENDING" : "PAID")
                .build();
        paymentRepository.save(payment);

        return savedOrder;
    }

    @Transactional
    public Order acceptOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getStatus().equals("CREATED")) {
            throw new IllegalStateException("Only CREATED orders can be accepted");
        }

        // Check inventory and reserve stock
        FuelInventory inventory = inventoryRepository.findByProviderAndFuelType(order.getProvider(), order.getFuelType())
                .orElseThrow(() -> new IllegalStateException("Provider does not stock this fuel type"));

        if (inventory.getCurrentStockLiters() < order.getQuantityLiters()) {
            throw new IllegalStateException("Insufficient fuel stock in provider inventory");
        }

        // Deduct/Reserve stock
        inventory.setCurrentStockLiters(inventory.getCurrentStockLiters() - order.getQuantityLiters());
        inventoryRepository.save(inventory);

        order.setStatus("ACCEPTED");
        return orderRepository.save(order);
    }

    @Transactional
    public Order rejectOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getStatus().equals("CREATED")) {
            throw new IllegalStateException("Only CREATED orders can be rejected");
        }

        order.setStatus("REJECTED");
        return orderRepository.save(order);
    }

    @Transactional
    public Order assignAgent(Long orderId, Long agentUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getStatus().equals("ACCEPTED")) {
            throw new IllegalStateException("Order must be ACCEPTED before assigning an agent");
        }

        User agentUser = userRepository.findById(agentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery agent not found"));
        
        DeliveryAgent agentInfo = agentRepository.findByUser(agentUser)
                .orElseThrow(() -> new IllegalArgumentException("Delivery agent profile not found"));

        if (!agentInfo.getStatus().equals("AVAILABLE")) {
            throw new IllegalStateException("Delivery agent is not currently available");
        }

        agentInfo.setStatus("BUSY");
        agentRepository.save(agentInfo);

        order.setAgent(agentUser);
        order.setStatus("AGENT_ASSIGNED");
        return orderRepository.save(order);
    }

    @Transactional
    public Order acceptDelivery(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getStatus().equals("AGENT_ASSIGNED")) {
            throw new IllegalStateException("Order must be in AGENT_ASSIGNED status");
        }

        order.setStatus("OUT_FOR_DELIVERY");
        return orderRepository.save(order);
    }

    @Transactional
    public boolean verifyOtpAndDeliver(Long orderId, String inputOtp) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getStatus().equals("OUT_FOR_DELIVERY")) {
            throw new IllegalStateException("Order must be OUT_FOR_DELIVERY to be delivered");
        }

        OtpVerification otpVer = otpVerificationRepository.findByOrder(order)
                .orElseThrow(() -> new IllegalStateException("OTP record not found"));

        if (otpVer.isVerified()) {
            return true;
        }

        if (otpVer.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired. Please request a new one.");
        }

        if (!otpVer.getOtpCode().equals(inputOtp)) {
            return false;
        }

        // Verify and update OTP status
        otpVer.setVerified(true);
        otpVerificationRepository.save(otpVer);

        // Update Order
        order.setStatus("DELIVERED");
        if (order.getPaymentMethod().equals("COD")) {
            order.setPaymentStatus("PAID");
            Payment payment = paymentRepository.findByOrder(order)
                    .orElseThrow(() -> new IllegalStateException("Payment record not found"));
            payment.setPaymentStatus("PAID");
            payment.setPaymentDate(LocalDateTime.now());
            paymentRepository.save(payment);
        }
        orderRepository.save(order);

        // Free the Agent
        if (order.getAgent() != null) {
            DeliveryAgent agentInfo = agentRepository.findByUser(order.getAgent())
                    .orElseThrow(() -> new IllegalStateException("Agent profile not found"));
            agentInfo.setStatus("AVAILABLE");
            agentRepository.save(agentInfo);
        }

        // Increase Customer Trust Score for successful delivery
        userService.updateTrustScore(order.getCustomer(), 5);

        return true;
    }

    @Transactional
    public Review submitReview(Long orderId, Integer rating, String comment) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getStatus().equals("DELIVERED")) {
            throw new IllegalStateException("Review can only be submitted for DELIVERED orders");
        }

        Review review = Review.builder()
                .order(order)
                .customer(order.getCustomer())
                .provider(order.getProvider())
                .rating(rating)
                .comment(comment)
                .build();

        Review savedReview = reviewRepository.save(review);

        // Update customer trust score based on their review activity (+3 for positive review)
        if (rating >= 4) {
            userService.updateTrustScore(order.getCustomer(), 3);
        } else if (rating <= 2) {
            // Negative rating given by customer doesn't deduct score from customer,
            // but let's say submitting reviews builds trust.
            userService.updateTrustScore(order.getCustomer(), 1);
        }

        // Update status of order to show it's reviewed
        order.setStatus("REVIEWED");
        orderRepository.save(order);

        return savedReview;
    }

    public Page<Order> getOrdersForCustomer(User customer, Pageable pageable) {
        return orderRepository.findByCustomer(customer, pageable);
    }

    public Page<Order> getOrdersForProvider(User provider, Pageable pageable) {
        return orderRepository.findByProvider(provider, pageable);
    }

    public Page<Order> getOrdersForAgent(User agent, Pageable pageable) {
        return orderRepository.findByAgent(agent, pageable);
    }

    public List<Order> getOrdersForAgent(User agent) {
        return orderRepository.findByAgent(agent);
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Page<Order> searchAndFilterOrders(String status, Long fuelTypeId, String customerName, 
                                            LocalDateTime start, LocalDateTime end, Pageable pageable) {
        LocalDateTime startDt = start != null ? start : LocalDateTime.now().minusYears(1);
        LocalDateTime endDt = end != null ? end : LocalDateTime.now().plusYears(1);
        return orderRepository.searchOrders(
                (status == null || status.isBlank()) ? null : status,
                fuelTypeId,
                (customerName == null || customerName.isBlank()) ? null : customerName,
                startDt,
                endDt,
                pageable
        );
    }

    private String generateOtp() {
        int randomPin = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(randomPin);
    }
}
