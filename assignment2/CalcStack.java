import java.util.*;

/**
 * Backend stack for calculations
 *
 * @author William Lynch
 * @author Bilal Quadri
 * @author Bryant Satterfield
 */


public class CalcStack {

    protected Stack<Double> stack;

    /**
     * Initializes new CalcStack.
     */
    public CalcStack() {
        stack = new Stack<Double>();
    }

    /**
     * Runs the command that the server wishes to execute.
     * @param cmd Command to run.
     * @return Null if number is pushed to the stack,
     * else a double[] containing the command results
     * @throws EmptyStackException Thrown if the number of elements in
     * the stack is < 2
     * @throws UnsupportedOperationException Thrown if the given command
     * is not valid
     */
    public double[] exec(String cmd)
        throws EmptyStackException,UnsupportedOperationException{
        /*
         * Try to parse the command to see if it is a number.
         * If not, treat it as an operation.
         */
        try {
            double d = Double.parseDouble(cmd);
            stack.push(d);
            return null;
        } catch (NumberFormatException e) {
            return operate(cmd);
        }
    }

    /**
     * Perform the given operation (+,-,*,/) on the stack
     * @param cmd Operation to be performed
     * @return The result of the operation performed
     * @throws EmptyStackException Thrown if the size of the stack is < 2
     * @throws UnsupportedOperationException Thrown if the given command
     * is not valid
     */
    public double[] operate(String cmd)
        throws EmptyStackException,UnsupportedOperationException {
        double a,b,c;

        if (cmd.equals("show")) {
            return show();
        } else if (cmd.length() != 1){
            throw(new UnsupportedOperationException());
        } else if (!(cmd.equals("+") || cmd.equals("-") || cmd.equals("*") || cmd.equals("/"))){
            throw(new UnsupportedOperationException());
        }
        char op = cmd.charAt(0);

        a = stack.pop();

        /* If there is only 1 element in the stack, then we must push
         * a back on the stack before throwing the exception
         */
        try {
            b = stack.pop();
        } catch (EmptyStackException e) {
            stack.push(a);
            throw(new EmptyStackException());
        }
        
        // Execute the correct opertaion
        switch(op) {
            case '+':
                c = b+a;
                break;
            case '-':
                c = b-a;
                break;
            case '*':
                c = b*a;
                break;
            case '/':
                c = b/a;
                break;
            default:
                //Sanity check. We should never get here
                c=0;
                stack.push(b);
                stack.push(a);
                throw(new UnsupportedOperationException());
        }

        double[] retval = { stack.push(c) };
        return retval;
    }

    /**
     * Return the list of elements currently in the stack
     * @return the list of elements in the stack
     */
    public double[] show() {
        int size = stack.size();
        double[] retval = new double[size];
        Iterator<Double> iter = stack.iterator();
        for (int i = 1; i <= size; i++){
            retval[size-i] = iter.next();
        }
        return retval;
    }
}
