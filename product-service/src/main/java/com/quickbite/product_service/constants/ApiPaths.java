package com.quickbite.product_service.constants;

public final class ApiPaths {
	
	private ApiPaths() {}

	public static final String API_V1 = "/api/1";

	public static final String RESTAURANTS = API_V1 + "/restaurants";
	public static final String PRODUCTS    = API_V1 + "/products";
	public static final String CATEGORIES  = API_V1 + "/categories";

	public static final String BY_ID       = "/{id}";
	public static final String SEARCH      = "/search";
	public static final String FEATURED    = "/featured";
	public static final String RESTAURANT  = "/restaurant/{restaurantId}";
	public static final String CATEGORY    = "/category/{categoryId}";
	public static final String OWNER       = "/owner/{ownerId}";
	public static final String CUISINE     = "/cuisine/{cuisineType}";
	public static final String RATING      = "/rating";
}