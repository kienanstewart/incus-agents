/*
 * SPDX-FileCopyrightText: 2025 Kienan Stewart <kstewart@efficios.com>
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.efficios.jenkins.plugins.incus_agent;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.security.ACL;
import hudson.slaves.Cloud;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class Incus extends Cloud {

    private String authType;
    private String publicCertificate;
    private String clientCertificateId;
    private String project;
    private String protocol;
    private String url;

    private List<String> apiVersions;
    private List<String> authMethods;

    private static final Logger LOGGER = Logger.getLogger(Incus.class.getName());

    @DataBoundConstructor
    public Incus(
            String name,
            String authType,
            String publicCertificate,
            String clientCertificateId,
            String project,
            String protocol,
            String url,
            List<String> apiVersions,
            List<String> authMethods) {
        super(name);
        this.authType = authType;
        this.publicCertificate = publicCertificate;
        this.clientCertificateId = clientCertificateId;
        this.project = project;
        this.protocol = protocol;
        this.url = url;
        this.apiVersions = apiVersions;
        this.authMethods = authMethods;
    }

    @Override
    public boolean canProvision(Label label) {
        return false;
    }

    public String getAuthType() {
        return this.authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getClientCertificateId() {
        return this.clientCertificateId;
    }

    public void setClientCertificateId(String clientCertificateId) {
        this.clientCertificateId = clientCertificateId;
    }

    public String getPublicCertificate() {
        return this.publicCertificate;
    }

    public void setPublicCertificate(String publicCertificate) {
        this.publicCertificate = publicCertificate;
    }

    public String getProject() {
        return this.project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private CloseableHttpClient httpClient() {
        return _httpClient(this.publicCertificate, this.clientCertificateId);
    }

    private static KeyStore _publicCertificateKeyStore(String publicCertificate) {
        KeyStore keyStore = null;
        try {
            ByteArrayInputStream certStream = new ByteArrayInputStream(publicCertificate.getBytes("UTF-8"));
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(certStream);

            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            keyStore.setCertificateEntry("incus", certificate);
        } catch (java.io.UnsupportedEncodingException e) {
            LOGGER.warning("EncodingException: " + e);
            keyStore = null;
        } catch (java.io.IOException e) {
            LOGGER.warning("IOException: " + e);
            keyStore = null;
        } catch (java.security.cert.CertificateException e) {
            LOGGER.warning("CertificateException: " + e);
            keyStore = null;
        } catch (java.security.KeyStoreException e) {
            LOGGER.warning("KeyStoreException: " + e);
            keyStore = null;
        } catch (java.security.NoSuchAlgorithmException e) {
            LOGGER.warning("NoSuchAlgorithmException: " + e);
            keyStore = null;
        }
        return keyStore;
    }

    private static CloseableHttpClient _httpClient(String publicCertificate, String clientCertificateId) {
        // Trust the given public certificate, if any.
        HttpClientConnectionManager connectionManager = null;
        SSLContext sslContext = null;
        KeyStore keyStore = _publicCertificateKeyStore(publicCertificate);

        KeyStore clientKeyStore = null;
        try {
            clientKeyStore = null;
            StandardCertificateCredentials certCredentials =
                    (StandardCertificateCredentials) CredentialsProvider.lookupCredentialsInItem(
                                    StandardCertificateCredentials.class, null, null, java.util.Collections.emptyList())
                            .get(0);
            if (certCredentials != null) {
                clientKeyStore = certCredentials.getKeyStore();
            }
        } catch (Exception e) {
            LOGGER.warning("Exception trying to build key store: " + e);
        }

        SSLContextBuilder sslContextBuilder = SSLContexts.custom();
        if (keyStore != null) {
            try {
                sslContextBuilder = sslContextBuilder.loadTrustMaterial(keyStore, null);
            } catch (Exception e) {
                LOGGER.warning("Exception: " + e);
            }
        }

        if (clientKeyStore != null) {
            try {
                sslContextBuilder = sslContextBuilder.loadKeyMaterial(clientKeyStore, null, null);
            } catch (Exception e) {
                LOGGER.warning("Exception: " + e);
            }
        }

        try {
            sslContextBuilder.loadTrustMaterial((KeyStore) null, null);
            sslContext = sslContextBuilder.build();
        } catch (Exception e) {
            sslContext = null;
            LOGGER.warning("Exception: " + e);
        }

        if (sslContext != null) {
            SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                    .setSslContext(sslContext)
                    .build();
            connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(sslSocketFactory)
                    .build();
            LOGGER.info("Using custom connection manager with a custom HttpClient");
            return HttpClients.custom().setConnectionManager(connectionManager).build();
        }

        LOGGER.info("Using default HttpClient");
        return HttpClients.createDefault();
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<Cloud> {
        @Override
        public String getDisplayName() {
            return "Incus Remote";
        }

        public ListBoxModel doFillAuthTypeItems() {
            return new ListBoxModel().add("tls").add("oidc");
        }

        public ListBoxModel doFillProtocolItems() {
            return new ListBoxModel().add("incus").add("simplestreams");
        }

        public ListBoxModel doFillClientCertificateIdItems(@QueryParameter String clientCertificateId) {
            StandardListBoxModel result = new StandardListBoxModel();
            result.includeMatchingAs(
                    ACL.SYSTEM2,
                    Jenkins.get(),
                    StandardCertificateCredentials.class,
                    java.util.Collections.emptyList(),
                    CredentialsMatchers.always());
            return result.includeEmptyValue().includeCurrentValue(clientCertificateId);
        }


        public FormValidation doCheckAuthentication(
                @QueryParameter("url") String url,
                @QueryParameter("publicCertificate") String publicCertificate,
                @QueryParameter("clientCertificateId") String clientCertificateId,
                @QueryParameter("authType") String authType,
                @QueryParameter("project") String project,
                @QueryParameter("ptocol") String protocol)

        {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            CloseableHttpClient client = _httpClient(publicCertificate, clientCertificateId);

            JSONObject response;
            try{
                response = (JSONObject) JSONSerializer.toJSON(Request.get(url).execute(client).returnContent().asString());
                if (response.getInt("status_code") != 200) {
                    return FormValidation.error("Checking available API versions status code: " + response.getInt("status_code"));
                }

                List apiVersions = (List) JSONArray.toCollection(response.getJSONArray("metadata"));
                LOGGER.info("Valid API versions: " + apiVersions);

                String apiVersion = (String) apiVersions.get(apiVersions.size() - 1);
                LOGGER.info("Using API version: " + apiVersion);

                response = (JSONObject) JSONSerializer.toJSON(Request.get(url + apiVersion).execute(client).returnContent().asString());
                if (response.getInt("status_code") != 200) {
                    return FormValidation.error("Checking authentication with API status code: " + response.getInt("status_code"));
                }

                JSONObject metadata = response.getJSONObject("metadata");
                if (!metadata.getString("auth").equals("trusted")) {
                    LOGGER.warning(metadata.toString());
                    return FormValidation.warning("Authentication is not trusted, got " + metadata.getString("auth"));
                }
                return FormValidation.ok("Authenticated trusted with API version " + apiVersion);
           }
            catch (java.io.IOException e) {
                LOGGER.warning("IOException: " + e);
            }
            finally {
                try {
                    client.close();
                } catch (java.io.IOException e) {}
            }
            return FormValidation.ok("OK");
        }

        public FormValidation doCheckUrl(
                @QueryParameter("url") String url,
                @QueryParameter("publicCertificate") String publicCertificate,
                @QueryParameter("clientCertificateId") String clientCertificateId) {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);

            JSONObject response;
            CloseableHttpClient client = null;
            try {
                client = _httpClient(publicCertificate, clientCertificateId);
                response = (JSONObject) JSONSerializer.toJSON(
                        Request.get(url).execute(client).returnContent().asString());
            } catch (java.io.IOException e) {
                return FormValidation.error("IOException: " + e);
            } finally {
                if (client != null) {
                    try {
                        client.close();
                    } catch (java.io.IOException e) {
                        LOGGER.warning("IOException: " + e);
                        // Do nothing?
                    }
                }
            }

            if (response.getInt("status_code") == 200) {
                return FormValidation.ok("Got status code 200");
            }

            return FormValidation.warning("Status code " + Integer.toString(response.getInt("status_code")));
        }
    }
}
