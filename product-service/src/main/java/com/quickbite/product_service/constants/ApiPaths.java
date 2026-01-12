package com.quickbite.product_service.constants;

public final class ApiPaths {
	
	private ApiPaths() {}

	public static final String API = "/api";

	public static final String CATEGORIES = API + "/categories";
	public static final String PRODUCTS = API + "/products";
	public static final String RESTAURANTS = API + "/restaurants";

	public static final String SEARCH = "/search";
	public static final String BY_ID = "/{id}";
}