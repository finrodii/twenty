/* $Id: BulletedTextListAdapter.java 57 2007-11-21 18:31:52Z steven $
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;

/** @author Steven Osborn - http://steven.bitsetters.com */
public class CheckBoxifiedIndexAdapter extends ArrayAdapter<CheckBoxifiedText> implements SectionIndexer { //extends BaseAdapter{

     /** Remember our context so we can use it when constructing views. */
     private Context mContext;

     private List<CheckBoxifiedText> mItems = new ArrayList<CheckBoxifiedText>();
     private List<CheckBoxifiedTextView> mViews = new ArrayList<CheckBoxifiedTextView>();
     
 	 List<CheckBoxifiedText> myElements;
     HashMap<String, Integer> alphaIndexer;

     String[] sections;

     public CheckBoxifiedIndexAdapter(Context context) {
    	  super(context, 0);
          mContext = context;
          clear();
     }

     public void clear(){
    	 mItems.clear();
    	 mViews.clear();
     }
     
     public void addItem(CheckBoxifiedText it, int pos) { 
    	 mItems.add(it); 
    	 CheckBoxifiedTextView btv; 
    	 btv = new CheckBoxifiedTextView(this, mContext, it);
		 btv.setPosition(pos);
		 mViews.add(pos, btv);
		 //mViews.add(null);  //Views show up a little more quickly if they are added lazily in getView() (see below).
		 					  //but the scroll gets a little choppy that way. Better to just add time to search.
     }

     public void setListItems(List<CheckBoxifiedText> lit) { mItems = lit; }

     /** @return The number of items in the */
     public int getCount() { return mItems.size(); }

     public boolean hasItem(int position){
    	 if(position < 0 || position >= mItems.size())
    		 return false;
    	 return true;
     }
     
     public CheckBoxifiedText getItem(int position) { return mItems.get(position); }
     
     public Object get_View(int position) { return mViews.get(position); }
 
     public ArrayList<CheckBoxifiedText> getSelectedItems(){
    	 ArrayList<CheckBoxifiedText> selected = new ArrayList<CheckBoxifiedText>();
    	 Iterator<CheckBoxifiedText> iter = mItems.iterator();
    	 while(iter.hasNext()){
    		 CheckBoxifiedText item = iter.next();
    		 if(item.getChecked())
    			 selected.add(item);
    	 }
    	 return selected;
     }
     
     public boolean isItemChecked(int position) {
    	 CheckBoxifiedText cbox = mItems.get(position);
    	 return cbox.getChecked();
     }
     
     public void setChecked(boolean value, int position){
         mItems.get(position).setChecked(value);
         //Log.d("twenty", "set pos: "+position+" to "+value);
     }
//     public void selectAll(){
//         for(CheckBoxifiedText cboxtxt: mItems)
//              cboxtxt.setChecked(true);
//         /* Things have changed, do a redraw. */
//         this.notifyDataSetInvalidated();
//     }
//     public void deselectAll()
//     {
//         for(CheckBoxifiedText cboxtxt: mItems)
//             cboxtxt.setChecked(false);
//        /* Things have changed, do a redraw. */
//        this.notifyDataSetInvalidated();
//     }

     public boolean areAllItemsSelectable() { return false; }

     /** Use the array index as a unique id. */
     public long getItemId(int position) {
    	 return position;
     }

     /** @param convertView The old view to overwrite, if one is passed
      * @returns a CheckBoxifiedTextView that holds wraps around an CheckBoxifiedText */
     public View getView(int position, View convertView, ViewGroup parent){

    	 //Log.i("twenty", "getView for pos: "+ position);
    	 CheckBoxifiedTextView btv; 
    	 if(mViews.get(position) != null)
    		 btv = mViews.get(position);
    	 else {
    		 btv = new CheckBoxifiedTextView(this, mContext, mItems.get(position));
    		 btv.setPosition(position);
    		 mViews.set(position, btv);
    		 //Log.i("twenty","Created new view");
    	 }
		 return btv;
     }

     /**Lets us use fastSearch on the search results **/
     public void makeIndex(){
         myElements = mItems;
         // here is the tricky stuff
         alphaIndexer = new HashMap<String, Integer>();
         // in this hashmap we will store here the positions for
         // the sections

         int size = mItems.size();
         for (int i = size - 1; i >= 0; i--) {
        	 if(mItems.get(i).isParent()){
	             String element = mItems.get(i).getText();
	             alphaIndexer.put(element.substring(0, 1), i);
        	 }
             //We store the first letter of the word, and its index.
             //The Hashmap will replace the value for identical keys are putted in
         }

         // now we have an hashmap containing for each first-letter
         // sections(key), the index(value) in where this sections begins

         // we have now to build the sections(letters to be displayed)
         // array .it must contains the keys, and must (I do so...) be
         // ordered alphabetically

         Set<String> keys = alphaIndexer.keySet(); // set of letters ...sets
         // cannot be sorted...

         Iterator<String> it = keys.iterator();
         ArrayList<String> keyList = new ArrayList<String>(); // list can be
         // sorted

         while (it.hasNext()) {
             String key = it.next();
             keyList.add(key);
         }

         Collections.sort(keyList);

         sections = new String[keyList.size()]; // simple conversion to an
         // array of object
         keyList.toArray(sections);
     }
     
	@Override
	public int getPositionForSection(int section) {
        // Log.v("getPositionForSection", ""+section);
        String letter = sections[section];

        return alphaIndexer.get(letter);
	}

	@Override
	public int getSectionForPosition(int position) {
        // you will notice it will be never called (right?)
        //Log.v("getSectionForPosition", "called");
        return 0;
	}

	@Override
	public Object[] getSections() {
    	return sections; // to string will be called each object, to display the letter
	}
}
