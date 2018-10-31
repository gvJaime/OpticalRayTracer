package opticalraytracer;

public class ComplexInt {
	int x,y;
    public ComplexInt(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public ComplexInt(ComplexInt p) {
    	x = p.x;
    	y = p.y;
    }
    public ComplexInt() {
    }
    public void scale(double s) {
    	x = (int) (x*s);
    	y = (int) (y*s);
    }
    
    public void assign(ComplexInt p) {
    	x = p.x;
    	y = p.y;
    }
    
    public String toString() {
    	return String.format("{%d,%d}",x,y);
    }
}
