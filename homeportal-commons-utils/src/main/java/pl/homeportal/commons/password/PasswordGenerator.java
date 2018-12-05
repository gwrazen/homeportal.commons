package pl.homeportal.commons.password;

/**
 * Created with IntelliJ IDEA.
 * User: gwrazen
 * Date: 25/01/14
 * Time: 16:49
 * To change this template use File | Settings | File Templates.
 */
public class PasswordGenerator
{
    public static String generate(int n)
    {
        char[] pw = new char[n];
        int c = 'A';
        int r1 = 0;
        for (int i = 0; i < n; i++)
        {
            r1 = (int) (Math.random() * 3);
            switch (r1)
            {
                case 0:
                    c = '0' + (int) (Math.random() * 10);
                    break;
                case 1:
                    c = 'a' + (int) (Math.random() * 26);
                    break;
                case 2:
                    c = 'A' + (int) (Math.random() * 26);
                    break;
            }
            pw[i] = (char) c;
        }
        return new String(pw);
    }

}
