package page.chungjungsoo.to_dosample

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.WindowInsetsController
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.add_todo_dialog.*
import page.chungjungsoo.to_dosample.todo.Todo
import page.chungjungsoo.to_dosample.todo.TodoDatabaseHelper
import page.chungjungsoo.to_dosample.todo.TodoListViewAdapter
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    var dbHandler : TodoDatabaseHelper? = null
    private val mFormat: SimpleDateFormat = SimpleDateFormat("yyyy/M/d")
    private val tFormat: SimpleDateFormat = SimpleDateFormat("kk:mm")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set content view - loads activity_main.xml
        setContentView(R.layout.activity_main)
        // Set app status bar color : white, force light status bar mode
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        // Set light status bar mode depending on the android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController!!.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        }
        else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Add database helper and load data from database
        dbHandler = TodoDatabaseHelper(this) // sqlite 로 쓰는거
        var todolist: MutableList<Todo> = dbHandler!!.getAll() // dbHandler 에서 todolist를 모두 받아오기.

        // Put data with custom listview adapter
        todoList.adapter = TodoListViewAdapter(this, R.layout.todo_item, todolist)
        todoList.emptyView = helpText




        // Onclick listener for add button
        addBtn.setOnClickListener {
            // By pressing the add button, we will inflate an AlertDialog.
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_todo_dialog, null)//?

            // Get elements from custom dialog layout (add_todo_dialog.xml)
            val titleToAdd = dialogView.findViewById<EditText>(R.id.todoTitle) // 에디트로 우리가 설정한 제목 Title
            val desciptionToAdd = dialogView.findViewById<EditText>(R.id.todoDescription) // 설명문description 변수로 지정.
            val dateToAdd = dialogView.findViewById<TextView>(R.id.todoDate) // 데이트 텍스트
            val dateBtnToAdd = dialogView.findViewById<Button>(R.id.todoDateBtn)// 데이트 버튼
            val timeToAdd = dialogView.findViewById<TextView>(R.id.todoTime)
            val timeBtnToAdd = dialogView.findViewById<Button>(R.id.todoTimeBtn)
            val finishedToAdd = dialogView.findViewById<CheckBox>(R.id.todoFinished)
            val finishedBtn = todoList.findViewById<Button>(R.id.finished)

            // Add InputMethodManager for auto keyboard popup
            val ime = applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            // Cursor auto focus on title when AlertDialog is inflated
            // 아 자동적으로 title 에 커서가 가게끔 조절해주는구나
            titleToAdd.requestFocus()


            // Show keyboard when AlertDialog is inflated
            // 그럼 이거는 바로 키보드 보여주는거고
            ime.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)


            // Add positive button and negative button for AlertDialog.
            // Pressing the positive button: Add data to the database and also add them in listview and update.
            // Pressing the negative button: Do nothing. Close the AlertDialog
            val add = builder.setView(dialogView)
                .setPositiveButton("추가") { _, _ ->
                    if (!TextUtils.isEmpty(titleToAdd.text.trim())) {
                        // Add item to the database
                        val todo = Todo(
                                titleToAdd.text.toString(),
                                desciptionToAdd.text.toString(),
                                finishedToAdd.isChecked,
                                dateToAdd.text.toString(),
                                timeToAdd.text.toString()
                        )
                        dbHandler!!.addTodo(todo)

                        // Add them to listview and update.
                        (todoList.adapter as TodoListViewAdapter).add(todo)
                        (todoList.adapter as TodoListViewAdapter).notifyDataSetChanged()

                        // Close keyboard
                        ime.hideSoftInputFromWindow(titleToAdd.windowToken, 0)
                    }
                    else {
                        Toast.makeText(this,
                                "제목을 입력하세요!", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("취소") { _, _ ->
                    // Cancel Btn. Do nothing. Close keyboard.
                    ime.hideSoftInputFromWindow(titleToAdd.windowToken, 0)
                }
                .show()
                .getButton(DialogInterface.BUTTON_POSITIVE) // 취소버튼과 추가버튼.

            // Default status of add button should be disabled. Because when AlertDialog inflates,
            // the title is empty by default and we do not want empty titles to be added to listview
            // and in databases.
            add.isEnabled = false

            // Listener for title text. If something is inputted in title, we should re-enable the add button.
            titleToAdd.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    if (!TextUtils.isEmpty(p0.toString().trim())) {
                        add.isEnabled = true
                    } else {
                        titleToAdd.error = "TODO 제목을 입력하세요!"
                        add.isEnabled = false
                    }
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })
            // 날짜 클릭이 안되어도 현재 날짜로 표시 되게끔.
            val date = Date()
            val time: String = mFormat.format(date)
            dateToAdd.text = time
            // 날짜 변경 버튼 클릭 리스너
            dateBtnToAdd.setOnClickListener { view ->
                var cal = Calendar.getInstance()
                var year = cal.get(Calendar.YEAR)
                var month = cal.get(Calendar.MONTH)
                var day = cal.get(Calendar.DAY_OF_MONTH)

                var date_listener = object : DatePickerDialog.OnDateSetListener{
                    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                        dateToAdd.text = "${year}/${month +1}/${dayOfMonth}"
                    }
                }
                var builder = DatePickerDialog(this, date_listener, year, month, day)
                builder.show()
            }
            val now = System.currentTimeMillis()
            val tmp = Date(now)
            timeToAdd.text = tFormat.format(tmp)
            // 시간 변경 버튼 ( 아직 데이터 변경은 안넣음. ) -> 넣음.
            timeBtnToAdd.setOnClickListener { view ->
                var time = Calendar.getInstance()
                var hour = time.get(Calendar.HOUR)
                var minute = time.get(Calendar.MINUTE)

                var timeListener = object : TimePickerDialog.OnTimeSetListener{
                    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                        timeToAdd.text = "${hourOfDay}:${minute}"
                    }
                }

                var builder = TimePickerDialog(this, timeListener, hour, minute, false)
                builder.show()
            }

        }
    }
}