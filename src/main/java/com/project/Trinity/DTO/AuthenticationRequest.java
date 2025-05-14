
package com.project.Trinity.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class AuthenticationRequest {
    @NotBlank(message = "Kullanıcı adı zorunludur")
    private String username;

    @NotBlank(message = "Şifre zorunludur")
    private String password;

}//Login isteği için veri transfer nesnesi (DTO)
//Neden?: Login verilerini güvenli ve düzenli taşımak için.
