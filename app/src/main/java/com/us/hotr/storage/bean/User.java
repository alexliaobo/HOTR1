package com.us.hotr.storage.bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.net.URLDecoder;

/**
 * Created by liaobo on 2017/12/18.
 */

public class User implements Serializable {
    private String city_name;
    private String username;
    @SerializedName(value = "province_name", alternate = {"provinceName"})
    private String province_name;
    @SerializedName(value = "head_portrait", alternate = {"head_image_path", "head_image"})
    private String head_portrait;
    @SerializedName(value = "attentionCount", alternate = {"follow_numb"})
    private int attentionCount;
    @SerializedName(value = "user_typ", alternate = {"user_type", "userType"})
    private int user_typ;
    @SerializedName(value = "fanCount", alternate = {"fans_numb"})
    private int fanCount;
    @SerializedName(value = "userId", alternate = {"id"})
    private long userId;
    private Integer gender;
    private String signature;
    private String mobile;
    private int orientation;
    @SerializedName(value = "nickname", alternate = {"nick_name", "nickName"})
    private String nickname;
    private Integer age;
    private int is_attention;
    private int hotTopicCount;
    private int contrastPhotoCount;
    private String openid;
    private String youzan_cookie_key;
    private String youzan_access_token;
    private String youzan_cookie_value;
    private long massagist_id;

    public long getMassagist_id() {
        return massagist_id;
    }

    public void setMassagist_id(long massagist_id) {
        this.massagist_id = massagist_id;
    }

    public String getYouzan_cookie_key() {
        return youzan_cookie_key;
    }

    public void setYouzan_cookie_key(String youzan_cookie_key) {
        this.youzan_cookie_key = youzan_cookie_key;
    }

    public String getYouzan_access_token() {
        return youzan_access_token;
    }

    public void setYouzan_access_token(String youzan_access_token) {
        this.youzan_access_token = youzan_access_token;
    }

    public String getYouzan_cookie_value() {
        return youzan_cookie_value;
    }

    public void setYouzan_cookie_value(String youzan_cookie_value) {
        this.youzan_cookie_value = youzan_cookie_value;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public int getHotTopicCount() {
        return hotTopicCount;
    }

    public void setHotTopicCount(int hotTopicCount) {
        this.hotTopicCount = hotTopicCount;
    }

    public int getContrastPhotoCount() {
        return contrastPhotoCount;
    }

    public void setContrastPhotoCount(int contrastPhotoCount) {
        this.contrastPhotoCount = contrastPhotoCount;
    }

    public int getIs_attention() {
        return is_attention;
    }

    public void setIs_attention(int is_attention) {
        this.is_attention = is_attention;
    }

    public String getCity_name() {
        return city_name;
    }

    public void setCity_name(String city_name) {
        this.city_name = city_name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProvince_name() {
        return province_name;
    }

    public void setProvince_name(String province_name) {
        this.province_name = province_name;
    }

    public String getHead_portrait() {
        return head_portrait;
    }

    public void setHead_portrait(String head_portrait) {
        this.head_portrait = head_portrait;
    }

    public int getAttentionCount() {
        return attentionCount;
    }

    public void setAttentionCount(int attentionCount) {
        this.attentionCount = attentionCount;
    }

    public int getUser_typ() {
        return user_typ;
    }

    public void setUser_typ(int user_typ) {
        this.user_typ = user_typ;
    }

    public int getFanCount() {
        return fanCount;
    }

    public void setFanCount(int fanCount) {
        this.fanCount = fanCount;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public String getNickname() {
        try {
            return URLDecoder.decode(nickname, "UTF-8");
        } catch (Throwable t) {
            return null;
        }

    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
