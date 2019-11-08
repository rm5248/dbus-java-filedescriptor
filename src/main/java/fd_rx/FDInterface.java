package fd_rx;

import java.io.FileDescriptor;
import org.freedesktop.dbus.interfaces.DBusInterface;

public interface FDInterface extends DBusInterface {

    public void setFileDescriptor( FileDescriptor fd );
}
