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
 
package com.tvh.gmaildrafter;

import java.io.*;

public class Util {

    // TODO ask around if there is a better way to do this
    public static String readEntireStdin() {
        String result = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String s;
        try {
            while ((s = br.readLine()) != null) {
                result = result + s + "\n";
            }
        } catch (IOException ex) {
            //ignored
        }

        return result;
    }

    public static String readFileAsString(String filePath) throws java.io.IOException {
        byte[] buffer = new byte[(int) new File(filePath).length()];
        FileInputStream f = new FileInputStream(filePath);
        f.read(buffer);
        f.close();
        return new String(buffer);
    }
}