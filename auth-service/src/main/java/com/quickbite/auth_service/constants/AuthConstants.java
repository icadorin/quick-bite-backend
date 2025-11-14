package com.quickbite.auth_service.constants;

import java.util.regex.Pattern;

public class AuthConstants {

    public static final String EMAIL_REQUIRED = "Email é obrigatório";
    public static final String INVALID_EMAIL_FORMAT = "Formato de email inválido";
    public static final String PASSWORD_REQUIRED = "Senha é obrigatória";
    public static final String PASSWORD_TOO_SHORT = "Senha deve ter pelo menos 6 caracteres";
    public static final String FULL_NAME_REQUIRED = "Nome completo é obrigatório";
    public static final String FULL_NAME_TOO_SHORT = "Nome completo deve ter pelo menos 2 caracteres";
    public static final String INVALID_PHONE = "Número de telefone inválido";

    public static final String INVALID_CREDENTIALS = "Email ou senha incorretos";
    public static final String USER_DISABLED = "Usuário desativado";
    public static final String AUTHENTICATION_ERROR = "Erro de autenticação";
    public static final String USER_ALREADY_EXISTS = "Email já cadastrado";
    public static final String INVALID_USER_STATUS = "Usuário inativo. Entre em contato com o suporte";

    public static final String REFRESH_TOKEN_NOT_PROVIDED = "Refresh token não fornecido";
    public static final String INVALID_REFRESH_TOKEN = "Refresh token inválido";
    public static final String REFRESH_TOKEN_REVOKED = "Refresh token revogado";
    public static final String REFRESH_TOKEN_EXPIRED = "Refresh token expirado";
    public static final String INVALID_ROLE = "Role inválida: ";

    public static final int REFRESH_TOKEN_EXPIRATION_DAYS = 7;
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private AuthConstants() {

    }
}
