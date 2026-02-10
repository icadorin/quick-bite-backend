package com.quickbite.product_service.constants;

public final class PublicEndPoints {

	public static final String[] PUBLIC = {
		ApiPaths.PRODUCTS + "/**",
		ApiPaths.RESTAURANTS + "/**",
		ApiPaths.CATEGORIES + "/**",
		"/actuator/health",
		"/error"
	};
}
