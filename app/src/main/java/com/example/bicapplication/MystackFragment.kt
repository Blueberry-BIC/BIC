package com.example.bicapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bicapplication.databinding.FragmentMystackBinding
import com.example.bicapplication.responseObject.ListResponseData
import com.example.bicapplication.retrofit.RetrofitInterface
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class MystackFragment : Fragment() {
    private lateinit var binding: FragmentMystackBinding
    val retrofitInterface = RetrofitInterface.create(GlobalVari.getUrl())
    private var STACK: Array<String> = arrayOf("코딩스킬","시사교양","신체단련","생활")
    private var values: ArrayList<BarEntry> = ArrayList()  // 1. [BarEntry]  (x, y) 쌍으로 Bar Chart에 표시될 데이터를 저장하여 이를 리스트에 추가

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=FragmentMystackBinding.inflate(inflater, container, false)

        getMyInformation() //서버로부터 내정보 가져오기
        configureChartAppearance() //BarChart의 기본적인 ui등 세팅

        return binding.root
    }

    companion object {
        fun newInstance() : MystackFragment {
            return MystackFragment()
        }
    }

    //서버로부터 내정보 가져오기
    private fun getMyInformation(){

        val userid = "65b537f388bb8423ff6e0f8d"
        retrofitInterface.getUser(userid).enqueue(object : Callback<ListResponseData> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(
                call: Call<ListResponseData>,
                response: Response<ListResponseData>
            ) {
                if (response.isSuccessful) {
                    //List<Any>타입으로 받은 data값을 jsonarray로 다시 만들어줌
                    val jsonArray = JSONArray(response.body()?.result)
                    val jsonObject = jsonArray.getJSONObject(0) // 본인 유저정보 json으로 가져옴

                    //각 스택 필드를 가져와서 각 갯수를 그래프 data에 삽입
                    val stack1 = jsonObject.getJSONArray("stack1") //jsonarray타입으로 몽고db에 저장된 array필드 가져옴
                    val stack2 = jsonObject.getJSONArray("stack2")
                    val stack3 = jsonObject.getJSONArray("stack3")
                    val stack4 = jsonObject.getJSONArray("stack4")
                    values.add(BarEntry(0f, stack1.length().toFloat()))
                    values.add(BarEntry(1f, stack2.length().toFloat()))
                    values.add(BarEntry(2f, stack3.length().toFloat()))
                    values.add(BarEntry(3f, stack4.length().toFloat()))

                    //참가중인 챌린지 필드 가져와서 갯수 보여주기
                    val progress_chall = jsonObject.getJSONArray("progress_chall")
                    binding.textView1.text = "${progress_chall.length()}개"

                    //완료한 총 챌린지수(모든 스택들 크기 합)가져와서 갯수 보여주기
                    val completedChallNum =  stack1.length() +  stack2.length() +  stack3.length() + stack4.length()
                    binding.textView3.text = "${completedChallNum}개"

                    //서버로부터 받아온 데이터 이용해서 막대그래프 그리기 작업 진행
                    createChartData().let { prepareChartData(it) }

                    //Log.e("태그", "내정보 가져오기 통신 성공 stack1: , $stack1"+ "  : "+stack1.get(0))
                } else {
                    Log.e(
                        "태그",
                        "내정보 가져오기 서버접근했지만 실패: response.errorBody()?.string()" + response.errorBody().toString()
                    )
                }
            }
            override fun onFailure(call: Call<ListResponseData>, t: Throwable) {
                Log.d("CHALL", "fail 내정보 가져오기 ${t}")
            }
        })
    }


    //BarChart의 기본적인 것들 세팅
    private fun configureChartAppearance() {
        binding.chart.getDescription().setEnabled(false) // chart 밑에 description 표시 유무
        binding.chart.setTouchEnabled(false) // 터치 유무
        binding.chart.getLegend().setEnabled(false) // Legend는 차트의 범례
        binding.chart.setExtraOffsets(10f, 0f, 40f, 0f)

        // X, Y 바의 애니메이션 효과
        binding.chart.animateY(1000)
        binding.chart.animateX(1000) 

        // XAxis - 선 유무, 사이즈, 색상, 축 위치 설정
        val xAxis: XAxis = binding.chart.xAxis
        xAxis.setDrawAxisLine(false)
        xAxis.granularity = 1f
        xAxis.textSize = 11f
        xAxis.gridLineWidth = 25f
        xAxis.gridColor = Color.parseColor("#80E5E5E5")
        xAxis.position = XAxis.XAxisPosition.BOTTOM // X 축 데이터 표시 위치

        // YAxis - 선 유무, 데이터 최솟값/최댓값, label 유무
        val axisLeft: YAxis = binding.chart.axisLeft
        axisLeft.setDrawGridLines(false)
        axisLeft.setDrawAxisLine(false)
        axisLeft.axisMinimum = 0f // 최솟값
        axisLeft.axisMaximum = 10f // 최댓값 //막대그래프 최대 길이 나타냄
        axisLeft.granularity = 1f // 값만큼 라인선 설정
        axisLeft.setDrawLabels(false) // label 삭제

        // YAxis - 사이즈, 선 유무
        val axisRight: YAxis = binding.chart.axisRight
        axisRight.textSize = 12f
        axisRight.setDrawLabels(false) // label 삭제
        axisRight.setDrawGridLines(false)
        axisRight.setDrawAxisLine(false)

        // XAxis에 원하는 String 설정하기 (스택 종류)
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return STACK[value.toInt()]
            }
        }
    }

    //BarChart에 표시될 데이터를 생성
    private fun createChartData(): BarData {
        val SET_LABEL = "ddd"

        // 2. [BarDataSet] 단순 데이터를 막대 모양으로 표시, BarChart의 막대 커스텀
        val set2 = BarDataSet(values, SET_LABEL)
        set2.setDrawIcons(false)
        set2.setDrawValues(true)
        set2.color = Color.parseColor("#66767676") // 색상 설정
        // 데이터 값 원하는 String 포맷으로 설정하기 (ex. ~회)
        set2.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString() + "회"
            }
        }
        // 3. [BarData] 보여질 데이터 구성
        val data = BarData(set2)
        data.barWidth = 0.5f
        data.setValueTextSize(15f)
        return data
    }


    //생성된 BarData를 실제 BarChart 객체에 전달하고 BarChart를 갱신해 데이터를 표시
    private fun prepareChartData(data: BarData) {
        binding.chart.data = data // BarData 전달
        binding.chart.invalidate() // BarChart 갱신해 데이터 표시
    }






}