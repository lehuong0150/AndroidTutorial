package com.example.androidtutorial.layout

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.androidtutorial.adapter.EmployeeAdapter
import com.example.androidtutorial.databinding.ActivityProfileBinding
import com.example.androidtutorial.models.Person

/*Demo thay đổi về 2 cách hiển thị danh sách
    - Dạng danh sách (ListView)
    - Dạng lưới so le (Staggered Grid – kiểu ghi chú như S2Note/Keep)
 */
class EmployeeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var adapter: EmployeeAdapter
    private lateinit var employees: MutableList<Person>
    private var isCustom = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        employees = mutableListOf()
        adapter = EmployeeAdapter(employees) {
            val newEmp = Person(
                employees.size + 1,
                name = "Nhân viên: ${employees.size + 1}",
                position = "Chưa có chức vụ"
            )
            employees.add(newEmp)
            adapter.notifyItemInserted(employees.size)
        }

        binding.recyclerEmployee.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = this@EmployeeActivity.adapter
        }

        binding.btnCustom.setOnClickListener {
            isCustom = !isCustom
            binding.recyclerEmployee.also { recycler ->
                recycler.layoutManager = if (isCustom) {
                    StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                } else {
                    LinearLayoutManager(this)
                }
                recycler.adapter = adapter
            }
        }
    }
}
