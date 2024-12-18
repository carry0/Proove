package com.proove.ble.data;

public class ProductCateItem {
    private String cateId;
    private String name;
    private String cateImage;
    private boolean isSelect;

    public String getCateId() {
        return cateId;
    }

    public void setCateId(String cateId) {
        this.cateId = cateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCateImage() {
        return cateImage;
    }

    public void setCateImage(String cateImage) {
        this.cateImage = cateImage;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public ProductCateItem cloneNew() {
        ProductCateItem productCateItem = new ProductCateItem();
        productCateItem.setSelect(isSelect);
        productCateItem.setName(name);
        productCateItem.setCateId(cateId);
        productCateItem.setCateImage(cateImage);
        return productCateItem;
    }
}
