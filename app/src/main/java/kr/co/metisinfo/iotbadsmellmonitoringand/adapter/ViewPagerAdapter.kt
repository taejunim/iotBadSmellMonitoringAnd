package kr.co.metisinfo.iotbadsmellmonitoringand.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import kr.co.metisinfo.iotbadsmellmonitoringand.fragment.RegisterStatusFragment
import kr.co.metisinfo.iotbadsmellmonitoringand.fragment.StatisticsFragment
import lombok.NonNull

//금일 악취 접수 현황, 우리동네 악취 현황 viewPager
class ViewPagerAdapter(fa: FragmentActivity?, var mCount: Int) : FragmentStateAdapter(fa!!) {
    @NonNull
    override fun createFragment(position: Int): Fragment {

        return if (position == 0) {
            RegisterStatusFragment()
        } else {
            StatisticsFragment()
        }
    }

    override fun getItemCount(): Int {
        return 2
    }
}
