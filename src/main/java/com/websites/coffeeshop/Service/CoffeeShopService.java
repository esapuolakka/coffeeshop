package com.websites.coffeeshop.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.websites.coffeeshop.repository.DiscountRepository;
import com.websites.coffeeshop.repository.ImageRepository;
import com.websites.coffeeshop.repository.ItemRepository;
import com.websites.coffeeshop.model.Discount;
import com.websites.coffeeshop.model.Image;
import com.websites.coffeeshop.model.Item;
import com.websites.coffeeshop.model.ItemWithImageUrl;
import com.websites.coffeeshop.storage.ImageStorage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@Service
public class CoffeeShopService {

  @Autowired
  private ImageStorage imageStorage;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private ImageRepository imageRepository;

  @Autowired
  private DiscountRepository discountRepository;

  public Item getItemById(Long id) {
    return itemRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Item not found"));
  }

  public List<Item> getAllItems() {
    return itemRepository.findAll();
  }

  public Page<Item> getAllByCategory(Long categoryId, Pageable pageable) {
    return itemRepository.findByCategoryId(categoryId, pageable);
  }

  public Image getImageById(Long id) {
    return imageRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Image not found"));
  }

  public byte[] getImageContent(Image image) {
    return imageStorage.get(image.getStorageKey());
  }

  public Page<ItemWithImageUrl> getAllItemsWithImageUrls(Long categoryId, Pageable pageable) {
    String categoryPath = categoryId == 1 ? "/products/coffee-machines/" : "/products/coffee-products/";
    Page<Item> items = getAllByCategory(categoryId, pageable);

    List<ItemWithImageUrl> itemsWithImageUrls = items.getContent().stream()
        .map(item -> {
            String imageUrl = (item.getImage() != null) ? categoryPath + item.getImage().getId() + "/image"
              : "/images/No_Image_Available.png";
          BigDecimal discountedPrice = BigDecimal.valueOf(getDiscountedPrice(item.getPrice()));
          return new ItemWithImageUrl(item, imageUrl, discountedPrice);
        })
        .toList();
    return new PageImpl<>(itemsWithImageUrls, pageable, items.getTotalElements());
  }

  public Page<ItemWithImageUrl> searchItemsWithImageUrls(Long categoryId, String name, Pageable pageable) {
    String categoryPath = categoryId == 1 ? "/products/coffee-machines/" : "/products/coffee-products/";
    Page<Item> items = itemRepository.findByCategoryIdAndName(categoryId, name, pageable);

    List<ItemWithImageUrl> itemsWithImageUrls = items.getContent().stream()
        .map(item -> {
            String imageUrl = (item.getImage() != null) ? categoryPath + item.getImage().getId() + "/image"
              : "/images/No_Image_Available.png";
          BigDecimal discountedPrice = BigDecimal.valueOf(getDiscountedPrice(item.getPrice()));
          return new ItemWithImageUrl(item, imageUrl, discountedPrice);
        })
        .toList();
    return new PageImpl<>(itemsWithImageUrls, pageable, items.getTotalElements());
  }

  public Discount getDiscount() {
    return discountRepository.findById(1L)
        .orElseThrow(() -> new RuntimeException("Discount not found"));
  }

  // Laskee alennetun hinnan
  public double getDiscountedPrice(BigDecimal itemPrice) {
    double discount = getDiscount().getDiscount();
    BigDecimal discountedPrice = itemPrice
        .subtract(itemPrice.multiply(BigDecimal.valueOf(discount)).divide(BigDecimal.valueOf(100)));
    discountedPrice = discountedPrice.setScale(2, RoundingMode.HALF_UP);
    return discountedPrice.doubleValue();
  }
}
