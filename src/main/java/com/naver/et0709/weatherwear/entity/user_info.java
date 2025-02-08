package com.naver.et0709.weatherwear.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import com.naver.et0709.weatherwear.util.UUIDUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "user_info")
public class user_info {
    @Id
    @Column(name = "uuid", columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private byte[] uuid; // 고유 사용자 ID (UUID)

    @PrePersist
    public void prePersist() {
        if (uuid == null) {
            UUID generatedUuid = UUID.randomUUID();  // 새로운 UUID 생성
            uuid = UUIDUtils.asBytes(generatedUuid); // UUID를 byte[]로 변환하여 uuid 필드에 저장
        }
    }
    //UUID 확인용
    public String getUuidHex() {
        return UUIDUtils.byteArrayToHex(uuid);  // byte[]를 Hex 문자열로 변환
    }

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email; // 사용자 이메일

    @Column(name = "pw", nullable = false, length = 255)
    private String userPw; // 사용자 비밀번호 (암호화 적용 필요)

    @Column(name = "name", nullable = false, length = 255)
    private String name; // 사용자 이름

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate; // 계정 생성일
}
