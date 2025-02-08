package com.naver.et0709.weatherwear.service;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

//이메일 인증번호를 생성, 저장, 검증, 제거 하는 서비스
@Slf4j
@Data
@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final Map<String, VerificationEntry> verificationStore = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;


    private static class VerificationEntry {
        @Getter
        private final String code;
        private final long expiryTime;

        public VerificationEntry(String code, long expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }

    }

    public String sendVerificationCode(String email) {
        String code = String.format("%06d", secureRandom.nextInt(999999));
        long expiryTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10);
        verificationStore.put(email, new VerificationEntry(code, expiryTime));

        // 이메일 전송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Your Verification Code");
        message.setText("Your verification code is: " + code);
        try {
            mailSender.send(message);
            log.info("Verification code sent to: {}", email);
        } catch (MailException e) {
            log.error("Failed to send email to: {}", email, e);
            throw new RuntimeException("Email sending failed", e);
        }

        return code;
    }

    public boolean verifyCode(String email, String code) {
        VerificationEntry entry = verificationStore.get(email);
        if (entry == null || entry.isExpired()) {
            verificationStore.remove(email);
            log.warn("Verification code expired or not found for email: {}", email);
            return false;
        }
        boolean isVerified = entry.getCode().equals(code);
        if (isVerified) {
            verificationStore.remove(email);
            log.info("Verification successful for email: {}", email);
        } else {
            log.warn("Verification failed for email: {}", email);
        }
        return isVerified;
    }

    public void removeVerificationCode(String email) {
        verificationStore.remove(email);
        log.info("Removed verification code for email: {}", email);
    }
}
