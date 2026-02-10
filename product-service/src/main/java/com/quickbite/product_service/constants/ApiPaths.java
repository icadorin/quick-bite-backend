package com.quickbite.product_service.constants;

public final class ApiPaths {
	
	private ApiPaths() {}

	public static final String API_V1 = "/api/v1";

	public static final String RESTAURANTS = API_V1 + "/restaurants";
	public static final String PRODUCTS    = API_V1 + "/products";
	public static final String CATEGORIES  = API_V1 + "/categories";

	public static final String BY_ID = "/{id}";
}