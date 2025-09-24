package com.RestaurantSystem.Services.Utils;

import java.math.BigInteger;
import java.util.Base64;

public class Base64Url {

    public static String encodeUnsigned(BigInteger value) {
        byte[] bytes = value.toByteArray();

        // strip leading 0x00 if present (caused by BigInteger signed representation)
        if (bytes.length > 1 && bytes[0] == 0x00) {
            byte[] tmp = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, tmp, 0, tmp.length);
            bytes = tmp;
        }

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
