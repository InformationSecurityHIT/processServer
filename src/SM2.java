import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

/**
 * @description: TODO
 * @author: shen.x.z
 * @modified By: shen.x.z
 * @date: Created in 2021/8/29 14:49
 * @version:v1.0
 */
public class SM2 {

    public static BigInteger n = new BigInteger(
        "FFFFFFFE" + "FFFFFFFF" + "FFFFFFFF" + "FFFFFFFF" + "7203DF6B" + "21C6052B" + "53BBF409" + "39D54123", 16);
    public static BigInteger p = new BigInteger(
        "FFFFFFFE" + "FFFFFFFF" + "FFFFFFFF" + "FFFFFFFF" + "FFFFFFFF" + "00000000" + "FFFFFFFF" + "FFFFFFFF", 16);
    public static BigInteger a = new BigInteger(
        "FFFFFFFE" + "FFFFFFFF" + "FFFFFFFF" + "FFFFFFFF" + "FFFFFFFF" + "00000000" + "FFFFFFFF" + "FFFFFFFC", 16);
    public static BigInteger b = new BigInteger(
        "28E9FA9E" + "9D9F5E34" + "4D5A9E4B" + "CF6509A7" + "F39789F5" + "15AB8F92" + "DDBCBD41" + "4D940E93", 16);
    public static BigInteger gx = new BigInteger(
        "32C4AE2C" + "1F198119" + "5F990446" + "6A39C994" + "8FE30BBF" + "F2660BE1" + "715A4589" + "334C74C7", 16);
    public static BigInteger gy = new BigInteger(
        "BC3736A2" + "F4F6779C" + "59BDCEE3" + "6B692153" + "D0A9877C" + "C62A4740" + "02DF32E5" + "2139F0A0", 16);

    public static ECCurve.Fp curve;
    public static ECPoint G;
    public static ECDomainParameters ecc_bc_spec;
    public static SecureRandom random = new SecureRandom();

    public SM2() {
        curve = new ECCurve.Fp(p, a, b);
        G = curve.createPoint(gx, gy);
        ecc_bc_spec = new ECDomainParameters(curve, G, n);
    }

    /**
     * 随机数生成器
     */
    private static BigInteger random(BigInteger max) {
        BigInteger r = new BigInteger(256, random);
        while (r.compareTo(max) >= 0) {
            r = new BigInteger(128, random);
        }
        return r;
    }

    /**
     * 生成私钥
     */
    public BigInteger setPrivateKey(BigInteger n) {
        BigInteger d = random(n.subtract(new BigInteger("1")));
        return d;
    }

    /**
     * 生成公钥
     */
    public ECPoint setPublicKey(BigInteger d) {
        return G.multiply(d).normalize();
    }

    /**
     * 导出私钥到本地
     */
    public void exportPrivateKey(BigInteger privateKey, String path) {
        File file = new File(path);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(privateKey);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 导出公钥到本地
     */
    public void exportPublicKey(ECPoint publicKey, String path) {
        File file = new File(path);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            byte buffer[] = publicKey.getEncoded(false);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(buffer);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从本地导入私钥
     */
    public BigInteger importPrivateKey(String path) {
        File file = new File(path);
        try {
            if (!file.exists()) {
                return null;
            }
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            BigInteger res = (BigInteger) (ois.readObject());
            ois.close();
            fis.close();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从本地导入公钥
     */
    public ECPoint importPublicKey(String path) {
        File file = new File(path);
        try {
            if (!file.exists()) {
                return null;
            }
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte buffer[] = new byte[16];
            int size;
            while ((size = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, size);
            }
            fis.close();
            return curve.decodePoint(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        SM2 sm2 = new SM2();
        BigInteger privateKey = sm2.setPrivateKey(SM2.n);
        ECPoint publicKey = sm2.setPublicKey(privateKey);
        System.out.println("privateKey******" + privateKey);
        System.out.println("publicKey******" + publicKey);
        sm2.exportPrivateKey(privateKey, "D:\\InformationSecurityCompetition\\gameUse\\processServer\\sm2\\privateKey.pem");
        sm2.exportPublicKey(publicKey, "D:\\InformationSecurityCompetition\\gameUse\\processServer\\sm2\\publicKey.pem");
        BigInteger privateKeyImport = sm2.importPrivateKey("D:\\InformationSecurityCompetition\\gameUse\\processServer\\sm2\\privateKey.pem");
        ECPoint publicKeyImport = sm2.importPublicKey("D:\\InformationSecurityCompetition\\gameUse\\processServer\\sm2\\publicKey.pem");
        System.out.println("privateKeyImport******" + privateKeyImport);
        System.out.println("publicKeyImport******" + publicKeyImport);
    }

}
