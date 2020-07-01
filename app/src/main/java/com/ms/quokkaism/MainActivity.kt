package com.ms.quokkaism

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.annotation.ColorInt
import androidx.navigation.findNavController
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.ms.quokkaism.extension.convertDpToPixel
import com.ms.quokkaism.ui.base.BaseFragment
import com.ms.quokkaism.ui.sidemenu.SideMenuFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

    //private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //main_content_bg?.background = RoundedDrawable()

        main_content_frame?.setOnClickListener {
            if(isSideMenuOpened())
            {
                closeSideMenu()
            }
        }

        //showSideMenu();

//        val toolbar: Toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)
//
//        val fab: FloatingActionButton = findViewById(R.id.fab)
//        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
//        }
//        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
//        val navView: NavigationView = findViewById(R.id.nav_view)
//        val navController = findNavController(R.id.nav_host_fragment)
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//        appBarConfiguration = AppBarConfiguration(setOf(
//                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow), drawerLayout)
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)
        subscribeToViewChanges()
    }

    private fun subscribeToViewChanges() {
        getNavController().addOnDestinationChangedListener(this)
    }

    private fun unsubscribeToViewChanges() {
        getNavController().removeOnDestinationChangedListener(this)
    }

    fun toggleSideMenu() {
        if(isSideMenuOpened())
        {
            closeSideMenu()
        }
        else
        {
            openSideMenu()
        }
    }

    private fun isSideMenuOpened() : Boolean
    {
        return main_side_menu_frame?.visibility == View.VISIBLE
    }


     fun openSideMenu() {
        main_side_menu_frame?.post{
            val translateOffset = (drawer_layout.measuredWidth * 0.6).toInt()
            main_side_menu_frame?.x = -translateOffset.toFloat()
            val sideMenuAnimator = ValueAnimator.ofInt(-translateOffset,0)
            sideMenuAnimator.addUpdateListener {
                val value = it.animatedValue as Int
                main_side_menu_frame?.x = value.toFloat()
            }
            val mainTranslateAnimator = ValueAnimator.ofInt(0,translateOffset)
            mainTranslateAnimator.addUpdateListener {
                val value = it.animatedValue as Int
                main_content_frame?.x = value.toFloat()
            }
            val mainCornerAnimator = ValueAnimator.ofFloat(0f,convertDpToPixel(this,30f))
            mainCornerAnimator.addUpdateListener {
                val value = it.animatedValue as Float
                //main_content_bg?.radius = value
                main_content_frame?.radius = value
            }
            val mainScaleAnimator = ValueAnimator.ofFloat(1f,0.8f)
            mainScaleAnimator.addUpdateListener {
                val value = it.animatedValue as Float
                main_content_frame?.scaleX = value
                main_content_frame?.scaleY = value
            }
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(sideMenuAnimator,mainTranslateAnimator,mainScaleAnimator,mainCornerAnimator)
            animatorSet.duration = 400
            animatorSet.addListener(object : Animator.AnimatorListener{
                override fun onAnimationRepeat(p0: Animator?) {

                }

                override fun onAnimationEnd(p0: Animator?) {
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationStart(p0: Animator?) {
                    main_side_menu_frame?.visibility = View.VISIBLE
                }


            })
            animatorSet.start()
        }
    }

    fun closeSideMenu()
    {
        main_side_menu_frame?.post{
            val translateOffset = (drawer_layout.measuredWidth * 0.6).toInt()
            val sideMenuAnimator = ValueAnimator.ofInt(0,-translateOffset)
            sideMenuAnimator.addUpdateListener {
                val value = it.animatedValue as Int
                main_side_menu_frame?.x = value.toFloat()
            }
            val mainTranslateAnimator = ValueAnimator.ofInt(translateOffset,0)
            mainTranslateAnimator.addUpdateListener {
                val value = it.animatedValue as Int
                main_content_frame?.x = value.toFloat()
            }
            val mainCornerAnimator = ValueAnimator.ofFloat(convertDpToPixel(this,30f),0f)
            mainCornerAnimator.addUpdateListener {
                val value = it.animatedValue as Float
                main_content_frame?.radius = value
            }
            val mainScaleAnimator = ValueAnimator.ofFloat(0.8f,1f)
            mainScaleAnimator.addUpdateListener {
                val value = it.animatedValue as Float
                main_content_frame?.scaleX = value
                main_content_frame?.scaleY = value
            }
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(sideMenuAnimator,mainTranslateAnimator,mainScaleAnimator,mainCornerAnimator)
            animatorSet.duration = 400
            animatorSet.addListener(object : Animator.AnimatorListener{
                override fun onAnimationRepeat(p0: Animator?) {

                }

                override fun onAnimationEnd(p0: Animator?) {
                    main_side_menu_frame?.visibility = View.GONE
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationStart(p0: Animator?) {

                }


            })
            animatorSet.start()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onBackPressed() {
        if(isSideMenuOpened())
        {
            closeSideMenu()
        }
        else
        {
            val fragmentManager = supportFragmentManager
            (fragmentManager.fragments.takeIf { it.isNotEmpty() && it[0] is NavHostFragment }?.get(0) as NavHostFragment).let {
                it.childFragmentManager.fragments.takeIf { it.isNotEmpty() }?.let { fragments ->
                    fragments.reverse()
                    var handledBack = false
                    for (fragment in fragments) {
                        handledBack = if (fragment is BaseFragment) {
                            fragment.onBackPress()
                        } else {
                            false
                        }
                        if (handledBack) {
                            break
                        }
                    }
                    if (!handledBack) {
                        navigateUpFragment()
                    }
                } ?: kotlin.run {
                    //unsubscribeToViewChanges()
                    super.onBackPressed()
                }
            }
        }
    }

    private fun navigateUpFragment()
    {
        val navController = findNavController(R.id.main_content_nav_host)
        if(navController.currentDestination?.id == navController.graph.startDestination)
        {
            //unsubscribeToViewChanges()
            super.onBackPressed()
        }
        else {
            navController.navigateUp()
        }
    }

    fun getNavController(): NavController {
        return findNavController(R.id.main_content_nav_host)
    }

    fun setMainBackgroundColor(@ColorInt color : Int)
    {
        //main_content_frame?.bgColor = color
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        when(destination.id) {
            R.id.nav_home -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_side_menu_frame,SideMenuFragment())
                    .commit()
                unsubscribeToViewChanges()
            }
            else -> {}
        }
    }

//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.main_content_nav_host)
//        return navController.navigateUp() || super.onSupportNavigateUp()
//    }
}
