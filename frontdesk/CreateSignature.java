package frontdesk;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cms.CMSAbsentContent;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.DefaultSignedAttributeTableGenerator;
import org.bouncycastle.cms.SignerInfoGeneratorBuilder;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.util.Store;
//import org.junit.BeforeClass;
//import org.junit.Test;

/**
 * @author mkl
 */
public class CreateSignature
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "sign");

    public static final String KEYSTORE = "keystores/demo-rsa2048.ks"; 
    public static final char[] PASSWORD = "demo-rsa2048".toCharArray(); 

    public static KeyStore ks = null;
    public static PrivateKey pk = null;
    public static Certificate[] chain = null;

//    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();

        BouncyCastleProvider bcp = new BouncyCastleProvider();
        Security.insertProviderAt(bcp, 1);

        ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(new FileInputStream(KEYSTORE), PASSWORD);
        String alias = (String) ks.aliases().nextElement();
        pk = (PrivateKey) ks.getKey(alias, PASSWORD);
        chain = ks.getCertificateChain(alias);
    }

    /**
     * <a href="http://stackoverflow.com/questions/41767351/create-pkcs7-signature-from-file-digest">
     * Create pkcs7 signature from file digest
     * </a>
     * <p>
     * This test uses the OP's own <code>sign</code> method: {@link #signBySnox(InputStream)}.
     * There are small errors in it, so the result is rejected by verification. These errors
     * are corrected in {@link #signWithSeparatedHashing(InputStream)} which is tested in
     * {@link #testSignWithSeparatedHashing()}.
     * </p>
     */
