# -----------------------------------------------------------------------------
#
# Autoconf
#
# -----------------------------------------------------------------------------

AC_INIT(cameo-api-cpp, 0.3.0)
LIBRARY_VERSION=0:3:0

AC_CONFIG_AUX_DIR(config)
AC_CONFIG_SRCDIR(src/cameo/Application.cpp)

AM_INIT_AUTOMAKE([subdir-objects])

AC_PROG_CXX
AC_LANG(C++)

AM_DISABLE_STATIC

AC_PROG_LIBTOOL
AC_CONFIG_MACRO_DIR([m4])

AC_SUBST(LIBRARY_VERSION)

AX_CXX_COMPILE_STDCXX_11

AC_ZMQ
AC_PROTOBUF_LITE

CAMEO_CXXFLAGS="$ZMQ_CFLAGS \
	$PROTOBUF_CFLAGS"
	
CAMEO_LDFLAGS="$ZMQ_LDFLAGS \
	$PROTOBUF_LDFLAGS"
 
CAMEO_LIBS="$ZMQ_LIB \
	$PROTOBUF_LIB"

AC_SUBST(CAMEO_CXXFLAGS)
AC_SUBST(CAMEO_LDFLAGS)
AC_SUBST(CAMEO_LIBS)

top_srcdir=.

AC_OUTPUT([
Makefile
src/Makefile
])
