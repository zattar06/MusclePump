package com.example.musclepump.home

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.musclepump.R
import com.example.musclepump.databinding.ActivityHomeBinding
import com.example.musclepump.login.LoginActivity
import com.example.musclepump.model.Train
import com.example.musclepump.train.detail.TrainDetailActivity
import com.example.musclepump.util.AuthUtil
import com.example.musclepump.view.UserAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val trainList: MutableList<Train> = mutableListOf()
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userId: String

    private val adapter: UserAdapter by lazy {
        UserAdapter(trainList, ::goToTrainDetail, ::showDatePickerDialog)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAddNewTrain()

        databaseReference = FirebaseDatabase.getInstance().reference
        userId = AuthUtil().getUser()?.uid!!

        val name = intent.getStringExtra("name")
        binding.tlbToolbar.title = "Hello, $name"
        binding.rvTraines.adapter = adapter
        getTrains()
        setupLogout()
        setupPlaceholder()
    }

    private fun setupLogout() {
        binding.tlbToolbar.setOnMenuItemClickListener {
            val dialog = MaterialAlertDialogBuilder(this)
            dialog.setTitle("Logout")
            dialog.setMessage("Are you sure you want to log out?")
            dialog.setNegativeButton("Cancel", DialogInterface.OnClickListener { d, _ ->
                d.dismiss()
            })
            dialog.setPositiveButton("Yes", DialogInterface.OnClickListener { di, _ ->
                if (it.itemId == R.id.logout) {
                    AuthUtil().logout()
                    val user = AuthUtil().getUser()
                    if (user == null) {
                        goToLogin()
                    }
                }
            })
            dialog.show()
            false
        }
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun setupPlaceholder() {
        binding.llcPlaceholder.visibility = if (trainList.isEmpty()) View.VISIBLE else View.GONE
        binding.rvTraines.visibility = if (trainList.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun setupAddNewTrain() {
        binding.fabCreateTrain.setOnClickListener {
            openModalAddNewTrain()
        }
    }

    private fun openModalAddNewTrain() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.modal_add_new_train, null)
        dialog.setContentView(view)
        dialog.show()

        val btnAddNewTrain = view.findViewById<Button>(R.id.btn_add_new_train)
        btnAddNewTrain.setOnClickListener {
            val trainName = view.findViewById<TextInputLayout>(R.id.til_name_newtrain)
            val trainNameUserInput = trainName.editText?.text.toString()

            val trainDescription = view.findViewById<TextInputLayout>(R.id.til_description_newtrain)
            val trainDescriptionUserInput = trainDescription.editText?.text.toString()

            val train = Train(trainNameUserInput, trainDescriptionUserInput, userId)
            addNewTrain(train)

            dialog.dismiss()
        }
    }

    private fun addNewTrain(train: Train) {
        val trainsReference = databaseReference.child("trains").push()
        val trainId = trainsReference.key
        databaseReference.child("trains").child(trainId!!).setValue(train)
            .addOnSuccessListener {
                getTrains()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Error on adding a new train.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun getTrains() {
        databaseReference.child("trains").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allTrains = mutableListOf<Train>()
                snapshot.children.forEach { train ->
                    val trainData = train.getValue(Train::class.java)
                    trainData?.let {
                        allTrains.add(it)
                    }
                }
                trainList.clear()
                trainList.addAll(allTrains.filter { it.userId == userId })
                setupPlaceholder()
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("firebase", "Error getting trains", error.toException())
            }
        })
    }

    private fun showDatePickerDialog(train: Train, position: Int) {
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                train.date = formatDate(selectedDate)

                adapter.notifyItemChanged(position)
            },
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun formatDate(calendar: Calendar): String {
        val dateFormat = "dd/MM/yyyy"
        return android.text.format.DateFormat.format(dateFormat, calendar.time).toString()
    }

    private fun goToTrainDetail(train: Train) {
        val intent = Intent(this, TrainDetailActivity::class.java)
        intent.putExtra("trainId", train.userId)
        startActivity(intent)
    }
}
