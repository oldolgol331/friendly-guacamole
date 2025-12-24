package com.example.demo.common.util;

import static com.example.demo.common.util.CommonUtils.getClientIpAddress;
import static com.example.demo.common.util.CommonUtils.isAllowedIpRange;
import static com.example.demo.common.util.CommonUtils.isLocalIpAddress;
import static com.example.demo.common.util.CommonUtils.isProxyHeader;
import static com.example.demo.common.util.CommonUtils.isValidIpAddress;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

/**
 * PackageName : com.example.demo.common.util
 * FileName    : CommonUtilsTest
 * Author      : oldolgol331
 * Date        : 25. 12. 24.
 * Description : CommonUtils 테스트
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 24.   oldolgol331          Initial creation
 */
class CommonUtilsTest {

    @Nested
    @DisplayName("getClientIpAddress() 테스트")
    class GetClientIpAddressTests {

        @RepeatedTest(10)
        @DisplayName("X-Forwarded-For 헤더에서 IP 주소 추출")
        void getClientIpAddress_XForwardedFor() {
            // given
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1, 172.16.0.1");

            // when
            String clientIpAddress = getClientIpAddress(request);

            // then
            assertEquals("192.168.1.1", clientIpAddress);
        }

        @RepeatedTest(10)
        @DisplayName("X-Forwarded-For 헤더가 없을 때 X-Real-IP 헤더에서 IP 주소 추출")
        void getClientIpAddress_XRealIp() {
            // given
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.2");

            // when
            String clientIpAddress = getClientIpAddress(request);

            // then
            assertEquals("192.168.1.2", clientIpAddress);
        }

        @RepeatedTest(10)
        @DisplayName("Proxy-Client-IP 헤더에서 IP 주소 추출")
        void getClientIpAddress_ProxyClientIp() {
            // given
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getHeader("Proxy-Client-IP")).thenReturn("192.168.1.3");

            // when
            String clientIpAddress = getClientIpAddress(request);

            // then
            assertEquals("192.168.1.3", clientIpAddress);
        }

        @RepeatedTest(10)
        @DisplayName("WL-Proxy-Client-IP 헤더에서 IP 주소 추출")
        void getClientIpAddress_WLProxyClientIp() {
            // given
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
            when(request.getHeader("WL-Proxy-Client-IP")).thenReturn("192.168.1.4");

            // when
            String clientIpAddress = getClientIpAddress(request);

            // then
            assertEquals("192.168.1.4", clientIpAddress);
        }

        @RepeatedTest(10)
        @DisplayName("HTTP_CLIENT_IP 헤더에서 IP 주소 추출")
        void getClientIpAddress_HttpClientIp() {
            // given
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
            when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
            when(request.getHeader("HTTP_CLIENT_IP")).thenReturn("192.168.1.5");

            // when
            String clientIpAddress = getClientIpAddress(request);

            // then
            assertEquals("192.168.1.5", clientIpAddress);
        }

        @RepeatedTest(10)
        @DisplayName("HTTP_X_FORWARDED_FOR 헤더에서 IP 주소 추출")
        void getClientIpAddress_HttpXForwardedFor() {
            // given
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
            when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
            when(request.getHeader("HTTP_CLIENT_IP")).thenReturn(null);
            when(request.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn("192.168.1.6");

            // when
            String clientIpAddress = getClientIpAddress(request);

            // then
            assertEquals("192.168.1.6", clientIpAddress);
        }

        @RepeatedTest(10)
        @DisplayName("getRemoteAddr()에서 IP 주소 추출")
        void getClientIpAddress_RemoteAddr() {
            // given
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
            when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
            when(request.getHeader("HTTP_CLIENT_IP")).thenReturn(null);
            when(request.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("192.168.1.7");

            // when
            String clientIpAddress = getClientIpAddress(request);

            // then
            assertEquals("192.168.1.7", clientIpAddress);
        }

        @RepeatedTest(10)
        @DisplayName("localhost IP 주소 변환")
        void getClientIpAddress_Localhost() {
            // given
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("X-Forwarded-For")).thenReturn("127.0.0.1");

            // when
            String clientIpAddress = getClientIpAddress(request);

            // then
            assertEquals("127.0.0.1", clientIpAddress);
        }

        @RepeatedTest(10)
        @DisplayName("unknown 값이 포함된 X-Forwarded-For 헤더")
        void getClientIpAddress_UnknownXForwardedFor() {
            // given
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("X-Forwarded-For")).thenReturn("unknown");
            when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.8");

            // when
            String clientIpAddress = getClientIpAddress(request);

            // then
            assertEquals("192.168.1.8", clientIpAddress);
        }

        @RepeatedTest(10)
        @DisplayName("X-Forwarded-For 헤더가 blank인 경우")
        void getClientIpAddress_BlankXForwardedFor() {
            // given
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("X-Forwarded-For")).thenReturn("   ");
            when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.9");

            // when
            String clientIpAddress = getClientIpAddress(request);

