package com.example.myapplication.classes

import com.google.firebase.firestore.Exclude

//the other way using data classes the first way used was mutableMaps with key value pairs

data class Note(val title:String ,val description:String,val priority:Int)
{
  constructor():this("","",0)//public no arg constructor needed otherwise app will crash compiler tells u

  @Exclude  //this excludes the id from the document content itself we don't want it to be displayed in the document
  var id:String=""  //id value
}