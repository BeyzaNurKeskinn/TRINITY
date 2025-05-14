
package com.project.Trinity.Service;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String message) {
        super(message);
    }//Aynı kullanıcı adı için hata sınıfı.
}