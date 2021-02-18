package com.realapps.chat.utils;

import com.didisoft.pgp.CompressionAlgorithm;
import com.didisoft.pgp.CypherAlgorithm;
import com.didisoft.pgp.KeyPairInformation;
import com.didisoft.pgp.PGPLib;
import com.didisoft.pgp.bc.BCFactory;
import com.didisoft.pgp.bc.DirectByteArrayOutputStream;
import com.didisoft.pgp.bc.IOUtil;
import com.didisoft.pgp.bc.PGPObjectFactory2;
import com.didisoft.pgp.exceptions.KeyIsExpiredException;
import com.didisoft.pgp.exceptions.KeyIsRevokedException;
import com.didisoft.pgp.exceptions.NoPublicKeyFoundException;

import org.spongycastle.bcpg.ArmoredInputStream;
import org.spongycastle.bcpg.ArmoredOutputStream;
import org.spongycastle.bcpg.BCPGOutputStream;
import org.spongycastle.openpgp.PGPCompressedDataGenerator;
import org.spongycastle.openpgp.PGPEncryptedDataGenerator;
import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPLiteralDataGenerator;
import org.spongycastle.openpgp.PGPPublicKey;
import org.spongycastle.openpgp.PGPPublicKeyRing;
import org.spongycastle.openpgp.PGPPublicKeyRingCollection;
import org.spongycastle.openpgp.PGPSignature;
import org.spongycastle.openpgp.PGPUtil;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.didisoft.pgp.bc.IOUtil.getSecureRandom;
import static org.spongycastle.util.io.Streams.pipeAll;

/**
 * Created by Prashant Kumar Sharma on 6/29/2017.
 */

public class GroupChatUtils {
    public Logger log = Logger.getLogger(PGPLib.class.getName());
    private BCFactory bcFactory = new BCFactory(false);
    private CypherAlgorithm cypher = CypherAlgorithm.CAST5;
    private CompressionAlgorithm compression = CompressionAlgorithm.ZIP;
    private boolean useExpiredKeys = false;
    private boolean useRevokedKeys = false;
    private String asciiVersionHeader = "Created by Prashant Kumar Sharma";

    static int parseCompressionAlgorithm(CompressionAlgorithm compressionType) {
        if (CompressionAlgorithm.ZLIB.equals(compressionType))
            return 2;
        if (CompressionAlgorithm.ZIP.equals(compressionType))
            return 1;
        if (CompressionAlgorithm.UNCOMPRESSED.equals(compressionType))
            return 0;
        if (CompressionAlgorithm.BZIP2.equals(compressionType)) {
            return 3;
        }
        return -1;
    }

    static int parseSymmetricAlgorithm(CypherAlgorithm cipherType) {
        if (CypherAlgorithm.TRIPLE_DES == cipherType)
            return 2;
        if (CypherAlgorithm.CAST5 == cipherType)
            return 3;
        if (CypherAlgorithm.BLOWFISH == cipherType)
            return 4;
        if (CypherAlgorithm.AES_128 == cipherType)
            return 7;
        if (CypherAlgorithm.AES_192 == cipherType)
            return 8;
        if (CypherAlgorithm.AES_256.equals(cipherType))
            return 9;
        if (CypherAlgorithm.TWOFISH.equals(cipherType))
            return 10;
        if (CypherAlgorithm.DES.equals(cipherType))
            return 6;
        if (CypherAlgorithm.SAFER.equals(cipherType))
            return 5;
        if (CypherAlgorithm.IDEA.equals(cipherType)) {
            return 5;
        }
        return -1;
    }

    static String cypherToString(int alg) {
        switch (alg) {
            case 7:
                return "AES 128";
            case 8:
                return "AES 192";
            case 9:
                return "AES 256";
            case 4:
                return "Blowfish";
            case 3:
                return "Cast 5";
            case 6:
                return "DES";
            case 1:
                return "IDEA";
            case 5:
                return "Safer";
            case 2:
                return "3 DES";
            case 10:
                return "Twofish";
        }
        return "Unknown";
    }