//    @Test
    public void testSignWithSeparatedHashingLikeSnox() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("test.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "testSignedLikeSnox.pdf"));
                PDDocument pdDocument = PDDocument.load(resource)   )
        {
            sign(pdDocument, result, data -> signBySnox(data));
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/41767351/create-pkcs7-signature-from-file-digest">
     * Create pkcs7 signature from file digest
     * </a>
     * <p>
     * This test uses a fixed version of the OP's <code>sign</code> method:
     * {@link #signWithSeparatedHashing(InputStream)}. Here the errors from
     * {@link #signBySnox(InputStream)} are corrected, so the result is not
     * rejected by verification anymore.
     * </p>
     */
//    @Test
    public void testSignWithSeparatedHashing() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("test.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "testSignedWithSeparatedHashing.pdf"));
                PDDocument pdDocument = PDDocument.load(resource)   )
        {
            sign(pdDocument, result, data -> signWithSeparatedHashing(data));
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/41767351/create-pkcs7-signature-from-file-digest">
     * Create pkcs7 signature from file digest
     * </a>
     * <p>
     * A minimal signing frame work merely requiring a {@link SignatureInterface}
     * instance.
     * </p>
     */
    void sign(PDDocument document, OutputStream output, SignatureInterface signatureInterface) throws IOException
    {
        PDSignature signature = new PDSignature();
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        signature.setName("Example User");
        signature.setLocation("Los Angeles, CA");
        signature.setReason("Testing");
        signature.setSignDate(Calendar.getInstance());
        document.addSignature(signature);
        ExternalSigningSupport externalSigning =
                document.saveIncrementalForExternalSigning(output);
        // invoke external signature service
        byte[] cmsSignature = signatureInterface.sign(externalSigning.getContent());
        // set signature bytes received from the service
        externalSigning.setSignature(cmsSignature);
    }

    /**
     * <a href="http://stackoverflow.com/questions/41767351/create-pkcs7-signature-from-file-digest">
     * Create pkcs7 signature from file digest
     * </a>
     * <p>
     * The OP's own <code>sign</code> method which has some errors. These
     * errors are fixed in {@link #signWithSeparatedHashing(InputStream)}.
     * </p>
     */
    public byte[] signBySnox(InputStream content) throws IOException {
        // testSHA1WithRSAAndAttributeTable
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1", "BC");
            List<Certificate> certList = new ArrayList<Certificate>();
            CMSTypedData msg = new CMSProcessableByteArray(IOUtils.toByteArray(content));

            certList.addAll(Arrays.asList(chain));

            Store certs = new JcaCertStore(certList);

            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();

            Attribute attr = new Attribute(CMSAttributes.messageDigest,
                    new DERSet(new DEROctetString(md.digest(IOUtils.toByteArray(content)))));

            ASN1EncodableVector v = new ASN1EncodableVector();

            v.add(attr);

            SignerInfoGeneratorBuilder builder = new SignerInfoGeneratorBuilder(new BcDigestCalculatorProvider())
                    .setSignedAttributeGenerator(new DefaultSignedAttributeTableGenerator(new AttributeTable(v)));

            AlgorithmIdentifier sha1withRSA = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1withRSA");

            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(chain[0].getEncoded());
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);

            gen.addSignerInfoGenerator(builder.build(
                    new BcRSAContentSignerBuilder(sha1withRSA,
                            new DefaultDigestAlgorithmIdentifierFinder().find(sha1withRSA))
                                    .build(PrivateKeyFactory.createKey(pk.getEncoded())),
                    new JcaX509CertificateHolder(cert)));

            gen.addCertificates(certs);

            CMSSignedData s = gen.generate(new CMSAbsentContent(), false);
            return new CMSSignedData(msg, s.getEncoded()).getEncoded();

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e);
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/41767351/create-pkcs7-signature-from-file-digest">
     * Create pkcs7 signature from file digest
     * </a>
     * <p>
     * The OP's <code>sign</code> method after fixing some errors. The
     * OP's original method is {@link #signBySnox(InputStream)}. The
     * errors were
     * </p>
     * <ul>
     * <li>multiple attempts at reading the {@link InputStream} parameter;
     * <li>convoluted creation of final CMS container.
     * </ul>
     * <p>
     * Additionally this method uses SHA256 instead of SHA-1.
     * </p>
     */
    public byte[] signWithSeparatedHashing(InputStream content) throws IOException
    {
        try
        {
            // Digest generation step
            MessageDigest md = MessageDigest.getInstance("SHA256", "BC");
            byte[] digest = md.digest(IOUtils.toByteArray(content));

            // Separate signature container creation step
            List<Certificate> certList = Arrays.asList(chain);
            JcaCertStore certs = new JcaCertStore(certList);

            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();

            Attribute attr = new Attribute(CMSAttributes.messageDigest,
                    new DERSet(new DEROctetString(digest)));

            ASN1EncodableVector v = new ASN1EncodableVector();

            v.add(attr);

            SignerInfoGeneratorBuilder builder = new SignerInfoGeneratorBuilder(new BcDigestCalculatorProvider())
                    .setSignedAttributeGenerator(new DefaultSignedAttributeTableGenerator(new AttributeTable(v)));

            AlgorithmIdentifier sha256withRSA = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256withRSA");

            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(chain[0].getEncoded());
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);

            gen.addSignerInfoGenerator(builder.build(
                    new BcRSAContentSignerBuilder(sha256withRSA,
                            new DefaultDigestAlgorithmIdentifierFinder().find(sha256withRSA))
                                    .build(PrivateKeyFactory.createKey(pk.getEncoded())),
                    new JcaX509CertificateHolder(cert)));

            gen.addCertificates(certs);

            CMSSignedData s = gen.generate(new CMSAbsentContent(), false);
            return s.getEncoded();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new IOException(e);
        }
    }
}
///*
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
////package org.apache.pdfbox.examples.signature;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.security.GeneralSecurityException;
//import java.security.KeyStore;
//import java.security.KeyStoreException;
//import java.security.NoSuchAlgorithmException;
//import java.security.UnrecoverableKeyException;
//import java.security.cert.CertificateException;
//import java.util.Calendar;
//
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
//import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
//import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
//
///**
// * An example for signing a PDF with bouncy castle.
// * A keystore can be created with the java keytool, for example:
// *
// * {@code keytool -genkeypair -storepass 123456 -storetype pkcs12 -alias test -validity 365
// *        -v -keyalg RSA -keystore keystore.p12 }
// *
// * @author Thomas Chojecki
// * @author Vakhtang Koroghlishvili
// * @author John Hewson
// */
//public class CreateSignature extends CreateSignatureBase
//{
//
//    /**
//     * Initialize the signature creator with a keystore and certficate password.
//     *
//     * @param keystore the pkcs12 keystore containing the signing certificate
//     * @param pin the password for recovering the key
//     * @throws KeyStoreException if the keystore has not been initialized (loaded)
//     * @throws NoSuchAlgorithmException if the algorithm for recovering the key cannot be found
//     * @throws UnrecoverableKeyException if the given password is wrong
//     * @throws CertificateException if the certificate is not valid as signing time
//     * @throws IOException if no certificate could be found
//     */
//    public CreateSignature(KeyStore keystore, char[] pin)
//            throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, IOException
//    {
//        super(keystore, pin);
//    }
//
//    /**
//     * Signs the given PDF file. Alters the original file on disk.
//     * @param file the PDF file to sign
//     * @throws IOException if the file could not be read or written
//     */
//    public void signDetached(File file) throws IOException
//    {
//        signDetached(file, file, null);
//    }
//
//    /**
//     * Signs the given PDF file.
//     * @param inFile input PDF file
//     * @param outFile output PDF file
//     * @throws IOException if the input file could not be read
//     */
//    public void signDetached(File inFile, File outFile) throws IOException
//    {
//        signDetached(inFile, outFile, null);
//    }
//
//    /**
//     * Signs the given PDF file.
//     * @param inFile input PDF file
//     * @param outFile output PDF file
//     * @param tsaUrl optional TSA url
//     * @throws IOException if the input file could not be read
//     */
//    public void signDetached(File inFile, File outFile, String tsaUrl) throws IOException
//    {
//        if (inFile == null || !inFile.exists())
//        {
//            throw new FileNotFoundException("Document for signing does not exist");
//        }
//        
//        setTsaUrl(tsaUrl);
//
//        // sign
//        try (FileOutputStream fos = new FileOutputStream(outFile);
//            PDDocument doc = PDDocument.load(inFile))
//        {
//            signDetached(doc, fos);
//        }
//    }
//
//    public void signDetached(PDDocument document, OutputStream output)
//            throws IOException
//    {
//        int accessPermissions = SigUtils.getMDPPermission(document);
//        if (accessPermissions == 1)
//        {
//            throw new IllegalStateException("No changes to the document are permitted due to DocMDP transform parameters dictionary");
//        }     
//
//        // create signature dictionary
//        PDSignature signature = new PDSignature();
//        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
//        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
//        signature.setName("Example User");
//        signature.setLocation("Los Angeles, CA");
//        signature.setReason("Testing");
//        // TODO extract the above details from the signing certificate? Reason as a parameter?
//
//        // the signing date, needed for valid signature
//        signature.setSignDate(Calendar.getInstance());
//
//        // Optional: certify 
//        if (accessPermissions == 0)
//        {
//            SigUtils.setMDPPermission(document, signature, 2);
//        }        
//
//        if (isExternalSigning())
//        {
//            document.addSignature(signature);
//            ExternalSigningSupport externalSigning =
//                    document.saveIncrementalForExternalSigning(output);
//            // invoke external signature service
//            byte[] cmsSignature = sign(externalSigning.getContent());
//            // set signature bytes received from the service
//            externalSigning.setSignature(cmsSignature);
//        }
//        else
//        {
//            SignatureOptions signatureOptions = new SignatureOptions();
//            // Size can vary, but should be enough for purpose.
//            signatureOptions.setPreferredSignatureSize(SignatureOptions.DEFAULT_SIGNATURE_SIZE * 2);
//            // register signature dictionary and sign interface
//            document.addSignature(signature, this, signatureOptions);
//
//            // write incremental (only for signing purpose)
//            document.saveIncremental(output);
//        }
//    }
//
//    public static void main(String[] args) throws IOException, GeneralSecurityException
//    {
////        if (args.length < 3)
////        {
////            usage();
////            System.exit(1);
////        }
//
//        String tsaUrl = null;
//        boolean externalSig = false;
////        for (int i = 0; i < args.length; i++)
////        {
////            if (args[i].equals("-tsa"))
////            {
////                i++;
////                if (i >= args.length)
////                {
////                    usage();
////                    System.exit(1);
////                }
////                tsaUrl = args[i];
////            }
////            if (args[i].equals("-e"))
////            {
//                externalSig = true;
////            }
////        }
//
//        // load the keystore
//        KeyStore keystore = KeyStore.getInstance("PKCS12");
//        String testttt = "PASSWORD";
//        char[] password = testttt.toCharArray(); // TODO use Java 6 java.io.Console.readPassword
//        keystore.load(new FileInputStream("TESTpkcs12"), password);
//        // TODO alias command line argument
//
//        // sign PDF
//        CreateSignature signing = new CreateSignature(keystore, password);
//        signing.setExternalSigning(externalSig);
//
//        File inFile = new File("WO1.pdf");
//        String name = inFile.getName();
//        String substring = name.substring(0, name.lastIndexOf('.'));
//
//        File outFile = new File(inFile.getParent(), substring + "_signed.pdf");
//        signing.signDetached(inFile, outFile, tsaUrl);
//    }
//
//    private static void usage()
//    {
//        System.err.println("usage: java " + CreateSignature.class.getName() + " " +
//                           "<pkcs12_keystore> <password> <pdf_to_sign>\n" + "" +
//                           "options:\n" +
//                           "  -tsa <url>    sign timestamp using the given TSA server\n" +
//                           "  -e            sign using external signature creation scenario");
//    }
//}