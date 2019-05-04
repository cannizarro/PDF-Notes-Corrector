/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tom_roush.pdfbox.encryption;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import com.tom_roush.pdfbox.io.IOUtils;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDDocumentCatalog;
import com.tom_roush.pdfbox.pdmodel.PDDocumentNameDictionary;
import com.tom_roush.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import com.tom_roush.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import com.tom_roush.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission;
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import com.tom_roush.pdfbox.rendering.PDFRenderer;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for symmetric key encryption.
 *
 * IMPORTANT! When making changes in the encryption / decryption methods, do
 * also check whether the six generated encrypted files (to be found in
 * pdfbox/target/test-output/crypto and named *encrypted.pdf) can be opened with
 * Adobe Reader by providing the owner password and the user password.
 *
 * @author Ralf Hauser
 * @author Tilman Hausherr
 *
 */
public class TestSymmetricKeyEncryption
{

    private File testResultsDir;

    private AccessPermission permission;

    static final String USERPASSWORD = "1234567890abcdefghijk1234567890abcdefghijk";
    static final String OWNERPASSWORD = "abcdefghijk1234567890abcdefghijk1234567890";

    Context testContext;

    @Before
    public void setUp() throws Exception
    {
        if (Cipher.getMaxAllowedKeyLength("AES") != Integer.MAX_VALUE)
        {
            // we need strong encryption for these tests
//            fail("JCE unlimited strength jurisdiction policy files are not installed");
        }

        testContext = InstrumentationRegistry.getInstrumentation().getContext();
        PDFBoxResourceLoader.init(testContext);
        testResultsDir = new File(android.os.Environment.getExternalStorageDirectory(), "Download/pdfbox-test-output/crypto");
        testResultsDir.mkdirs();

        permission = new AccessPermission();
        permission.setCanAssembleDocument(false);
        permission.setCanExtractContent(false);
        permission.setCanExtractForAccessibility(true);
        permission.setCanFillInForm(false);
        permission.setCanModify(false);
        permission.setCanModifyAnnotations(false);
        permission.setCanPrint(true);
        permission.setCanPrintDegraded(false);
        permission.setReadOnly();
    }

    /**
     * Test that permissions work as intended: the user psw ("user") is enough
     * to open the PDF with possibly restricted rights, the owner psw ("owner")
     * gives full permissions. The 3 files of this test were created by Maruan
     * Sayhoun, NOT with PDFBox, but with Adobe Acrobat to ensure "the gold
     * standard". The restricted permissions prevent printing and text
     * extraction. In the 128 and 256 bit encrypted files, AssembleDocument,
     * ExtractForAccessibility and PrintDegraded are also disabled.
     */
    @Test
    public void testPermissions() throws Exception
    {
        AccessPermission fullAP = new AccessPermission();
        AccessPermission restrAP = new AccessPermission();
        restrAP.setCanPrint(false);
        restrAP.setCanExtractContent(false);
        restrAP.setCanModify(false);

        byte[] inputFileAsByteArray = getFileResourceAsByteArray("PasswordSample-40bit.pdf");
        checkPerms(inputFileAsByteArray, "owner", fullAP);
        checkPerms(inputFileAsByteArray, "user", restrAP);
        try
        {
            checkPerms(inputFileAsByteArray, "", null);
            fail("wrong password not detected");
        }
        catch (IOException ex)
        {
            assertEquals("Cannot decrypt PDF, the password is incorrect", ex.getMessage());
        }

        restrAP.setCanAssembleDocument(false);
        restrAP.setCanExtractForAccessibility(false);
        restrAP.setCanPrintDegraded(false);

        inputFileAsByteArray = getFileResourceAsByteArray("PasswordSample-128bit.pdf");
        checkPerms(inputFileAsByteArray, "owner", fullAP);
        checkPerms(inputFileAsByteArray, "user", restrAP);
        try
        {
            checkPerms(inputFileAsByteArray, "", null);
            fail("wrong password not detected");
        }
        catch (IOException ex)
        {
            assertEquals("Cannot decrypt PDF, the password is incorrect", ex.getMessage());
        }

//        inputFileAsByteArray = getFileResourceAsByteArray("PasswordSample-256bit.pdf");
//        checkPerms(inputFileAsByteArray, "owner", fullAP);
//        checkPerms(inputFileAsByteArray, "user", restrAP);
//        try
//        {
//            checkPerms(inputFileAsByteArray, "", null);
//            fail("wrong password not detected");
//        }
//        catch (IOException ex)
//        {
//            assertEquals("Cannot decrypt PDF, the password is incorrect", ex.getMessage());
//        } TODO: PdfBox-Android
    }

