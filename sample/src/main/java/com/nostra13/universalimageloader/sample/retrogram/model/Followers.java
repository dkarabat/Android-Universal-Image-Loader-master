package com.nostra13.universalimageloader.sample.retrogram.model;

import java.util.List;

public class Followers {

    private Pagination pagination;
    private List<User> data;

    public Pagination getPagination() {
        return pagination;
    }

    public List<User> getUsers() {
        return data;
    }

}
