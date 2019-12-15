project( fd-test )

cmake_minimum_required( VERSION 3.5 )

include( FindPkgConfig )
pkg_check_modules( dbus-cxx REQUIRED dbus-cxx-1.0>=0.10.0 )

set( EXAMPLES_LINK dbus-cxx ${dbus_LDFLAGS} ${sigc_LDFLAGS} -lrt )
set( CMAKE_CXX_STANDARD 11 )

add_executable( FDTX FDTX.cpp  )
target_include_directories( FDTX  PUBLIC ${dbus-cxx_INCLUDE_DIRS} )
target_link_libraries( FDTX ${dbus-cxx_LDFLAGS} )

add_executable( FDRX FDRX.cpp  )
target_include_directories( FDRX  PUBLIC ${dbus-cxx_INCLUDE_DIRS} )
target_link_libraries( FDRX ${dbus-cxx_LDFLAGS} )

add_executable( FDGetter FDGetter.cpp  )
target_include_directories( FDGetter  PUBLIC ${dbus-cxx_INCLUDE_DIRS} )
target_link_libraries( FDGetter ${dbus-cxx_LDFLAGS} )

#add_executable( filedescriptor-server filedescriptor_server.cpp  )
#target_link_libraries( filedescriptor-server ${EXAMPLES_LINK} )
