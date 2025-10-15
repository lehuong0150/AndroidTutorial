package com.example.androidtutorial.layout

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.androidtutorial.R
import com.example.androidtutorial.databinding.ActivityLayoutBinding

/*  Demo sử dụng các loại layout thiết kế giao diện
    - ConstraintLayout: Layout cha bao ngoài, cho phép sắp xếp linh hoạt các thành phần bằng
    ràng buộc (constraints).
    - RelativeLayout: Dùng cho phần Header, sắp xếp avatar, tên người dùng và icon giỏ hàng
    theo vị trí tương đối.
    - LinearLayout (AppCompat/Compat): Dùng cho các phần hiển thị theo hàng ngang hoặc dọc,
    ví dụ ô tìm kiếm, bộ lọc, tiêu đề sản phẩm.
    - FrameLayout: Bọc icon giỏ hàng và badge số lượng (hiển thị chồng lên nhau).
    - ScrollView: Cho phép cuộn nội dung khi vượt quá kích thước màn hình (áp dụng cho
    danh sách danh mục và danh sách sản phẩm).
    - GridLayout: Dùng để chia nội dung theo lưới (ví dụ: các danh mục hoặc sản phẩm).
    - TableLayout + TableRow: Hiển thị danh sách sản phẩm theo dạng bảng nhiều hàng.
Ứng dụng minh họa có sử dụng Spinner để chọn các tuỳ chọn lọc (theo mới nhất hoặc theo giá).
*/

class LayoutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val newestAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.newest_filter_options,
            android.R.layout.simple_spinner_item

        )
        newestAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spNewest.adapter = newestAdapter

        val priceAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.price_filter_options,
            android.R.layout.simple_spinner_item

        )
        newestAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spPrice.adapter = priceAdapter
    }
}