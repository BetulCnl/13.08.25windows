package com.betul.productmanagement.service;

import com.betul.productmanagement.entity.CartItem;
import com.betul.productmanagement.entity.Product;
import com.betul.productmanagement.entity.User;
import com.betul.productmanagement.exception.InvalidQuantityException;
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
                .orElseThrow(() -> new InvalidQuantityException("Product not found"));

        if (quantity < 1 || quantity > product.getStock()) {
            throw new InvalidQuantityException(
                    "Quantity must be at least 1 and at most " + product.getStock()
            );
        }

        return cartItemRepository.findByUserIdAndProductId(user.getId(), productId)
                .map(existing -> {
                    int newQty = existing.getQuantity() + quantity;
                    if (newQty > product.getStock()) {
                        throw new InvalidQuantityException(
                                "Quantity exceeds available stock (" + product.getStock() + ")"
                        );
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
                .orElseThrow(() -> new InvalidQuantityException("Cart item not found"));

        int stock = item.getProduct().getStock();
        if (quantity < 1 || quantity > stock) {
            throw new InvalidQuantityException(
                    "Quantity must be at least 1 and at most " + stock
            );
        }

        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    @Transactional
    public void removeItem(User user, Long cartItemId) {
        CartItem item = cartItemRepository.findByIdAndUserId(cartItemId, user.getId())
                .orElseThrow(() -> new InvalidQuantityException("Cart item not found"));
        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(User user) {
        cartItemRepository.deleteByUser(user);
    }
}