    private void checkPerms(byte[] inputFileAsByteArray, String password,
        AccessPermission expectedPermissions) throws IOException
    {
        PDDocument doc = PDDocument.load(
            new ByteArrayInputStream(inputFileAsByteArray),
            password);

        AccessPermission currentAccessPermission = doc.getCurrentAccessPermission();

        // check permissions
        assertEquals(expectedPermissions.isOwnerPermission(), currentAccessPermission.isOwnerPermission());
        assertEquals(expectedPermissions.isReadOnly(), currentAccessPermission.isReadOnly());
        assertEquals(expectedPermissions.canAssembleDocument(), currentAccessPermission.canAssembleDocument());
        assertEquals(expectedPermissions.canExtractContent(), currentAccessPermission.canExtractContent());
        assertEquals(expectedPermissions.canExtractForAccessibility(), currentAccessPermission.canExtractForAccessibility());
        assertEquals(expectedPermissions.canFillInForm(), currentAccessPermission.canFillInForm());
        assertEquals(expectedPermissions.canModify(), currentAccessPermission.canModify());
        assertEquals(expectedPermissions.canModifyAnnotations(), currentAccessPermission.canModifyAnnotations());
        assertEquals(expectedPermissions.canPrint(), currentAccessPermission.canPrint());
        assertEquals(expectedPermissions.canPrintDegraded(), currentAccessPermission.canPrintDegraded());

//        new PDFRenderer(doc).renderImage(0); TODO: PdfBox-Android

        doc.close();
    }

    /**
     * Protect a document with a key and try to reopen it with that key and
     * compare.
     *
     * @throws Exception If there is an unexpected error during the test.
     */
    @Test
    public void testProtection() throws Exception
    {
        byte[] inputFileAsByteArray = getFileResourceAsByteArray("Acroform-PDFBOX-2333.pdf");
        int sizePriorToEncryption = inputFileAsByteArray.length;

        testSymmEncrForKeySize(40, sizePriorToEncryption, inputFileAsByteArray,
            USERPASSWORD, OWNERPASSWORD, permission);

        testSymmEncrForKeySize(128, sizePriorToEncryption, inputFileAsByteArray,
            USERPASSWORD, OWNERPASSWORD, permission);

//        testSymmEncrForKeySize(256, sizePriorToEncryption, inputFileAsByteArray,
//                USERPASSWORD, OWNERPASSWORD, permission); TODO: PdfBox-Android
    }

    /**
     * Protect a document with an embedded PDF with a key and try to reopen it
     * with that key and compare.
     *
     * @throws Exception If there is an unexpected error during the test.
     */
    @Test
    public void testProtectionInnerAttachment() throws Exception
    {
        String testFileName = "preEnc_20141025_105451.pdf";
        byte[] inputFileWithEmbeddedFileAsByteArray = getFileResourceAsByteArray(testFileName);

        int sizeOfFileWithEmbeddedFile = inputFileWithEmbeddedFileAsByteArray.length;

        File extractedEmbeddedFile
            = extractEmbeddedFile(new ByteArrayInputStream(inputFileWithEmbeddedFileAsByteArray), "innerFile.pdf");

        testSymmEncrForKeySizeInner(40, sizeOfFileWithEmbeddedFile,
            inputFileWithEmbeddedFileAsByteArray, extractedEmbeddedFile, USERPASSWORD, OWNERPASSWORD);

        testSymmEncrForKeySizeInner(128, sizeOfFileWithEmbeddedFile,
            inputFileWithEmbeddedFileAsByteArray, extractedEmbeddedFile, USERPASSWORD, OWNERPASSWORD);

//        testSymmEncrForKeySizeInner(256, sizeOfFileWithEmbeddedFile,
//                inputFileWithEmbeddedFileAsByteArray, extractedEmbeddedFile, USERPASSWORD, OWNERPASSWORD); TODO: PdfBox-Android
    }

