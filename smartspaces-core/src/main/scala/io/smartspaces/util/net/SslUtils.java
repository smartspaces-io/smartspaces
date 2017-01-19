/*
 * Copyright (C) 2017 Keith M. Hughes
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.smartspaces.util.net;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * Utilities for working with SSL/TLS.
 * 
 * @author Keith M. Hughes
 */
public class SslUtils {

  private static final String SECURITY_PROVIDER_BOUNCY_CASTLE = "BC";
  /**
   * Password for internal dynamic keystores. The APIs insist on a password,
   * even though it won't be used for access.
   */
  private static final String VIRTUAL_KEYSTORE_PASSWORD = "password";

  /**
   * Get an SSL socket factory that provides a client certificate for the socket
   * connections.
   * 
   * @param caCrtFile
   *          file path to the certificate authority certificate
   * @param clientCrtFile
   *          file path to the certificate for the client
   * @param clientKeyFile
   *          file path to the private key for the client
   * 
   * @return the socket factory providing the client functionality
   * 
   * @throws Exception
   */
  public static SSLSocketFactory configureSSLSocketFactory(String caCrtFile, String clientCrtFile,
      String clientKeyFile) throws Exception {
    Security.addProvider(new BouncyCastleProvider());

    JcaX509CertificateConverter certificateConverter =
        new JcaX509CertificateConverter().setProvider(SECURITY_PROVIDER_BOUNCY_CASTLE);

    // load CA certificate
    PEMParser reader = new PEMParser(
        new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(Paths.get(caCrtFile)))));
    X509Certificate caCert =
        certificateConverter.getCertificate((X509CertificateHolder) reader.readObject());
    reader.close();

    // load client certificate
    reader = new PEMParser(new InputStreamReader(
        new ByteArrayInputStream(Files.readAllBytes(Paths.get(clientCrtFile)))));
    X509Certificate cert =
        certificateConverter.getCertificate((X509CertificateHolder) reader.readObject());
    reader.close();

    // load client private key
    JcaPEMKeyConverter keyConverter =
        new JcaPEMKeyConverter().setProvider(SECURITY_PROVIDER_BOUNCY_CASTLE);
    reader = new PEMParser(new InputStreamReader(
        new ByteArrayInputStream(Files.readAllBytes(Paths.get(clientKeyFile)))));
    KeyPair key = keyConverter.getKeyPair((PEMKeyPair) reader.readObject());
    reader.close();

    // CA certificate is used to authenticate server
    KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
    caKs.load(null, null);
    caKs.setCertificateEntry("ca-certificate", caCert);
    TrustManagerFactory tmf =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(caKs);

    // client key and certificates are sent to server so it can authenticate
    // the client.F
    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
    ks.load(null, null);
    ks.setCertificateEntry("certificate", cert);

    // This assumes that the client key is not password protected. We need a
    // password, but it could be anything.
    char[] password = VIRTUAL_KEYSTORE_PASSWORD.toCharArray();
    ks.setKeyEntry("private-key", key.getPrivate(), password,
        new java.security.cert.Certificate[] { cert });
    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmf.init(ks, password);

    // finally, create SSL socket factory.
    SSLContext context = SSLContext.getInstance("TLS");
    context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

    return context.getSocketFactory();
  }

  /**
   * Create a socket factory from a keystore.
   * 
   * <p>
   * This will support certs for the server side, but no client certs.
   * 
   * @param keystorePath
   *          the file path to the key store
   * @param keystorePassword
   *          the password for the key store
   * 
   * @return the socket factory
   * 
   * @throws Exception
   */
  public static SSLSocketFactory configureSslSocketFactory(String keystorePath,
      String keystorePassword) throws Exception {
    KeyStore ks = KeyStore.getInstance("JKS");

    FileInputStream jksInputStream = new FileInputStream(keystorePath);
    ks.load(jksInputStream, keystorePassword.toCharArray());

    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmf.init(ks, keystorePassword.toCharArray());

    TrustManagerFactory tmf =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(ks);

    SSLContext sc = SSLContext.getInstance("TLS");
    TrustManager[] trustManagers = tmf.getTrustManagers();
    sc.init(kmf.getKeyManagers(), trustManagers, null);

    return sc.getSocketFactory();
  }
}
