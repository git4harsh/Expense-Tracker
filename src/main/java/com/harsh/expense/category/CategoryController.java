package com.harsh.expense.category;

import com.harsh.expense.user.UserRepository;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {
  private final CategoryRepository categories;
  private final UserRepository users;

  public CategoryController(CategoryRepository categories, UserRepository users) {
    this.categories = categories;
    this.users = users;
  }

  @GetMapping
  public List<CategoryDtos.Response> list(@Parameter(hidden = true) Authentication auth) {
    return categories.findAllByOwnerEmailOrderByName(auth.getName()).stream()
        .map(CategoryDtos.Response::from)
        .toList();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CategoryDtos.Response create(
      @Valid @RequestBody CategoryDtos.Request request,
      @Parameter(hidden = true) Authentication auth) {
    var user = users.findByEmailIgnoreCase(auth.getName()).orElseThrow();
    String name = request.name().trim();
    if (categories.existsByOwnerIdAndNameIgnoreCase(user.getId(), name))
      throw new IllegalArgumentException("Category already exists");
    return CategoryDtos.Response.from(categories.save(new Category(name, user)));
  }

  @PutMapping("/{id}")
  public CategoryDtos.Response rename(
      @PathVariable Long id,
      @Valid @RequestBody CategoryDtos.Request request,
      @Parameter(hidden = true) Authentication auth) {
    var category =
        categories
            .findByIdAndOwnerEmail(id, auth.getName())
            .orElseThrow(() -> new IllegalArgumentException("Category not found"));
    String name = request.name().trim();
    if (!category.getName().equalsIgnoreCase(name)
        && categories.existsByOwnerIdAndNameIgnoreCase(category.getOwner().getId(), name)) {
      throw new IllegalArgumentException("Category already exists");
    }
    category.setName(name);
    return CategoryDtos.Response.from(categories.save(category));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id, @Parameter(hidden = true) Authentication auth) {
    var category =
        categories
            .findByIdAndOwnerEmail(id, auth.getName())
            .orElseThrow(() -> new IllegalArgumentException("Category not found"));
    categories.delete(category);
  }
}
