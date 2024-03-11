import java.util.Objects;

public class Fraction implements Fractionable{
    private int num;
    private int denum;

    public Fraction(int num, int denum) {
        this.num = num;
        this.denum = denum;
    }

    @Mutator
    public void setNum(int num) {
        this.num = num;
    }

    @Mutator
    public void setDenum(int denum) {
        this.denum = denum;
    }

    @Override
    @Cache(1000)
    public double doubleValue() {
        System.out.println("invoke double value");
        return (double) num/denum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fraction fraction = (Fraction) o;
        return num == fraction.num && denum == fraction.denum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(num, denum);
    }
}
