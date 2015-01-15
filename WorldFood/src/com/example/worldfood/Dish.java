package com.example.worldfood;

/**
 * Holds data for each dish  
 *
 */
public class Dish {

	private String price;
	private String description;
	private String name;

	private int flagImageRecource = 0;

	public Dish( String description, String name,String price, int flagImageRecource) {
		this.price = price;
		this.description = description;
		this.name = name;
		this.flagImageRecource = flagImageRecource;
	}

	public String getPrice() {
		return price;
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public int getFlagImageRecource() {
		return flagImageRecource;
	}

	
	
	
}
