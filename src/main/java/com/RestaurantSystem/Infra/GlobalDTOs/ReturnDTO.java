package com.RestaurantSystem.Infra.GlobalDTOs;

public class ReturnDTO {
    private Object data;
    private String error;

    // <>------------ Constructors ------------<>
    public ReturnDTO() {
    }
    public ReturnDTO(Object data, String error) {
        this.data = data;
        this.error = error;
    }


    // <>------------ Getters and Setters ------------<>
    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
