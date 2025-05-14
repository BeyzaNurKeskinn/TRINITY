package com.project.Trinity.Config;
import com.project.Trinity.Service.UsernameAlreadyExistsException;
import com.project.Trinity.Service.InvalidRefreshTokenException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(UsernameAlreadyExistsException.class)
	public ResponseEntity<String> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {
	    return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
	}//Kullanıcı kaydolurken aynı kullanıcı adını seçerse, istemciye anlaşılır bir hata mesajı göstermek için.

	@ExceptionHandler(InvalidRefreshTokenException.class)
	public ResponseEntity<String> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
	    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
	}//Refresh token ile yeni bir access token alınmaya çalışıldığında hata olursa, istemciye neden başarısız olduğunu bildirir.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException ex) {
        return new ResponseEntity<>("Erişim reddedildi", HttpStatus.FORBIDDEN);
    }//Güvenlik açısından, yetkisiz erişimlerde istemciye net bir mesaj verir.

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return new ResponseEntity<>("Bir hata oluştu: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }//Beklenmeyen hataları yakalamak için bir güvenlik ağı.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);//Base64 doğrulama hatası veya kategori adı çakışması için fırlatılıyor.
    }
}


//buranın amacı uygulamada oluşan hataları (exception’ları) yakalayıp istemciye düzgün yanıtlar döndürmek.
//REST API’lerde hata yönetimi için kullanılır.