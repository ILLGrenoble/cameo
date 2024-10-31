/*
 * Copyright 2015 Institut Laue-Langevin
 *
 * Licensed under the EUPL, Version 1.1 only (the "License");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
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


#endif
