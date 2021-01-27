package page.chungjungsoo.to_dosample.todo

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import page.chungjungsoo.to_dosample.MainActivity
import page.chungjungsoo.to_dosample.R
import java.util.*


class TodoListViewAdapter (context: Context, var resource: Int, var items: MutableList<Todo> ) : ArrayAdapter<Todo>(context, resource, items){
    private lateinit var db: TodoDatabaseHelper

    override fun getView(position: Int, convertView: View?, p2: ViewGroup): View {
        val layoutInflater : LayoutInflater = LayoutInflater.from(context)
        val view : View = layoutInflater.inflate(resource , null )

        val title : TextView = view.findViewById(R.id.listTitle)
        val description : TextView = view.findViewById(R.id.listDesciption)
        val edit : Button = view.findViewById(R.id.editBtn)
        val delete : Button = view.findViewById(R.id.delBtn)
        val finishedBtn : Button = view.findViewById(R.id.finished)
        val background : LinearLayout = view.findViewById(R.id.klist)

        db = TodoDatabaseHelper(this.context)
        finishedBtn.isClickable = false
        // Get to-do item
        var todo = items[position]

        // Load title and description to single ListView item
        title.text = todo.title
        description.text = todo.date + " : "+ todo.description
        // list description 에 다 넣음 .
        // 귀찮으니까 description에다가 모두 넣기.

        // OnClick Listener for edit button on every ListView items
        // 에디트 버튼 .
        edit.setOnClickListener {
            // Very similar to the code in MainActivity.kt
            val builder = AlertDialog.Builder(this.context)
            val dialogView = layoutInflater.inflate(R.layout.add_todo_dialog, null)
            val titleToAdd = dialogView.findViewById<EditText>(R.id.todoTitle)
            val desciptionToAdd = dialogView.findViewById<EditText>(R.id.todoDescription)
            val dateToAdd = dialogView.findViewById<TextView>(R.id.todoDate)
            val dateBtnToAdd = dialogView.findViewById<Button>(R.id.todoDateBtn)// 데이트 버튼
            val timeToAdd = dialogView.findViewById<TextView>(R.id.todoTime)
            val timeBtnToAdd = dialogView.findViewById<Button>(R.id.todoTimeBtn)
            val finishedToAdd = dialogView.findViewById<CheckBox>(R.id.todoFinished)


            val ime = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            //load한거 표현하기.
            titleToAdd.setText(todo.title)
            desciptionToAdd.setText(todo.description)
            dateToAdd.setText(todo.date)
            timeToAdd.setText(todo.time)
            finishedToAdd.isChecked = todo.finished


            titleToAdd.requestFocus()
            ime.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

            dateBtnToAdd.setOnClickListener{ view ->
                var cal = Calendar.getInstance()
                var year = cal.get(Calendar.YEAR)
                var month = cal.get(Calendar.MONTH)
                var day = cal.get(Calendar.DAY_OF_MONTH)

                var date_listener = object : DatePickerDialog.OnDateSetListener{
                    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                        dateToAdd.text = "${year}/${month +1}/${dayOfMonth}"
                    }
                }
                var bil = DatePickerDialog(this.context, date_listener, year, month, day)
                bil.show()
            }
            // 시간 변경 버튼 ( 아직 데이터 변경은 안넣음. )'''
            timeBtnToAdd.setOnClickListener { view ->
                var time = Calendar.getInstance()
                var hour = time.get(Calendar.HOUR)
                var minute = time.get(Calendar.MINUTE)

                var timeListener = object : TimePickerDialog.OnTimeSetListener{
                    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                        timeToAdd.text = "${hourOfDay}:${minute}"
                    }
                }

                var builder = TimePickerDialog(this.context, timeListener, hour, minute, false)
                builder.show()
            }

            builder.setView(dialogView)
                .setPositiveButton("수정") { _, _ ->
                    val tmp = Todo(
                        titleToAdd.text.toString(),
                        desciptionToAdd.text.toString(),
                        finishedToAdd.isChecked,
                        dateToAdd.text.toString(),
                        timeToAdd.text.toString()
                    )

                    val result = db.updateTodo(tmp, position)
                    if (result) {
                        todo.title = titleToAdd.text.toString()
                        todo.description = desciptionToAdd.text.toString()
                        todo.finished = finishedToAdd.isChecked
                        todo.date = dateToAdd.text.toString()
                        todo.time = timeToAdd.text.toString()


                        notifyDataSetChanged()
                        ime.hideSoftInputFromWindow(titleToAdd.windowToken, 0)
                    }
                    else {
                        Toast.makeText(this.context, "수정 실패! :(", Toast.LENGTH_SHORT).show()
                        notifyDataSetChanged()
                    }
                }
                .setNegativeButton("취소") {_, _ ->
                    // Cancel Btn. Do nothing. Close keyboard.
                    ime.hideSoftInputFromWindow(titleToAdd.windowToken, 0)
                }
                .show()
        }

        // OnClick Listener for X(delete) button on every ListView items
        delete.setOnClickListener {
            val result = db.delTodo(position)
            if (result) {
                items.removeAt(position)
                notifyDataSetChanged()
            }
            else {
                Toast.makeText(this.context, "삭제 실패! :(", Toast.LENGTH_SHORT).show()
                notifyDataSetChanged()
            }
        }



        return view
    }
}