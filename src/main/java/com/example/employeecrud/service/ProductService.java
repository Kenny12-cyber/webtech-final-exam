package com.example.employeecrud.service;

import com.example.employeecrud.model.Product;
import com.example.employeecrud.repository.CategoryRepository;
import com.example.employeecrud.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    ProductRepository productRepository;
    @Autowired
    CategoryRepository categoryRepository;
    public void addProduct(Product product){productRepository.save(product);}
    public List<Product> getAllProduct(){ return productRepository.findAll();}
    public void deleteProductById(long id){ productRepository.deleteById(id);}
    public Optional<Product> getProductById(long id){return productRepository.findById(id);}
    public List<Product> getAllProductByCategoryId(int id){return productRepository.findAllByCategoryId(id);}
}
