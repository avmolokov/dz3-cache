import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Cache {
    int value() default 1000;
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Mutator {
}

public class Main {
    public static void main(String... args) throws Exception {
        Fraction fr= new Fraction(2,4);
        Fractionable num = Utils.cache(fr);
        var ff = num.doubleValue();// sout сработал
        System.out.println(ff);
        ff = num.doubleValue();// sout молчит
        System.out.println(ff);
        ff = num.doubleValue();// sout молчит
        System.out.println(ff);
        num.setNum(5);
        ff = num.doubleValue();// sout сработал
        System.out.println(ff);
        ff = num.doubleValue();// sout молчит
        System.out.println(ff);
        num.setNum(2);
        ff = num.doubleValue();// sout молчит

        Thread.sleep(1500);
        num.doubleValue();// sout сработал
        num.doubleValue();// sout молчит
    }
}
