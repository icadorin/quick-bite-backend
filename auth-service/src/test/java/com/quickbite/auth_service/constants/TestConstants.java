package com.quickbite.auth_service.constants;

public class TestConstants {

    public static final String USER_INACTIVE_MESSAGE = "Usuário inativo. Entre em contato com o suporte";
    public static final String USER_DISABLED_MESSAGE = "Usuário desativado";
    public static final String INVALID_CREDENTIALS_MESSAGE = "Email ou senha incorretos";
    public static final String PASSWORD_TOO_SHORT_MESSAGE = "Senha deve ter pelo menos 6 caracteres";
    public static final String EMAIL_ALREADY_EXISTS_MESSAGE = "Email já cadastrado";
    public static final String REFRESH_TOKEN_REQUIRED_MESSAGE = "Refresh token não fornecido";

    public static final String EMAIL_REQUIRED_MESSAGE = "Email é obrigatório";
    public static final String INVALID_EMAIL_FORMAT_MESSAGE = "Formato de email inválido";
    public static final String PASSWORD_REQUIRED_MESSAGE = "Senha é obrigatória";
    public static final String FULL_NAME_REQUIRED_MESSAGE = "Nome completo é obrigatório";
    public static final String FULL_NAME_TOO_SHORT_MESSAGE = "Nome completo deve ter pelo menos 2 caracteres";
    public static final String INVALID_PHONE_MESSAGE = "Número de telefone inválido";
    public static final String INVALID_REFRESH_TOKEN_MESSAGE = "Refresh token inválido";

    public static final String VALID_EMAIL = "usuario@example.com";
    public static final String VALID_PASSWORD = "senha123";
    public static final String VALID_FULL_NAME = "Usuário Teste";
    public static final String SHORT_PASSWORD = "123";
    public static final String VALID_REFRESH_TOKEN = "valid-refresh-token";
    public static final String NEW_ACCESS_TOKEN = "new-access-token";

    public static final Long USER_ID = 1L;
    public static final Long TOKEN_EXPIRATION = 3600L;
}
