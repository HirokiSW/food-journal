package com.example.foodjournal.review;

import java.util.Date;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class FoodReview extends RealmObject {

    @PrimaryKey
    private String reviewUuid = UUID.randomUUID().toString();

    private String restaurantUuid;

    private String userUuid;

    private String name;

    private double price;

    private float rating;

    private String description;

    private Date lastEaten;


    public FoodReview() {}


    public String getReviewUuid() {
        return reviewUuid;
    }

    public void setReviewUuid(String reviewUuid) {
        this.reviewUuid = reviewUuid;
    }

    public String getRestaurantUuid() {
        return restaurantUuid;
    }

    public void setRestaurantUuid(String restaurantUuid) {
        this.restaurantUuid = restaurantUuid;
    }

    public String getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLastEaten() {
        return lastEaten;
    }

    public void setLastEaten(Date lastEaten) {
        this.lastEaten = lastEaten;
    }


    @Override
    public String toString() {
        return "FoodReview{" +
                "reviewUuid='" + reviewUuid + '\'' +
                ", restaurantUuid='" + restaurantUuid + '\'' +
                ", userUuid='" + userUuid + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", rating=" + rating +
                ", description='" + description + '\'' +
                ", lastEaten=" + lastEaten +
                '}';
    }
}
