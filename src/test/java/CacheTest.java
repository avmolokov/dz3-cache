import org.junit.*;
import org.junit.Test;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class CacheTest {

    private ByteArrayOutputStream output = new ByteArrayOutputStream();
    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(output));
    }
    @Test
    @DisplayName("Проверка работы Fraction правельности расчета")
    public void testFraction() {
        // Создаем Fraction
        Fraction fr= new Fraction(2,4);
        // Кешируем Fraction
        Fractionable num = Utils.cache(fr);
        var ff = num.doubleValue();
        Assertions.assertEquals(0.5,ff);
        //проверям повторный запуск сумма равно
        ff = num.doubleValue();//
        Assertions.assertEquals(0.5,ff);
        ff = num.doubleValue();//
        num.setNum(5);
        ff = num.doubleValue();
        Assertions.assertEquals(1.25,ff);
    }

    @Test
    @DisplayName("Проверка работу Fraction Cache на очистку через время")
    public void testFractionCache() throws InterruptedException {
        // Создаем Fraction
        Fraction fr= new Fraction(2,4);
        // Кешируем Fraction
        Fractionable num =Utils.cache(fr);
        var ff = num.doubleValue();// sout сработал
        //проверим что есть слова invoke double value
        Assertions.assertEquals(true, output.toString().trim().contains("invoke double value")   );
        output.reset();
        //проверям повторный запуск сумма равно
        ff = num.doubleValue();// sout молчит
        //проверим что нету слова invoke double value
        Assertions.assertEquals(false, output.toString().trim().contains("invoke double value")   );
        ff = num.doubleValue();// sout молчит
        output.reset();
        //меняем значения
        num.setNum(5);
        ff = num.doubleValue();// sout сработал
        Assertions.assertEquals(true, output.toString().trim().contains("invoke double value")   );
        output.reset();
        ff = num.doubleValue();// sout молчит
        Assertions.assertEquals(false, output.toString().trim().contains("invoke double value")   );

        Thread.sleep(1500); // подождем чтобы очистилась
        output.reset();
        ff = num.doubleValue();// sout сработал
        Assertions.assertEquals(true, output.toString().trim().contains("invoke double value")   );
        output.reset();
        ff = num.doubleValue();// sout молчит
        Assertions.assertEquals(false, output.toString().trim().contains("invoke double value")   );
        output.reset();
    }


    @After
    public void cleanUpStreams() {
        System.setOut(null);
    }

}
