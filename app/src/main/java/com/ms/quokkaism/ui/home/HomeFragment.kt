package com.ms.quokkaism.ui.home

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.ms.quokkaism.NotificationPublisher
import com.ms.quokkaism.R
import com.ms.quokkaism.db.Quote
import com.ms.quokkaism.extension.toggleSideMenu
import com.ms.quokkaism.model.ProfileSetting
import com.ms.quokkaism.ui.base.BaseFragment
import com.ms.quokkaism.ui.home.adapter.QuoteFullscreenAdapter
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.fragment_home.*


class HomeFragment : BaseFragment(), QuoteFullscreenAdapter.OnItemClickListener {

    private lateinit var homeViewModel: HomeViewModel
    private var quoteFullscreenAdapter: QuoteFullscreenAdapter? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        subscribeToViewModel()
        subscribeToViewEvents()
        scheduleNotification()
    }

    private fun subscribeToViewModel() {
        homeViewModel.quotes.observe(viewLifecycleOwner, Observer {
            it?.takeIf { it.isNotEmpty() } ?.let {
                home_quote_rv?.visibility = View.VISIBLE
                initRecycler(it)
            } ?: kotlin.run {
                home_quote_rv?.visibility = View.GONE
                home_welcome_tv?.visibility = View.VISIBLE
            }
        })

        homeViewModel.like.observe(viewLifecycleOwner, Observer {
            if(it.second) {
                quoteFullscreenAdapter?.likeItemAt(it.first)
            } else {
                quoteFullscreenAdapter?.dislikeItemAt(it.first)
            }
        })

        homeViewModel.likeError.observe(viewLifecycleOwner, Observer {
            quoteFullscreenAdapter?.revertLikeItem(it)
        })
    }

    private fun initRecycler(quotesList: MutableList<Quote?>) {
        quoteFullscreenAdapter = QuoteFullscreenAdapter(quotesList,this)
        home_quote_rv?.layoutManager = LinearLayoutManager(activity,RecyclerView.HORIZONTAL,false)
        home_quote_rv?.adapter = quoteFullscreenAdapter
        PagerSnapHelper().attachToRecyclerView(home_quote_rv)
    }

    private fun subscribeToViewEvents() {
        home_side_menu_btn?.setOnClickListener {
            toggleSideMenu()
        }
    }

    private fun scheduleNotification() {
        if(Hawk.contains(ProfileSetting.NOTIFICATIONS_ARE_SET_KEY) && Hawk.get<Boolean>(ProfileSetting.NOTIFICATIONS_ARE_SET_KEY) == true)
        {
            return
        }
        if(Hawk.contains(ProfileSetting.PROFILE_SETTING_KEY))
        {
            val setting = Hawk.get<ProfileSetting?>(ProfileSetting.PROFILE_SETTING_KEY)
            setting?.let {
                val intent = Intent(activity,NotificationPublisher::class.java)
                val pendingIntent = PendingIntent.getBroadcast(activity,NotificationPublisher.INTENT_REQUEST_CODE,intent,PendingIntent.FLAG_UPDATE_CURRENT)

                val futureInMillis = SystemClock.elapsedRealtime() + it.interval.times(3600000)
                val alarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                alarmManager?.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,futureInMillis,it.interval.times(3600000).toLong(),pendingIntent)
                Hawk.put(ProfileSetting.NOTIFICATIONS_ARE_SET_KEY,true)
            }
        }
    }

    override fun onItemShareClick(position: Int, quote: Quote) {
        val intent = Intent(Intent.ACTION_SEND)
        val shareBody = quote.text
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(intent, getString(R.string.share_using)))
    }

    override fun onItemCopyClick(position: Int, quote: Quote) {
        val clipboardManager = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clipData = ClipData.newPlainText("quote",quote.text)
        clipboardManager?.setPrimaryClip(clipData)
        Toast.makeText(activity,R.string.the_text_copied_successfully,Toast.LENGTH_SHORT).show()
    }

    override fun onItemLikeClick(position: Int, quote: Quote) {
        quote.id?.let {
            if(quote.isLiked()) {
                homeViewModel.dislike(position,quote)
            } else {
                homeViewModel.like(position,quote)
            }
        }
    }

}
