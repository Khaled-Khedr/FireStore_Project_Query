package com.example.myapplication

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.myapplication.classes.Note
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var editTextTitle: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var editTextPriority: EditText
    private lateinit var saveButton: Button
    private lateinit var loadButton: Button
    private lateinit var textViewData: TextView


    //private lateinit var listener:ListenerRegistration  //allows us to remove a listener when we remove the app
    private val db: FirebaseFirestore =
        FirebaseFirestore.getInstance()  //getting an instance of the db
    private val docRf: DocumentReference = db.collection("Notebook").document("My first note")
    private val noteBookRf: CollectionReference =
        db.collection("Notebook") //this allows us to add new notes to it and its refers to a collection of documents

    //a document reference in our db so we dont keep on typing this over and over again
    private val KEY_TITLE = "title"
    private val KEY_DESCRIPTION = "description"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextTitle = findViewById(R.id.edit_text_title)
        editTextDescription = findViewById(R.id.edit_text_description)
        saveButton = findViewById(R.id.button_add_button)
        loadButton = findViewById(R.id.load_button)
        textViewData = findViewById(R.id.text_view_data)
        editTextPriority = findViewById(R.id.edit_text_priority)


        saveButton.setOnClickListener {
            addNote()
        }

        loadButton.setOnClickListener {
            loadNotes()
        }

    }

    override fun onStart() {
        super.onStart()
        noteBookRf.whereGreaterThanOrEqualTo("priority", 2)
            .orderBy("priority", Query.Direction.ASCENDING)
            .addSnapshotListener(this) { documentSnapShots, error ->
                //this basically detaches the listener when u exit the app
                //Where greater than or equal to 2 displays only the fields with priority of 2 + basically orders them by priority
                //They are ordered in ascending direction
                error?.let {//if the error isn't null
                    return@addSnapshotListener
                }

                documentSnapShots?.let {
                    var data = " "  //used to append the data we get

                    for (documentSnapshot in it) {  //it refers to the query-snapshots (aka first parameter)

                        val note = documentSnapshot.toObject(Note::class.java)
                        note.id =
                            documentSnapshot.id  //assigning the documentsSpot id to the id to be used in the db

                        val title = note.title
                        val description = note.description
                        val priority = note.priority
                        data += "ID: ${note.id}"+"\nTitle: " + title + "\nDescription: $description" +
                                "\nPriority: $priority \n\n"
                    }
                    textViewData.text = data

                }

            }
    }


    private fun addNote() {
        val title = editTextTitle.text.toString()
        val description = editTextDescription.text.toString()
        /*  //THE FIRST METHOD MAP
            val note =
                mutableMapOf<String, Any>()  //in firebase data is stored in pairs so we need a mutable map
            note.put(KEY_TITLE, title)
            note.put(KEY_DESCRIPTION, description)

         */
        if (editTextPriority.text.toString().isEmpty()) {
            editTextPriority.setText("0")
        }
        val priority = editTextPriority.text.toString().toInt()
        val note = Note(title, description, priority)

        noteBookRf.add(note)  //adds a new document
            //or u can type docRf.set(note) since we made a reference
            //setting a collection name and a document name firebase can do it auto tho then setting it to the note map
            .addOnSuccessListener {
                Toast.makeText(this@MainActivity, "Note added!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this@MainActivity, "Error: note was not added!", Toast.LENGTH_SHORT)
                    .show()

            }

    }

    private fun loadNotes() {
       val task1 = noteBookRf.whereLessThan("priority", 2).orderBy("priority").get()
       val task2 = noteBookRf.whereGreaterThan("priority", 2).orderBy("priority").get()
       //val task3= noteBookRf.whereNotEqualTo("priority",2).orderBy("priority").get() my way literally first 2 lines in 1
        val allTasks: Task<List<QuerySnapshot>> = Tasks.whenAllSuccess(task1,task2)
        // allTasks of type Task of query snapshot lists
        allTasks.addOnSuccessListener {
            var data=""

            for (querySnapshot in it) {  //it refers to list of query snapshots that we created
                for (documentSnapshot in querySnapshot) {
                    val note = documentSnapshot.toObject(Note::class.java)
                    note.id = documentSnapshot.id

                    val title = note.title
                    val description = note.description
                    val priority = note.priority
                    data += "ID: ${note.id}"+"\nTitle: " + title + "\nDescription: $description" +
                            "\nPriority: $priority \n\n"

                }
            }
            textViewData.text=data
        }

        /*
    .addOnSuccessListener {  //we are basically looping through a query of document snapshots
            querydocumentsnapshots ->
        var data = " "  //used to append the data we get

        for (documentSnapshot in querydocumentsnapshots) {

            val note = documentSnapshot.toObject(Note::class.java)
            val title = note.title
            val description = note.description
            val priority = note.priority
            data += "Title: " + title + "\nDescription: $description" +
                    "\nPriority: $priority \n\n"
        }
        textViewData.text = data
    }.addOnFailureListener {
        Log.d(TAG,it.toString())
    }

         */
    }
}



