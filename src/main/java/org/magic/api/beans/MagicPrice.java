package org.magic.api.beans;

import java.util.Currency;

public class MagicPrice implements Comparable<MagicPrice> {
	private Double value = 0.0;
	private String seller;
	private String url;
	private Currency currency;
	private String site;
	private boolean foil;
	private String language;
	private String quality;
	private Object shopItem;
	private String country;
	private MagicCard magicCard;
	private int qty = 1;
	private String sellerUrl;


	public String getSellerUrl() {
		return sellerUrl;
	}

	public void setSellerUrl(String sellerUrl) {
		this.sellerUrl = sellerUrl;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}

	public int getQty() {
		return qty;
	}

	public MagicCard getMagicCard() {
		return magicCard;
	}

	public void setMagicCard(MagicCard magicCard) {
		this.magicCard = magicCard;
	}

	public Object getShopItem() {
		return shopItem;
	}

	public void setShopItem(Object shopItem) {
		this.shopItem = shopItem;
	}

	public String getQuality() {
		if (quality == null)
			return "";

		return quality;
	}

	public void setQuality(String quality) {
		this.quality = quality;
	}

	public boolean isFoil() {
		return foil;
	}

	public void setFoil(boolean foil) {
		this.foil = foil;
	}

	public String getLanguage() {
		if (language == null)
			return "";
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public Currency getCurrency() {
		return currency;
	}


	public void setCurrency(String currencyCode)
	{
		if(!currencyCode.isEmpty())
			this.currency=Currency.getInstance(currencyCode.toUpperCase());
	}


	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public String getSeller() {
		return seller;
	}

	public void setSeller(String seller) {
		this.seller = seller;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return String.valueOf(magicCard);
	}

	@Override
	public int compareTo(MagicPrice o) {
		return (int) (getValue() - o.getValue());
	}

	public void setCountry(String c) {
		country = c;

	}

	public String getCountry() {
		return country;
	}
}
