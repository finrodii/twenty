/*
 * Copyright 2007 Steven Osborn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* Code modifications by Daniel Ricciotti
 * This code was built using the IconifiedText tutorial by Steven Osborn
 * http://www.anddev.org/iconified_textlist_-_the_making_of-t97.html
 * 
 * Copyright 2008 Daniel Ricciotti
 */
package org.byu.cs.gen.global;


/** Steven Osborn - http://steven.bitsetters.com */
/** Modified by Daniel Ricciotti **/
/** @author Modified by Seth Dickson **/

public class CheckBoxifiedText implements Comparable<CheckBoxifiedText>{
   
     private String mText = "";
     private String mType = "";
     private String mPersonId = "";
     private boolean mChecked;    
     public CheckBoxifiedText(String text, String personId, String type, boolean checked) {
    	 /* constructor */ 
          mText = text;
          mType = type;
          mPersonId = personId;
          mChecked = checked;
     }
     public void setChecked(boolean value)
     {
    	 this.mChecked = value;
     }
     public boolean getChecked(){
    	 return this.mChecked;
     }
     
     public String getText() {
          return mText;
     }
     public String getType() {
    	 return mType;
     }
     public String getPersonId(){
    	 return mPersonId;
     }
     
     public void setText(String text) {
          mText = text;
     }
     public void setType(String type) {
    	 mType = type;
     }
     public void setPersonId(String personId){
    	 mPersonId = personId;
     }

     public boolean isParent(){
    	 return mType.compareTo("parent")==0;
     }
     
     /** Make CheckBoxifiedText comparable by its name */
     //@Override
     public int compareTo(CheckBoxifiedText other) {
          if(this.mText != null)
               return this.mText.compareTo(other.getText());
          else
               throw new IllegalArgumentException();
     }
} 