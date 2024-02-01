
public class Main {
    public static void main(String[] args) {
        // parse args
        if (args.length == 0) {
            System.out.println("Unspecified arguments, see --help or -help to know arguments");
        } else if (args[0].equals("-help") || args[0].equals("--help")) {
            System.out.println("possible arguments : \n" 
            + " -help : for info"
            );
        }
    }
}
