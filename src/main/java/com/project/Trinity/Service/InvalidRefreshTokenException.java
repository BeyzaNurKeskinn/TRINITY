package com.project.Trinity.Service;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}//Geçersiz refresh token için özel hata sınıfı.//Özel exception’lar, hata yönetimini kolaylaştırır.