package com.betul.productmanagement.service;

import com.betul.productmanagement.entity.CartItem;
import com.betul.productmanagement.entity.Product;
import com.betul.productmanagement.entity.User;
import com.betul.productmanagement.repository.CartItemRepository;
import com.betul.productmanagement.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public List<CartItem> getCartItems(User user) {
        return cartItemRepository.findByUser(user);
    }

    @Transactional
    public CartItem addItem(User user, Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        if (quantity < 1) throw new RuntimeException("Quantity must be at least 1");
        if (quantity > product.getStock()) throw new RuntimeException("Quantity exceeds stock");

        return cartItemRepository.findByUserIdAndProductId(user.getId(), productId)
                .map(existing -> {
                    int newQty = existing.getQuantity() + quantity;
                    if (newQty > product.getStock()) {
                        throw new RuntimeException("Quantity exceeds stock");
                    }
                    existing.setQuantity(newQty);
                    return cartItemRepository.save(existing);
                })
                .orElseGet(() -> {
                    CartItem item = CartItem.builder()
                            .user(user)
                            .product(product)
                            .quantity(quantity)
                            .build();
                    return cartItemRepository.save(item);
                });
    }

    @Transactional
    public CartItem updateQuantity(User user, Long cartItemId, int quantity) {
        CartItem item = cartItemRepository.findByIdAndUserId(cartItemId, user.getId())
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        if (quantity < 1) throw new RuntimeException("Quantity must be at least 1");
        if (quantity > item.getProduct().getStock()) throw new RuntimeException("Quantity exceeds stock");
        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    @Transactional
    public void removeItem(User user, Long cartItemId) {
        CartItem item = cartItemRepository.findByIdAndUserId(cartItemId, user.getId())
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(User user) {
        cartItemRepository.deleteByUser(user);
    }
} 