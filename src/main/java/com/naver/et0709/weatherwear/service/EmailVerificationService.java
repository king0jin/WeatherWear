package com.naver.et0709.weatherwear.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

//이메일 인증번호를 생성, 저장, 검증, 제거 하는 서비스
@Slf4j
@Data
@Service
public class EmailVerificationService {
    private static final DecimalFormat CODE_FORMAT = new DecimalFormat("000000");
    private final SecureRandom secureRandom = new SecureRandom();
    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${custom.auth-code-expiration-millis}")
    private long authCodeExpirationMillis;

    public EmailVerificationService(StringRedisTemplate redisTemplate, JavaMailSender mailSender) {
        this.redisTemplate = redisTemplate;
        this.mailSender = mailSender;
    }
    // 인증 코드 생성 및 Redis에 저장
    public String sendVerificationCode(String email) {
        String code = generateCode();

        // Create VerificationEntry and store in Redis (serialized as JSON)
        VerificationEntry entry = new VerificationEntry(code, calculateExpiryTime());
        storeInRedis(email, entry);

        sendEmail(email, code);
        return code;
    }
    // 이메일 전송
    private void sendEmail(String email, String code) {
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
    }

    // 인증 코드 검증
    public boolean verifyCode(String email, String verificationCode) {
        log.debug("Verifying code for email: {}", email);

        VerificationEntry entry = retrieveFromRedis(email);
        if (entry == null || entry.isExpired()) {
            log.warn("Verification code not found or expired for email: {}", email);
            redisTemplate.delete(email);
            return false;
        }

        boolean isVerified = entry.getCode().equals(verificationCode);
        if (isVerified) {
            //redisTemplate.delete(email);
            log.info("Verification successful for email: {}", email);
        } else {
            log.warn("Verification failed for email: {}", email);
        }
        return isVerified;
    }

    // Remove verification code from Redis
    public void removeVerificationCode(String email) {
        redisTemplate.delete(email);
        log.info("Removed verification code for email: {}", email);
    }

    // Generate a new verification code
    private String generateCode() {
        return CODE_FORMAT.format(secureRandom.nextInt(1000000));
    }

    // Calculate the expiry time
    private long calculateExpiryTime() {
        return System.currentTimeMillis() + authCodeExpirationMillis;
    }

    // Store VerificationEntry in Redis
    private void storeInRedis(String email, VerificationEntry entry) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(entry);
            redisTemplate.opsForValue().set(email, json, authCodeExpirationMillis, TimeUnit.MILLISECONDS);
        } catch (JsonProcessingException e) {
            log.error("Error converting VerificationEntry to JSON", e);
            throw new RuntimeException("Error converting to JSON", e);
        }
    }
    // Retrieve VerificationEntry from Redis
    private VerificationEntry retrieveFromRedis(String email) {
        String storedJson = redisTemplate.opsForValue().get(email);
        if (storedJson == null) {
            return null;
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(storedJson, VerificationEntry.class);
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to VerificationEntry", e);
            throw new RuntimeException("Error converting from JSON", e);
        }
    }

    // Inner class for verification entry
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    private static class VerificationEntry {
        private final String code;
        private final long expiryTime;

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }

        @JsonCreator
        public VerificationEntry(@JsonProperty("code") String code, @JsonProperty("expiryTime") long expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }
    }

}
