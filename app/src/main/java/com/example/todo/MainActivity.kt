package com.example.todo

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.Data.Data
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlin.collections.ArrayList

const val DATA = "Data"



class MainActivity : AppCompatActivity() {

    companion object{
        private const val REQUEST_CODE = 233
    }
    private lateinit var database : DatabaseReference
    private lateinit var recyclerView: RecyclerView

    private var dataList = ArrayList<Data>()

    lateinit var adapter: RecyclerViewAdapter
    var a = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.rvMain)


        database = FirebaseDatabase.getInstance().getReference("Data")

        dataList = getUserData()

        adapter = RecyclerViewAdapter(this, dataList)


        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        //To click the add activity button view
        adapter.setOnItemClickListener(object: RecyclerViewAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                val dataListPosition = dataList[position]
                if (dataListPosition.viewType == 2){
                    Intent(this@MainActivity, AddNewTask::class.java).also{
                        startActivityForResult(it, REQUEST_CODE) }
                }
                else{
                    val editIntent =Intent(this@MainActivity, EditTask::class.java)
                    editIntent.putExtra("heading",dataListPosition.textTitle)
                    editIntent.putExtra("desc",dataListPosition.textDesc)
                    editIntent.putExtra("duration",dataListPosition.textDuration)
                    editIntent.putExtra("done",dataListPosition.textDone)
                    editIntent.putExtra("mainDate",dataListPosition.textMainDate)
                    editIntent.putExtra("MainDatetoggle",dataListPosition.dayDatevisibilityRev)
                    editIntent.putExtra("weekDayName",dataListPosition.textWeekDayName)
                    editIntent.putExtra("simpleDateID",dataListPosition.simpleDateID)
                    editIntent.putExtra("dayDatevisibilityRev",dataListPosition.dayDatevisibilityRev)
                    startActivityForResult(editIntent, REQUEST_CODE)
                }
            }
        })



    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, info: Intent?) {
        var except = 0
        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val data = info?.getSerializableExtra(DATA) as Data
            val similarTextTitle: String? = data.textTitle
            val similarMainDate: String? = data.textMainDate
            val similarDateID: String? = data.simpleDateID
            val similarDataTransfer: String? = data.dataTransfer


            if(similarDataTransfer == "addNewTask"){
                for(i in 0..dataList.size-1) {
                    if (similarMainDate == dataList[i].textMainDate){
                        data.dayDatevisibilityRev = true
                    }}

                for(i in 0..dataList.size-2){
                    if(similarTextTitle == dataList[i].textTitle){
                        dataList[i] = data
                        except+=1
                        break
                    }}}
            if(similarDataTransfer == "editTask"){
                for(i in 0..dataList.size-2){
                    if(similarDateID == dataList[i].simpleDateID){
                        dataList[i] = data
                        except+=1
                        break
                    }}
            }

            dataList.sortBy {
                it.simpleDateID
            }
            //Adding data to firebase
            database.child(data.textTitle.toString()).setValue(data)
            Log.e("datalit",dataList.toString())


            adapter.notifyDataSetChanged()
            dataList.clear()


        }
        super.onActivityResult(requestCode, resultCode, info)

    }


    private fun getUserData(): ArrayList<Data> {
        var fireBaseDataList: ArrayList<Data> = arrayListOf()
        if(a == 0){
            database.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    fireBaseDataList.clear()
                    for (userSnapshot in snapshot.children){
                        val data = userSnapshot.getValue(Data::class.java) 

                        if (data != null) {
                            fireBaseDataList.add(data)
                        }
                    }
                    dataList.add(Data(RecyclerViewAdapter.VIEW_ADD_BUTTON, "Na",true,"Na","na","na","na","na","na","na"))

                }
                else{
                    dataList.add(Data(RecyclerViewAdapter.VIEW_ADD_BUTTON, "Na",true,"Na","na","na","na","na","na","na"))
                }

                adapter.notifyDataSetChanged()
                Log.e("Schanged",a.toString())
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
            a =a +1
        }

        return fireBaseDataList
    }

}