    private void testSymmEncrForKeySize(int keyLength,
        int sizePriorToEncr, byte[] inputFileAsByteArray,
        String userpassword, String ownerpassword,
        AccessPermission permission) throws IOException
    {
        PDDocument document = PDDocument.load(new ByteArrayInputStream(inputFileAsByteArray));
        String prefix = "Simple-";
        int numSrcPages = document.getNumberOfPages();
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        List<Bitmap> srcImgTab = new ArrayList<Bitmap>();
        List<byte[]> srcContentStreamTab = new ArrayList<byte[]>();
        for (int i = 0; i < numSrcPages; ++i)
        {
//            srcImgTab.add(pdfRenderer.renderImage(i)); TODO: PdfBox-Android
            InputStream unfilteredStream = document.getPage(i).getContents();
            byte[] bytes = IOUtils.toByteArray(unfilteredStream);
            unfilteredStream.close();
            srcContentStreamTab.add(bytes);
        }

        PDDocument encryptedDoc = encrypt(keyLength, sizePriorToEncr, document,
            prefix, permission, userpassword, ownerpassword);

        assertEquals(numSrcPages, encryptedDoc.getNumberOfPages());
        pdfRenderer = new PDFRenderer(encryptedDoc);
        for (int i = 0; i < encryptedDoc.getNumberOfPages(); ++i)
        {
            // compare rendering
//            Bitmap bim = pdfRenderer.renderImage(i);
//            ValidateXImage.checkIdent(bim, srcImgTab.get(i)); TODO: PdfBox-Android

            // compare content streams
            InputStream unfilteredStream = encryptedDoc.getPage(i).getContents();
            byte[] bytes = IOUtils.toByteArray(unfilteredStream);
            unfilteredStream.close();
            Assert.assertArrayEquals("content stream of page " + i + " not identical",
                srcContentStreamTab.get(i), bytes);
        }

        File pdfFile = new File(testResultsDir, prefix + keyLength + "-bit-decrypted.pdf");
        encryptedDoc.setAllSecurityToBeRemoved(true);
        encryptedDoc.save(pdfFile);
        encryptedDoc.close();
    }

    // encrypt with keylength and permission, save, check sizes before and after encryption
    // reopen, decrypt and return document
    private PDDocument encrypt(int keyLength, int sizePriorToEncr,
        PDDocument doc, String prefix, AccessPermission permission,
        String userpassword, String ownerpassword) throws IOException
    {
        AccessPermission ap = new AccessPermission();
        StandardProtectionPolicy spp = new StandardProtectionPolicy(ownerpassword, userpassword, ap);
        spp.setEncryptionKeyLength(keyLength);
        spp.setPermissions(permission);

        // This must have no effect and should only log a warning.
        doc.setAllSecurityToBeRemoved(true);

        doc.protect(spp);

        File pdfFile = new File(testResultsDir, prefix + keyLength + "-bit-encrypted.pdf");

        doc.save(pdfFile);
        doc.close();
        long sizeEncrypted = pdfFile.length();
        Assert.assertTrue(keyLength
                + "-bit encrypted pdf should not have same size as plain one",
            sizeEncrypted != sizePriorToEncr);

        PDDocument encryptedDoc;

        // test with owner password => full permissions
        encryptedDoc = PDDocument.load(pdfFile, ownerpassword);
        Assert.assertTrue(encryptedDoc.isEncrypted());
        Assert.assertTrue(encryptedDoc.getCurrentAccessPermission().isOwnerPermission());
        encryptedDoc.close();

        // test with owner password => restricted permissions
        encryptedDoc = PDDocument.load(pdfFile, userpassword);
        Assert.assertTrue(encryptedDoc.isEncrypted());
        Assert.assertFalse(encryptedDoc.getCurrentAccessPermission().isOwnerPermission());

        assertEquals(permission.getPermissionBytes(), encryptedDoc.getCurrentAccessPermission().getPermissionBytes());

        return encryptedDoc;
    }

