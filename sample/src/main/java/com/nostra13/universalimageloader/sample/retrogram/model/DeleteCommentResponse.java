package com.nostra13.universalimageloader.sample.retrogram.model;

public class DeleteCommentResponse {

    private Meta meta;

    public boolean isSuccessfull() {
        return meta.code.equals(200);
    }

    private static class Meta {

        private Integer code;

    }

}
