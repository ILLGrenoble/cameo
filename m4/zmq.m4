###############################################################################
# Version 01/07/2015
# defines ZMQ_CFLAGS, ZMQ_LDFLAGS, ZMQ_LIB
#
AC_DEFUN([AC_ZMQ],
[ AC_ARG_WITH([zmq],
         AS_HELP_STRING([--with-zmq=PREFIX],[Specify zeromq library location]),
         [],
              [with_zmq=yes])

    ZEROMQ_CFLAGS=
    ZEROMQ_LIBS=
    if test $with_zmq != no; then
        if test $with_zmq != yes; then
            zeromq_possible_path="$with_zmq"
        else
            zeromq_possible_path="/usr/local /usr /opt /var"
        fi
        AC_MSG_CHECKING([for zeromq headers])
        zeromq_save_CXXFLAGS="$CXXFLAGS"
        zeromq_found=no
        for zeromq_path_tmp in $zeromq_possible_path ; do
            # test include
            CXXFLAGS="$CXXFLAGS -I$zeromq_path_tmp/include"
            AC_COMPILE_IFELSE([AC_LANG_PROGRAM([[#include <zmq.h>]],[[]])],
                        [ZEROMQ_CFLAGS="-I$zeromq_path_tmp/include"
                         ZEROMQ_LIBS="-L$zeromq_path_tmp/lib"
                         zeromq_found=yes]
                        [])
            CXXFLAGS="$zeromq_save_CXXFLAGS"
            if test $zeromq_found = yes; then
                break;
            fi
        done

        AC_MSG_RESULT($zeromq_found)
        if test $zeromq_found = yes; then
            AC_MSG_CHECKING([for zeromq -lzmq])
            zeromq_save_LIBS="$LIBS"
            CXXFLAGS="$CXXFLAGS $ZEROMQ_CFLAGS"

            # search for library
            LIBS="$LIBS $ZEROMQ_LIBS -lzmq"
            AC_LINK_IFELSE([AC_LANG_PROGRAM([[]],
                                     [[]])],
                     [ zeromq_found=yes],
                     [ zeromq_found=no])
            CXXFLAGS="$zeromq_save_CXXFLAGS"
            LIBS="$zeromq_save_LIBS"
            if test $zeromq_found = yes; then
                    
                HAVE_ZMQ=1
                LIBS="$zeromq_save_LIBS"
                ZMQ_LDFLAGS="$ZEROMQ_LIBS"
                ZMQ_LIB="-lzmq"
            fi

            if test $zeromq_found = yes; then
                AC_MSG_RESULT(yes)
                AC_SUBST(ZMQ_CFLAGS)
                AC_SUBST(ZMQ_LDFLAGS)
                AC_SUBST(ZMQ_LIB)
            else
                AC_MSG_RESULT(no)
            fi
        fi
    fi
])
