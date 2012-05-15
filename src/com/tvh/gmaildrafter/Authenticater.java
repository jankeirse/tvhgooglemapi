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


import com.tvh.gmaildrafter.ui.GetCredentials;

/**
 * Used to obtain and validate credentials for a user with google 
 */
public  class Authenticater {
    private static GetCredentials getCredentialsInstance;
    private static boolean firstrun = true;
    
    public static Credentials getValidCredentials(String username, String password ) throws LoginException {
        boolean thisIsTheFirstRun = firstrun;
        firstrun = false;
        
        if(username == null || password == null || !thisIsTheFirstRun){
            
            Credentials cred;
            if(thisIsTheFirstRun) {
                
                PasswordStore store = new PasswordStore();
                cred = store.getStoredLogin();
                if (cred!=null){
                    if(testCredentials(cred))
                        return cred;
                    else if (username == null)
                        username = cred.getUsername(); //password must have changed, so we'll use it as initial for the window
                }
            }
            
            if (getCredentialsInstance == null)
                getCredentialsInstance = new GetCredentials(null, true, username);
            
            getCredentialsInstance.setVisible(true);
            cred = getCredentialsInstance.getCredentials();
            if (cred == null)
                throw new LoginException("Credentials not provided!");
            return cred;
        } else {
            Credentials cred = new Credentials(username, password);
            if (!testCredentials(cred))
                throw new LoginException("Invalid credentials passed.");
        }
        
        Credentials credentials = new Credentials(username, password);
        
        return credentials;
    }
    
    public static boolean testCredentials(Credentials cred){
        return true; //Not yet implemented, we're going to assume true for now. 
    }
    
    
    
}
