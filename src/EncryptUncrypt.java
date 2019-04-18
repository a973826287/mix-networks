public class EncryptUncrypt {
    public static  String encryptAndUncrypt(String value,char secret){
        byte[] bt = value.getBytes();
        for(int i = 0; i < bt.length; i++){
            bt[i] = (byte)(bt[i] ^ (int)secret);
        }
        return new String(bt,0,bt.length);
    }
    //测试用main（）
    /*public static  void main(String[] args){
        String value = "java";
        char secret = '8';
        System.out.println("yuanzifu:" + value);

        String encryptResult = EncryptUncrypt.encryptAndUncrypt(value,secret);
        System.out.println("jianmihou:" + encryptResult);
        String uncryptResult = EncryptUncrypt.encryptAndUncrypt(encryptResult,secret);
        System.out.println("jiemihou:  " + uncryptResult);
    }*/
}
