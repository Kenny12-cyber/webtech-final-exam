package com.example.employeecrud.controller;

import com.example.employeecrud.errorhandling.ExceptionDetails;
import com.example.employeecrud.errorhandling.RequestException;
import com.example.employeecrud.global.GlobalData;
import com.example.employeecrud.model.Product;
import com.example.employeecrud.service.OrderService;
import com.example.employeecrud.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.ZonedDateTime;
import java.util.Map;

@Controller
public class CartController {

    private final ProductService productService;
    private final OrderService orderService;

    @Autowired
    public CartController(ProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
    }

    @GetMapping("/addToCart/{id}")
    public String addToCart(@PathVariable("id") int id) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new RequestException("Product not found with id: " + id));
        GlobalData.cart.add(product);
        return "redirect:/shop";
    }

    @GetMapping("/cart")
    public String viewCart(Model model) {
        model.addAttribute("cartCount", GlobalData.cart.size());
        model.addAttribute("total", GlobalData.cart.stream().mapToDouble(Product::getPrice).sum());
        model.addAttribute("cart", GlobalData.cart);
        return "cart";
    }

    @GetMapping("/cart/removeItem/{index}")
    public String removeItem(@PathVariable("index") int index) {
        if (index < 0 || index >= GlobalData.cart.size()) {
            throw new RequestException("Invalid cart item index: " + index);
        }
        GlobalData.cart.remove(index);
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkout(Model model) {
        double total = GlobalData.cart.stream().mapToDouble(Product::getPrice).sum();
        model.addAttribute("total", total);
        return "checkout";
    }

    @PostMapping("/payNow")
    public String payNow(Model model) {
        String result = "Payment successful";

        Map<String, String> parameters = Map.of(
                "Item 1", "100",
                "Item 2", "200",
                "Total", "300"
        );

        model.addAttribute("result", result);
        model.addAttribute("parameters", parameters);
        return "orderPlaced";
    }

    @GetMapping("/cart/add/{id}")
    public String addOrderToCart(@PathVariable("id") Long id, Principal principal) {
        orderService.saveOrder(id, principal.getName(), 1);
        return "redirect:/cart";
    }

    @ExceptionHandler(RequestException.class)
    public String handleRequestException(RequestException ex, Model model) {
        ExceptionDetails details = new ExceptionDetails(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                ZonedDateTime.now()
        );
        model.addAttribute("exceptionDetails", details);
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        model.addAttribute("errorTitle", "Unexpected Error");
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
        model.addAttribute("timestamp", ZonedDateTime.now());
        return "error";
    }
}
