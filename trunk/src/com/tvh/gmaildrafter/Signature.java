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


import java.io.IOException;
import javax.swing.JOptionPane;

public class Signature {

    public static String getSignature(Credentials cred) {

        String signature;
        try {
            signature = Util.readFileAsString(System.getProperty("user.home") + "/.gmaildrafter/signatures/" + cred.getUsername() + ".html" );
        } catch (IOException ex) {
            return null;
        }
        
        return signature;
    }
}