    // extract the embedded file, saves it, and return the extracted saved file
    private File extractEmbeddedFile(InputStream pdfInputStream, String name) throws IOException
    {
        PDDocument docWithEmbeddedFile;
        docWithEmbeddedFile = PDDocument.load(pdfInputStream);
        PDDocumentCatalog catalog = docWithEmbeddedFile.getDocumentCatalog();
        PDDocumentNameDictionary names = catalog.getNames();
        PDEmbeddedFilesNameTreeNode embeddedFiles = names.getEmbeddedFiles();
        Map<String, PDComplexFileSpecification> embeddedFileNames = embeddedFiles.getNames();
        assertEquals(1, embeddedFileNames.size());
        Map.Entry<String, PDComplexFileSpecification> entry = embeddedFileNames.entrySet().iterator().next();
        Log.i("PdfBox-Android", "Processing embedded file " + entry.getKey() + ":");
        PDComplexFileSpecification complexFileSpec = entry.getValue();
        PDEmbeddedFile embeddedFile = complexFileSpec.getEmbeddedFile();

        File resultFile = new File(testResultsDir, name);
        FileOutputStream fos = new FileOutputStream(resultFile);
        InputStream is = embeddedFile.createInputStream();
        IOUtils.copy(is, fos);
        fos.close();
        is.close();

        Log.i("PdfBox-Android", "  size: " + embeddedFile.getSize());
        assertEquals(embeddedFile.getSize(), resultFile.length());

        return resultFile;
    }

    private void testSymmEncrForKeySizeInner(int keyLength,
        int sizePriorToEncr, byte[] inputFileWithEmbeddedFileAsByteArray,
        File embeddedFilePriorToEncryption,
        String userpassword, String ownerpassword) throws IOException
    {
        PDDocument document = PDDocument.load(new ByteArrayInputStream(inputFileWithEmbeddedFileAsByteArray));
        PDDocument encryptedDoc = encrypt(keyLength, sizePriorToEncr, document, "ContainsEmbedded-", permission, userpassword, ownerpassword);

        File decryptedFile = new File(testResultsDir, "DecryptedContainsEmbedded-" + keyLength + "-bit.pdf");
        encryptedDoc.setAllSecurityToBeRemoved(true);
        encryptedDoc.save(decryptedFile);

        File extractedEmbeddedFile = extractEmbeddedFile(new FileInputStream(decryptedFile), "decryptedInnerFile-" + keyLength + "-bit.pdf");

        assertEquals(keyLength + "-bit decrypted inner attachment pdf should have same size as plain one",
            embeddedFilePriorToEncryption.length(), extractedEmbeddedFile.length());

        // compare the two embedded files
        Assert.assertArrayEquals(
            getFileAsByteArray(embeddedFilePriorToEncryption),
            getFileAsByteArray(extractedEmbeddedFile));
        encryptedDoc.close();
    }

    private byte[] getStreamAsByteArray(InputStream is) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(is, baos);
        is.close();
        return baos.toByteArray();
    }

    private byte[] getFileResourceAsByteArray(String testFileName) throws IOException
    {
        return getStreamAsByteArray(testContext.getAssets().open("pdfbox/com/tom_roush/pdfbox/pdmodel/encryption/" + testFileName));
    }

    private byte[] getFileAsByteArray(File f) throws IOException
    {
        return getStreamAsByteArray(new FileInputStream(f));
    }
}
