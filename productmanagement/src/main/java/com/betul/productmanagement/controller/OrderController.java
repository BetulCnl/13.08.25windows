package com.betul.productmanagement.controller;

import com.betul.productmanagement.entity.Order;
import com.betul.productmanagement.entity.User;
import com.betul.productmanagement.service.CurrentUserService;
import com.betul.productmanagement.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class OrderController {

    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<Order> checkout() {
        User user = currentUserService.getCurrentUserOrThrow();
        Order order = orderService.checkout(user);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<Page<Order>> listMyOrders(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        User user = currentUserService.getCurrentUserOrThrow();
        return ResponseEntity.ok(orderService.listUserOrders(user, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id, Authentication authentication) {
        Order order = orderService.getOrder(id);
        User current = currentUserService.getCurrentUserOrThrow();
        boolean isOwner = order.getUser().getId().equals(current.getId());
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(order);
    }
} 