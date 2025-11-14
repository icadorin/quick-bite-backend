package com.quickbite.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Nome completo é obrigatório")
    @Size(min = 2, message = "Nome completo deve ter pelo menos 2 caracteres")
    private String fullName;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Formato de emil inválido")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    private String password;

    private String phone;
    private String address;
    private String role = "CUSTOMER";
}
