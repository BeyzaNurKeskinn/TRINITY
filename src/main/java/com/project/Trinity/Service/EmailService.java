package com.project.Trinity.Service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Şifre sıfırlama için (mevcut)
    public void sendResetCodeEmail(String to, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Trinity Şifre Sıfırlama Kodu");
        helper.setText(
                "<h3>Şifre Sıfırlama Kodu</h3>" +
                "<p>Şifre sıfırlama kodunuz: <strong>" + code + "</strong></p>" +
                "<p>Bu kod 15 dakika boyunca geçerlidir.</p>" +
                "<p>Eğer bu isteği siz yapmadıysanız, lütfen \r\n"
                + "trinity.suport0@gmail.com ile iletişime geçin.</p>",
                true
        );

        mailSender.send(message);
    }

    // Şifreyi görmek için
    public void sendViewPasswordCodeEmail(String to, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Trinity Şifreyi Görmek İçin Doğrulama Kodu");
        helper.setText(
                "<h3>Şifreyi Görmek İçin Doğrulama Kodu</h3>" +
                "<p>Şifrenizi görmek için doğrulama kodunuz: <strong>" + code + "</strong></p>" +
                "<p>Bu kod 15 dakika boyunca geçerlidir.</p>" +
                "<p>Eğer bu isteği siz yapmadıysanız, lütfen \r\n"
                + "trinity.suport0@gmail.com ile iletişime geçin.</p>",
                true
        );

        mailSender.send(message);
    }

    // Şifreyi güncellemek için
    public void sendUpdatePasswordCodeEmail(String to, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Trinity Şifreyi Değiştirmek İçin Doğrulama Kodu");
        helper.setText(
                "<h3>Şifreyi Değiştirmek İçin Doğrulama Kodu</h3>" +
                "<p>Şifrenizi güncellemek için doğrulama kodunuz: <strong>" + code + "</strong></p>" +
                "<p>Bu kod 15 dakika boyunca geçerlidir.</p>" +
                "<p>Eğer bu isteği siz yapmadıysanız, lütfen \r\n"
                + "trinity.suport0@gmail.com ile iletişime geçin.</p>",
                true
        );

        mailSender.send(message);
    }
}