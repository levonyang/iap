package com.haizhi.iap.follow.service;

import com.haizhi.iap.common.exception.ServiceAccessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Slf4j
@Service
public class ClientConnectionPool {

    private static CloseableHttpClient httpClient = null;

    @PostConstruct
    private void init(){
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.create();
        ConnectionSocketFactory plainSF = new PlainConnectionSocketFactory();
        registryBuilder.register("controller",plainSF);

        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            TrustStrategy anyTrustStrategy = new TrustStrategy() {//默认所有证书都可信
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            };
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(trustStore, anyTrustStrategy).build();
            LayeredConnectionSocketFactory sslSF = new SSLConnectionSocketFactory(sslContext);
            registryBuilder
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslSF);
        }catch (Exception e){
            log.error("https create error",e);
        }

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registryBuilder.build());
        cm.setMaxTotal(10);
        cm.setDefaultMaxPerRoute(10);
        HttpHost localhost = new HttpHost("localhost",80);
        cm.setMaxPerRoute(new HttpRoute(localhost),10);
        httpClient = HttpClients.custom().setConnectionManager(cm).build();

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 执行http请求
     * @param request
     * @return
     * @throws IOException
     */
    public String execute(final HttpUriRequest request) throws IOException, URISyntaxException {
        if(httpClient == null){
            init();
        }
        CloseableHttpResponse response = null;
        String responseStr = null;
        try {
            response = httpClient.execute(request);

            if(request instanceof HttpPost){
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY
                        || response.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) {
                    // 从头中取出转向的地址
                    Header[] hs = response.getHeaders("Location");
                    Header locationHeader = hs[0];
                    String location = null;
                    if (locationHeader != null) {
                        ((HttpPost) request).setURI(new URI(locationHeader.getValue()));
                        log.warn("The page was redirected to:" + location);
                        response = httpClient.execute(request);//用跳转后的页面重新请求。
                    }
                }
            }

            if (response.getStatusLine().getStatusCode() != Response.Status.OK.getStatusCode()) {
                log.error(response.toString());
                throw new ServiceAccessException(-1, response.toString());
            }

            responseStr = EntityUtils.toString(response.getEntity(), Charset.forName("utf-8"));
        }finally {
            if(response != null)
                response.close();
        }

        return responseStr;
    }

    @PreDestroy
    private void shutDown(){
        if(httpClient != null){
            try {
                httpClient.close();
            } catch (IOException e) {

            }
        }
    }

}