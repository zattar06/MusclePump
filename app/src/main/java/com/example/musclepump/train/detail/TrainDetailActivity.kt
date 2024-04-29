package com.example.musclepump.train.detail

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.musclepump.R
import com.example.musclepump.databinding.ActivityTrainDetailBinding
import com.example.musclepump.model.Exercise
import com.example.musclepump.util.AuthUtil
import com.example.musclepump.view.ExerciseAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TrainDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrainDetailBinding
    private val listExercise: MutableList<Exercise> = mutableListOf()
    private lateinit var databaseReference: DatabaseReference
    private lateinit var exerciseId: String
    private lateinit var adapter: ExerciseAdapter

    private val PICK_IMAGE_REQUEST = 1
    private var selectedPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        adapter = ExerciseAdapter(listExercise, this::onSelectImageClick)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance().reference.child("users").child(userId!!)

        exerciseId = AuthUtil().getUser()?.uid ?: ""
        setSupportActionBar(binding.toolbar)
        val exerciseList: RecyclerView = findViewById(R.id.rv_exercises)
        exerciseList.adapter = adapter
        binding.fabCreateExercise.setOnClickListener {
            openModalAddNewExercise()
        }

        getExercise()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_train_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.back -> {
                onBackPressed()
                true
            }

            R.id.delete_all -> {
                deleteAllExercises()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun openModalAddNewExercise() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.modal_add_new_exercise, null)
        dialog.setContentView(view)
        dialog.show()

        val btnAddNewExercise = view.findViewById<Button>(R.id.btn_add_new_exercise)
        btnAddNewExercise?.setOnClickListener {

            val exerciseName = view.findViewById<TextInputLayout>(R.id.til_exercise)
            val exerciseNameInput = exerciseName.editText?.text.toString()

            val obsExercise = view.findViewById<TextInputLayout>(R.id.til_observation)
            val obsExerciseInput = obsExercise.editText?.text.toString()

            val exercise = Exercise(exerciseNameInput, obsExerciseInput)
            addNewExercise(exerciseId, exercise)

            dialog.dismiss()
        }

        val btnSelectImage = view.findViewById<Button>(R.id.btn_select_image)
        btnSelectImage?.setOnClickListener {
            onSelectImageClick(-1)
            dialog.dismiss()
        }
    }



    private fun addNewExercise(trainId: String, exercise: Exercise) {
        exercise.trainId = trainId
        val exercisesReference = databaseReference.child("exercises").push()
        val exerciseId = exercisesReference.key
        databaseReference.child("exercises").child(exerciseId!!).setValue(exercise)
            .addOnSuccessListener {
                getExercise()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Error on adding a new exercise.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun getExercise() {
        databaseReference.child("exercises").orderByChild("trainId").equalTo(exerciseId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val allExercise = mutableListOf<Exercise>()
                    snapshot.children.forEach { exercise ->
                        val exerciseData = exercise.getValue(Exercise::class.java)
                        exerciseData?.let {
                            allExercise.add(it)
                        }
                    }
                    listExercise.clear()
                    listExercise.addAll(allExercise)
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("firebase", "Error getting exercises", error.toException())
                }
            })
    }

    private fun deleteAllExercises() {
        databaseReference.child("exercises").removeValue()
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "All exercises deleted successfully.",
                    Toast.LENGTH_SHORT
                ).show()
                getExercise()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Error deleting exercises.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun onSelectImageClick(position: Int) {
        selectedPosition = position
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage: Uri = data.data!!

            applyImageToItem(selectedImage)
        }
    }

    private fun applyImageToItem(imageUri: Uri) {
        if (selectedPosition != -1) {
            val exercise = listExercise[selectedPosition]
            exercise.imageUri = imageUri.toString()
            adapter.notifyItemChanged(selectedPosition)
        }
    }
}
