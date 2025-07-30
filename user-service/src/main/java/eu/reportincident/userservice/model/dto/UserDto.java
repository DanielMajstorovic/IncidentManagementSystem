package eu.reportincident.userservice.model.dto;

import eu.reportincident.userservice.model.entity.User;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String email;
    private String name;
    private String role; // Vraćamo ime role kao string
    private boolean blocked;
    private boolean deleted;

    // Helper konstruktor za laku konverziju iz entiteta
    public UserDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.role = user.getRole().getName();
        this.blocked = user.isBlocked();
        this.deleted = user.isDeleted();
    }
}