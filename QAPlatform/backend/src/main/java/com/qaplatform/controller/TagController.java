package com.qaplatform.controller;

import com.qaplatform.model.Tag;
import com.qaplatform.repository.TagRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签控制器
 */
@RestController
@RequestMapping("/tags")
@io.swagger.v3.oas.annotations.tags.Tag(name = "标签管理", description = "标签相关操作")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"})
public class TagController {

    @Autowired
    private TagRepository tagRepository;

    /**
     * 获取所有标签
     */
    @GetMapping
    @Operation(summary = "获取标签列表")
    public ResponseEntity<List<Tag>> getAllTags() {
        List<Tag> tags = tagRepository.findAll(Sort.by(Sort.Direction.DESC, "usageCount"));
        return ResponseEntity.ok(tags);
    }

    /**
     * 获取标签（分页）
     */
    @GetMapping("/page")
    @Operation(summary = "获取标签列表（分页）")
    public ResponseEntity<Page<Tag>> getTagsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "usageCount") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Tag> tags = tagRepository.findAll(pageable);
        return ResponseEntity.ok(tags);
    }

    /**
     * 搜索标签
     */
    @GetMapping("/search")
    @Operation(summary = "搜索标签")
    public ResponseEntity<List<Tag>> searchTags(@RequestParam String keyword) {
        // 使用基本的查询方法，如果TagRepository中没有这个方法，我们返回所有标签并在应用层过滤
        List<Tag> allTags = tagRepository.findAll();
        List<Tag> filteredTags = allTags.stream()
            .filter(tag -> tag.getName().toLowerCase().contains(keyword.toLowerCase()))
            .toList();
        return ResponseEntity.ok(filteredTags);
    }

    /**
     * 获取热门标签
     */
    @GetMapping("/popular")
    @Operation(summary = "获取热门标签")
    public ResponseEntity<List<Tag>> getPopularTags(
            @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "usageCount"));
        Page<Tag> tagsPage = tagRepository.findAll(pageable);
        return ResponseEntity.ok(tagsPage.getContent());
    }

    /**
     * 根据ID获取标签
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取标签详情")
    public ResponseEntity<Tag> getTag(@PathVariable Long id) {
        return tagRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}