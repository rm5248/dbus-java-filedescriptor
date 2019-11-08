package fd_rx;

import com.sun.jna.IntegerType;
import com.sun.jna.Library;
import com.sun.jna.Native;


public interface NativeLib extends Library {

    public static class size_t extends IntegerType {
        public size_t() { this(0); }
        public size_t(long value) { super(Native.SIZE_T_SIZE, value); }
    }

    
    NativeLib INSTANCE = (NativeLib)Native.load( "c", NativeLib.class );

    public int pipe(int[] fds);

    public size_t read(int fd, int[] buffer, size_t count);
    public size_t write(int fd, int[] buffer, size_t count);
}
