/* $Id: BulletedTextView.java 57 2007-11-21 18:31:52Z steven $
 *
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

import org.byu.cs.gen.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CheckBoxifiedTextView extends LinearLayout {
     
     private TextView mText;
     private CheckBox mCheckBox;
     private CheckBoxifiedText mCheckBoxText;
     private CheckBoxifiedIndexAdapter cbla;
     private int position;
     private Drawable check_box;
     private Drawable check_in_box;
     
     private CheckBoxifiedTextView instance;
     
     public CheckBoxifiedTextView(CheckBoxifiedIndexAdapter cboxListAdapter, Context context, CheckBoxifiedText aCheckBoxifiedText) {
          super(context);

          instance = this;
          cbla = cboxListAdapter;
          position = 0;
          /* First CheckBox and the Text to the right (horizontal),
           * not above and below (vertical) */
          this.setOrientation(HORIZONTAL);
          mCheckBoxText = aCheckBoxifiedText;
          mCheckBox = new CheckBox(context);

          check_box = context.getResources().getDrawable(R.drawable.check_off);
          check_in_box = context.getResources().getDrawable(R.drawable.check_on);
          mCheckBox.setPadding(0, 0, 25, 0); // 5px to the right
          /* Set the initial state of the checkbox. */
          if(aCheckBoxifiedText.getChecked()){
        	  mCheckBox.setChecked(true);
        	  mCheckBox.setButtonDrawable(check_in_box);
          }
          else{
        	  mCheckBox.setChecked(false);
        	  mCheckBox.setButtonDrawable(check_box);        	  
          }
          
          mText = new TextView(context);
          if(aCheckBoxifiedText.isParent()){
        	  mText.setTextSize(30);
          	  mCheckBox.setVisibility(GONE);
          }
          else
        	  mText.setTextSize(18);
          mText.setText(aCheckBoxifiedText.getText());
          mText.setPadding(5, 0, 15, 0);
          
          
          /* At first, add the CheckBox to ourself
           * (! we are extending LinearLayout) */
          addView(mCheckBox,  new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));          
          addView(mText, new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
          
          mText.setOnClickListener( new OnClickListener()
          {
				@Override
				public void onClick(View v) {
					toggleCheckBoxState();
				}

          }); 
          mCheckBox.setOnClickListener( new OnClickListener()
          {
               @Override
               public void onClick(View v) {
            	   setCheckBoxState(getCheckBoxState());
               }
          });
          instance.setSelected(mCheckBox.isChecked());
          instance.setOnClickListener(new OnClickListener(){ //Takes care of a click anywhere in the row.
			@Override
			public void onClick(View v) {
				toggleCheckBoxState();
			}
        	  
          });
     }

     public void setPosition(int pos){
    	 position = pos;
     }
     
     public void setText(String words) {
          mText.setText(words);
     }
 
     public void toggleCheckBoxState()
     {
         setCheckBoxState(!getCheckBoxState());
     }
    
     public void setCheckBoxState(boolean bool)
     {
         mCheckBox.setChecked(bool);
         instance.setSelected(mCheckBox.isChecked());
         mCheckBoxText.setChecked(bool);
         cbla.setChecked(bool, position);
         if(bool)
        	 mCheckBox.setButtonDrawable(check_in_box);
         else
        	 mCheckBox.setButtonDrawable(check_box);
         if(mCheckBoxText.isParent()){
        	 int childPosition = position+1;
        	 while(cbla.hasItem(childPosition) && !((CheckBoxifiedText) cbla.getItem(childPosition)).isParent()){
        		 CheckBoxifiedTextView cb_view = (CheckBoxifiedTextView)cbla.get_View(childPosition);
        		 if(cb_view.getCheckBoxState()!=bool);
        		 	cb_view.setCheckBoxState(bool);
        		 childPosition++;
        	 }
         } 
         else{ //run up and down to see if all the children have been selected or deselected, then make the parent match if they have.
        	 int childPosition = position+1;
        	 while(cbla.hasItem(childPosition) && !((CheckBoxifiedText) cbla.getItem(childPosition)).isParent()){
        		 boolean neighbor = ((CheckBoxifiedTextView)cbla.get_View(childPosition)).getCheckBoxState();
        		 if(neighbor!=bool)
        			 return;
        		 childPosition++;
        	 }
        	 childPosition = position-1;
        	 while(cbla.hasItem(childPosition) && !((CheckBoxifiedText) cbla.getItem(childPosition)).isParent()){
        		 boolean neighbor = ((CheckBoxifiedTextView)cbla.get_View(childPosition)).getCheckBoxState();
        		 if(neighbor!=bool)
        			 return;
        		 childPosition--;
        	 }
        	 //if you get here they're all the same and childPosition should be on the parent, set it to match.
    		 boolean neighbor = ((CheckBoxifiedTextView)cbla.get_View(childPosition)).getCheckBoxState();
    		 if(neighbor==bool)
    			 return;
        	 ((CheckBoxifiedTextView)cbla.get_View(childPosition)).setCheckBoxState(bool);
         }
     }
    
     public boolean getCheckBoxState()
     {
         return mCheckBox.isChecked();
     }
}