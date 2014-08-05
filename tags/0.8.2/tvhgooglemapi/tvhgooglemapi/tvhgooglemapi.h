/**
 * Copyright (C) 2012 TVH Group NV. <kalman.tiboldi@tvh.com>
 *
 * This file is part of the tvhgooglemapi project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// The following ifdef block is the standard way of creating macros which make exporting 
// from a DLL simpler. All files within this DLL are compiled with the TVHGOOGLEMAPI_EXPORTS
// symbol defined on the command line. This symbol should not be defined on any project
// that uses this DLL. This way any other project whose source files include this file see 
// TVHGOOGLEMAPI_API functions as being imported from a DLL, whereas this DLL sees symbols
// defined with this macro as being exported.
#ifdef TVHGOOGLEMAPI_EXPORTS
#define TVHGOOGLEMAPI_API extern "C" __declspec( dllexport ) 
#else
#define TVHGOOGLEMAPI_API extern "C" __declspec( dllexport ) 
#endif

