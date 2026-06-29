# QuickBite Core

Módulo compartilhado com componentes reutilizáveis.

## Componentes

- BaseEntity: Classe base com createdAt e updatedAt
- UserRole: Enum com papéis (CUSTOMER, RESTAURANT_OWNER, ADMIN)
- Exceções customizadas: ResourceNotFoundException, BusinessRuleViolationException, etc
- ErrorResponse e ApiError: Padronização de respostas de erro
- PatchMapperConfig: Configuração para mapeamento parcial

## Dependências

- Lombok
- SLF4J API
- Jakarta Validation API
- Jakarta Persistence API
- Jackson Databind + JSR310
- MapStruct

## Uso

Adicione a dependência no pom.xml:

<dependency>
    <groupId>com.quickbite</groupId>
    <artifactId>quickbite-core</artifactId>
    <version>${project.version}</version>
</dependency>

Para testes:

<dependency>
    <groupId>com.quickbite</groupId>
    <artifactId>quickbite-core</artifactId>
    <version>${project.version}</version>
    <type>test-jar</type>
    <scope>test</scope>
</dependency>