package com.ms.quokkaism.ui.home

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.ms.quokkaism.App
import com.ms.quokkaism.NotificationPublisher
import com.ms.quokkaism.R
import com.ms.quokkaism.db.model.Quote
import com.ms.quokkaism.extension.dismissDialog
import com.ms.quokkaism.extension.showLoadingDialog
import com.ms.quokkaism.extension.toggleSideMenu
import com.ms.quokkaism.model.ProfileSetting
import com.ms.quokkaism.ui.base.BaseFragment
import com.ms.quokkaism.ui.home.adapter.QuoteFullscreenAdapter
import com.ms.quokkaism.util.LoadingDialog
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.fragment_home.*


class HomeFragment : BaseFragment(), QuoteFullscreenAdapter.OnItemClickListener {

    private lateinit var homeViewModel: HomeViewModel
    private var loadingDialog: LoadingDialog? = null
    private var quoteFullscreenAdapter: QuoteFullscreenAdapter? = null
    private var layoutManager: LinearLayoutManager? = null
    private var pagerSnapHelper: PagerSnapHelper? = null
    private var isAllEmpty = true

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
        homeViewModel.searchQueryText.value = ""
        initRecycler()
        subscribeToViewModel()
        subscribeToViewEvents()
        scheduleNotification()
    }

    private fun subscribeToViewModel() {
        homeViewModel.lastReadQuotes.observe(viewLifecycleOwner, Observer {
            it?.takeIf { it.isNotEmpty() } ?.let {
                home_welcome_tv?.visibility = View.GONE
                home_no_result_tv?.visibility = View.GONE
                home_quote_rv?.visibility = View.VISIBLE
                home_arrow_group?.visibility = View.VISIBLE
                isAllEmpty = false
                quoteFullscreenAdapter?.submitList(it)
            } ?: kotlin.run {
                home_quote_rv?.visibility = View.GONE
                home_arrow_group?.visibility = View.GONE
                if(isAllEmpty) {
                    home_welcome_tv?.visibility = View.VISIBLE
                }
                else {
                    home_no_result_tv?.visibility = View.VISIBLE
                }
            }
        })
        homeViewModel.syncIsRunning.observe(viewLifecycleOwner, Observer {
            if(it) {
                loadingDialog = showLoadingDialog(R.string.syncing_with_server)
            } else {
                dismissDialog(loadingDialog)
            }
        })

        homeViewModel.quotesFetched.observe(viewLifecycleOwner, Observer {
            if(it) {
                scheduleNotification()
            }
        })
    }

    private fun initRecycler() {
        quoteFullscreenAdapter = QuoteFullscreenAdapter(this)
        layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        home_quote_rv?.layoutManager = layoutManager
        home_quote_rv?.adapter = quoteFullscreenAdapter
        pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper?.attachToRecyclerView(home_quote_rv)
    }

    private fun subscribeToViewEvents() {
        home_side_menu_btn?.setOnClickListener {
            toggleSideMenu()
        }

        home_search_et?.doOnTextChanged { text, start, before, count ->
            text?.let {
                homeViewModel.searchQueryText.value = "%$it%"
            } ?: kotlin.run {
                homeViewModel.searchQueryText.value = ""
            }
        }

        home_search_btn?.setOnClickListener {
            if(isSearchBoxCollapsed()) {
                expandSearchBox()
            } else {
                collapseSearchBox()
            }
        }

        home_next_btn?.setOnClickListener {
            showNext()
        }

        home_previous_btn?.setOnClickListener {
            showPrevious()
        }
    }

    private fun showNext() {
        layoutManager?.let {
            val pos = it.findLastCompletelyVisibleItemPosition()
            try {
                home_quote_rv?.smoothScrollToPosition(pos+1)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showPrevious() {
        layoutManager?.let {
            val pos = it.findLastCompletelyVisibleItemPosition()
            try {
                home_quote_rv?.smoothScrollToPosition(pos-1)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun expandSearchBox() {
        activity?.let { ctx ->
            home_search_box?.let { box ->
                val startWidth = box.measuredWidth
                val endWidth = ctx.resources.getDimensionPixelSize(R.dimen.home_search_width)
                val animator = ValueAnimator.ofInt(startWidth,endWidth)
                animator.duration = 400
                animator.addUpdateListener {
                    val value = it.animatedValue as Int
                    val params = box.layoutParams
                    params.width = value
                    box.layoutParams = params
                }
                animator.addListener(object : Animator.AnimatorListener{
                    override fun onAnimationRepeat(p0: Animator?) {}
                    override fun onAnimationEnd(p0: Animator?) {
                        home_search_et?.visibility = View.VISIBLE
                    }
                    override fun onAnimationCancel(p0: Animator?) {}
                    override fun onAnimationStart(p0: Animator?) {}

                })
                animator.start()
            }
        }
    }

    private fun collapseSearchBox() {
        activity?.let { ctx ->
            home_search_box?.let { box ->
                val startWidth = box.measuredWidth
                val endWidth = ctx.resources.getDimensionPixelSize(R.dimen.button_height_min)
                val animator = ValueAnimator.ofInt(startWidth,endWidth)
                animator.duration = 400
                animator.addUpdateListener {
                    val value = it.animatedValue as Int
                    val params = box.layoutParams
                    params.width = value
                    box.layoutParams = params
                }
                animator.addListener(object : Animator.AnimatorListener{
                    override fun onAnimationRepeat(p0: Animator?) {}
                    override fun onAnimationEnd(p0: Animator?) {}
                    override fun onAnimationCancel(p0: Animator?) {}
                    override fun onAnimationStart(p0: Animator?) {
                        home_search_et?.visibility = View.INVISIBLE
                    }

                })
                animator.start()
            }
        }
    }

    private fun isSearchBoxCollapsed(): Boolean {
        activity?.let { ctx ->
            home_search_box?.let { box ->
                val collapsedWidth = ctx.resources.getDimensionPixelSize(R.dimen.button_height_min)
                return box.measuredWidth == collapsedWidth
            } ?: kotlin.run {
                return true
            }
        } ?: kotlin.run {
            return true
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
                val intent = Intent(App.appContext,NotificationPublisher::class.java)
                val pendingIntent = PendingIntent.getBroadcast(App.appContext,NotificationPublisher.INTENT_REQUEST_CODE,intent
                    ,if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_UPDATE_CURRENT)
                val futureInMillis = System.currentTimeMillis() + (it.interval.toLong() * 3600000)
                val alarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                        alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,futureInMillis,pendingIntent)
                    }
                    else -> {
                        alarmManager?.setExact(AlarmManager.RTC_WAKEUP,futureInMillis,pendingIntent)
                    }
                }
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

    override fun onDestroyView() {
        dismissDialog(loadingDialog)
        quoteFullscreenAdapter = null
        super.onDestroyView()
    }

    override fun onBackPress(): Boolean {
        return if(!isSearchBoxCollapsed()) {
            collapseSearchBox()
            true
        } else {
            super.onBackPress()
        }
    }

}
