public class Log {

    public static void log(Object obj, String msg) {
        System.out.println(msg + ": " + obj.getClass().getName() + " -> " + obj);
    }
}