    static String compressionToString(int alg) {
        switch (alg) {
            case 1:
                return "Zip";
            case 3:
                return "BZip2";
            case 2:
                return "ZLib";
            case 0:
                return "No compression";
        }
        return "Unknown";
    }

    protected static PGPPublicKeyRingCollection createPGPPublicKeyRingCollection(InputStream inputstream)
            throws IOException, com.didisoft.pgp.PGPException {
        inputstream = PGPUtil.getDecoderStream(inputstream);

        if ((inputstream instanceof ArmoredInputStream)) {
            ArmoredInputStream aIn = (ArmoredInputStream) inputstream;
            while (!aIn.isEndOfStream()) {
                PGPPublicKeyRingCollection pubRing = createPGPPublicKeyRingCollectionSub(aIn);
                if (pubRing.size() > 0) {
                    return pubRing;
                }
            }
        } else {
            return createPGPPublicKeyRingCollectionSub(inputstream);
        }
        try {
            return new PGPPublicKeyRingCollection(new ArrayList());
        } catch (org.spongycastle.openpgp.PGPException e) {
            throw IOUtil.newPGPException(e);
        }
    }

    public static PGPPublicKeyRingCollection createPGPPublicKeyRingCollectionSub(InputStream inputstream)
            throws IOException, com.didisoft.pgp.PGPException {
        PGPObjectFactory2 pgpFact = new PGPObjectFactory2(inputstream);
        Map pubRings = new HashMap();
        try {
            Object obj;
            while ((obj = pgpFact.nextObject()) != null) {
                if ((obj instanceof PGPPublicKeyRing)) {
                    PGPPublicKeyRing pgpPub = (PGPPublicKeyRing) obj;
                    Long key = new Long(pgpPub.getPublicKey().getKeyID());
                    pubRings.put(key, pgpPub);
                }
            }
        } catch (IOException e) {
            throw new NoPublicKeyFoundException(e.getMessage(), e);
        }
        try {
            return new PGPPublicKeyRingCollection(pubRings.values());
        } catch (org.spongycastle.openpgp.PGPException e) {
            throw IOUtil.newPGPException(e);
        }
    }

    public void inernalEncryptStream(InputStream dataStream, String fileName, PGPPublicKey[] publicKeys, OutputStream outStream, Date fileDate, boolean asciiArmor, boolean withIntegrityCheck, boolean writePgpMarker)
            throws PGPException, IOException {
        try {
            OutputStream underAsciiStream = null;
            if (asciiArmor) {
                underAsciiStream = outStream;
                outStream = new ArmoredOutputStream(underAsciiStream);
                setAsciiVersionHeader(outStream);
            }
            int compression = parseCompressionAlgorithm(this.compression);
            int preferredCypher = parseSymmetricAlgorithm(cypher);
            if (publicKeys.length == 1) {
                compression = preferredCompression(publicKeys[0]);
                preferredCypher = preferredCypher(publicKeys[0]);

                Debug("Encrypting with cypher {0}", cypherToString(preferredCypher));
                Debug("Compression is {0}", compressionToString(compression));
            }
            if (writePgpMarker) {
                BCPGOutputStream bo = new BCPGOutputStream(outStream);
                writeMarker(bo);
                bo.flush();
            }
            PGPEncryptedDataGenerator cPk = new PGPEncryptedDataGenerator(bcFactory.CreatePGPDataEncryptorBuilder(preferredCypher, withIntegrityCheck, getSecureRandom()));
            OutputStream encryptedOut = null;
            try {
                for (int i = 0; i < publicKeys.length; i++) {
                    Debug("Encrypting with key {0} ", KeyPairInformation.keyId2Hex(publicKeys[i].getKeyID()));
                    cPk.addMethod(bcFactory.CreatePublicKeyKeyEncryptionMethodGenerator(publicKeys[i]));
                }
                encryptedOut = cPk.open(outStream, new byte[1048576]);
            } catch (org.spongycastle.openpgp.PGPException e) {
                throw IOUtil.newPGPException(e);
            }
            try {
                PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(compression);
                PGPLiteralDataGenerator ld = new PGPLiteralDataGenerator();
                OutputStream literalStream = null;
                try {
                    if (compression == 0) {
                        literalStream = ld.open(encryptedOut, 'b', fileName, fileDate, new byte[1048576]);
                        pipeAll(dataStream, literalStream);
                    } else {
                        literalStream = ld.open(comData.open(encryptedOut), 'b', fileName, fileDate, new byte[1048576]);
                        pipeAll(dataStream, literalStream);
                    }
                } finally {
                    if (ld != null) {
                        ld.close();
                    }
                    IOUtil.closeStream(literalStream);
                    IOUtil.closeStream(dataStream);
                    comData.close();
                }
            } finally {
                IOUtil.closeStream(encryptedOut);
                if (asciiArmor) {
                    IOUtil.closeStream(outStream);
                }
            }
        } catch (IOException e) {
            throw e;
        }
    }

