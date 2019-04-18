import java.util.Scanner;

public class test {
    public static void main(String[] args) throws Exception {
        Scanner in = new Scanner(System.in);
        String letter = in.nextLine();
        for(int i = 0; i < letter.length(); i++)
        {

        }

        int length = letter.length();
        int num = 0;
        int number = 0;
        for(int i = 0; i < length; i++) {
            char ch = letter.charAt(length - i - 1);
            num = (int)(ch - 'A' + 1) ;
            num *= Math.pow(26, i);
            number += num;
        }
        System.out.println(letter + "  " + number);

        //去掉前缀
        /*
        if(s.startsWith("0"))
        {
            s = s.substring(1);
        }
        else if(s.startsWith("00"))
        {
            s = s.substring(2);
        } else if (s.startsWith("000"))
        {
            s = s.substring(3);
        }


        String endPackage1 = s.substring(s.length()-4,s.length());
        String endPackage2 = s.substring(s.length()-7,s.length());
        switch (endPackage1){
            case "mix1":

                break;
        }

*/


    }
}
