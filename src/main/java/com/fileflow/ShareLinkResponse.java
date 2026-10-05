package com.fileflow;

import java.time.Instant;

public class ShareLinkResponse {
    private String token ; 
    private Instant createdAt ; 
    private Instant expiresAt ;
    private String url; 


    public ShareLinkResponse(Instant createdAt , Instant expiresAt , String token,  String baseUrl) {
        this.token = token ; 
        this.url = baseUrl + "/share/" + token;
        this.createdAt = createdAt ; 
        this.expiresAt = expiresAt ; 
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getUrl() {
        return url;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

}
