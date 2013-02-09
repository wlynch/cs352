import java.util.EmptyStackException;

/**
 * Test driver for the CalcStack class. To be removed later.
 * @author wlynch92
 *
 */
public class StackTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        CalcStack s = new CalcStack();
        double[] retval;
        retval = s.exec("1");
        retval = s.exec("2");
        retval = s.exec("+");
        printResult(retval);
        retval=s.exec("9");
        retval = s.exec("*");
        printResult(retval);
        retval = s.exec("111");
        retval = s.exec("222");
        retval = s.exec("show");
        printResult(retval);
        retval = s.exec("*");
        printResult(retval);
        retval = s.exec("/");
        printResult(retval);
        try {
            s.exec("print");
        } catch (UnsupportedOperationException e){
            System.out.println("?");
        }
        try {
            s.exec("+");
        } catch (EmptyStackException e){
            System.out.println("not enough numbers on the stack!");
        }
    }

    public static void printResult(double [] retval){
        if (retval == null){
            System.out.println("null");
        } else {
            for(double d:retval){
                System.out.print(d+" ");
            }
            System.out.println();
        }
    }

}
