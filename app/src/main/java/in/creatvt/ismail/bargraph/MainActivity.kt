package `in`.creatvt.ismail.bargraph

import `in`.creatvt.ismail.simplegraph.SimpleBarGraph
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        simpleBarGraph.setData(arrayListOf(
            SimpleBarGraph.BarData(70f,"Java"),
            SimpleBarGraph.BarData(85f,"Python"),
            SimpleBarGraph.BarData(75f,"JavaScript"),
            SimpleBarGraph.BarData(45f,"C/C++"),
            SimpleBarGraph.BarData(60f,"PHP")
        ))

        animateBars.setOnClickListener { simpleBarGraph.animateBars() }

    }
}
