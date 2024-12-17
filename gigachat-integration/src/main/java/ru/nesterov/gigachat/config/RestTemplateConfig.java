package ru.nesterov.gigachat.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.cert.X509Certificate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate gigachatRestTemplate(RestTemplateBuilder builder) {
        return builder
                .requestFactory(NonValidatingClientHttpRequestFactory::new)
                .build();
    }

    private static class NonValidatingClientHttpRequestFactory extends org.springframework.http.client.SimpleClientHttpRequestFactory {
        @Override
        protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException, IOException {
            if (connection instanceof HttpsURLConnection) {
                HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
                httpsConnection.setSSLSocketFactory(createTrustAllSocketFactory());
                httpsConnection.setHostnameVerifier(createTrustAllHostnameVerifier());
            }
            super.prepareConnection(connection, httpMethod);
        }

        private SSLSocketFactory createTrustAllSocketFactory() {
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{createTrustAllTrustManager()}, new java.security.SecureRandom());
                return sslContext.getSocketFactory();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create a SSL Socket Factory", e);
            }
        }

        private TrustManager createTrustAllTrustManager() {
            return new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {}

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {}

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
        }

        private HostnameVerifier createTrustAllHostnameVerifier() {
            return (hostname, session) -> true;
        }
    }
}