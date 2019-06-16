public class EncryptUncrypt {
    public static  String encrypt(String value,int secret){
        int a[]=new int[100];
        char ch[]=new char[100];
        ch=value.toCharArray();
        for(int i=0;i<value.length();i++)
        {


            a[i]=(int)ch[i];

            a[i]=a[i]+secret;

            ch[i]=(char)a[i];

        }
        return new String(ch,0,ch.length);
    }
    public static String uncrypt(String value, int secret){
        int a[]=new int[100];
        char ch[]=new char[100];
        ch=value.toCharArray();
        for(int i=0;i<value.length();i++)
        {


            a[i]=(int)ch[i];

            a[i]=a[i]-secret;

            ch[i]=(char)a[i];

        }
        return new String(ch,0,ch.length);
    }
    //测试用main（）
    public static  void main(String[] args){
        String value = "HHHHlcfja{>UQmix1";
        char secret = 1;
        System.out.println("yuanzifu:" + value);

        String encryptResult = EncryptUncrypt.encrypt(value,secret);
        System.out.println("jianmihou:" + encryptResult);
        String uncryptResult = EncryptUncrypt.uncrypt(encryptResult,secret);
        System.out.println("jiemihou:" + uncryptResult);
    }




/*    public static  String encryptAndUncrypt(String value,char secret){
        byte[] bt = value.getBytes();
        for(int i = 0; i < bt.length; i++){
            bt[i] = (byte)(bt[i] ^ (int)secret);
        }
        return new String(bt,0,bt.length);
    }
*/

}

