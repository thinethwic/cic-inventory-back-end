package com.cic.inventory.controllers.v1;

import com.cic.inventory.controllers.AbstractController;
import com.cic.inventory.dtos.auth.AuthUserResponse;
import com.cic.inventory.dtos.users.UserCreateRequest;
import com.cic.inventory.dtos.users.UserUpdateRequest;
import com.cic.inventory.security.UserPrincipal;
import com.cic.inventory.services.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController extends AbstractController {

    private final UserManagementService userManagementService;

    @GetMapping
    public ResponseEntity<Page<AuthUserResponse>> getUsers(Pageable pageable) {
        return sendOkResponse(userManagementService.getUsers(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthUserResponse> getUser(@PathVariable Long id) {
        return sendOkResponse(userManagementService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<AuthUserResponse> createUser(
            @Valid @RequestBody UserCreateRequest request,
            Authentication authentication
    ) {
        return sendCreatedResponse(
                userManagementService.createUser(request, (UserPrincipal) authentication.getPrincipal())
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuthUserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request,
            Authentication authentication
    ) {
        return sendOkResponse(
                userManagementService.updateUser(id, request, (UserPrincipal) authentication.getPrincipal())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            Authentication authentication
    ) {
        userManagementService.deleteUser(id, (UserPrincipal) authentication.getPrincipal());
        return sendNoContentResponse();
    }
}
