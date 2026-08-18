package com.example.foodjournal.restaurant;

import java.util.Date;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Restaurant extends RealmObject {

    @PrimaryKey
    private String restaurantUuid = UUID.randomUUID().toString();

    private String userUuid;

    private String name;

    private String address;

    private String description;

    private Date lastVisited;


    public Restaurant() {}


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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLastVisited() {
        return lastVisited;
    }

    public void setLastVisited(Date lastVisited) {
        this.lastVisited = lastVisited;
    }


    @Override
    public String toString() {
        return "Restaurant{" +
                "restaurantUuid='" + restaurantUuid + '\'' +
                ", userUuid='" + userUuid + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", description='" + description + '\'' +
                ", lastVisited=" + lastVisited +
                '}';
    }
}
