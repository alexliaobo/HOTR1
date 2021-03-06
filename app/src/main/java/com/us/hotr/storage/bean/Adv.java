package com.us.hotr.storage.bean;

import java.io.Serializable;

/**
 * Created by liaobo on 2017/12/12.
 */

public class Adv implements Serializable {
    private String forwart_url;
    private String forwart_type;
    private String forward_model;
    private String image;
    private String params;
    private int page;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getForwart_type() {
        return forwart_type;
    }

    public void setForwart_type(String forwart_type) {
        this.forwart_type = forwart_type;
    }

    public String getForwart_url() {
        return forwart_url;
    }

    public void setForwart_url(String forwart_url) {
        this.forwart_url = forwart_url;
    }

    public String getForward_model() {
        return forward_model;
    }

    public void setForward_model(String forward_model) {
        this.forward_model = forward_model;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public class Params implements Serializable{
        private String url;
        private int id;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}
