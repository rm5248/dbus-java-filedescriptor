package test;

import org.freedesktop.dbus.FileDescriptor;
import org.freedesktop.dbus.interfaces.DBusInterface;

public interface ReturnFDInterface extends DBusInterface {

    public FileDescriptor getFileDescriptor();
}
