package com.naver.et0709.weatherwear.util;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDUtils {
    // UUID를 byte[]로 변환하는 메소드
    public static byte[] asBytes(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        byte[] result = buffer.array();

        if (result.length != 16) {
            throw new IllegalStateException("UUID conversion error: Length is not 16 bytes.");
        }
        return result;
    }

    // byte[]를 Hex 문자열로 변환하여 보기 편하게 출력하는 메소드
    public static String byteArrayToHex(byte[] byteArray) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : byteArray) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}
