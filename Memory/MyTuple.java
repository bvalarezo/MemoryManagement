package osp.Memory;

/**
 * Name: Bryan Valarezo
 * StudentID: 110362410
 * 
 * I pledge my honor that all parts of this project were done by me individually, without 
 * collaboration with anyone, and without consulting any external sources that provide 
 * full or partial solutions to a similar project. 
 * I understand that breaking this pledge will result in an “F” for the entire course.
 */

/**
 * My Custom Tuple Class
 * 
 */
public class MyTuple { 
    public final int status; 
    public final FrameTableEntry freeFrame;

    public MyTuple(int status, FrameTableEntry freeFrame) { 
      this.status = status;
      this.freeFrame = freeFrame;

    }
    
    public int getStatus()
    {
      return status;
    }

    public FrameTableEntry getFreeFrame() {
      return freeFrame;
    }
  } 