    public void setAsciiVersionHeader(String creator) {
        asciiVersionHeader = creator;
    }

    public void setAsciiVersionHeader(OutputStream out) {
        if ((out instanceof ArmoredOutputStream)) {
            ((ArmoredOutputStream) out).setHeader("Version", asciiVersionHeader);
        }
    }

    public void Debug(String s1, String s2) {
        if (log.isLoggable(Level.FINE)) {
            log.fine(MessageFormat.format(s1, s2));
        }
    }

    public void writeMarker(BCPGOutputStream bcOut) {
        Class c = BCPGOutputStream.class;
        byte[] marker = {80, 71, 80};
        Method method = null;
        Method[] methods = c.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().endsWith("writePacket")) {
                Class[] params = methods[i].getParameterTypes();
                if (params.length == 3) {
                    method = methods[i];
                    method.setAccessible(true);
                    try {
                        method.invoke(bcOut, new Integer(10), marker, new Boolean(true));
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
        }
        if (method == null) {
            throw new Error("No such method: writeMarkerPacket");
        }
    }

    public int preferredCompression(PGPPublicKey encKey) {
        int comp = parseCompressionAlgorithm(compression);
        int prefered = -1;
        Iterator sIt = encKey.getSignatures();
        while ((prefered == -1) && (sIt.hasNext())) {
            PGPSignature sig = (PGPSignature) sIt.next();
            if ((sig.getHashedSubPackets() != null) && (sig.getHashedSubPackets().getPreferredCompressionAlgorithms() != null)) {
                int[] algs = sig.getHashedSubPackets().getPreferredCompressionAlgorithms();
                for (int i = 0; i < algs.length; i++) {
                    prefered = algs[i];
                    if (comp == prefered) {
                        break;
                    }
                }
            }
        }
        if (prefered == -1) {
            prefered = comp;
        }
        Debug("Compression: {0}", compressionToString(prefered));
        return prefered;
    }

    public int preferredCypher(PGPPublicKey encKey) {
        int cypher = parseSymmetricAlgorithm(this.cypher);
        int preferedCypher = 0;
        Iterator sIt = encKey.getSignatures();
        while ((preferedCypher == 0) && (sIt.hasNext())) {
            PGPSignature sig = (PGPSignature) sIt.next();
            if ((sig.getHashedSubPackets() != null) && (sig.getHashedSubPackets().getPreferredSymmetricAlgorithms() != null)) {
                int[] algs = sig.getHashedSubPackets().getPreferredSymmetricAlgorithms();
                for (int i = 0; i < algs.length; i++) {
                    preferedCypher = algs[i];
                    if (cypher == preferedCypher) {
                        break;
                    }
                }
            }
        }
        if (preferedCypher == 0) {
            if (encKey.getVersion() == 3) {
                preferedCypher = 1;
            } else {
                preferedCypher = cypher;
            }
        }
        Debug("Cypher: {0}", cypherToString(preferedCypher));
        return preferedCypher;
    }

    public String encryptString(String stringToEncrypt, String[] publicKeysFileNames, String charsetName)
            throws PGPException, IOException {
        InputStream streamFromString = null;
        try {
            DirectByteArrayOutputStream encryptedStream = new DirectByteArrayOutputStream(1048576);
            streamFromString = new ByteArrayInputStream(stringToEncrypt.getBytes(charsetName));
            encryptStream(streamFromString, "", publicKeysFileNames, encryptedStream, true, false);
            return new String(encryptedStream.getArray(), 0, encryptedStream.size(), "UTF-8");
        } finally {
            IOUtil.closeStream(streamFromString);
        }
    }

    public void encryptStream(InputStream dataStream, String fileName, String[] publicKeysFileNames, OutputStream outputStream, boolean asciiArmor, boolean withIntegrityCheck)
            throws PGPException, IOException {
        try {
             List listKeys = new LinkedList();
            InputStream publicKeyStream = null;
            for (int i = 0; i < publicKeysFileNames.length; i++) {
                try {
                    publicKeyStream = new FileInputStream(publicKeysFileNames[i]);
                    PGPPublicKey key = readPublicKey(publicKeyStream);
                    listKeys.add(key);
                } finally {
                    IOUtil.closeStream(publicKeyStream);
                }
            }
            boolean writePgpMarker = false;
            PGPPublicKey[] encKeys = (PGPPublicKey[]) listKeys.toArray(new PGPPublicKey[listKeys.size()]);
            inernalEncryptStream(dataStream, fileName, encKeys, outputStream, new Date(), asciiArmor, withIntegrityCheck, writePgpMarker);
           } finally {
            IOUtil.closeStream(dataStream);
        }
    }

    public PGPPublicKey readPublicKey(InputStream in)
            throws IOException, PGPException {
        PGPPublicKeyRingCollection pgpPub = createPGPPublicKeyRingCollection(in);
        PGPPublicKey key = null;
        Iterator rIt = pgpPub.getKeyRings();
        while ((key == null) && (rIt.hasNext())) {
            PGPPublicKeyRing kRing = (PGPPublicKeyRing) rIt.next();
            Iterator kIt = kRing.getPublicKeys();
            while ((key == null) && (kIt.hasNext())) {
                PGPPublicKey k = (PGPPublicKey) kIt.next();
                if (k.isEncryptionKey()) {
                    key = k;
                }
            }
        }
        if (key == null) {
            throw new NoPublicKeyFoundException("Can't find encryption key in key ring.");
        }
        checkKeyIsExpired(key);
        checkKeyIsRevoked(key);
        return key;
    }

    public void checkKeyIsExpired(PGPPublicKey key)
            throws KeyIsExpiredException {
        if (key == null) return;
        if (useExpiredKeys) return;
        if (key.getValidDays() <= 0) {
            return;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(key.getCreationTime());
        cal.add(Calendar.DAY_OF_MONTH, key.getValidDays());
        if (cal.getTime().before(new Date())) {
            String userId = "";
            Iterator itIds = key.getUserIDs();
            if (itIds.hasNext()) {
                userId = (String) itIds.next();
            }
            Debug("The key {0} is expired", KeyPairInformation.keyId2Hex(key.getKeyID()));
            throw new KeyIsExpiredException("The key with Id:" + KeyPairInformation.keyId2Hex(key.getKeyID()) + " [" + userId + "] has expired. See PGPLib.setUseExpiredKeys for more information.");
        }
    }

    public void checkKeyIsRevoked(PGPPublicKey key)
            throws KeyIsRevokedException {
        if (key == null) return;
        if (useRevokedKeys) {
            return;
        }
        if (key.isRevoked()) {
            String userId = "";
            Iterator itIds = key.getUserIDs();
            if (itIds.hasNext()) {
                userId = (String) itIds.next();
            }
            Debug("The key {0} is revoked", KeyPairInformation.keyId2Hex(key.getKeyID()));
            throw new KeyIsRevokedException("The key with Id:" + key.getKeyID() + " [" + userId + "] is revoked. See PGPLib.setUseRevokedKeys for more information.");
        }
    }

    public String encryptStrings(String stringToEncrypt, String[] publicKeysFileNames, String charsetName)
            throws PGPException, IOException {
        InputStream publicEncryptionKeyStream = null;
        setAsciiVersionHeader("Created By Prashant Kumar Sharma");
        try {
            return encryptString(stringToEncrypt, publicKeysFileNames, charsetName);
        } finally {
            IOUtil.closeStream(publicEncryptionKeyStream);
        }
    }

}
