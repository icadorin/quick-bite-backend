package com.quickbite.product_service.constants;

public final class PublicEndPoints {

	public static final String[] PUBLIC = {
		ApiPaths.PRODUCTS,
		ApiPaths.PRODUCTS + ApiPaths.BY_ID,
		ApiPaths.PRODUCTS + ApiPaths.SEARCH,
		ApiPaths.PRODUCTS + ApiPaths.FEATURED,
		ApiPaths.PRODUCTS + "/restaurant/**",

		ApiPaths.RESTAURANTS,
		ApiPaths.RESTAURANTS + ApiPaths.BY_ID,
		ApiPaths.RESTAURANTS + ApiPaths.SEARCH,
		ApiPaths.RESTAURANTS + ApiPaths.CUISINE,
		ApiPaths.RESTAURANTS + ApiPaths.RATING,

		ApiPaths.CATEGORIES,
		ApiPaths.CATEGORIES + ApiPaths.BY_ID,

		"/api/v1/actuator/health",
		"/error"
	};
}
