package com.quickbite.product_service.constants;

public final class PublicEndPoints {
	
	private PublicEndPoints() {}

	public static final String PRODUCTS = "/products/**";
	public static final String RESTAURANTS = "/restaurants/**";
	public static final String API_PRODUCTS = "/api/products/**";
	public static final String API_RESTAURANTS = "/api/restaurants/**";
	public static final String ACTUATOR_HEALTH = "/actuator/health";
	public static final String ERROR = "/errors";
}