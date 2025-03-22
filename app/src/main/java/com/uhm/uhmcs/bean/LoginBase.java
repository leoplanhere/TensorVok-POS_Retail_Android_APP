package com.uhm.uhmcs.bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LoginBase implements Serializable {

    /**
     * code : 1
     * msg : 登录成功
     * time : 1736130353
     * data : {"userinfo":{"id":172,"username":"","nickname":"服务员","mobile":"13812345678","avatar":"data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZlcnNpb249IjEuMSIgaGVpZ2h0PSIxMDAiIHdpZHRoPSIxMDAiPjxyZWN0IGZpbGw9InJnYigxNjAsMTgwLDIyOSkiIHg9IjAiIHk9IjAiIHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIj48L3JlY3Q+PHRleHQgeD0iNTAiIHk9IjUwIiBmb250LXNpemU9IjUwIiB0ZXh0LWNvcHk9ImZhc3QiIGZpbGw9IiNmZmZmZmYiIHRleHQtYW5jaG9yPSJtaWRkbGUiIHRleHQtcmlnaHRzPSJhZG1pbiIgZG9taW5hbnQtYmFzZWxpbmU9ImNlbnRyYWwiPuacjTwvdGV4dD48L3N2Zz4=","score":0,"jurisdiction":2,"token":"32462012-70e9-48d2-a3f2-80252f3d862c","user_id":172,"createtime":1736130354,"expiretime":1736216754,"expires_in":86400}}
     */

    @SerializedName("code")
    private int code;
    @SerializedName("msg")
    private String msg;
    @SerializedName("time")
    private String time;
    @SerializedName("data")
    private DataBean data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean implements Serializable {
        /**
         * userinfo : {"id":172,"username":"","nickname":"服务员","mobile":"13812345678","avatar":"data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZlcnNpb249IjEuMSIgaGVpZ2h0PSIxMDAiIHdpZHRoPSIxMDAiPjxyZWN0IGZpbGw9InJnYigxNjAsMTgwLDIyOSkiIHg9IjAiIHk9IjAiIHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIj48L3JlY3Q+PHRleHQgeD0iNTAiIHk9IjUwIiBmb250LXNpemU9IjUwIiB0ZXh0LWNvcHk9ImZhc3QiIGZpbGw9IiNmZmZmZmYiIHRleHQtYW5jaG9yPSJtaWRkbGUiIHRleHQtcmlnaHRzPSJhZG1pbiIgZG9taW5hbnQtYmFzZWxpbmU9ImNlbnRyYWwiPuacjTwvdGV4dD48L3N2Zz4=","score":0,"jurisdiction":2,"token":"32462012-70e9-48d2-a3f2-80252f3d862c","user_id":172,"createtime":1736130354,"expiretime":1736216754,"expires_in":86400}
         */

        @SerializedName("userinfo")
        private UserinfoBean userinfo;

        public UserinfoBean getUserinfo() {
            return userinfo;
        }

        public void setUserinfo(UserinfoBean userinfo) {
            this.userinfo = userinfo;
        }

        public static class UserinfoBean implements Serializable {
            /**
             * id : 172
             * username :
             * nickname : 服务员
             * mobile : 13812345678
             * avatar : data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZlcnNpb249IjEuMSIgaGVpZ2h0PSIxMDAiIHdpZHRoPSIxMDAiPjxyZWN0IGZpbGw9InJnYigxNjAsMTgwLDIyOSkiIHg9IjAiIHk9IjAiIHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIj48L3JlY3Q+PHRleHQgeD0iNTAiIHk9IjUwIiBmb250LXNpemU9IjUwIiB0ZXh0LWNvcHk9ImZhc3QiIGZpbGw9IiNmZmZmZmYiIHRleHQtYW5jaG9yPSJtaWRkbGUiIHRleHQtcmlnaHRzPSJhZG1pbiIgZG9taW5hbnQtYmFzZWxpbmU9ImNlbnRyYWwiPuacjTwvdGV4dD48L3N2Zz4=
             * score : 0
             * jurisdiction : 2
             * token : 32462012-70e9-48d2-a3f2-80252f3d862c
             * user_id : 172
             * createtime : 1736130354
             * expiretime : 1736216754
             * expires_in : 86400
             */

            @SerializedName("id")
            private int id;
            @SerializedName("username")
            private String username;
            @SerializedName("nickname")
            private String nickname;
            @SerializedName("mobile")
            private String mobile;
            @SerializedName("avatar")
            private String avatar;
            @SerializedName("score")
            private int score;
            @SerializedName("jurisdiction")
            private int jurisdiction;
            @SerializedName("token")
            private String token;
            @SerializedName("user_id")
            private int userId;
            @SerializedName("createtime")
            private int createtime;
            @SerializedName("expiretime")
            private int expiretime;
            @SerializedName("expires_in")
            private int expiresIn;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getUsername() {
                return username;
            }

            public void setUsername(String username) {
                this.username = username;
            }

            public String getNickname() {
                return nickname;
            }

            public void setNickname(String nickname) {
                this.nickname = nickname;
            }

            public String getMobile() {
                return mobile;
            }

            public void setMobile(String mobile) {
                this.mobile = mobile;
            }

            public String getAvatar() {
                return avatar;
            }

            public void setAvatar(String avatar) {
                this.avatar = avatar;
            }

            public int getScore() {
                return score;
            }

            public void setScore(int score) {
                this.score = score;
            }

            public int getJurisdiction() {
                return jurisdiction;
            }

            public void setJurisdiction(int jurisdiction) {
                this.jurisdiction = jurisdiction;
            }

            public String getToken() {
                return token;
            }

            public void setToken(String token) {
                this.token = token;
            }

            public int getUserId() {
                return userId;
            }

            public void setUserId(int userId) {
                this.userId = userId;
            }

            public int getCreatetime() {
                return createtime;
            }

            public void setCreatetime(int createtime) {
                this.createtime = createtime;
            }

            public int getExpiretime() {
                return expiretime;
            }

            public void setExpiretime(int expiretime) {
                this.expiretime = expiretime;
            }

            public int getExpiresIn() {
                return expiresIn;
            }

            public void setExpiresIn(int expiresIn) {
                this.expiresIn = expiresIn;
            }
        }
    }
}
