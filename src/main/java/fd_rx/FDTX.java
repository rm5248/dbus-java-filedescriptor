package fd_rx;

import java.io.DataInputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;

public class FDTX {

    private static final Logger logger = LogManager.getLogger();

    private DBusConnection m_conn;
    private FDInterface m_fdIface;

    FDTX() throws DBusException{
        m_conn = DBusConnection.getConnection( DBusConnection.DBusBusType.SESSION );

        m_fdIface = m_conn.getRemoteObject( "test.filedescriptor", "/", FDInterface.class );

        int[] pipe = new int[2];
        if( NativeLib.INSTANCE.pipe( pipe ) < 0 ){
            logger.error( "Can't make pipe" );
        }

        // Send this file descriptor to the other process.
        FileDescriptor tosend = createFileDescriptorByReflection( pipe[ 1 ] );
        m_fdIface.setFileDescriptor( tosend );

        DataInputStream dis = new DataInputStream(
                new FileInputStream( createFileDescriptorByReflection( pipe[ 0 ] ) )
        );
        
        try{
            char c = dis.readChar();
            logger.debug( "got char {}", c );
        }catch( IOException ex ){
            logger.error( "Could not read", ex );
        }
    }

    // NOTE: THIS COMES FROM DBUS-JAVA IMPLEMENTATION
    private FileDescriptor createFileDescriptorByReflection(int _demarshallint){
        try {
            Constructor<FileDescriptor> constructor = FileDescriptor.class.getDeclaredConstructor(int.class);
            constructor.setAccessible(true);
            return constructor.newInstance( _demarshallint);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException _ex) {
            logger.error("Could not create new FileDescriptor instance by reflection.", _ex);
        }

        return null;
    }

    public static void main(String[] args) throws DBusException{
        new FDTX();
    }
}
