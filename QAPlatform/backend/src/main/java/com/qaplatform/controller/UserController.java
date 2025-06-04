package com.qaplatform.controller;

import com.qaplatform.model.User;
import com.qaplatform.model.Question;
import com.qaplatform.model.UserFollow;
import com.qaplatform.model.Answer;
import com.qaplatform.model.UserBadge;
import com.qaplatform.model.UserCheckin;
import com.qaplatform.service.UserService;
import com.qaplatform.service.QuestionService;
import com.qaplatform.service.OnlineUserService;
import com.qaplatform.utils.JwtUtil;
import com.qaplatform.repository.UserFollowRepository;
import com.qaplatform.repository.AnswerRepository;
import com.qaplatform.repository.UserBadgeRepository;
import com.qaplatform.repository.UserCheckinRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Optional;

/**
 * 用户控制器
 * MVC模式中的Controller层，处理用户相关的HTTP请求
 */
@RestController
@RequestMapping("/users")
@Tag(name = "用户管理", description = "用户相关的API接口")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"})
@Slf4j
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired 
    private QuestionService questionService;
    
    @Autowired
    private UserFollowRepository userFollowRepository;
    
    @Autowired
    private AnswerRepository answerRepository;
    
    @Autowired
    private UserBadgeRepository userBadgeRepository;
    
    @Autowired
    private UserCheckinRepository userCheckinRepository;
    
    @Autowired
    private OnlineUserService onlineUserService;
    
    @Value("${file.upload-dir:./uploads/}")
    private String uploadDir;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户账号")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                false  // 普通用户
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("userId", user.getId());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录验证")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            User user = userService.authenticateUser(request.getUsername(), request.getPassword());
            
            // 生成JWT token
            String token = JwtUtil.generateToken(user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", token);
            response.put("user", user);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
    
    /**
     * 获取用户信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取用户信息", description = "根据ID获取用户详细信息")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/profile")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                            @RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        try {
            Long id = userId != null ? userId : currentUserId;
            User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/profile")
    @Operation(summary = "更新用户信息", description = "更新当前用户的个人信息")
    public ResponseEntity<?> updateProfile(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                           @RequestAttribute(value = "currentUserId", required = false) Long currentUserId,
                                           @Valid @RequestBody UpdateProfileRequest request) {
        try {
            Long id = userId != null ? userId : currentUserId;
            User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 更新个人资料
            if (request.getBio() != null) {
                user.setBio(request.getBio());
            }
            if (request.getAvatar() != null) {
                user.setAvatar(request.getAvatar());
            }
            if (request.getWebsite() != null) {
                user.setWebsite(request.getWebsite());
            }
            if (request.getLocation() != null) {
                user.setLocation(request.getLocation());
            }
            
            User updatedUser = userService.updateUser(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "个人资料更新成功");
            response.put("user", updatedUser);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "个人资料更新失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 更新指定用户信息
     */
    @PutMapping("/{id}/profile")
    @Operation(summary = "更新指定用户信息", description = "更新指定用户的个人信息")
    public ResponseEntity<?> updateUserProfile(@PathVariable Long id,
                                            @Valid @RequestBody UpdateProfileRequest request) {
        try {
            User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 更新个人资料
            if (request.getBio() != null) {
                user.setBio(request.getBio());
            }
            if (request.getAvatar() != null) {
                user.setAvatar(request.getAvatar());
            }
            if (request.getWebsite() != null) {
                user.setWebsite(request.getWebsite());
            }
            if (request.getLocation() != null) {
                user.setLocation(request.getLocation());
            }
            
            User updatedUser = userService.updateUser(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "个人资料更新成功");
            response.put("user", updatedUser);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "个人资料更新失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    @Operation(summary = "修改密码", description = "修改用户密码")
    public ResponseEntity<?> changePassword(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                            @RequestAttribute(value = "currentUserId", required = false) Long currentUserId,
                                            @Valid @RequestBody ChangePasswordRequest request) {
        try {
            String oldPassword = request.getOldPassword();
            String newPassword = request.getNewPassword();
            
            Long id = userId != null ? userId : currentUserId;
            boolean success = userService.changePassword(id, oldPassword, newPassword);
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "密码修改成功");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "原密码错误");
                return ResponseEntity.badRequest().body(error);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "密码修改失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 获取用户排行榜
     */
    @GetMapping("/ranking")
    @Operation(summary = "获取用户排行榜", description = "获取声望值最高的用户列表")
    public ResponseEntity<List<User>> getUserRanking(@RequestParam(defaultValue = "10") int limit) {
        List<User> topUsers = userService.getTopUsers(limit);
        return ResponseEntity.ok(topUsers);
    }
    
    /**
     * 搜索用户
     */
    @GetMapping("/search")
    @Operation(summary = "搜索用户", description = "根据关键词搜索用户")
    public ResponseEntity<Page<User>> searchUsers(@RequestParam String keyword, Pageable pageable) {
        Page<User> users = userService.searchUsers(keyword, pageable);
        return ResponseEntity.ok(users);
    }
    
    /**
     * 获取所有用户
     */
    @GetMapping
    @Operation(summary = "获取所有用户", description = "获取用户列表，支持分页和排序")
    public ResponseEntity<Page<UserSummary>> getAllUsers(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "reputation") String sort,
            @RequestParam(defaultValue = "") String role,
            Pageable pageable) {
        
        try {
            List<User> allUsers = userService.getAllUsers();
            
            // 转换为UserSummary并应用过滤器
            List<UserSummary> userSummaries = allUsers.stream()
                .filter(user -> search.isEmpty() || 
                    user.getUsername().toLowerCase().contains(search.toLowerCase()) ||
                    (user.getBio() != null && user.getBio().toLowerCase().contains(search.toLowerCase())))
                .filter(user -> role.isEmpty() || user.getRole().toString().equalsIgnoreCase(role))
                .map(user -> {
                    UserSummary summary = new UserSummary();
                    summary.setId(user.getId());
                    summary.setUsername(user.getUsername());
                    summary.setEmail(user.getEmail());
                    summary.setAvatar(user.getAvatar());
                    summary.setBio(user.getBio());
                    summary.setReputation(user.getReputation());
                    summary.setRole(user.getRole().toString());
                    summary.setCreatedAt(user.getCreatedAt());
                    summary.setIsOnline(onlineUserService.isUserOnline(user.getId()));
                    summary.setQuestionCount(0);
                    summary.setAnswerCount(0);
                    summary.setAcceptedAnswers(0);
                    summary.setFollowersCount(0);
                    return summary;
                })
                .collect(Collectors.toList());
        
        // 应用排序
        switch (sort.toLowerCase()) {
            case "newest":
                userSummaries.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
                break;
            case "activity":
                userSummaries.sort((a, b) -> Integer.compare(
                    b.getQuestionCount() + b.getAnswerCount(),
                    a.getQuestionCount() + a.getAnswerCount()));
                break;
            case "questions":
                userSummaries.sort((a, b) -> Integer.compare(b.getQuestionCount(), a.getQuestionCount()));
                break;
            case "answers":
                userSummaries.sort((a, b) -> Integer.compare(b.getAnswerCount(), a.getAnswerCount()));
                break;
            default: // reputation
                userSummaries.sort((a, b) -> Integer.compare(b.getReputation(), a.getReputation()));
        }
        
        Page<UserSummary> page = new PageImpl<>(userSummaries, pageable, userSummaries.size());
        return ResponseEntity.ok(page);
        
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            List<UserSummary> emptyList = new ArrayList<>();
            Page<UserSummary> emptyPage = new PageImpl<>(emptyList, pageable, 0);
            return ResponseEntity.ok(emptyPage);
        }
    }

    private List<UserSummary> createMockUsers() {
        // 这个方法已经不再使用，保留以防需要
        return new ArrayList<>();
    }

    @Data
    public static class UserSummary {
        private Long id;
        private String username;
        private String email;
        private String avatar;
        private String bio;
        private Integer reputation;
        private String role;
        private LocalDateTime createdAt;
        private Boolean isOnline;
        private Integer questionCount;
        private Integer answerCount;
        private Integer acceptedAnswers;
        private Integer followersCount;
    }
    
    // 请求对象类
    @Data
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 20, message = "用户名长度必须在3-20之间")
        private String username;
        
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, message = "密码长度至少6位")
        private String password;
        
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;
    }
    
    @Data
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        
        @NotBlank(message = "密码不能为空")
        private String password;
    }
    
    @Data
    public static class UpdateProfileRequest {
        @Size(max = 500, message = "个人简介不能超过500字")
        private String bio;
        
        private String avatar;
        
        private String website;
        
        private String location;
    }
    
    @Data
    public static class ChangePasswordRequest {
        @NotBlank(message = "原密码不能为空")
        private String oldPassword;
        
        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, message = "密码长度至少6位")
        private String newPassword;
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/{id}/avatar")
    @Operation(summary = "上传用户头像")
    @Transactional
    public ResponseEntity<?> uploadAvatar(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        
        try {
            // 验证文件
            if (file.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "文件不能为空");
                return ResponseEntity.badRequest().body(error);
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "只支持JPG和PNG格式的图片");
                return ResponseEntity.badRequest().body(error);
            }
            
            // 验证文件大小（2MB）
            if (file.getSize() > 2 * 1024 * 1024) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "文件大小不能超过2MB");
                return ResponseEntity.badRequest().body(error);
            }
            
            // 创建上传目录 - 使用绝对路径
            String projectDir = System.getProperty("user.dir");
            String avatarUploadDir = projectDir + "/uploads/avatars";
            File dir = new File(avatarUploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = "avatar_" + id + "_" + System.currentTimeMillis() + extension;
            
            // 保存文件
            File targetFile = new File(avatarUploadDir, filename);
            file.transferTo(targetFile);
            
            log.info("头像文件已保存到: {}", targetFile.getAbsolutePath());
            
            // 生成访问URL
            String avatarUrl = "/api/uploads/avatars/" + filename;
            
            // 更新用户头像
            User user = userService.updateAvatar(id, avatarUrl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "头像上传成功");
            response.put("url", avatarUrl);
            response.put("user", user);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("头像上传失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "头像上传失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 获取用户的问题列表
     */
    @GetMapping("/{id}/questions")
    @Operation(summary = "获取用户问题列表", description = "获取指定用户发布的所有问题")
    public ResponseEntity<Page<Question>> getUserQuestions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        try {
            // 验证用户是否存在
            userService.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 构建排序对象
            Sort.Direction direction = Sort.Direction.fromString(sortDirection);
            Sort sort = Sort.by(direction, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // 获取用户的问题
            Page<Question> questions = questionService.findByAuthorId(id, pageable);
            
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            log.error("获取用户问题失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/{id}/stats")
    @Operation(summary = "获取用户统计信息", description = "获取用户的问题数、回答数等统计信息")
    public ResponseEntity<Map<String, Object>> getUserStats(@PathVariable Long id) {
        try {
            // 验证用户是否存在
            userService.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 统计用户数据
            long questionCount = questionService.countByAuthorId(id);
            long answerCount = answerRepository.countByAuthorId(id);
            long acceptedAnswers = answerRepository.countAcceptedAnswersByAuthorId(id);
            long followersCount = userFollowRepository.countFollowersByUserId(id);
            long followingCount = userFollowRepository.countFollowingByUserId(id);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("questionCount", questionCount);
            stats.put("answerCount", answerCount);
            stats.put("acceptedAnswers", acceptedAnswers);
            stats.put("followersCount", followersCount);
            stats.put("followingCount", followingCount);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("获取用户统计信息失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 关注用户
     */
    @PostMapping("/{id}/follow")
    @Operation(summary = "关注用户", description = "关注指定用户")
    @Transactional
    public ResponseEntity<Map<String, Object>> followUser(
            @PathVariable Long id,
            @RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (currentUserId == null) {
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            if (currentUserId.equals(id)) {
                response.put("success", false);
                response.put("message", "不能关注自己");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 检查用户是否存在
            User targetUser = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            User currentUser = userService.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("当前用户不存在"));
            
            // 检查是否已经关注
            boolean alreadyFollowing = userFollowRepository.existsByFollowerIdAndFollowingId(currentUserId, id);
            if (alreadyFollowing) {
                response.put("success", false);
                response.put("message", "已经关注了该用户");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 创建关注关系
            UserFollow userFollow = new UserFollow(currentUser, targetUser);
            userFollowRepository.save(userFollow);
            
            response.put("success", true);
            response.put("message", "关注成功");
            response.put("isFollowing", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("关注用户失败", e);
            response.put("success", false);
            response.put("message", "关注失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 取消关注用户
     */
    @DeleteMapping("/{id}/follow")
    @Operation(summary = "取消关注用户", description = "取消关注指定用户")
    @Transactional
    public ResponseEntity<Map<String, Object>> unfollowUser(
            @PathVariable Long id,
            @RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (currentUserId == null) {
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // 检查关注关系是否存在
            boolean isFollowing = userFollowRepository.existsByFollowerIdAndFollowingId(currentUserId, id);
            if (!isFollowing) {
                response.put("success", false);
                response.put("message", "未关注该用户");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 删除关注关系
            userFollowRepository.deleteByFollowerIdAndFollowingId(currentUserId, id);
            
            response.put("success", true);
            response.put("message", "取消关注成功");
            response.put("isFollowing", false);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("取消关注失败", e);
            response.put("success", false);
            response.put("message", "取消关注失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取关注状态
     */
    @GetMapping("/{id}/follow-status")
    @Operation(summary = "获取关注状态", description = "获取当前用户是否关注了指定用户")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getFollowStatus(
            @PathVariable Long id,
            @RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (currentUserId == null) {
                response.put("isFollowing", false);
                response.put("message", "未登录");
                return ResponseEntity.ok(response);
            }
            
            boolean isFollowing = userFollowRepository.existsByFollowerIdAndFollowingId(currentUserId, id);
            response.put("isFollowing", isFollowing);
            response.put("success", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取关注状态失败", e);
            response.put("success", false);
            response.put("message", "获取关注状态失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取用户的粉丝列表
     */
    @GetMapping("/{id}/followers")
    @Operation(summary = "获取用户粉丝列表", description = "获取关注该用户的所有用户")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<User>> getUserFollowers(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            // 验证用户是否存在
            userService.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
            
            Pageable pageable = PageRequest.of(page, size);
            Page<UserFollow> userFollowPage = userFollowRepository.findFollowersByUserId(id, pageable);
            
            // 将 UserFollow 转换为 User
            List<User> followers = userFollowPage.getContent().stream()
                    .map(UserFollow::getFollower)
                    .collect(Collectors.toList());
            
            Page<User> result = new PageImpl<>(followers, pageable, userFollowPage.getTotalElements());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取粉丝列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取用户关注的人列表
     */
    @GetMapping("/{id}/following")
    @Operation(summary = "获取用户关注列表", description = "获取该用户关注的所有用户")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<User>> getUserFollowing(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            // 验证用户是否存在
            userService.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
            
            Pageable pageable = PageRequest.of(page, size);
            Page<UserFollow> userFollowPage = userFollowRepository.findFollowingByUserId(id, pageable);
            
            // 将 UserFollow 转换为 User
            List<User> following = userFollowPage.getContent().stream()
                    .map(UserFollow::getFollowing)
                    .collect(Collectors.toList());
            
            Page<User> result = new PageImpl<>(following, pageable, userFollowPage.getTotalElements());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取关注列表失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取用户的回答列表
     */
    @GetMapping("/{id}/answers")
    @Operation(summary = "获取用户回答列表", description = "获取指定用户的所有回答")
    public ResponseEntity<Page<Answer>> getUserAnswers(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        try {
            // 验证用户是否存在
            userService.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 使用预加载问题信息的查询方法
            List<Answer> allAnswers = answerRepository.findByAuthorIdWithQuestionAndAuthorSorted(id, sortBy, sortDirection);
            
            // 手动实现分页
            int start = page * size;
            int end = Math.min(start + size, allAnswers.size());
            
            List<Answer> pageContent = start > allAnswers.size() ? 
                java.util.Collections.emptyList() : allAnswers.subList(start, end);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Answer> answers = new PageImpl<>(pageContent, pageable, allAnswers.size());
            
            return ResponseEntity.ok(answers);
        } catch (Exception e) {
            log.error("获取用户回答失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取用户草稿列表
     */
    @GetMapping("/drafts")
    @Operation(summary = "获取用户草稿列表", description = "获取当前用户的所有草稿")
    public ResponseEntity<?> getUserDrafts(@RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        try {
            // 这里暂时返回空列表，实际项目中可以从数据库获取
            List<Map<String, Object>> drafts = new ArrayList<>();
            return ResponseEntity.ok(drafts);
        } catch (Exception e) {
            log.error("获取用户草稿失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取草稿失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 保存草稿
     */
    @PostMapping("/drafts")
    @Operation(summary = "保存草稿", description = "保存问题草稿")
    public ResponseEntity<?> saveDraft(
            @RequestBody Map<String, Object> draftData,
            @RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        try {
            // 这里暂时只返回成功响应，实际项目中可以保存到数据库
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "草稿保存成功");
            response.put("draftId", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("保存草稿失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "保存草稿失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 删除草稿
     */
    @DeleteMapping("/drafts/{draftId}")
    @Operation(summary = "删除草稿", description = "删除指定的草稿")
    public ResponseEntity<?> deleteDraft(
            @PathVariable Long draftId,
            @RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        try {
            // 这里暂时只返回成功响应，实际项目中可以从数据库删除
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "草稿删除成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除草稿失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "删除草稿失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // 获取用户徽章
    @GetMapping("/{id}/badges")
    public ResponseEntity<List<Map<String, Object>>> getUserBadges(@PathVariable Long id) {
        try {
            List<UserBadge> userBadges = userBadgeRepository.findByUserId(id);
            
            List<Map<String, Object>> badges = userBadges.stream().map(badge -> {
                Map<String, Object> badgeInfo = new HashMap<>();
                badgeInfo.put("id", badge.getId());
                badgeInfo.put("name", badge.getBadgeName());
                badgeInfo.put("description", badge.getBadgeDescription());
                badgeInfo.put("level", badge.getBadgeLevel());
                badgeInfo.put("icon", badge.getBadgeIcon());
                badgeInfo.put("earnedAt", badge.getEarnedAt());
                badgeInfo.put("earnedReason", badge.getEarnedReason());
                return badgeInfo;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(badges);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // ================ 签到功能API ================
    
    /**
     * 每日签到
     */
    @PostMapping("/checkin")
    @Operation(summary = "每日签到", description = "用户每日签到，获得声望奖励")
    @Transactional
    public ResponseEntity<Map<String, Object>> dailyCheckin(
            @RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (currentUserId == null) {
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Map<String, Object> result = userService.dailyCheckin(currentUserId);
            return ResponseEntity.ok(result);
            
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("签到失败", e);
            response.put("success", false);
            response.put("message", "签到失败，请稍后重试");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 检查签到状态
     */
    @GetMapping("/checkin/status")
    @Operation(summary = "检查签到状态", description = "检查用户今天是否已经签到")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getCheckinStatus(
            @RequestAttribute(value = "currentUserId", required = false) Long currentUserId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (currentUserId == null) {
                response.put("success", false);
                response.put("message", "请先登录");
                response.put("hasCheckedIn", false);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // 检查今天是否已签到
            LocalDate today = LocalDate.now();
            Optional<UserCheckin> todayCheckin = userCheckinRepository.findByUserIdAndCheckinDate(currentUserId, today);
            
            response.put("success", true);
            response.put("hasCheckedIn", todayCheckin.isPresent());
            
            if (todayCheckin.isPresent()) {
                UserCheckin checkin = todayCheckin.get();
                response.put("checkinDate", checkin.getCheckinDate());
                response.put("consecutiveDays", checkin.getConsecutiveDays());
                response.put("reputationEarned", checkin.getReputationEarned());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("检查签到状态失败", e);
            response.put("success", false);
            response.put("message", "检查签到状态失败");
            response.put("hasCheckedIn", false);
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 检查指定用户今日签到状态
     */
    @GetMapping("/{id}/checkin/status")
    @Operation(summary = "检查指定用户签到状态", description = "检查指定用户今天是否已经签到")
    public ResponseEntity<Map<String, Object>> getUserCheckinStatus(@PathVariable Long id) {
        try {
            // 验证用户是否存在
            userService.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
            
            boolean hasCheckedIn = userService.hasCheckinToday(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("hasCheckedInToday", hasCheckedIn);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取用户签到状态失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取签到状态失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 获取用户签到统计
     */
    @GetMapping("/{id}/checkin/stats")
    @Operation(summary = "获取签到统计", description = "获取用户的签到统计信息")
    public ResponseEntity<Map<String, Object>> getUserCheckinStats(@PathVariable Long id) {
        try {
            // 验证用户是否存在
            userService.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
            
            Map<String, Object> stats = userService.getUserCheckinStats(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取签到统计失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取签到统计失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 获取用户签到历史
     */
    @GetMapping("/{id}/checkin/history")
    @Operation(summary = "获取签到历史", description = "获取用户的签到历史记录")
    public ResponseEntity<Map<String, Object>> getUserCheckinHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        
        try {
            // 验证用户是否存在
            userService.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
            
            List<UserCheckin> allHistory = userService.getUserCheckinHistory(id);
            
            // 手动分页
            int start = page * size;
            int end = Math.min(start + size, allHistory.size());
            List<UserCheckin> pageContent = start > allHistory.size() ? 
                java.util.Collections.emptyList() : allHistory.subList(start, end);
            
            // 转换为简化的数据结构，避免序列化User对象
            List<Map<String, Object>> simplifiedHistory = pageContent.stream()
                .map(checkin -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", checkin.getId());
                    item.put("checkinDate", checkin.getCheckinDate().toString());
                    item.put("consecutiveDays", checkin.getConsecutiveDays());
                    item.put("reputationEarned", checkin.getReputationEarned());
                    item.put("createdAt", checkin.getCreatedAt().toString());
                    return item;
                })
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", simplifiedHistory);
            response.put("totalElements", allHistory.size());
            response.put("totalPages", (int) Math.ceil((double) allHistory.size() / size));
            response.put("currentPage", page);
            response.put("pageSize", size);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取签到历史失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 重置今日签到状态（仅用于测试和调试）
     */
    @DeleteMapping("/{id}/checkin/today")
    @Operation(summary = "重置今日签到状态", description = "删除今天的签到记录（仅用于测试）")
    public ResponseEntity<Map<String, Object>> resetTodayCheckin(@PathVariable Long id) {
        try {
            // 验证用户是否存在
            userService.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 删除今天的签到记录
            boolean deleted = userService.deleteTodayCheckin(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", deleted ? "今日签到记录已重置" : "今日无签到记录");
            response.put("deleted", deleted);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("重置签到状态失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "重置失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * 清理所有今日签到记录（临时调试用）
     */
    @DeleteMapping("/checkin/today/all")
    @Operation(summary = "清理所有今日签到记录", description = "删除所有用户今天的签到记录（仅用于调试）")
    @Transactional
    public ResponseEntity<Map<String, Object>> clearAllTodayCheckins() {
        try {
            // 直接执行SQL删除今天的所有签到记录
            int deletedCount = userCheckinRepository.deleteByCheckinDate(java.time.LocalDate.now());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "已清理 " + deletedCount + " 条今日签到记录");
            response.put("deletedCount", deletedCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("清理今日签到记录失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "清理失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 测试签到逻辑（临时）
     */
    @PostMapping("/{id}/checkin/test")
    @Operation(summary = "测试签到逻辑", description = "直接调用UserService的签到方法")
    public ResponseEntity<Map<String, Object>> testCheckin(@PathVariable Long id) {
        try {
            Map<String, Object> result = userService.dailyCheckin(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("error", e.getClass().getSimpleName());
            return ResponseEntity.ok(error);
        }
    }

    /**
     * 直接查询签到记录（调试用）
     */
    @GetMapping("/{id}/checkin/raw")
    @Operation(summary = "直接查询签到记录", description = "绕过所有逻辑直接查询数据库")
    public ResponseEntity<Map<String, Object>> rawCheckinQuery(@PathVariable Long id) {
        try {
            Map<String, Object> result = new HashMap<>();
            LocalDate today = LocalDate.now();
            
            // 直接使用JPA查询
            List<UserCheckin> allCheckins = userCheckinRepository.findAll();
            List<UserCheckin> todayCheckins = allCheckins.stream()
                .filter(c -> c.getCheckinDate().equals(today))
                .collect(Collectors.toList());
            
            List<UserCheckin> userCheckins = allCheckins.stream()
                .filter(c -> c.getUser().getId().equals(id))
                .collect(Collectors.toList());
            
            List<UserCheckin> userTodayCheckins = allCheckins.stream()
                .filter(c -> c.getUser().getId().equals(id) && c.getCheckinDate().equals(today))
                .collect(Collectors.toList());
            
            result.put("today", today.toString());
            result.put("userId", id);
            result.put("totalCheckins", allCheckins.size());
            result.put("todayAllCheckins", todayCheckins.stream().map(c -> Map.of(
                "id", c.getId(),
                "userId", c.getUser().getId(),
                "date", c.getCheckinDate()
            )).collect(Collectors.toList()));
            result.put("userAllCheckins", userCheckins.stream().map(c -> Map.of(
                "id", c.getId(),
                "userId", c.getUser().getId(),
                "date", c.getCheckinDate()
            )).collect(Collectors.toList()));
            result.put("userTodayCheckins", userTodayCheckins.stream().map(c -> Map.of(
                "id", c.getId(),
                "userId", c.getUser().getId(),
                "date", c.getCheckinDate()
            )).collect(Collectors.toList()));
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.ok(error);
        }
    }

    /**
     * 测试特定查询方法（调试用）
     */
    @GetMapping("/{id}/checkin/query-test")
    @Operation(summary = "测试特定查询方法", description = "直接测试findByUserIdAndCheckinDate查询")
    public ResponseEntity<Map<String, Object>> testSpecificQuery(@PathVariable Long id) {
        try {
            Map<String, Object> result = new HashMap<>();
            LocalDate today = LocalDate.now();
            
            // 测试我们的查询方法
            Optional<UserCheckin> queryResult = userCheckinRepository.findByUserIdAndCheckinDate(id, today);
            
            result.put("userId", id);
            result.put("today", today.toString());
            result.put("queryResult", queryResult.isPresent() ? Map.of(
                "found", true,
                "id", queryResult.get().getId(),
                "userId", queryResult.get().getUser().getId(),
                "date", queryResult.get().getCheckinDate()
            ) : Map.of("found", false));
            
            // 同时测试新的查询方法
            Optional<UserCheckin> latestResult = userCheckinRepository.findFirstByUserIdOrderByCheckinDateDesc(id);
            result.put("latestQueryResult", latestResult.isPresent() ? Map.of(
                "found", true,
                "id", latestResult.get().getId(),
                "userId", latestResult.get().getUser().getId(),
                "date", latestResult.get().getCheckinDate()
            ) : Map.of("found", false));
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("stackTrace", e.getStackTrace());
            return ResponseEntity.ok(error);
        }
    }

    /**
     * 简化签到（调试用）
     */
    @PostMapping("/{id}/checkin/simple")
    @Operation(summary = "简化签到", description = "去掉所有复杂逻辑的简单签到")
    public ResponseEntity<Map<String, Object>> simpleCheckin(@PathVariable Long id) {
        try {
            Map<String, Object> result = new HashMap<>();
            LocalDate today = LocalDate.now();
            
            // 检查用户是否存在
            Optional<User> userOpt = userService.findById(id);
            if (!userOpt.isPresent()) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return ResponseEntity.ok(result);
            }
            User user = userOpt.get();
            
            // 检查今天是否已签到
            Optional<UserCheckin> todayCheckin = userCheckinRepository.findByUserIdAndCheckinDate(id, today);
            if (todayCheckin.isPresent()) {
                result.put("success", false);
                result.put("message", "今天已经签到过了");
                result.put("existingCheckin", Map.of(
                    "id", todayCheckin.get().getId(),
                    "date", todayCheckin.get().getCheckinDate()
                ));
                return ResponseEntity.ok(result);
            }
            
            // 创建签到记录
            UserCheckin checkin = new UserCheckin();
            checkin.setUser(user);
            checkin.setCheckinDate(today);
            checkin.setConsecutiveDays(1);
            checkin.setReputationEarned(10);
            
            UserCheckin saved = userCheckinRepository.save(checkin);
            
            // 更新用户声望
            userService.updateReputation(id, 10);
            
            // 重新获取用户以获得最新的声望值
            User updatedUser = userService.findById(id).orElse(user);
            
            result.put("success", true);
            result.put("message", "签到成功");
            result.put("checkinId", saved.getId());
            result.put("newReputation", updatedUser.getReputation());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "签到失败: " + e.getMessage());
            error.put("error", e.getClass().getSimpleName());
            return ResponseEntity.ok(error);
        }
    }

    /**
     * 获取在线用户统计
     */
    @GetMapping("/online-stats")
    public ResponseEntity<Map<String, Object>> getOnlineStats() {
        return ResponseEntity.ok(onlineUserService.getOnlineStats());
    }
    
    /**
     * 更新用户活跃状态
     */
    @PostMapping("/{id}/activity")
    public ResponseEntity<String> updateUserActivity(@PathVariable Long id) {
        onlineUserService.updateUserActivity(id);
        return ResponseEntity.ok("Activity updated");
    }
    
    /**
     * 测试关注功能API
     */
    @GetMapping("/test/follow-system")
    @Operation(summary = "测试关注系统", description = "测试关注系统是否正常工作")
    public ResponseEntity<Map<String, Object>> testFollowSystem() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 测试基本的数据库连接和Repository
            long totalFollows = userFollowRepository.count();
            
            response.put("success", true);
            response.put("message", "关注系统测试通过");
            response.put("totalFollows", totalFollows);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("关注系统测试失败", e);
            response.put("success", false);
            response.put("message", "关注系统测试失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 测试JWT认证API
     */
    @GetMapping("/test/jwt-auth")
    @Operation(summary = "测试JWT认证", description = "测试JWT认证是否正常工作")
    public ResponseEntity<Map<String, Object>> testJwtAuth(
            @RequestAttribute(value = "currentUserId", required = false) Long currentUserId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("success", true);
            response.put("message", "JWT认证测试");
            response.put("authHeader", authHeader);
            response.put("currentUserId", currentUserId);
            response.put("hasAuth", authHeader != null);
            response.put("hasUserId", currentUserId != null);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("JWT认证测试失败", e);
            response.put("success", false);
            response.put("message", "JWT认证测试失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 生成测试Token
     */
    @GetMapping("/test/generate-token")
    @Operation(summary = "生成测试Token", description = "为调试生成JWT Token")
    public ResponseEntity<Map<String, Object>> generateTestToken(
            @RequestParam(defaultValue = "1") Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = JwtUtil.generateToken(userId);
            
            response.put("success", true);
            response.put("message", "测试Token生成成功");
            response.put("token", token);
            response.put("userId", userId);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("生成测试Token失败", e);
            response.put("success", false);
            response.put("message", "生成测试Token失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 