            // then
            assertEquals("192.168.1.9", clientIpAddress);
        }

    }

    @Nested
    @DisplayName("isValidIpAddress() 테스트")
    class IsValidIpAddressTests {

        @RepeatedTest(10)
        @DisplayName("유효한 IPv4 주소")
        void isValidIpAddress_ValidIPv4() {
            // when & then
            assertTrue(isValidIpAddress("192.168.1.1"));
            assertTrue(isValidIpAddress("10.0.0.1"));
            assertTrue(isValidIpAddress("172.16.0.1"));
        }

        @RepeatedTest(10)
        @DisplayName("유효하지 않은 IP 주소")
        void isValidIpAddress_Invalid() {
            // when & then
            assertFalse(isValidIpAddress("999.99.999"));
            assertFalse(isValidIpAddress("invalid.ip.address"));
            assertFalse(isValidIpAddress("192.168.1"));
            assertFalse(isValidIpAddress("192.168.1.1.1"));
        }

        @Test
        @DisplayName("null IP 주소")
        void isValidIpAddress_Null() {
            // when & then
            assertFalse(isValidIpAddress(null));
        }

        @Test
        @DisplayName("blank IP 주소")
        void isValidIpAddress_Blank() {
            // when & then
            assertFalse(isValidIpAddress("   "));
        }

    }

    @Nested
    @DisplayName("isLocalIpAddress() 테스트")
    class IsLocalIpAddressTests {

        @RepeatedTest(10)
        @DisplayName("로컬 IP 주소 - 10.x.x.x 대역")
        void isLocalIpAddress_ClassA() {
            // when & then
            assertTrue(isLocalIpAddress("10.0.1"));
            assertTrue(isLocalIpAddress("10.255.255"));
        }

        @RepeatedTest(10)
        @DisplayName("로컬 IP 주소 - 172.x.x 대역")
        void isLocalIpAddress_ClassB() {
            // when & then
            assertTrue(isLocalIpAddress("172.16.0.1"));
            assertTrue(isLocalIpAddress("172.31.255.255"));
        }

        @RepeatedTest(10)
        @DisplayName("로컬 IP 주소 - 192.x.x.x 대역")
        void isLocalIpAddress_ClassC() {
            // when & then
            assertTrue(isLocalIpAddress("192.168.0.1"));
            assertTrue(isLocalIpAddress("192.168.255.255"));
        }

        @RepeatedTest(10)
        @DisplayName("로컬 IP 주소 - 특수 IP")
        void isLocalIpAddress_Special() {
            // when & then
            assertTrue(isLocalIpAddress("127.0.0.1"));
            assertTrue(isLocalIpAddress("0:0:0:0:0:1"));
            assertTrue(isLocalIpAddress("::1"));
        }

        @RepeatedTest(10)
        @DisplayName("로컬 IP 주소가 아닌 경우")
        void isLocalIpAddress_NotLocal() {
            // when & then
            assertFalse(isLocalIpAddress("8.8.8"));
            assertFalse(isLocalIpAddress("173.32.0.1"));
            assertFalse(isLocalIpAddress("11.0.0.1"));
        }

        @Test
        @DisplayName("null IP 주소")
        void isLocalIpAddress_Null() {
            // when & then
            assertFalse(isLocalIpAddress(null));
        }

        @Test
        @DisplayName("blank IP 주소")
        void isLocalIpAddress_Blank() {
            // when & then
            assertFalse(isLocalIpAddress("   "));
        }

    }

    @Nested
    @DisplayName("isProxyHeader() 테스트")
    class IsProxyHeaderTests {

        @RepeatedTest(10)
        @DisplayName("프록시 헤더가 포함된 IP 주소")
        void isProxyHeader_ContainsComma() {
            // when & then
            assertTrue(isProxyHeader("192.168.1.1, 10.0.0.1, 172.16.0.1"));
            assertTrue(isProxyHeader("192.168.1.1,10.0.0.1"));
            assertTrue(isProxyHeader("192.168.1.1, 10.0.1, 172.16.0.1"));
        }

        @RepeatedTest(10)
        @DisplayName("프록시 헤더가 포함되지 않은 IP 주소")
        void isProxyHeader_NotContainsComma() {
            // when & then
            assertFalse(isProxyHeader("192.168.1.1"));
            assertFalse(isProxyHeader("2001:0db8:85a3:000:0000:8a2e:0370:7334"));
        }

        @Test
        @DisplayName("null IP 주소")
        void isProxyHeader_Null() {
            // when & then
            assertFalse(isProxyHeader(null));
        }

        @Test
        @DisplayName("blank IP 주소")
        void isProxyHeader_Blank() {
            // when & then
            assertFalse(isProxyHeader("   "));
        }

    }

    @Nested
    @DisplayName("isAllowedIpRange() 테스트")
    class IsAllowedIpRangeTests {

        @RepeatedTest(10)
        @DisplayName("IP 대역 허용 여부 확인 - 항상 true 반환")
        void isAllowedIpRange_AlwaysTrue() {
            // when & then
            assertTrue(isAllowedIpRange("192.168.1.1"));
            assertTrue(isAllowedIpRange("8.8.8.8"));
            assertTrue(isAllowedIpRange("10.0.1"));
            assertTrue(isAllowedIpRange("172.16.0.1"));
        }

        @Test
        @DisplayName("null IP 주소")
        void isAllowedIpRange_Null() {
            // when & then
            assertFalse(isAllowedIpRange(null));
        }

        @Test
        @DisplayName("blank IP 주소")
        void isAllowedIpRange_Blank() {
            // when & then
            assertFalse(isAllowedIpRange("   "));
        }

    }

}
