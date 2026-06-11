package org.example.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.project.dto.ApiResponse;
import org.example.project.dto.UserRequest;
import org.example.project.entity.Course;
import org.example.project.entity.User;
import org.example.project.service.CourseService;
import org.example.project.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final CourseService courseService;

    @GetMapping("/users")
    public ApiResponse<Page<User>> getUsers(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.success(userService.getUsers(keyword, pageable));
    }

    @GetMapping("/users/{id}")
    public ApiResponse<User> getUser(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserById(id));
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<User> createUser(@Valid @RequestBody UserRequest request) {
        return ApiResponse.success("Tạo người dùng thành công", userService.createUser(request));
    }

    @PutMapping("/users/{id}")
    public ApiResponse<User> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return ApiResponse.success("Cập nhật người dùng thành công", userService.updateUser(id, request));
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success("Xóa người dùng thành công", null);
    }

    @GetMapping("/courses")
    public ApiResponse<Page<Course>> getCourses(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.success(courseService.getCourses(keyword, pageable));
    }

    @GetMapping("/courses/{id}")
    public ApiResponse<Course> getCourse(@PathVariable Long id) {
        return ApiResponse.success(courseService.getCourseById(id));
    }

    @PostMapping("/courses")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Course> createCourse(@Valid @RequestBody Course course) {
        return ApiResponse.success("Tạo khóa học thành công", courseService.createCourse(course));
    }

    @PutMapping("/courses/{id}")
    public ApiResponse<Course> updateCourse(@PathVariable Long id, @Valid @RequestBody Course request) {
        return ApiResponse.success("Cập nhật khóa học thành công", courseService.updateCourse(id, request));
    }

    @DeleteMapping("/courses/{id}")
    public ApiResponse<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ApiResponse.success("Xóa khóa học thành công", null);
    }
}
