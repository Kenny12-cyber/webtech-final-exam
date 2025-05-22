package com.example.employeecrud.controller;

import com.example.employeecrud.dto.ProductDto;
import com.example.employeecrud.errorhandling.RequestException;
import com.example.employeecrud.model.Category;
import com.example.employeecrud.model.Product;
import com.example.employeecrud.model.User;
import com.example.employeecrud.repository.UserRepository;
import com.example.employeecrud.service.CategoryService;
import com.example.employeecrud.service.ProductService;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.opencsv.CSVWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/src/main/resources/static/imageProduct";

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String adminHome() {
        return "adminHome";
    }

    // ######## Category Section ########

    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategory());
        return "categories";
    }

    @GetMapping("/categories/add")
    public String addCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "categoriesAdd";
    }

    @PostMapping("/categories/add")
    public String addCategory(@ModelAttribute("category") Category category) {
        categoryService.addCategory(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable int id) {
        if (categoryService.getCategoryById(id).isEmpty()) {
            throw new RequestException("Category not found with id: " + id);
        }
        categoryService.deleteCategoryById(id);
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/update/{id}")
    public String updateCategory(@PathVariable int id, Model model) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new RequestException("Category not found with id: " + id));
        model.addAttribute("category", category);
        return "categoriesAdd";
    }

    // ######## Product Section ########

    @GetMapping("/products")
    public String listProducts(Model model) {
        model.addAttribute("products", productService.getAllProduct());
        return "products";
    }

    @GetMapping("/products/add")
    public String addProductForm(Model model) {
        model.addAttribute("productDTO", new ProductDto());
        model.addAttribute("categories", categoryService.getAllCategory());
        return "productsAdd";
    }

    @PostMapping("/products/add")
    public String addProduct(@ModelAttribute("productDTO") ProductDto productDTO,
                             @RequestParam("imageProduct") MultipartFile file,
                             @RequestParam("imgName") String imgName) throws IOException {

        Product product = convertDtoToProduct(productDTO, file, imgName);
        productService.addProduct(product);
        return "redirect:/admin/products";
    }

    @GetMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable int id) {
        if (productService.getProductById(id).isEmpty()) {
            throw new RequestException("Product not found with id: " + id);
        }
        productService.deleteProductById(id);
        return "redirect:/products";
    }

    @GetMapping("/product/update/{id}")
    public String updateProductForm(@PathVariable long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new RequestException("Product not found with id: " + id));
        ProductDto dto = convertProductToDto(product);

        model.addAttribute("productDTO", dto);
        model.addAttribute("categories", categoryService.getAllCategory());
        return "productsAdd";
    }

    // ######## User Section ########

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "users";
    }

    @GetMapping("/users/pdf")
    public void exportUsersToPdf(HttpServletResponse response) throws DocumentException, IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=users_list.pdf");

        List<User> users = userRepository.findAll();
        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        document.add(new Paragraph("Users List\n\n"));

        for (User user : users) {
            document.add(new Paragraph("ID: " + user.getId()));
            document.add(new Paragraph("First Name: " + user.getFirstName()));
            document.add(new Paragraph("Last Name: " + user.getLastName()));
            document.add(new Paragraph("Email: " + user.getEmail()));
            document.add(new Paragraph(" "));
        }

        document.close();
    }

    @GetMapping("/users/csv")
    public void exportUsersToCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=users_list.csv");

        List<User> users = userRepository.findAll();

        try (CSVWriter writer = new CSVWriter(response.getWriter())) {
            writer.writeNext(new String[]{"ID", "First Name", "Last Name", "Email"});
            for (User user : users) {
                writer.writeNext(new String[]{
                        String.valueOf(user.getId()),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail()
                });
            }
        }
    }

    // ######## Helper Methods ########

    private Product convertDtoToProduct(ProductDto dto, MultipartFile file, String imgName) throws IOException {
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setCategory(categoryService.getCategoryById(dto.getCategoryId())
                .orElseThrow(() -> new RequestException("Category not found with id: " + dto.getCategoryId())));
        product.setPrice(dto.getPrice());
        product.setWeight(dto.getWeight());
        product.setDescription(dto.getDescription());

        String imageName = imgName;
        if (!file.isEmpty()) {
            imageName = file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR, imageName);
            Files.write(path, file.getBytes());
        }

        product.setImageName(imageName);
        return product;
    }

    private ProductDto convertProductToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setCategoryId(product.getCategory().getId());
        dto.setPrice(product.getPrice());
        dto.setWeight(product.getWeight());
        dto.setDescription(product.getDescription());
        dto.setImageName(product.getImageName());
        return dto;
    }
}
