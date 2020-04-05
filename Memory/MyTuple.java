package osp.Memory;

/**
 * My Custom Tuple Class
 * 
 * @param <X>
 * @param <Y>
 */
public class MyTuple<X, Y> { 
    public final X x = null; 
    public final Y y = null;

    public Tuple(X x, Y y) { 
      this.x = x; 
      this.y = y; 
    }
    
    public X getX()
    {
      return x;
    }

    public Y getY() {
      return y;
    }
  } 