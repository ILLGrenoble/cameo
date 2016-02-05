###############################################################################
# Version 01/07/2015
# defines PROTOBUF_CFLAGS, PROTOBUF_LDFLAGS, PROTOBUF_LIB
#
AC_DEFUN([AC_PROTOBUFLITE],
[ AC_ARG_WITH([protobuf],
         AS_HELP_STRING([--with-protobuf=PREFIX],[Specify protobuf library location]),
         [],
              [with_protobuf=yes])

    PROTOBUF_CFLAGS=
    PROTOBUF_LIBS=
    if test $with_protobuf != no; then
        if test $with_protobuf != yes; then
            protobuf_possible_path="$with_protobuf"
        else
            protobuf_possible_path="/usr/local /usr /opt /var"
        fi
        AC_MSG_CHECKING([for protobuf headers])
        protobuf_save_CXXFLAGS="$CXXFLAGS"
        protobuf_found=no
        for protobuf_path_tmp in $protobuf_possible_path ; do
            # test include
            CXXFLAGS="$CXXFLAGS -I$protobuf_path_tmp/include"
            AC_COMPILE_IFELSE([AC_LANG_PROGRAM([[#include <google/protobuf/descriptor.h>]],[[]])],
                        [PROTOBUF_CFLAGS="-I$protobuf_path_tmp/include"
                         PROTOBUF_LIBS="-L$protobuf_path_tmp/lib"
                         protobuf_found=yes]
                        [])
            CXXFLAGS="$protobuf_save_CXXFLAGS"
            if test $protobuf_found = yes; then
                break;
            fi
        done

        AC_MSG_RESULT($protobuf_found)
        if test $protobuf_found = yes; then
            AC_MSG_CHECKING([for protobuf -lprotobuf-lite])
            protobuf_save_LIBS="$LIBS"
            CXXFLAGS="$CXXFLAGS $PROTOBUF_CFLAGS"

            # search for library
            LIBS="$LIBS $PROTOBUF_LIBS -lprotobuf-lite"
            AC_LINK_IFELSE([AC_LANG_PROGRAM([[#include <google/protobuf/descriptor.h>]],
                                     [[]])],
                     [ protobuf_found=yes],
                     [ protobuf_found=no])
            CXXFLAGS="$protobuf_save_CXXFLAGS"
            LIBS="$protobuf_save_LIBS"
            if test $protobuf_found = yes; then
                    
                HAVE_PROTOBUF=1
                LIBS="$protobuf_save_LIBS"
                PROTOBUF_LDFLAGS="$PROTOBUF_LIBS"
                PROTOBUF_LIB="-lprotobuf-lite"
                break;
            fi

            if test $protobuf_found = yes; then
                AC_MSG_RESULT(yes)
                AC_SUBST(PROTOBUF_CFLAGS)
                AC_SUBST(PROTOBUF_LDFLAGS)
                AC_SUBST(PROTOBUF_LIB)
            else
                AC_MSG_RESULT(no)
            fi
        fi
    fi
])
