###############################################################################
# Version 01/07/2015
# defines LIBPROTOBUF_LDFLAGS, LIBPROTOBUF_LIB
#
AC_DEFUN([AC_LIBPROTOBUF],
[ 
  AC_ARG_WITH([libprotobuf],
         AS_HELP_STRING([--with-libprotobuf=PREFIX],[Specify protobuf library location]),
         [],
              [with_libprotobuf=yes])

    PROTOBUF_LIBS=
    protobuf_save_LIBS="$LIBS"
    if test $with_libprotobuf != no; then
        if test $with_libprotobuf != yes; then
            protobuf_possible_path="$with_libprotobuf"
        else
            protobuf_possible_path="/usr/local /usr /opt /var"
        fi
        AC_MSG_CHECKING([for protobuf -lprotobuf])
        protobuf_found=no
        for protobuf_path_tmp in $protobuf_possible_path ; do
            LIBS="$LIBS $PROTOBUF_LIBS -lprotobuf"
            AC_COMPILE_IFELSE([AC_LANG_PROGRAM([[]],[[]])],
                        [PROTOBUF_LIBS="-L$protobuf_path_tmp/lib"
                         protobuf_found=yes]
                        [])
            if test $protobuf_found = yes; then
                break;
            fi
        done

        if test $protobuf_found = yes; then
                    
            LIBPROTOBUF_LDFLAGS="$PROTOBUF_LIBS"
            LIBPROTOBUF_LIB="-lprotobuf"
        
            AC_MSG_RESULT(yes)
            AC_SUBST(LIBPROTOBUF_LDFLAGS)
            AC_SUBST(LIBPROTOBUF_LIB)
        else
            AC_MSG_RESULT(no)
        fi
    fi
    LIBS="$protobuf_save_LIBS"
])
