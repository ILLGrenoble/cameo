/*
 * CAMEO
 *
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under BSD 3-Clause and GPL-v3 as described in license files.
 * You may not use this work except in compliance with the Licences.
 *
 */

#ifndef CAMEO_DEFINES_H_
#define CAMEO_DEFINES_H_

#if defined _WIN32
  #if defined CAMEO_STATIC
    #define CAMEO_EXPORT
  #elif defined DLL_EXPORT
    #define CAMEO_EXPORT __declspec(dllexport)
  #else
    #define CAMEO_EXPORT __declspec(dllimport)
  #endif
#else
  #if defined __SUNPRO_C || defined __SUNPRO_CC
    #define CAMEO_EXPORT __global
  #elif (defined __GNUC__ && __GNUC__ >= 4) || defined __INTEL_COMPILER
    #define CAMEO_EXPORT __attribute__ ((visibility ("default")))
  #else
    #define CAMEO_EXPORT
  #endif
#endif

// Using Visual Studio preprocessor.
// It must be improved in case of other compilers.
#ifdef _WIN32
	#include <process.h>
	#define GET_PROCESS_PID() _getpid()
#else
	#include <unistd.h>

	#define GET_PROCESS_PID() ::getpid()
#endif


#endif