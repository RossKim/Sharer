package com.ross.kbs.sharer.models;


public class AccountItem {

	private Integer itemImageId;
	private String itemText;

	public AccountItem(Integer imageId, String text) {
		// TODO Auto-generated constructor stub
		this.itemImageId = imageId;
		this.itemText = text;
	}

	public Integer getItemImageId() {
		return itemImageId;
	}

	public void setItemImageId(Integer imageId) {
		this.itemImageId = imageId;
	}

	public String getItemText() {
		return itemText;
	}

	public void setItemText(String itemText) {
		this.itemText = itemText;
	}

}
