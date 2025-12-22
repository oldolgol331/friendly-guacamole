package com.example.demo.common.util;

import static com.example.demo.common.constant.CommonConst.IP_ADDRESS_PATTERN;
import static lombok.AccessLevel.PRIVATE;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.example.demo.common.util
 * FileName    : CommonUtils
 * Author      : oldolgol331
 * Date        : 25. 12. 21.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 21.   oldolgol331          Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
public abstract class CommonUtils {

    /**
     * 클라이언트 IP를 추출합니다.
     *
     * @param request - HTTP 서블릿 요청
     * @return 클라이언트 IP
     */
    public static String getClientIpAddress(final HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip))
            if (ip.contains(","))
                ip = ip.split(",")[0].trim();

        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("X-Real-IP");

        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("Proxy-Client-IP");

        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("WL-Proxy-Client-IP");

        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("HTTP_CLIENT_IP");

        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");

        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip))
            ip = request.getRemoteAddr();

        if ("0:0:0:0:0:1".equals(ip)) ip = "127.0.0.1";

        return ip;
    }

    /**
     * IP 주소가 유효한지 검증합니다.
     *
     * @param ipAddress - IP 주소
     * @return 유효한 IP 주소 여부
     */
    public static boolean isValidIpAddress(final String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) return false;
        return IP_ADDRESS_PATTERN.matcher(ipAddress).matches();
    }

    /**
     * IP 주소가 로컬 IP인지 확인합니다.
     *
     * @param ipAddress - IP 주소
     * @return 로컬 IP 여부
     */
    public static boolean isLocalIpAddress(final String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) return false;
        return ipAddress.startsWith("10.")
               || ipAddress.startsWith("172.")
               || ipAddress.startsWith("192.")
               || ipAddress.equals("127.0.0.1")
               || ipAddress.equals("0:0:0:0:0:1")
               || ipAddress.equals("::1");
    }

    /**
     * 프록시 헤더가 포함된 IP 주소인지 확인합니다.
     *
     * @param ipAddress - IP 주소
     * @return 프록시 헤더 포함 여부
     */
    public static boolean isProxyHeader(final String ipAddress) {
        return ipAddress.contains(",");
    }

    /**
     *
     * @param ipAddress
     * @return
     */
    public static boolean isAllowedIpRange(final String ipAddress) {
        // TODO: 환경에 맞게 허용된 IP 대역 설정
        // 예: 회사 IP 대역, CDN IP 대역 등
        return true;
    }

}
