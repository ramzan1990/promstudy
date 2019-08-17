package promstudy.clustering;

public class Sequence implements Comparable{
    public Sequence(int[] seq, double v, int p) {
        this.seq = seq;
        this.v=v;
        this.p = p;
    }
    public int[] seq;
    public double v;
    public int p;

    @Override
    public int compareTo(Object o) {
        return Double.compare( this.v, ((Sequence) o).v);
    }
}
