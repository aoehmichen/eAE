package eae.plugin

import groovyx.net.http.AsyncHTTPBuilder
import groovyx.net.http.HTTPBuilder
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.ssl.SSLSocketFactory

import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.SecureRandom
import java.security.cert.X509Certificate

import static groovyx.net.http.ContentType.JSON

public class RestServiceFactory {

    /**
     * Overides all the ssl part to enable self signed certificates....
     * @param uri
     * @return {AsyncHTTPBuilder}
     */
     static def HTTPBuilder initializeHttpBuilder(String uri) {

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, (TrustManager[])[new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() { return null }
            public void checkClientTrusted(X509Certificate[] certs, String authType) { }
            public void checkServerTrusted(X509Certificate[] certs, String authType) { }
        }], new SecureRandom());

        SSLSocketFactory sslSocketFactory = new SSLSocketFactory(sslContext)
        sslSocketFactory.hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER

        def httpBuilder = new AsyncHTTPBuilder([uri: uri, poolSize: 10, contentType: JSON])

        httpBuilder.client.connectionManager.schemeRegistry.register(new Scheme("https", 443, sslSocketFactory))

        return httpBuilder
    }
}
